package core;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import config.PasswordManagerPersistent;
import config.Config;
import detection.DetectGameProcess;
import detection.GameDetection;
import detection.KillGame;
import notifier.NotificationListener;
import report.WeeklyUsageReporter;

public class UsageTimeManager implements Timecounter.NotificationListener {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Timecounter timecounter;
    private Config config;

    private boolean timing = false;//計時中
    private boolean timeUp = false;//時間用完 || 禁止時間
    private boolean firstTimeStart = true;//第一次啟動
    private boolean wasGameRunning = false; // 追蹤遊戲是否從未啟動變成啟動

    private final NotificationListener notificationListener;

    public UsageTimeManager(Config config, NotificationListener notificationListener) {
        this.config = config;
        this.notificationListener = notificationListener;
        this.timecounter = new Timecounter(config, this);
    }

    // 啟動整個管理服務
    public void start() {
        // 開始 Timecounter 的秒級計時
        //startTiming();

        // 定期(1分鐘)檢查遊戲狀態和時間規則
        scheduler.scheduleAtFixedRate(this::checkGameAndEnforceRules, 0, 10, TimeUnit.SECONDS);
    }

    // 主邏輯：判斷遊戲狀態與時間限制
    private LocalDate lastResetDate = LocalDate.now();

    private void checkGameAndEnforceRules() {

        List<String> runningGames = GameDetection.getRunningGames(DetectGameProcess.gameExecutables);
        boolean gameRunning = !runningGames.isEmpty();//是否有遊戲開啟

        if (gameRunning) {//print有沒有執行
            System.out.println("偵測到執行中的遊戲：" + runningGames);
        } else {
            System.out.println("無遊戲開啟");
        }
        // 發送開始與暫停通知（只在狀態變化時）
        if(!isRestrictedNow()){//非禁止時間段
            if (gameRunning && !wasGameRunning) {
                notificationListener.notify("INFO", "計時開始", "遊戲已啟動：" + runningGames);
            } else if (!gameRunning && wasGameRunning) {
                notificationListener.notify("INFO", "計時暫停", "偵測到遊戲已關閉或暫停");
            }
        }
        wasGameRunning = gameRunning;

        // 每日午夜重置使用時間
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate) && LocalTime.now().isAfter(LocalTime.MIDNIGHT)) {
            resetDailyUsage();
            lastResetDate = today;
        }

        // 判斷是否在禁止時段或時間已用盡
        if (isRestrictedNow()) {
            if (gameRunning) {
                KillGame.killRunningGames();
                if (config.isRestrictTime()) {//禁止時間段
                    notificationListener.notify("ERROR", "禁止遊戲時段", "將強制關閉遊戲，\n請勿在23:00~07:00遊玩");
                }else{//時間用盡
                    notificationListener.notify("ERROR", "遊戲時間用盡", "將強制關閉遊戲");
                }
            }
            pauseTimingAndSave();
            return;
        }

        if (gameRunning) {//有開
            if (!timing && !timeUp) {
                startTiming();
            }
        } else {
            //if (timing) {
            pauseTimingAndSave();
            //}
        }
    }

    // 開始計時
    public void startTiming() {

        if (!timing && !timeUp) {
            if(firstTimeStart){
                timecounter.start();
                firstTimeStart=false;
            }else{
                timecounter.resume();
            }
            timing = true;
        }
    }

    // 暫停計時並儲存紀錄
    public void pauseTimingAndSave() {
        timecounter.pause();  // 永遠停止 timer，避免重複跑
        if (timing) {
            saveUsageRecord();
        }
        timing = false;
    }

    // 停止計時，時間用盡狀態
    public void stopTimingAndSave() {
        timecounter.pause();
        timing = false;
        timeUp = true;
        saveUsageRecord();
    }

    // 是否正在計時中
    public boolean isTiming() {
        return timing;
    }

    // 是否時間用盡或處於禁止時段
    public boolean isRestrictedNow() {
        if (timeUp) return true; // 如果時間已用盡
        if (!config.isRestrictTime()) return false; // 如果未啟用禁止時段限制

        LocalTime now = LocalTime.now();
        LocalTime forbiddenStart = LocalTime.of(23, 0);
        LocalTime forbiddenEnd = LocalTime.of(7, 0);

        // 處理跨午夜的禁止區段 (23:00 ~ 07:00)
        return now.isAfter(forbiddenStart) || now.isBefore(forbiddenEnd);
    }


    // 重置每日遊戲使用時間
    private void resetDailyUsage() {
        Config newConfig = PasswordManagerPersistent.loadForOtherModules();
        if (newConfig != null) {
            this.config = newConfig;
            timeUp = false;
            timecounter.reset();
            timing = false;
            firstTimeStart=true;
        }
    }

    // 儲存使用紀錄
    private void saveUsageRecord() {
        File file = new File("usage_record.txt"); // 可改為更適當的路徑

        try (FileWriter writer = new FileWriter(file, true)) { // 以附加模式開啟檔案
            String date = LocalDate.now().toString();
            int usedSeconds = timecounter.getTotalPlayedTime();//使用時間
            int usedMinutes = usedSeconds / 60;
            int remainingSeconds = config.getDurationMinutes() * 60 - usedSeconds;

            writer.write(String.format("日期：%s，使用時間：%d 分鐘，剩餘時間：%d 秒\n",
                    date, usedMinutes, remainingSeconds));
            WeeklyUsageReporter.recordDailyUsage(config.getDurationMinutes(), remainingSeconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ----- Timecounter.NotificationListener 回調實作 -----
    //十分鐘倒數通知
    @Override
    public void onTenMinuteWarning(String currentRealTime) {
        notificationListener.notify("WARNING", "剩餘時間提醒", "遊戲將於十分鐘後關閉");
        //notificationListener.notify("WARNING", "剩餘時間提醒", "還剩10分鐘！ 現在時間:" + currentRealTime);
    }

    //三分鐘倒數通知
    @Override
    public void onThreeMinuteWarning(String currentRealTime) {
        notificationListener.notify("WARNING", "剩餘時間提醒", "遊戲將於三分鐘後關閉");
        //notificationListener.notify("WARNING", "剩餘時間提醒", "還剩3分鐘！ 現在時間:" + currentRealTime);
    }

    //時間用盡
    @Override
    public void onTimeExhausted(String currentRealTime) {
        System.out.println("遊戲時間已用盡，將強制關閉遊戲！"+ currentRealTime);
        notificationListener.notify("ERROR", "遊戲時間用盡", "將強制關閉遊戲");
        //notificationListener.notify("ERROR", "時間已到", "遊戲時間已用盡，將強制關閉遊戲！" + currentRealTime);
        timeUp = true;
        //timing = false;
        // 可強制關閉遊戲
        KillGame.killRunningGames();
    }

    @Override
    public void onTimeUpdate(String formattedTime, String realTime) {
        // 可以在這裡更新UI或記錄使用時間
        // System.out.println("剩餘時間：" + formattedTime);
    }

    //禁止時間段
    @Override
    public void onForbiddenTime(String currentRealTime) {
        System.out.println("目前為禁止遊戲時段" + currentRealTime);
        notificationListener.notify("ERROR", "禁止遊戲時段", "將強制關閉遊戲，\n請勿在23:00~07:00遊玩");
        timeUp = true;
        //timing = false;
        // 強制關閉遊戲
        KillGame.killRunningGames();
    }
}
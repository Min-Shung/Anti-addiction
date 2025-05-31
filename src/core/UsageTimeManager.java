package core;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.LocalTime;

import config.PasswordManagerPersistent;
import config.Config;
import detection.DetectGameProcess;
import detection.GameDetection;
import detection.KillGame;
import notifier.NotificationListener;

public class UsageTimeManager implements Timecounter.NotificationListener {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Timecounter timecounter;
    private Config config;

    private boolean timing = false;
    private boolean timeUp = false;

    private final NotificationListener notificationListener;

    public UsageTimeManager(Config config, NotificationListener notificationListener) {
        this.config = config;
        this.notificationListener = notificationListener;
        this.timecounter = new Timecounter(config, this);
    }

    // 啟動整個管理服務
    public void start() {
        // 開始 Timecounter 的秒級計時
        startTiming();

        // 定期(1分鐘)檢查遊戲狀態和時間規則
        scheduler.scheduleAtFixedRate(this::checkGameAndEnforceRules, 0, 1, TimeUnit.MINUTES);
    }

    // 主邏輯：判斷遊戲狀態與時間限制
    private LocalDate lastResetDate = LocalDate.now();

    private void checkGameAndEnforceRules() {

        List<String> runningGames = GameDetection.getRunningGames(DetectGameProcess.gameExecutables);
        boolean gameRunning = !runningGames.isEmpty();

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
                notificationListener.notify("ERROR", "禁止遊戲時間", "目前為禁止遊戲時段。" );
            }
            pauseTimingAndSave();
            return;
        }

        if (gameRunning) {
            if (!timing && !timeUp) {
                startTiming();
            }
        } else {
            if (timing) {
                pauseTimingAndSave();
            }
        }
    }

    // 開始計時
    public void startTiming() {
        if (!timing && !timeUp) {
            timecounter.start();
            timing = true;
        }
    }

    // 暫停計時並儲存紀錄
    public void pauseTimingAndSave() {
        if (timing) {
            timecounter.pause();
            timing = false;
            saveUsageRecord();
        }
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

    // 是否時間用盡或禁止時段
    public boolean isRestrictedNow() {
        return timeUp || timecounter.isForbiddenTime();
    }

    // 重置每日遊戲使用時間
    private void resetDailyUsage() {
        Config newConfig = PasswordManagerPersistent.loadForOtherModules();
        if (newConfig != null) {
            this.config = newConfig;
            timeUp = false;
            timecounter.reset();
            timing = false;
        }
    }

    // 儲存使用時間紀錄（自行實作）
    private void saveUsageRecord() {
        // 例如寫檔、資料庫或更新UI等
    }

    // ----- Timecounter.NotificationListener 回調實作 -----

    @Override
    public void onTenMinuteWarning(String currentRealTime) {
        notificationListener.notify("WARNING", "剩餘時間提醒", "還剩10分鐘！ 結束時間:" + currentRealTime);
    }

    @Override
    public void onThreeMinuteWarning(String currentRealTime) {
        notificationListener.notify("WARNING", "剩餘時間提醒", "還剩3分鐘！ 結束時間:" + currentRealTime);
    }

    @Override
    public void onTimeExhausted(String currentRealTime) {
        notificationListener.notify("ERROR", "時間已到", "遊戲時間已用盡，將強制關閉遊戲！" + currentRealTime);
        timeUp = true;
        timing = false;
        // 可強制關閉遊戲
        KillGame.killRunningGames();
    }

    @Override
    public void onTimeUpdate(String formattedTime, String realTime) {
        // 可以在這裡更新UI或記錄使用時間
        // System.out.println("剩餘時間：" + formattedTime);
    }

    @Override
    public void onForbiddenTime(String currentRealTime) {
        notificationListener.notify("ERROR", "禁止遊戲時間", "目前為禁止遊戲時段，已停止計時。" + currentRealTime);
        timeUp = true;
        timing = false;
        // 強制關閉遊戲
        KillGame.killRunningGames();
    }
}

package core;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.time.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Calendar;
import org.json.JSONObject;
import org.json.JSONException;

import config.Config;

public class Timecounter {
    private Timer timer; // 計時器實例
    private long startTime; // 開始時間(毫秒)
    private int remainingTime; // 剩餘時間(秒)
    private final int dailyLimit; // 每日限制時間(秒)
    private static final String STATE_FILE = "remaining_time_state.json"; // 狀態儲存檔案名稱
    
    // 警告時間設定
    private static final int TEN_MIN_WARNING = 600; // 10分鐘警告閾值(600秒)
    private static final int THREE_MIN_WARNING = 180; // 3分鐘警告閾值(180秒)
    
    // 禁止時段設定 (23:00-07:00)
    private static final int FORBIDDEN_START_HOUR = 23; // 禁止時段開始小時(23點)
    private static final int FORBIDDEN_END_HOUR = 7; // 禁止時段結束小時(7點)
    
    // 狀態標記
    private boolean tenMinWarningSent = false; // 是否已發送10分鐘警告
    private boolean threeMinWarningSent = false; // 是否已發送3分鐘警告
    private boolean timemeout = false; // 時間是否已用盡
    
    private final NotificationListener listener; // 通知監聽器
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); // 時間格式化工具
    private final boolean restrictTime; // 是否啟用時間限制

    // 通知監聽器介面
    public interface NotificationListener {
        void onTenMinuteWarning(String currentTime); // 10分鐘警告回調
        void onThreeMinuteWarning(String currentTime); // 3分鐘警告回調
        void onTimeExhausted(String currentTime); // 時間用盡回調
        void onTimeUpdate(String formattedTime, String currentTime); // 時間更新回調
        void onForbiddenTime(String currentTime); // 禁止時段回調
    }

    // 建構子
    public Timecounter(Config config, NotificationListener listener) {
        this.dailyLimit = config.getDurationMinutes() * 60; // 從設定檔取得每日限制時間(轉換為秒)
        this.listener = listener; // 設定監聽器
        this.restrictTime = config.isRestrictTime(); // 從設定檔取得是否啟用時間限制
        this.remainingTime = loadRemainingTime(); // 載入剩餘時間
    }

    // 載入剩餘時間狀態
    private int loadRemainingTime() {
        File file = new File(STATE_FILE); // 建立檔案物件
        if (file.exists()) { // 檢查狀態檔案是否存在
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // 嘗試讀取檔案
                String json = reader.readLine(); // 讀取JSON字串
                JSONObject obj = new JSONObject(json); // 解析JSON物件
                
                // 檢查是否是同一天
                String savedDate = obj.optString("date", ""); // 取得儲存的日期
                String today = LocalDate.now().toString(); // 取得今日日期
                
                if (savedDate.equals(today)) { // 如果是同一天
                    return obj.getInt("remainingTime"); // 返回儲存的剩餘時間
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace(); // 例外處理
            }
        }
        return dailyLimit; // 預設返回每日限制
    }

    // 儲存狀態
    private void saveState() {
        JSONObject obj = new JSONObject(); // 建立JSON物件
        obj.put("date", LocalDate.now().toString()); // 儲存當前日期
        obj.put("remainingTime", remainingTime); // 儲存剩餘時間
        obj.put("totalPlayed", getTotalPlayedTime()); // 儲存已使用時間
        
        try (FileWriter writer = new FileWriter(STATE_FILE)) { // 嘗試寫入檔案
            writer.write(obj.toString()); // 將JSON轉為字串寫入
        } catch (IOException e) {
            e.printStackTrace(); // 例外處理
        }
    }

    // 刪除狀態檔案
    private void deleteStateFile() {
        File file = new File(STATE_FILE); // 建立檔案物件
        if (file.exists()) { // 檢查檔案是否存在
            file.delete(); // 刪除檔案
        }
    }

    // 開始計時
    public void start() {
        if (timer != null) { // 如果計時器已存在
            timer.cancel(); // 取消現有計時器
        }
        
        resetFlags(); // 重置警告標記
        startTime = System.currentTimeMillis(); // 記錄開始時間
        timer = new Timer(); // 建立新計時器
        
        // 設定定期任務(每秒執行一次)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkTime(); // 檢查時間
            }
        }, 0, 1000); // 立即開始，間隔1秒
    }

    // 檢查時間
    private void checkTime() {
        String currentTime = getCurrentTime(); // 取得當前時間字串
        
        if (isForbiddenTime()) { // 如果是禁止時段
            handleForbiddenTime(currentTime); // 處理禁止時段邏輯
            return;
        }
        
        updateRemainingTime(); // 更新剩餘時間
        saveState(); // 儲存狀態
        
        if (listener != null) { // 如果有監聽器
            listener.onTimeUpdate(getFormattedTime(), currentTime); // 觸發時間更新通知
            
            checkWarnings(currentTime); // 檢查是否需要發送警告
            
            if (remainingTime == 0 && !timemeout) { // 如果時間用盡且尚未通知
                timemeout = true; // 標記時間已用盡
                listener.onTimeExhausted(currentTime); // 觸發時間用盡通知
                timer.cancel(); // 停止計時器
            }
        }
    }

    // 檢查是否為禁止時段
    private boolean isForbiddenTime() {
        if (!restrictTime) return false; // 如果未啟用時間限制，直接返回false
        
        Calendar cal = Calendar.getInstance(); // 取得日曆實例
        int hour = cal.get(Calendar.HOUR_OF_DAY); // 取得當前小時
        return hour >= FORBIDDEN_START_HOUR || hour < FORBIDDEN_END_HOUR; // 檢查是否在禁止時段
    }

    // 處理禁止時段
    private void handleForbiddenTime(String currentTime) {
        if (timer != null) { // 如果計時器存在
            timer.cancel(); // 取消計時器
            timer = null; // 清空計時器引用
        }
        
        if (listener != null) { // 如果有監聽器
            listener.onForbiddenTime(currentTime); // 觸發禁止時段通知
        }
    }

    // 更新剩餘時間
    private void updateRemainingTime() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000; // 計算已過時間(秒)
        remainingTime = (int) Math.max(dailyLimit - elapsed, 0); // 計算剩餘時間(不小於0)
    }

    // 檢查警告
    private void checkWarnings(String currentTime) {
	    // 剩餘10分鐘且未發送警告
        if (remainingTime == TEN_MIN_WARNING && !tenMinWarningSent) {
            tenMinWarningSent = true; // 標記已發送
            listener.onTenMinuteWarning(currentTime); // 觸發10分鐘警告
        }

         // 剩餘3分鐘且未發送警告
        if (remainingTime == THREE_MIN_WARNING && !threeMinWarningSent) {
            threeMinWarningSent = true; // 標記已發送
            listener.onThreeMinuteWarning(currentTime); // 觸發3分鐘警告
        }
	    // 時間用完檢查
        if (remainingTime == 0 && !timemeout) {
            timemeout = true;
            timer.cancel();
            if (listener != null) {
                listener.onTimeExhausted(currentTime);
            }
        }
    }

    // 暫停計時
    public void pause() {
        if (timer != null) { // 如果計時器存在
            timer.cancel(); // 取消計時器
            timer = null; // 清空計時器引用
            saveState(); // 儲存狀態
        }
    }

    // 恢復計時
    public void resume() {
        if (timer == null && remainingTime > 0) { // 如果計時器不存在且還有剩餘時間
            // 重新計算開始時間(考慮已過時間)
            startTime = System.currentTimeMillis() - ((dailyLimit - remainingTime) * 1000L);
            start(); // 重新開始計時
        }
    }

    // 重置計時器
    public void reset() {
        pause(); // 暫停計時
        remainingTime = dailyLimit; // 重置剩餘時間
        resetFlags(); // 重置警告標記
        deleteStateFile(); // 刪除狀態檔案
    }

    // 取得總共已使用時間
    public int getTotalPlayedTime() {
        return dailyLimit - remainingTime; // 計算已使用時間
    }

    // 取得格式化後的剩餘時間
    public String getFormattedTime() {
        int hours = remainingTime / 3600; // 計算小時
        int minutes = (remainingTime % 3600) / 60; // 計算分鐘
        int seconds = remainingTime % 60; // 計算秒數
        
        if (hours > 0) { // 如果有小時
            return String.format("%02d:%02d:%02d", hours, minutes, seconds); // 回傳HH:MM:SS格式
        }
        return String.format("%02d:%02d", minutes, seconds); // 回傳MM:SS格式
    }

    // 取得當前時間字串
    private String getCurrentTime() {
        return timeFormat.format(new Date()); // 格式化當前時間
    }

    // 重置警告標記
    private void resetFlags() {
        tenMinWarningSent = false; // 重置10分鐘警告標記
        threeMinWarningSent = false; // 重置3分鐘警告標記
        timemeout = false; // 重置時間用盡標記
    }
}

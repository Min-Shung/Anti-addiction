import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class Timecounter {
    private Timer timer;                        // 計時器物件
    private long startTime;                     // 開始時間(毫秒)
    private int remainingTime;                  // 剩餘時間(秒)
    private final int gameTime;                 // 總允許時間(秒)
    private static final int TEN_MIN_WARNING = 50;   // 10分鐘警告(600秒)
    private static final int THREE_MIN_WARNING = 30; // 3分鐘警告(180秒)
    
    private final NotificationListener listener; // 通知監聽器
    private final SimpleDateFormat timeFormat;  // 時間格式化物件
    
    // 警告狀態標記
    private boolean tenMinWarningSent = false;   // 10分鐘警告是否已發送
    private boolean threeMinWarningSent = false; // 3分鐘警告是否已發送
    
    // 通知監聽器介面
    public interface NotificationListener {
        void onTenMinuteWarning(String currentRealTime);  // 10分鐘警告回調(帶現實時間)
        void onThreeMinuteWarning(String currentRealTime); // 3分鐘警告回調(帶現實時間)
        void onTimeExhausted(String currentRealTime);     // 時間用完回調(帶現實時間)
        void onTimeUpdate(String formattedTime, String realTime); // 時間更新回調
    }
    
    public Timecounter(int totalAllowedMinutes, NotificationListener listener) {
        this.gameTime = totalAllowedMinutes * 60; // 轉為秒
        this.listener = listener;
        this.remainingTime = gameTime;
        this.timeFormat = new SimpleDateFormat("HH:mm:ss"); // 時間格式 時:分:秒
    }
    
    // 獲取當前現實時間
    public String getCurrentRealTime() {
        return timeFormat.format(new Date());
    }
    
    // 啟動計時器
    public void start() {
        if (timer != null) {
            timer.cancel();
        }
        
        resetFlags();
        startTime = System.currentTimeMillis();
        timer = new Timer();
        
        // 設置定時任務，每1秒檢查一次
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkTime();
            }
        }, 0, 1000);
    }
    
    // 檢查時間並觸發相應事件
    private void checkTime() {
        // 計算已過時間(毫秒)並轉為秒
        long elapsedMillis = System.currentTimeMillis() - startTime;
        remainingTime = gameTime - (int)(elapsedMillis / 1000);
        
        // 確保剩餘時間不為負數
        remainingTime = Math.max(remainingTime, 0);
        
        // 獲取當前現實時間
        String currentRealTime = getCurrentRealTime();
        
        // 通知時間更新
        if (listener != null) {
            listener.onTimeUpdate(getFormattedRemainingTime(), currentRealTime);
        }
        
        // 10分鐘警告檢查
        if (remainingTime <= TEN_MIN_WARNING && !tenMinWarningSent) {
            tenMinWarningSent = true;
            if (listener != null) {
                listener.onTenMinuteWarning(currentRealTime);
            }
        }
        
        // 3分鐘警告檢查
        if (remainingTime <= THREE_MIN_WARNING && !threeMinWarningSent) {
            threeMinWarningSent = true;
            if (listener != null) {
                listener.onThreeMinuteWarning(currentRealTime);
            }
        }
        
        // 時間用完檢查
        if (remainingTime <= 0) {
            timer.cancel();
            if (listener != null) {
                listener.onTimeExhausted(currentRealTime);
            }
        }
    }
    
    
    // 暫停計時器
    public void pause() {
        if (timer != null) {
            timer.cancel();                     // 取消計時器
            timer = null;
        }
    }
    
    // 恢復計時器
    public void resume() {
        if (timer == null) {
            start();                            // 重新開始計時
        }
    }
    
    // 重置計時器
    public void reset() {
        pause();                                // 先暫停計時器
        remainingTime = gameTime;               // 重置剩餘時間
        resetFlags();                           // 重置警告狀態
    }
    
    // 獲取格式化剩餘時間(HH:MM:SS)
    public String getFormattedRemainingTime() {
        int hours = remainingTime / 3600;
        int minutes = (remainingTime % 3600) / 60;
        int seconds = remainingTime % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    // 獲取今日已遊戲時間佔比
    public float getTodayGameTimeRatio() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // 假設白天(8~22點)是主要遊戲時間
        if (hour >= 8 && hour < 22) {
            return hour / 24.0f;
        }
        return 0.0f;
    }

    // 重置警告標記
    private void resetFlags() {
        tenMinWarningSent = false;               // 重置10分鐘警告標記
        threeMinWarningSent = false;             // 重置3分鐘警告標記
    }
}

import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.net.*;
import java.util.Date;
import java.util.Calendar;

public class Timecounter {
    private Timer timer;                        // 計時器物件
    private long startTime;                     // 開始時間(毫秒)
    private int remainingTime;                  // 剩餘時間(秒)
    private final int gameTime;                 // 總允許時間(秒)
    private static final int TEN_MIN_WARNING = 600;   // 10分鐘警告(600秒)
    private static final int THREE_MIN_WARNING = 180; // 3分鐘警告(180秒)
    
    private final NotificationListener listener; // 通知監聽器
    private final SimpleDateFormat timeFormat;  // 時間格式化物件
    
    private static final String NTP_SERVER = "tw.pool.ntp.org"; // 台灣NTP伺服器
    private static final int FORBIDDEN_START_HOUR = 23; // 禁止開始時間(23:00)
    private static final int FORBIDDEN_END_HOUR = 1;    // 禁止結束時間(01:00)

    public static ZonedDateTime getNetworkTaipeiTime() throws Exception {
        // 從NTP獲取UTC時間 (簡化版，實際應完整實現NTP協議)
        long ntpTime = getNtpTime();
        Instant instant = Instant.ofEpochSecond(ntpTime);
        
        // 轉換為台北時區
        return instant.atZone(ZoneId.of("Asia/Taipei"));
    }

    private static long getNtpTime() throws Exception {
        // 這裡是簡化實現，實際應完整實現NTP協議
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(NTP_SERVER);
        byte[] buffer = new byte[48];
        buffer[0] = 0x1B;
        
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 123);
        socket.send(packet);
        socket.receive(packet);
        socket.close();
        
        // 解析NTP時間戳
        return ((buffer[40] & 0xFFL) << 24) | 
               ((buffer[41] & 0xFFL) << 16) | 
               ((buffer[42] & 0xFFL) << 8) | 
               (buffer[43] & 0xFFL);
    }

    // 警告狀態標記
    private boolean tenMinWarningSent = false;   // 10分鐘警告是否已發送
    private boolean threeMinWarningSent = false; // 3分鐘警告是否已發送
    
    // 通知監聽器介面
    public interface NotificationListener {
        void onTenMinuteWarning(String currentRealTime);  // 10分鐘警告回調(帶現實時間)
        void onThreeMinuteWarning(String currentRealTime); // 3分鐘警告回調(帶現實時間)
        void onTimeExhausted(String currentRealTime);     // 時間用完回調(帶現實時間)
        void onTimeUpdate(String formattedTime, String realTime); // 時間更新回調
        void onForbiddenTime(String currentRealTime); // 新增禁止時段通知
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
    
    //檢查當前是否在禁止遊玩時段
    //return true如果在禁止時段(23:00-01:00)
    private boolean isForbiddenTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= FORBIDDEN_START_HOUR || hour < FORBIDDEN_END_HOUR;
    }

    // 檢查時間並觸發相應事件
    private void checkTime() {
        // 獲取當前現實時間
        String currentRealTime = getCurrentRealTime();
        
        // 檢查是否在禁止時段
        if (isForbiddenTime()) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (listener != null) {
                listener.onForbiddenTime(currentRealTime);
            }
            return;
        }
        // 計算已過時間(毫秒)並轉為秒
        long elapsedMillis = System.currentTimeMillis() - startTime;
        remainingTime = gameTime - (int)(elapsedMillis / 1000);
        
        // 確保剩餘時間不為負數
        remainingTime = Math.max(remainingTime, 0);
        
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

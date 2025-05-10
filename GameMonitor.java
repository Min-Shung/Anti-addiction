//每秒偵測
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameMonitor {
    public static void main(String[] args) {
        String gameProcess = "chrome.exe";
        AtomicBoolean isContinue = new AtomicBoolean(false);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable checkGameTask = () -> {
            boolean isRunning = GameDetection.isGameRunning(gameProcess);

            if (isRunning != isContinue.get()) { // 狀態變化
                if (isRunning) {
                    System.out.printf("%s 已啟動%n", gameProcess);
                } else {
                    System.out.printf("%s 已關閉%n", gameProcess);
                    scheduler.shutdown(); // 停止檢測
                }
                isContinue.set(isRunning); // 更新狀態
            }
        };

        scheduler.scheduleAtFixedRate(checkGameTask, 0, 1, TimeUnit.SECONDS); // 每秒檢測一次
    }
}

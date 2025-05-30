import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameMonitorService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean gameRunning = new AtomicBoolean(false);

    public void startMonitoring() {
        Runnable checkTask = () -> {
            String runningGame = GameDetection.getRunningGame(DetectGameProcess.gameExecutables);
            boolean isRunning = (runningGame != null);
            gameRunning.set(isRunning);
            System.out.println("【監控】遊戲狀態：" + (isRunning ? "啟動" : "關閉"));
        };
        scheduler.scheduleAtFixedRate(checkTask, 0, 1, TimeUnit.SECONDS);
    }

    public boolean isGameRunning() {
        return gameRunning.get();
    }

    public void stopMonitoring() {
        scheduler.shutdownNow();
    }

}

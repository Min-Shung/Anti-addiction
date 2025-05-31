package detection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameMonitorService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean gameRunning = new AtomicBoolean(false);

    public void startMonitoring() {
        Runnable checkTask = () -> {
            List<String> runningGames = GameDetection.getRunningGames(DetectGameProcess.gameExecutables);
            boolean isRunning = !runningGames.isEmpty();
            gameRunning.set(isRunning);

            if (isRunning) {
                System.out.println("遊戲狀態：啟動，正在執行的遊戲：" + runningGames);
            } else {
                System.out.println("遊戲狀態：關閉");
            }
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class GameMonitor {

    public static void main(String[] args) {
        AtomicReference<String> currentRunningGame = new AtomicReference<>(null);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable checkTask = () -> {
            String runningGame = GameDetection.getRunningGame(DetectGameProcess.gameExecutables);

            String prevGame = currentRunningGame.get();

            if ((runningGame == null && prevGame != null) || 
                (runningGame != null && !runningGame.equals(prevGame))) {
                
                if (runningGame != null) {
                    System.out.println("偵測到遊戲已啟動：" + runningGame);
                } else {
                    System.out.println("全部遊戲已關閉，停止監控。");
                    scheduler.shutdown();
                }
                currentRunningGame.set(runningGame);
            }
        };

        scheduler.scheduleAtFixedRate(checkTask, 0, 1, TimeUnit.SECONDS);
    }
}

package detection;
import java.io.IOException;
import java.util.List;

public class KillGame {
    
    public static void killRunningGames() {
        List<String> runningGames = GameDetection.getRunningGames(DetectGameProcess.gameExecutables);

        if (!runningGames.isEmpty()) {
            for (String runningGame : runningGames) {
                try {
                    ProcessBuilder builder = new ProcessBuilder("taskkill", "/F", "/IM", runningGame);
                    builder.inheritIO();
                    builder.start();
                    System.out.println("已嘗試關閉：" + runningGame);
                } catch (IOException e) {
                    System.out.println("關閉 " + runningGame + " 失敗");
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("沒有偵測到正在執行的遊戲");
        }
    }
    
}

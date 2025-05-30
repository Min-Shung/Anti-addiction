import java.io.IOException;
public class KillGame {
    
    public static void killRunningGame() {
        String runningGame = GameDetection.getRunningGame(DetectGameProcess.gameExecutables);

        if (runningGame != null) {
            try {
                ProcessBuilder builder = new ProcessBuilder("taskkill", "/F", "/IM", runningGame);
                builder.inheritIO();
                builder.start();
                System.out.println("已嘗試關閉：" + runningGame);
            } catch (IOException e) {
                System.out.println("關閉失敗");
                e.printStackTrace();
            }
        }
    }
}

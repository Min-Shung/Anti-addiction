import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GameDetection {

    // 傳入遊戲執行檔清單，回傳第一個偵測到的正在執行的遊戲exe名稱，沒找到回傳null
    public static String getRunningGame(String[] gameExecutables) {
        try {
            ProcessBuilder pb = new ProcessBuilder("tasklist");
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                for (String exe : gameExecutables) {
                    if (line.contains(exe)) {
                        return exe;  // 回傳找到的遊戲執行檔名稱
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

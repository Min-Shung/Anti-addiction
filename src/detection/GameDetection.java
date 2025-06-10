package detection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameDetection {

    //回傳所有正在執行的遊戲，沒找到回傳空清單
    public static List<String> getRunningGames(String[] gameExecutables) {
        List<String> runningGames = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("tasklist");
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                for (String exe : gameExecutables) {
                    if (line.contains(exe) && !runningGames.contains(exe)) {
                        runningGames.add(exe);  // 收集符合的遊戲名稱
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return runningGames;
    }
}

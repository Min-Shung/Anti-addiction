import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KillGame {
    public static void main(String[] args) {
        System.out.println("輸入 1 關閉 chrome.exe");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String input = reader.readLine();

            if ("1".equals(input.trim())) {
                ProcessBuilder builder = new ProcessBuilder("taskkill", "/F", "/IM", "chrome.exe");
                builder.inheritIO(); // 顯示輸出到 console
                builder.start();     // 執行關閉命令
                System.out.println("正在嘗試關閉 Chrome");
            }
        } catch (IOException e) {
            System.out.println("出錯惹！");
            e.printStackTrace();
        }
    }
}

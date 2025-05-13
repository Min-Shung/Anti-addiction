import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;

public class BreakReminder {

    public static void main(String[] args) {
        Timer timer = new Timer();

        // 每 30 分鐘提醒一次（30 分鐘 = 1800 秒 = 1800000 毫秒）
        long delay = ˇ * 1000;

        // 第一次延遲時間 = 30 分鐘，之後每 30 分鐘重複
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 彈出休息提示視窗
                JOptionPane.showMessageDialog(null, "請休息一下，已經 30 分鐘了！");
            }
        }, delay, delay); // 初次延遲, 重複間隔

        // 假裝這是主要程式在運作（不然會程式結束）
        while (true) {
            // 模擬主程式持續運作（例如遊戲、學習、瀏覽）
            try {
                Thread.sleep(1000); // 每秒暫停一下
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

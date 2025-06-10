package test;
import core.UsageTimeManager;
import core.Timecounter;
import config.Config;
import notifier.NotificationListener;

public class UsageTimeManagerTest {
    public static void main(String[] args) {
        // 使用你的 Config 類別建構物件，設定密碼、限制分鐘數、是否限制時段
        Config config = new Config("1234", 1, false);  // 1 分鐘限制，不限制禁止時段

        // 建立通知監聽器，印出通知內容到控制台
        NotificationListener listener = new NotificationListener() {
            @Override
            public void notify(String type, String title, String message) {
                System.out.println("[" + type + "] " + title + ": " + message);
            }
        };

        // 建立使用時間管理器
        UsageTimeManager manager = new UsageTimeManager(config, listener);

        // 啟動時間管理器
        manager.start();

        System.out.println("UsageTimeManager 已啟動。");

        // 主程式等待 65 秒，讓計時器可運作並觸發通知
        try {
            Thread.sleep(65000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("測試結束。");
        System.exit(0);
    }
}

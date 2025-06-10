package test;
import notifier.NotificationListener;
import config.Config;
import notifier.WindowsNotificationListener;

public class TestTimecounterMain {
    public static void main(String[] args) {
        WindowsNotificationListener listener = new WindowsNotificationListener();
        listener.notify("INFO", "測試通知", "這是快速測試訊息！");
    }
}
package ui;

import config.Config;
import core.UsageTimeManager;
import notifier.NotificationListener;
import notifier.WindowsNotificationListener;
import config.PasswordManagerPersistent;

public class Main {
    public static void main(String[] args) {
        // 初始化通知模組
        NotificationListener notifier = new WindowsNotificationListener();

        // 讀取設定
        Config config = PasswordManagerPersistent.run();

        // 建立時間管理器，並將通知介面傳入
        UsageTimeManager usageTimeManager = new UsageTimeManager(config, notifier);
        usageTimeManager.start();

        // 其他程序持續運行
    }
}

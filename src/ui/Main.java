package ui;
import config.Config;
import core.UsageTimeManager;
import core.WindowsNotifier;
import config.PasswordManagerPersistent;
public class Main {
    public static void main(String[] args) {
        // 初始化通知模組
        WindowsNotifier notifier = new WindowsNotifier();

        // 讀取設定
        Config config = PasswordManagerPersistent.run();

        // 建立時間管理器，並啟動
        UsageTimeManager usageTimeManager = new UsageTimeManager(config, notifier);
        usageTimeManager.start();

        // 其他程序持續運行
    }
}

public class Main {
    public static void main(String[] args) {
        // 初始化通知模組
        WindowsNotifier notifier = new WindowsNotifier();

        // 第一步：密碼與設定管理
        PasswordManagerPersistent.run();  // 執行設定或登入

        // 第二步：開始進行遊戲監控
        GameMonitorService monitorService = new GameMonitorService();
        monitorService.startMonitoring();

        // 第三步：時間控制 + 遊戲限制邏輯
        UsageTimeManager timeManager = new UsageTimeManager(notifier);
        timeManager.start();
    }
}

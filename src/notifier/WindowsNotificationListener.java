package notifier;

import core.WindowsNotifier;

/**
 * 將通用 NotificationListener 介面實作為使用 Windows 系統通知。
 */
public class WindowsNotificationListener implements NotificationListener {
    private final WindowsNotifier notifier;

    public WindowsNotificationListener() {
        this.notifier = new WindowsNotifier();
    }

    @Override
    public void notify(String type, String title, String message) {
        // 你可以根據 type 做不同的顯示邏輯，例如圖示或分類處理
        notifier.showNotification(title, message);
    }
}

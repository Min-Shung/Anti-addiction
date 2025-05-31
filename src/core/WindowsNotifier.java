package core;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class WindowsNotifier {
    private TrayIcon trayIcon;

    public WindowsNotifier() {
        initializeSystemTray();
    }

    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported!");
            return;
        }

        try {
            SystemTray systemTray = SystemTray.getSystemTray();

            // 嘗試載入自訂圖示，若失敗則建立預設圖像
            Image trayImage;
            try {
                // 若有圖檔可使用此段載入（目前是註解狀態）
                // trayImage = ImageIO.read(getClass().getResourceAsStream("/notification_icon.png"));
                // 若沒有圖檔則使用預設圖像
                trayImage = createDefaultImage();
            } catch (Exception e) {
                trayImage = createDefaultImage();
            }

            trayIcon = new TrayIcon(trayImage, "Notifier");
            trayIcon.setImageAutoSize(true);

            // 選單（可選）
            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("退出");
            exitItem.addActionListener(e -> System.exit(0));
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            systemTray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("Failed to initialize system tray: " + e.getMessage());
        }
    }

    /**
     * 建立一個簡單的預設圖示
     */
    private Image createDefaultImage() {
        int width = 16, height = 16;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    /**
     * 顯示通知訊息
     * @param title   通知標題
     * @param message 通知內容
     */
    public void showNotification(String title, String message) {
        if (trayIcon == null) {
            System.err.println("System tray not initialized!");
            return;
        }
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    // 測試用 main 方法
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WindowsNotifier notifier = new WindowsNotifier();
            notifier.showNotification("測試標題", "這是來自其他 Class 的通知訊息！");
        });
    }
}

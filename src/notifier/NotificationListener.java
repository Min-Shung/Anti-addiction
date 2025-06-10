package notifier;

/**
 * 通用通知介面，可用於各種模組觸發通知訊息。
 */
public interface NotificationListener {

    /**
     * 接收一則通知。
     *
     * @param type    通知類型（例如："INFO", "WARNING", "ERROR", "TASK_UPDATE", "TIMER"）
     * @param title   通知標題（例如："時間提醒", "任務完成", "系統錯誤"）
     * @param message 通知內容（可包含詳細說明）
     */
    void notify(String type, String title, String message);
}

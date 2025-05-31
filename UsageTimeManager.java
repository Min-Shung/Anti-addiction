import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.*;

public class UsageTimeManager {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final WindowsNotifier notifier;
    private int remainingMinutes;
    private boolean restrictTime;

    public UsageTimeManager(WindowsNotifier notifier) {
        this.notifier = notifier;

        // 載入使用者設定
        String[] config = PasswordManagerPersistent.loadForOtherModules(); // 你可以提供這個方法供他模組取值
        if (config != null) {
            remainingMinutes = Integer.parseInt(config[1]);
            restrictTime = Boolean.parseBoolean(config[2]);
        }
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkGameAndEnforceRules, 0, 1, TimeUnit.MINUTES);
    }

    private void checkGameAndEnforceRules() {
        List<String> runningGames = GameDetection.getRunningGames(DetectGameProcess.gameExecutables);
        boolean gameRunning = !runningGames.isEmpty();

        // 每天凌晨重設時間
        if (LocalTime.now().equals(LocalTime.MIDNIGHT)) {
            resetDailyUsage();
        }

        if (restrictTime && isForbiddenTime()) {
            if (gameRunning) {
                KillGame.killRunningGames();
                notifier.showNotification("禁止時間", "目前為禁止時間，已關閉遊戲！");
            }
            return;
        }

        if (gameRunning) {
            if (remainingMinutes <= 0) {
                KillGame.killRunningGames();
                notifier.showNotification("時間已到", "遊戲已超出可使用時數，已強制關閉！");
                return;
            }

            // 通知使用者剩餘時間
            if (remainingMinutes == 10 || remainingMinutes == 3) {
                notifier.showNotification("剩餘時間提醒", "還剩 " + remainingMinutes + " 分鐘可使用！");
            }

            remainingMinutes--;
            // 可擴充記錄時間到報告模組
        }
    }

    private boolean isForbiddenTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0)); // 禁止時間範例：22:00～6:00
    }

    private void resetDailyUsage() {
        String[] config = PasswordManagerPersistent.loadForOtherModules();
        if (config != null) {
            remainingMinutes = Integer.parseInt(config[1]);
        }
    }
}

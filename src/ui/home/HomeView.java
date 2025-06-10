package ui.home;

import config.Config;
import config.PasswordManagerPersistent;
import core.UsageTimeManager;
import notifier.NotificationListener;
import notifier.WindowsNotificationListener;
import ui.login.LoginPane;
import ui.settings.SettingsPane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;

public class HomeView {

    @FXML
    private VBox infoBox;
    @FXML
    private Label logArea;  // 改成 Label
    @FXML
    private StackPane rightPane;
    @FXML
    private MediaView videoPlayer;
    @FXML
    private Label idleLabel;

    private MediaPlayer mediaPlayer;
    private Timeline logCheckTimeline;
    private PauseTransition clearTimer;

    private Config config;
    private UsageTimeManager usageManager;

    private PauseTransition clearLogTimer;

    private NotificationListener notifier;

    @FXML
    public void initialize() {
        // 初始化設定顯示
        Config config = PasswordManagerPersistent.loadForOtherModules();
        notifier = new WindowsNotificationListener();
        if (config != null) {
            updateInfo(config.getDurationMinutes() + " 分鐘", config.isRestrictTime());
            usageManager = new UsageTimeManager(config, notifier);
            usageManager.start();
        } else {
            updateInfo("尚未設定", false);
        }

        clearLogTimer = new PauseTransition(Duration.seconds(5));
        clearLogTimer.setOnFinished(e -> logArea.setText(""));

        // 播放影片設定
        setupVideoPlayer();// 每秒檢查 logArea 的內容，如果是空的就播放影片，否則停止
        logCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            String text = logArea.getText();
            boolean shouldIdle = (text == null || text.trim().isEmpty());

            if (mediaPlayer != null) {
                if (shouldIdle) {
                    videoPlayer.setVisible(true);
                    if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                        mediaPlayer.play();
                    }
                    idleLabel.setVisible(true);
                    if (typingTimeline == null || !typingTimeline.getStatus().equals(Animation.Status.RUNNING)) {
                        startTypingEffectLoop();
                    }
                } else {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.pause();
                    }
                    videoPlayer.setVisible(false);
                    idleLabel.setVisible(false);
                    stopTypingEffect();
                }
            }
        }));
        logCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        logCheckTimeline.play();


        videoPlayer.setVisible(false);
        idleLabel.setVisible(false);

        showLogin();
    }

    private void setupVideoPlayer() {
        String basePath = System.getProperty("user.dir"); // 執行程式的目錄
        File videoFile = new File(basePath, "resources/waiting.mp4");

        if (videoFile.exists()) {
            Media media = new Media(videoFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setRate(0.5);
            videoPlayer.setMediaPlayer(mediaPlayer);
        } else {
            appendLog("⚠ 無法找到影片：" + videoFile.getAbsolutePath());
        }
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login/LoginPane.fxml"));
            StackPane login = loader.load();
            LoginPane controller = loader.getController();
            controller.setHomeView(this);
            rightPane.getChildren().setAll(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSettings(Config config) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/settings/SettingsPane.fxml"));
            StackPane settings = loader.load();
            SettingsPane controller = loader.getController();
            controller.setHomeView(this);
            controller.setConfig(config);
            rightPane.getChildren().setAll(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateInfo(String timeText, boolean restrict) {
        infoBox.getChildren().clear();

        Label timeLabel = new Label("總時長限制: " + timeText);
        Label restrictLabel = new Label("夜間禁玩: " + (restrict ? "開啟" : "關閉"));

        infoBox.getChildren().addAll(timeLabel, restrictLabel);
    }

    public void showReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/report/ReportView.fxml"));
            BorderPane root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("使用報告");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            appendLog("⚠ 無法開啟使用報告視窗");
            e.printStackTrace();
        }
    }

    // 修改這個方法：顯示訊息，5秒後清空
    public void appendLog(String message) {
        if (!"無訊息...".equals(message)) {
            logArea.setText(message);
            clearLogTimer.stop();
            clearLogTimer.playFromStart();
        }
    }

    private Timeline typingTimeline;
    private final String idleMessage = "無訊息...";

    private void startTypingEffectLoop() {
        if (typingTimeline != null) {
            typingTimeline.stop();
        }

        typingTimeline = new Timeline();

        for (int i = 0; i <= idleMessage.length(); i++) {
            final int index = i;
            typingTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(200 * i), e -> {
                if (index == 0) {
                    idleLabel.setText("");
                } else {
                    idleLabel.setText(idleMessage.substring(0, index));
                }
            }));
        }

        // 一輪完成後延遲一下，再從頭開始
        typingTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(200 * (idleMessage.length() + 5)), e -> {
            idleLabel.setText("");
        }));

        typingTimeline.setCycleCount(Timeline.INDEFINITE);
        typingTimeline.play();
    }

    private void stopTypingEffect() {
        if (typingTimeline != null) {
            typingTimeline.stop();
        }
        idleLabel.setText("");
    }


}

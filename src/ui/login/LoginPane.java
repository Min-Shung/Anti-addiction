package ui.login;

import ui.home.HomeView;
import config.Config;
import config.PasswordManagerPersistent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;

public class LoginPane {

    @FXML private Label titleLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private HomeView homeView;
    private boolean isRegistered;

    private static final String USER_FILE = "config.txt";

    @FXML
    public void initialize() {
        // 限制只能輸入英文與數字（可自訂規則）
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z0-9]*")) {
                // 非法字元，還原成舊值
                passwordField.setText(oldValue);
                errorLabel.setText("密碼只能輸入英文和數字");
            }
            else {
                // 合法，清除錯誤提示
                errorLabel.setText("");
            }
        });
        isRegistered = new File(USER_FILE).exists();
        updateUIForRegistrationStatus();
        errorLabel.setStyle("-fx-text-fill: red;");
        // 設置按 Enter 就執行登入或註冊
        passwordField.setOnAction(e -> handleConfirm());
    }

    private void updateUIForRegistrationStatus() {
        if (!isRegistered) {
            titleLabel.setText("請設定密碼（首次使用）");
        } else {
            titleLabel.setText("請輸入密碼登入");
        }
        errorLabel.setText("");
        passwordField.clear();
    }

    public void setHomeView(HomeView homeView) {
        this.homeView = homeView;
    }

    @FXML
    public void handleConfirm() {
        errorLabel.setText("");
        String input = passwordField.getText();

        if (input == null || input.isEmpty()) {
            errorLabel.setText("密碼不可為空");
            return;
        }

        if (!isRegistered) {
            // 註冊流程：儲存密碼與預設設定
            Config config = new Config(input, 30, false);
            PasswordManagerPersistent.saveConfig(config);
            homeView.appendLog("用戶註冊完成");

            // 註冊完切換回登入狀態
            isRegistered = true;
            updateUIForRegistrationStatus();
        } else {
            // 登入驗證
            if (PasswordManagerPersistent.validatePassword(input)) {
                Config config = PasswordManagerPersistent.loadConfig();
                homeView.appendLog("登入成功");
                homeView.showSettings(config);
            } else {
                errorLabel.setText("密碼錯誤");
                passwordField.clear();
            }
        }
    }
}

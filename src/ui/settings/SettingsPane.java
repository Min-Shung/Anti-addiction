package ui.settings;

import config.Config;
import config.PasswordManagerPersistent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.home.HomeView; // 如果 HomeView 在 ui.home

public class SettingsPane {

    @FXML private TextField dailyMinutesField;
    @FXML private TextField nightStartField;
    @FXML private TextField nightEndField;
    @FXML private Label statusLabel;
    @FXML private CheckBox restrictCheckBox;


    private HomeView homeView;
    private Config config;

    public void setHomeView(HomeView homeView) {
        this.homeView = homeView;
    }

    public void setConfig(Config config) {
        this.config = config;
        dailyMinutesField.setText(String.valueOf(config.getDurationMinutes()));
        restrictCheckBox.setSelected(config.isRestrictTime());
    }

    @FXML
    public void handleSave() {
        try {
            int minutes = Integer.parseInt(dailyMinutesField.getText().trim());
            boolean restrict = restrictCheckBox.isSelected();

            config.setDurationMinutes(minutes);
            config.setRestrictTime(restrict);
            PasswordManagerPersistent.saveConfig(config);

            homeView.appendLog("設定已儲存");
            statusLabel.setText("設定已儲存！");

            // 儲存完成後切換回登入介面
            homeView.updateInfo(minutes + " 分鐘", restrict);
            homeView.showLogin();

        } catch (Exception e) {
            statusLabel.setText("儲存失敗：" + e.getMessage());
        }
    }
}

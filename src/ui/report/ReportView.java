package ui.report;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ReportView {

    @FXML
    private VBox reportContent;

    @FXML
    public void initialize() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("lastWeekRecord.json")));
            JSONObject report = new JSONObject(content);

            int total = report.optInt("totalUsedMinutes", 0);
            double average = report.optDouble("averageUsedMinutes", 0.0);
            JSONArray dailyUsage = report.optJSONArray("dailyUsage");

            reportContent.getChildren().add(new Label("總使用時間： " + total + " 分鐘"));
            reportContent.getChildren().add(new Label(String.format("平均每日使用時間： %.2f 分鐘", average)));
            reportContent.getChildren().add(new Label("每日使用時間："));

            if (dailyUsage != null) {
                for (int i = 0; i < dailyUsage.length(); i++) {
                    JSONObject day = dailyUsage.getJSONObject(i);
                    String dayName = day.getString("day");
                    int used = day.getInt("usedMinutes");
                    reportContent.getChildren().add(new Label("・" + dayName + ": " + used + " 分鐘"));
                }
            } else {
                reportContent.getChildren().add(new Label("沒有每日資料"));
            }

        } catch (Exception e) {
            reportContent.getChildren().add(new Label("無法讀取報告，可能尚未產生"));
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) reportContent.getScene().getWindow();
        stage.close();
    }
}

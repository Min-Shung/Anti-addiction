package report;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WeeklyReportPrinter {

    public static void printLastWeekReport() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("lastWeekRecord.json")));
            JSONObject report = new JSONObject(content);

            int total = report.optInt("totalUsedMinutes", 0);
            double average = report.optDouble("averageUsedMinutes", 0.0);
            JSONArray dailyUsage = report.optJSONArray("dailyUsage");

            System.out.println("====== 上週使用報告 ======");
            System.out.println("總使用時間 (分鐘): " + total);
            System.out.printf("平均每日使用時間 (分鐘): %.2f\n", average);
            System.out.println("每日使用時間明細：");

            if (dailyUsage != null) {
                for (int i = 0; i < dailyUsage.length(); i++) {
                    JSONObject dayRecord = dailyUsage.getJSONObject(i);
                    String day = dayRecord.getString("day");
                    int usedMinutes = dayRecord.getInt("usedMinutes");
                    System.out.println(day + ": " + usedMinutes + " 分鐘");
                }
            } else {
                System.out.println("沒有資料。");
            }

            System.out.println("=======================");
        } catch (Exception e) {
            System.out.println("讀取週報失敗，可能尚未產生報告。");
        }
    }
}

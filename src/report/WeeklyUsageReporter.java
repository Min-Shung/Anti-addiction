package report;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class WeeklyUsageReporter {
    private static final String THIS_WEEK_FILE = "thisWeekRecord.json"; // 儲存本週每日使用時間
    private static final String LAST_WEEK_FILE = "lastWeekRecord.json"; // 儲存上週統計報告
    private static final Locale LOCALE = Locale.ENGLISH; // 星期格式為英文

    /**
     * 記錄當天的使用時長。
     * dailyLimitMinutes：當天總可用時長
     * remainingMinutes：剩餘時間
     */
    public static void recordDailyUsage(int dailyLimitMinutes, int remainingMinutes) {
        int usedMinutes = dailyLimitMinutes - remainingMinutes;
        if (usedMinutes < 0) usedMinutes = 0;

        String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE).toUpperCase();

        JSONObject thisWeek = loadJson(THIS_WEEK_FILE);
        thisWeek.put(today, usedMinutes);
        saveJson(thisWeek, THIS_WEEK_FILE);

        if (LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
            generateWeeklyReport(thisWeek);
            saveJson(new JSONObject(), THIS_WEEK_FILE);
        }
    }

    /**
     * 模擬版：可指定日期記錄使用時間（方便測試）。
     */
    public static void recordDailyUsage(int dailyLimit, int remainingMinutes, LocalDate fakeDate) {
        int usedTime = dailyLimit - remainingMinutes;
        if (usedTime < 0) usedTime = 0;

        String dayName = fakeDate.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE).toUpperCase();

        JSONObject thisWeekData = loadJson(THIS_WEEK_FILE);
        thisWeekData.put(dayName, usedTime);
        saveJson(thisWeekData, THIS_WEEK_FILE);

        if (fakeDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            generateWeeklyReport(thisWeekData);
            saveJson(new JSONObject(), THIS_WEEK_FILE);
        }
    }

    /**
     * 根據本週資料生成上週報告（含總時長、每日時長陣列、平均時長）。
     */
    private static void generateWeeklyReport(JSONObject thisWeek) {
        int total = 0;
        JSONArray daily = new JSONArray();

        // 確保按照星期一到星期日排序
        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, LOCALE).toUpperCase();

            if (thisWeek.has(dayName)) {
                int used = thisWeek.getInt(dayName);
                total += used;

                JSONObject dayRecord = new JSONObject();
                dayRecord.put("day", dayName);
                dayRecord.put("usedMinutes", used);
                daily.put(dayRecord);
            }
        }

        double average = daily.length() > 0 ? (double) total / daily.length() : 0;

        JSONObject report = new JSONObject();
        report.put("totalUsedMinutes", total);
        report.put("dailyUsage", daily);
        report.put("averageUsedMinutes", average);

        saveJson(report, LAST_WEEK_FILE);
    }

    /**
     * 載入指定 JSON 檔案，若無檔案則回傳空 JSONObject。
     */
    private static JSONObject loadJson(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            return new JSONObject(content);
        } catch (IOException e) {
            return new JSONObject();
        }
    }

    /**
     * 儲存 JSON 物件到檔案（格式美化）。
     */
    private static void saveJson(JSONObject json, String path) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(json.toString(4)); // 縮排4格，美化輸出
        } catch (IOException e) {
            System.out.println("儲存 JSON 失敗：" + path);
        }
    }
}

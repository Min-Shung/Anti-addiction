package config;
import java.io.*;

import java.util.Base64;
import java.util.Scanner;
import config.Config;
import report.WeeklyReportPrinter;
import report.WeeklyUsageReporter; 

public class PasswordManagerPersistent {
    private static final String CONFIG_FILE = "config.txt";
    private static final String XOR_KEY = "mysecretkey";
    private static Scanner scanner = new Scanner(System.in);

    private static byte[] xorCrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    // 讀取設定檔並轉成 Config 物件
    public static Config loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) return null;

            BufferedReader br = new BufferedReader(new FileReader(file));
            String base64 = br.readLine();
            br.close();

            if (base64 == null) return null;

            byte[] encryptedBytes = Base64.getDecoder().decode(base64);
            byte[] decryptedBytes = xorCrypt(encryptedBytes, XOR_KEY.getBytes());
            String decrypted = new String(decryptedBytes);

            String[] parts = decrypted.split("\n");
            if (parts.length >= 3) {
                String password = parts[0];
                int duration = Integer.parseInt(parts[1]);
                boolean restrict = Boolean.parseBoolean(parts[2]);
                return new Config(password, duration, restrict);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 儲存 Config 物件
    public static void saveConfig(Config config) {
        try {
            String plain = config.getPassword() + "\n" + config.getDurationMinutes() + "\n" + config.isRestrictTime();
            byte[] plainBytes = plain.getBytes();
            byte[] encryptedBytes = xorCrypt(plainBytes, XOR_KEY.getBytes());
            String base64 = Base64.getEncoder().encodeToString(encryptedBytes);

            BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_FILE));
            bw.write(base64);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.out.println("寫入設定檔失敗！");
        }
    }

    // 執行設定或登入流程
    /*public static Config run() {
        Config config = loadConfig();

        if (config == null) {  // 第一次使用
            System.out.print("查無帳號，請先註冊!\n");
            System.out.print("請輸入密碼：");
            String password = scanner.nextLine();

            System.out.print("請輸入使用時長（分鐘）：");
            int duration = Integer.parseInt(scanner.nextLine());

            System.out.print("是否限制時間？(true/false)：");
            boolean restrictTime = Boolean.parseBoolean(scanner.nextLine());

            config = new Config(password, duration, restrictTime);
            saveConfig(config);
            System.out.println("設定完成");

        } else {  // 已有設定
            boolean loginSuccess = false;
            while (!loginSuccess) {
                System.out.print("請輸入密碼登入：");
                String input = scanner.nextLine();
                if (input.equals(config.getPassword())) {
                    loginSuccess = true;
                    System.out.println("登入成功！");
                } else {
                    System.out.println("密碼錯誤，請再試一次。");
                }
            }
            //目前
            System.out.println("目前使用時長設定為：" + config.getDurationMinutes() + " 分鐘");
            System.out.println("限制時間功能為：" + (config.isRestrictTime() ? "開啟" : "關閉"));
            
            //重設
            
            System.out.print("是否要重新設定時間與限制？(true/false)：");
            boolean shouldReset = Boolean.parseBoolean(scanner.nextLine());
            if (shouldReset) {
                System.out.print("請輸入新的使用時長（分鐘）：");
                int duration = Integer.parseInt(scanner.nextLine());
                config.setDurationMinutes(duration);

                System.out.print("是否限制時間？(true/false)：");
                boolean restrictTime = Boolean.parseBoolean(scanner.nextLine());
                config.setRestrictTime(restrictTime);

                saveConfig(config);
                System.out.println("新設定已儲存！");
            }    
            boolean exit = false;
            while (!exit) {
                System.out.println("\n輸入指令：");
                System.out.println("  showreport  顯示上週使用報告");
                System.out.println("  exit        離開程式");
                System.out.print("請輸入：");

                String cmd = scanner.nextLine().trim().toLowerCase();

                switch (cmd) {
                    case "showreport":
                        WeeklyReportPrinter.printLastWeekReport();
                        break;
                    case "exit":
                        exit = true;
                        System.out.println("遊戲偵測已啟動");
                        break;
                    default:
                        System.out.println("未知指令，請重新輸入。");
                }
            }

        }

        return config; // 回傳 Config 物件供其他模組使用
    }*/
    public static Config loadForOtherModules() {
        return loadConfig();
    }
    // 用於 GUI 判斷是否第一次使用
    public static boolean isRegistered() {
        return new File(CONFIG_FILE).exists();
    }

    // 僅檢查密碼是否正確
    public static boolean validatePassword(String inputPassword) {
        Config config = loadConfig();
        return config != null && config.getPassword().equals(inputPassword);
    }
}

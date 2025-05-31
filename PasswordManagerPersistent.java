import java.io.*;
import java.util.Base64;
import java.util.Scanner;

public class PasswordManagerPersistent {
    private static final String CONFIG_FILE = "config.txt";       // 設定檔名稱
    private static final String XOR_KEY = "mysecretkey";          // XOR 加密用的金鑰
    private static Scanner scanner = new Scanner(System.in);      // 用來接收使用者輸入

    // XOR 加密/解密（對稱加密）
    private static byte[] xorCrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    // 載入設定檔內容
    public static String[] loadConfig() {
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
                return new String[]{ parts[0], parts[1], parts[2] };
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 提供外部模組讀取用的靜態方法
    public static String[] loadForOtherModules() {
        return loadConfig();
    }
    
    // 儲存設定檔
    private static void saveConfig(String password, int duration, boolean restrict) {
        try {
            String plain = password + "\n" + duration + "\n" + restrict;
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

    // 主邏輯，可供外部呼叫
    public static void run() {
        String[] config = loadConfig();
        String password;
        int duration;
        boolean restrictTime;

        if (config == null) {  // 第一次使用
            System.out.print("請輸入密碼：");
            password = scanner.nextLine();

            System.out.print("請輸入使用時長（分鐘）：");
            duration = Integer.parseInt(scanner.nextLine());

            System.out.print("是否限制時間？(true/false)：");
            restrictTime = Boolean.parseBoolean(scanner.nextLine());

            saveConfig(password, duration, restrictTime);
            System.out.println("設定完成");

        } else {  // 已有設定檔
            password = config[0];
            duration = Integer.parseInt(config[1]);
            restrictTime = Boolean.parseBoolean(config[2]);

            boolean loginSuccess = false;
            while (!loginSuccess) {
                System.out.print("請輸入密碼登入：");
                String input = scanner.nextLine();
                if (input.equals(password)) {
                    loginSuccess = true;
                    System.out.println("登入成功！");
                } else {
                    System.out.println("密碼錯誤，請再試一次。");
                }
            }

            System.out.print("是否要重新設定時間與限制？(true/false)：");
            boolean shouldReset = Boolean.parseBoolean(scanner.nextLine());
            if (shouldReset) {
                System.out.print("請輸入新的使用時長（分鐘）：");
                duration = Integer.parseInt(scanner.nextLine());

                System.out.print("是否限制時間？(true/false)：");
                restrictTime = Boolean.parseBoolean(scanner.nextLine());

                saveConfig(password, duration, restrictTime);
                System.out.println("新設定已儲存！");
            }
        }
    }
}


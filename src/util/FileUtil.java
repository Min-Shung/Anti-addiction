package util;

import java.io.*;
import java.util.regex.*;

public class FileUtil {

    public static void saveJson(String path, String content) {
        try (PrintWriter out = new PrintWriter(path)) {
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadJsonField(String path, String field) {
        try {
            String text = new String(java.nio.file.Files.readAllBytes(new File(path).toPath()));
            Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"(.*?)\"").matcher(text);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            return null;
        }
    }
}

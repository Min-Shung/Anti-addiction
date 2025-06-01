/*package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Properties;

import config.Config;
import core.Timecounter;

public class GUI_UI extends JFrame {
    private static final String CONFIG_FILE = "parental_control.cfg";
    private static final String PASSWORD_HASH_FILE = "password.hash";
    private Properties config;
    private JPasswordField passwordField;
    private JTextField timeLimitField;
    private JCheckBox timeRestrictionCheckBox;

    public GUI_UI() {
        // 初始化配置
        config = new Properties();
        loadConfig();

        // 設置主窗口
        setTitle("時間管理小幫手");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中顯示

        // 檢查是否有密碼
        if (!hasPassword()) {
            showPasswordSetup();
        } else {
            showPasswordLogin();
        }
    }

    private void loadConfig() {
        try (InputStream input = Files.newInputStream(Paths.get(CONFIG_FILE))) {
            config.load(input);
        } catch (IOException e) {
            config.setProperty("timeLimit", "60"); // 默認60分鐘
            config.setProperty("timeRestriction", "true"); // 默認啟用時間限制
        }
    }

    private void saveConfig() {
        try (OutputStream output = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            config.store(output, "Parental Control Configuration");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存配置失敗: " + e.getMessage(), 
                "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasPassword() {
        return Files.exists(Paths.get(PASSWORD_HASH_FILE));
    }

    private void savePassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            Files.write(Paths.get(PASSWORD_HASH_FILE), hash);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "密碼保存失敗: " + e.getMessage(), 
                "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean verifyPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] inputHash = md.digest(password.getBytes());
            byte[] savedHash = Files.readAllBytes(Paths.get(PASSWORD_HASH_FILE));
            return MessageDigest.isEqual(inputHash, savedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private void showPasswordSetup() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel("首次使用，請設置密碼"), gbc);

        // 密碼輸入
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("設置密碼:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // 確認密碼
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("確認密碼:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        add(confirmField, gbc);

        // 保存按鈕
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton saveButton = new JButton("保存密碼");
        saveButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "密碼不能為空", 
                    "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "兩次輸入的密碼不一致", 
                    "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            savePassword(password);
            showMainControl();
        });
        add(saveButton, gbc);

        revalidate();
        repaint();
    }

    private void showPasswordLogin() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel("請輸入密碼"), gbc);

        // 密碼輸入
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("密碼:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // 登入按鈕
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("登入");
        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (verifyPassword(password)) {
                showMainControl();
            } else {
                JOptionPane.showMessageDialog(this, "密碼錯誤", 
                    "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(loginButton, gbc);

        revalidate();
        repaint();
    }

    private void showMainControl() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 遊戲時間設置
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("遊戲時間(分鐘):"), gbc);
        
        gbc.gridx = 1;
        timeLimitField = new JTextField(config.getProperty("timeLimit", "60"), 10);
        add(timeLimitField, gbc);

        // 時間限制設置
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("啟用23:00-01:00禁止遊玩:"), gbc);
        
        gbc.gridx = 1;
        timeRestrictionCheckBox = new JCheckBox();
        timeRestrictionCheckBox.setSelected(Boolean.parseBoolean(
            config.getProperty("timeRestriction", "true")));
        add(timeRestrictionCheckBox, gbc);

        // 保存按鈕
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton saveButton = new JButton("保存設置");
        saveButton.addActionListener(e -> {
            try {
                int timeLimit = Integer.parseInt(timeLimitField.getText());
                if (timeLimit <= 0) {
                    throw new NumberFormatException();
                }
                
                config.setProperty("timeLimit", String.valueOf(timeLimit));
                config.setProperty("timeRestriction", 
                    String.valueOf(timeRestrictionCheckBox.isSelected()));
                saveConfig();
                
                JOptionPane.showMessageDialog(this, "設置已保存", 
                    "成功", JOptionPane.INFORMATION_MESSAGE);
                
                // 這裡可以啟動防沉迷計時器
                startTimeCounter(timeLimit, timeRestrictionCheckBox.isSelected());
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "請輸入有效的正整數", 
                    "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(saveButton, gbc);

        revalidate();
        repaint();
    }

    private void startTimeCounter(int timeLimit, boolean timeRestriction) {
        // 這裡可以整合您之前的Timecounter類
        JOptionPane.showMessageDialog(this, 
            "防沉迷計時器已啟動\n" +
            "每日遊戲時間: " + timeLimit + "分鐘\n" +
            "時間限制: " + (timeRestriction ? "啟用(23:00-01:00禁止)" : "禁用"),
            "系統啟動", JOptionPane.INFORMATION_MESSAGE);
        
        // 實際應用中可以這樣使用:
        // Timecounter timer = new Timecounter(timeLimit, new NotificationListener() {...});
        // timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GUI_UI app = new GUI_UI();
            app.setVisible(true);
        });
    }
}
*/

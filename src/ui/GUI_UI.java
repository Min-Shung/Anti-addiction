import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Properties;

public class GUI_UI extends JFrame {
    private static final String CONFIG_FILE = "parental_control.cfg";
    private static final String PASSWORD_HASH_FILE = "password.hash";
    private Properties config;
    private JPasswordField passwordField;
    private JTextField timeLimitField;
    private JCheckBox timeRestrictionCheckBox;
    
    // 顏色定義
    private final Color SKY_BLUE = new Color(225, 240, 255);  // 淡藍色背景
    private final Color BUTTON_GRAY = new Color(220, 220, 20);    // 深灰色按鈕
    private final Color BUTTON_HOVER = new Color(130, 130, 130);   // 懸停狀態
    private final Color BUTTON_PRESSED = new Color(70, 70, 70);    // 按下狀態
    private final Color BUTTON_TEXT = Color.BLACK;                 // 按鈕文字顏色
    private final Color BORDER_COLOR = new Color(80, 80, 80);      // 按鈕邊框顏色
    
    // 字體定義
    private final Font TITLE_FONT = new Font("微軟正黑體", Font.BOLD, 20);
    private final Font LABEL_FONT = new Font("微軟正黑體", Font.PLAIN, 16);
    private final Font BUTTON_FONT = new Font("微軟正黑體", Font.BOLD, 16);
    private final Font TEXT_FIELD_FONT = new Font("微軟正黑體", Font.PLAIN, 14);

    public GUI_UI() {
        // 初始化配置
        config = new Properties();
        loadConfig();
        
        // 設置主窗口
        setTitle("時間管理小幫手(⁠ ⁠╹⁠▽⁠╹⁠ ⁠)");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SKY_BLUE); // 設置淡藍色背景

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
            config.setProperty("timeLimit", "60");
            config.setProperty("timeRestriction", "true");
        }
    }

    private void saveConfig() {
        try (OutputStream output = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            config.store(output, "Parental Control Configuration");
        } catch (IOException e) {
            showErrorDialog("保存配置失敗: " + e.getMessage());
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
            showErrorDialog("密碼保存失敗: " + e.getMessage());
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
        gbc.insets = new Insets(15, 15, 15, 15); // 增加間距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("首次使用，請設置密碼");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, gbc);

        // 密碼輸入
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel pwLabel = new JLabel("設置密碼:");
        pwLabel.setFont(LABEL_FONT);
        add(pwLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(TEXT_FIELD_FONT);
        add(passwordField, gbc);

        // 確認密碼
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel confirmLabel = new JLabel("確認密碼:");
        confirmLabel.setFont(LABEL_FONT);
        add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        confirmField.setFont(TEXT_FIELD_FONT);
        add(confirmField, gbc);

        // 保存按鈕
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton saveButton = createStyledButton("保存密碼");
        saveButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (password.isEmpty()) {
                showErrorDialog("密碼不能為空");
                return;
            }
            
            if (!password.equals(confirm)) {
                showErrorDialog("兩次輸入的密碼不一致");
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
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("請輸入密碼");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, gbc);

        // 密碼輸入
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel pwLabel = new JLabel("密碼:");
        pwLabel.setFont(LABEL_FONT);
        add(pwLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(TEXT_FIELD_FONT);
        add(passwordField, gbc);

        // 登入按鈕
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton loginButton = createStyledButton("登入");
        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (verifyPassword(password)) {
                showMainControl();
            } else {
                showErrorDialog("密碼錯誤");
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
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("時間管理設置");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, gbc);

        // 遊戲時間設置
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel timeLabel = new JLabel("遊戲時間(分鐘):");
        timeLabel.setFont(LABEL_FONT);
        add(timeLabel, gbc);
        
        gbc.gridx = 1;
        timeLimitField = new JTextField(config.getProperty("timeLimit", "60"), 10);
        timeLimitField.setFont(TEXT_FIELD_FONT);
        add(timeLimitField, gbc);

        // 時間限制設置
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel restrictLabel = new JLabel("啟用23:00-01:00禁止遊玩:");
        restrictLabel.setFont(LABEL_FONT);
        add(restrictLabel, gbc);
        
        gbc.gridx = 1;
        timeRestrictionCheckBox = new JCheckBox();
        timeRestrictionCheckBox.setBackground(SKY_BLUE);
        timeRestrictionCheckBox.setSelected(Boolean.parseBoolean(
            config.getProperty("timeRestriction", "true")));
        add(timeRestrictionCheckBox, gbc);

        // 保存按鈕
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton saveButton = createStyledButton("保存設置");
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
                
                showSuccessDialog("設置已保存");
                startTimeCounter(timeLimit, timeRestrictionCheckBox.isSelected());
                
            } catch (NumberFormatException ex) {
                showErrorDialog("請輸入有效的正整數");
            }
        });
        add(saveButton, gbc);

        revalidate();
        repaint();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(BUTTON_GRAY);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), // 灰色邊框
            BorderFactory.createEmptyBorder(10, 25, 10, 25)  // 內邊距
        ));
        
        // 滑鼠懸停效果
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR.darker()),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(BUTTON_GRAY);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
            public void mousePressed(MouseEvent evt) {
                button.setBackground(BUTTON_PRESSED); // 按下時更深
            }
            public void mouseReleased(MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
            }
        });
        
        return button;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, 
            "<html><div style='font-family:微軟正黑體;font-size:14px;'>" + message + "</div></html>", 
            "錯誤", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this, 
            "<html><div style='font-family:微軟正黑體;font-size:14px;'>" + message + "</div></html>", 
            "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startTimeCounter(int timeLimit, boolean timeRestriction) {
        JOptionPane.showMessageDialog(this, 
            "<html><div style='font-family:微軟正黑體;font-size:14px;'>" +
            "防沉迷計時器已啟動<br>" +
            "每日遊戲時間: " + timeLimit + "分鐘<br>" +
            "時間限制: " + (timeRestriction ? "啟用(23:00-01:00禁止)" : "禁用") +
            "</div></html>", 
            "系統啟動", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.put("Button.font", new Font("微軟正黑體", Font.PLAIN, 14));
                UIManager.put("Label.font", new Font("微軟正黑體", Font.PLAIN, 14));
                UIManager.put("TextField.font", new Font("微軟正黑體", Font.PLAIN, 14));
                UIManager.put("PasswordField.font", new Font("微軟正黑體", Font.PLAIN, 14));
                UIManager.put("OptionPane.messageFont", new Font("微軟正黑體", Font.PLAIN, 14));
                
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GUI_UI app = new GUI_UI();
            app.setVisible(true);
        });
    }
}

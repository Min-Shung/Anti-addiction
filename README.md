# Anti-addiction
資料夾結構
GameUsageLimiter/
│
├── src/                            // Java 原始碼
│   ├── config/                     // 設定、密碼與使用者資料
│   │   ├── Config.java
│   │   └── PasswordManagerPersistent.java
│
│   ├── core/                       // 核心邏輯（時間管理、監控、遊戲控制）
│   │   ├── Timecounter.java
│   │   ├── UsageTimeManager.java
│   │   └── WindowsNotifier.java
│
│   ├── detection/                 // 遊戲偵測與控制
│   │   ├── GameDetection.java
│   │   ├── DetectGameProcess.java
│   │   └── GameMonitorService.java
│
│   ├── ui/                         // 主程式與介面整合（可擴充為 GUI）
│   │   └── Main.java
│
|   ├── report/                        // ⬅️ 新增報告與分析模組
|   │   └── WeeklyReportGenerator.java
|
├── resources/                     // 靜態檔案，如 config.txt、遊戲清單等
│   ├── config.txt
│   └── games.json
│
├── build/                         // 編譯後的 class 檔或 jar
│
├── lib/                           // 可能用到的外部函式庫（如 JNA、NTP client 等）
│
├── README.md
└── pom.xml / build.gradle         // 若用 Maven 或 Gradle

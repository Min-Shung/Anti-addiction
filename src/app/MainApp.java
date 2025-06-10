package app;

import notifier.NotificationListener;
import core.WindowsNotifier;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;


public class MainApp extends Application {
    private WindowsNotifier windowsNotifier;
    @Override
    public void start(Stage primaryStage) throws Exception {
        windowsNotifier = new WindowsNotifier();
        javafx.scene.text.Font.loadFont(getClass().getResource("/fonts/static/NotoSansTC-Regular.ttf").toExternalForm(), 14);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/home/HomeView.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 500, 240);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("ChronoLock");
        primaryStage.setScene(scene);

        Image icon = new Image(getClass().getResourceAsStream("/chronolock.png"));
        primaryStage.getIcons().add(icon);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

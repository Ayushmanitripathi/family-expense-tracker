package com.familyexpense;

import com.familyexpense.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Initialize database
        DatabaseManager.getInstance().initializeDatabase();

        // Check if it's the first launch (no family name set)
        String familyName = DatabaseManager.getInstance().getFamilyName();

        if (familyName == null || familyName.isEmpty()) {
            // Show welcome / setup screen
            loadScene("/fxml/welcome.fxml", "🏠 Ghar Ka Kharcha - Welcome", 600, 500);
        } else {
            // Load main dashboard
            loadScene("/fxml/dashboard.fxml", "🏠 " + familyName + " - Ghar Ka Kharcha", 1100, 750);
        }

        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void loadScene(String fxmlPath, String title, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
            Objects.requireNonNull(Main.class.getResource("/css/styles.css")).toExternalForm()
        );
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void loadSceneWithData(String fxmlPath, String title, double width, double height, Object data) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        Parent root = loader.load();
        // Pass data to controller if needed
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
            Objects.requireNonNull(Main.class.getResource("/css/styles.css")).toExternalForm()
        );
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateToDashboard() {
        try {
            String familyName = DatabaseManager.getInstance().getFamilyName();
            loadScene("/fxml/dashboard.fxml", "🏠 " + familyName + " - Ghar Ka Kharcha", 1100, 750);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

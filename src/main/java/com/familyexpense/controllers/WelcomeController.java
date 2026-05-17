package com.familyexpense.controllers;

import com.familyexpense.Main;
import com.familyexpense.database.DatabaseManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class WelcomeController {

    @FXML private TextField familyNameField;
    @FXML private Label errorLabel;
    @FXML private VBox welcomeCard;

    @FXML
    public void initialize() {
        FadeTransition ft = new FadeTransition(Duration.millis(700), welcomeCard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    public void handleStart() {
        String name = familyNameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("⚠  Please enter your family name!");
            familyNameField.requestFocus();
            return;
        }
        DatabaseManager db = DatabaseManager.getInstance();
        db.setFamilyName(name);
        if (!db.isSampleDataLoaded()) {
            db.loadSampleData();
        }
        Main.navigateToDashboard();
    }

    @FXML
    public void handleEnterKey() {
        handleStart();
    }
}

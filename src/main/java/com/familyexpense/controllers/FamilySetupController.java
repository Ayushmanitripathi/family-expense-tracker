package com.familyexpense.controllers;

import com.familyexpense.Main;
import com.familyexpense.database.DatabaseManager;
import com.familyexpense.models.FamilyMember;
import com.familyexpense.utils.CategoryHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Optional;

public class FamilySetupController {

    @FXML private VBox memberListPane;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(CategoryHelper.getMemberRoles()));
        roleCombo.getSelectionModel().selectFirst();
        refreshMemberList();
    }

    private void refreshMemberList() {
        memberListPane.getChildren().clear();
        List<FamilyMember> members = DatabaseManager.getInstance().getAllMembers();

        if (members.isEmpty()) {
            Label empty = new Label("No family members added yet. Add one below! 👇");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
            memberListPane.getChildren().add(empty);
            return;
        }

        for (FamilyMember member : members) {
            HBox card = createMemberRow(member);
            memberListPane.getChildren().add(card);
        }
    }

    private HBox createMemberRow(FamilyMember member) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12px; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);");

        Label emoji = new Label(member.getEmoji());
        emoji.setFont(Font.font(28));

        VBox info = new VBox(3);
        Label name = new Label(member.getName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label role = new Label(member.getRole());
        role.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        info.getChildren().addAll(name, role);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑 Remove");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Member?");
            alert.setHeaderText("Remove " + member.getEmoji() + " " + member.getName() + " from the family?");
            alert.setContentText("Their past expense records will be preserved.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DatabaseManager.getInstance().deleteMember(member.getId());
                refreshMemberList();
                showStatus("✅ " + member.getName() + " has been removed.", true);
            }
        });

        row.getChildren().addAll(emoji, info, spacer, deleteBtn);
        return row;
    }

    @FXML
    public void handleAddMember() {
        String name = nameField.getText().trim();
        String role = roleCombo.getValue();

        if (name.isEmpty()) {
            showStatus("⚠ Please enter a name!", false);
            nameField.requestFocus();
            return;
        }

        String emoji = CategoryHelper.getEmojiForRole(role != null ? role : "Other");
        FamilyMember member = new FamilyMember(name, emoji, role != null ? role : "Other");
        int id = DatabaseManager.getInstance().addMember(member);

        if (id > 0) {
            showStatus("✅ " + name + " has been added to the family!", true);
            nameField.clear();
            roleCombo.getSelectionModel().selectFirst();
            refreshMemberList();
        } else {
            showStatus("⚠ Something went wrong. Please try again.", false);
        }
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
            ? "-fx-text-fill: #06D6A0; -fx-font-weight: bold;"
            : "-fx-text-fill: #EF233C; -fx-font-weight: bold;");
    }

    @FXML public void handleHome() { Main.navigateToDashboard(); }
    @FXML public void handleAddExpense() {
        try { Main.loadScene("/fxml/add_expense.fxml", "➕ Add Expense", 900, 680); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML public void handleHistory() {
        try { Main.loadScene("/fxml/history.fxml", "📋 Expense History", 1100, 750); }
        catch (Exception e) { e.printStackTrace(); }
    }
    @FXML public void handleReport() {
        try { Main.loadScene("/fxml/report.fxml", "📊 Monthly Report", 1100, 750); }
        catch (Exception e) { e.printStackTrace(); }
    }
}

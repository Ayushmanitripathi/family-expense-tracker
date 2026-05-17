package com.familyexpense.controllers;

import com.familyexpense.Main;
import com.familyexpense.database.DatabaseManager;
import com.familyexpense.models.Expense;
import com.familyexpense.models.FamilyMember;
import com.familyexpense.utils.CategoryHelper;
import com.familyexpense.utils.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AddExpenseController {

    @FXML private ComboBox<FamilyMember> memberCombo;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField noteField;
    @FXML private Label statusLabel;
    @FXML private Label totalLabel;
    @FXML private Button addBtn;

    @FXML
    public void initialize() {
        List<FamilyMember> members = DatabaseManager.getInstance().getAllMembers();
        memberCombo.setItems(FXCollections.observableArrayList(members));
        if (!members.isEmpty()) memberCombo.getSelectionModel().selectFirst();

        categoryCombo.setItems(FXCollections.observableArrayList(CategoryHelper.getCategoryNames()));
        categoryCombo.getSelectionModel().selectFirst();

        datePicker.setValue(LocalDate.now());
        refreshTotal();
    }

    private void refreshTotal() {
        LocalDate now = LocalDate.now();
        double total = DatabaseManager.getInstance().getTotalExpenseForMonth(now.getMonthValue(), now.getYear());
        totalLabel.setText("This Month's Total: " + CurrencyFormatter.format(total));
    }

    @FXML
    public void handleAdd() {
        FamilyMember member = memberCombo.getValue();
        String amountText = amountField.getText().trim();
        String category = categoryCombo.getValue();
        LocalDate date = datePicker.getValue();

        if (member == null) { showStatus("⚠ Please select a family member!", false); return; }
        if (amountText.isEmpty()) { showStatus("⚠ Please enter the amount!", false); return; }
        if (category == null) { showStatus("⚠ Please select a category!", false); return; }

        double amount;
        try {
            amount = Double.parseDouble(amountText.replace(",", ""));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showStatus("⚠ Please enter a valid number!", false);
            return;
        }

        Expense expense = new Expense();
        expense.setMemberId(member.getId());
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setCategoryEmoji(CategoryHelper.getEmoji(category));
        expense.setDate(date != null
            ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        expense.setNote(noteField.getText().trim());

        DatabaseManager.getInstance().addExpense(expense);
        showStatus("✅ Expense saved! " + CurrencyFormatter.format(amount) + " — " + category, true);

        // Budget warning check
        LocalDate now = LocalDate.now();
        double total = DatabaseManager.getInstance().getTotalExpenseForMonth(now.getMonthValue(), now.getYear());
        var budget = DatabaseManager.getInstance().getBudgetForMonth(now.getMonthValue(), now.getYear());
        if (budget != null && budget.getMonthlyBudget() > 0) {
            double pct = (total / budget.getMonthlyBudget()) * 100;
            if (pct >= 100) {
                showStatus("🚨 Budget exceeded! You have overspent this month!", false);
            } else if (pct >= 80) {
                showStatus("⚠ Warning! " + String.format("%.0f", pct) + "% of your budget has been used!", false);
            }
        }

        amountField.clear();
        noteField.clear();
        datePicker.setValue(LocalDate.now());
        refreshTotal();
    }

    @FXML public void handleBack() { Main.navigateToDashboard(); }

    @FXML public void handleGoHistory() {
        try { Main.loadScene("/fxml/history.fxml", "📋 Expense History", 1100, 750); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
            ? "-fx-text-fill: #06D6A0; -fx-font-weight: bold; -fx-font-size: 13px;"
            : "-fx-text-fill: #EF233C; -fx-font-weight: bold; -fx-font-size: 13px;");
    }
}

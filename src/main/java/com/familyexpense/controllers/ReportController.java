package com.familyexpense.controllers;

import com.familyexpense.Main;
import com.familyexpense.database.DatabaseManager;
import com.familyexpense.models.Budget;
import com.familyexpense.utils.CurrencyFormatter;
import com.familyexpense.utils.PDFExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class ReportController {

    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<Integer> yearCombo;
    @FXML private Label totalExpenseLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label savingsLabel;
    @FXML private Label topCategoryLabel;
    @FXML private Label topMemberLabel;
    @FXML private Label budgetStatusLabel;
    @FXML private PieChart reportChart;
    @FXML private TextField incomeField;
    @FXML private TextField budgetField;
    @FXML private Label budgetSaveStatus;

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    @FXML
    public void initialize() {
        LocalDate now = LocalDate.now();
        monthCombo.setItems(FXCollections.observableArrayList(MONTHS));
        monthCombo.getSelectionModel().select(now.getMonthValue() - 1);
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int y = now.getYear(); y >= now.getYear() - 3; y--) years.add(y);
        yearCombo.setItems(years);
        yearCombo.getSelectionModel().selectFirst();
        loadReport();
    }

    @FXML
    public void loadReport() {
        int month = monthCombo.getSelectionModel().getSelectedIndex() + 1;
        int year = yearCombo.getValue() != null ? yearCombo.getValue() : LocalDate.now().getYear();
        DatabaseManager db = DatabaseManager.getInstance();

        double totalExpense = db.getTotalExpenseForMonth(month, year);
        Budget budget = db.getBudgetForMonth(month, year);
        double income = budget != null ? budget.getTotalIncome() : 0;
        double savings = income - totalExpense;

        totalExpenseLabel.setText(CurrencyFormatter.format(totalExpense));
        totalIncomeLabel.setText(income > 0 ? CurrencyFormatter.format(income) : "Not set");
        savingsLabel.setText(income > 0 ? CurrencyFormatter.format(savings) : "---");
        savingsLabel.setStyle(savings >= 0
            ? "-fx-text-fill:#06D6A0;-fx-font-weight:bold;-fx-font-size:20px;"
            : "-fx-text-fill:#EF233C;-fx-font-weight:bold;-fx-font-size:20px;");

        if (budget != null && budget.getMonthlyBudget() > 0) {
            double pct = (totalExpense / budget.getMonthlyBudget()) * 100;
            budgetStatusLabel.setText(String.format("Budget: %s  |  Spent: %.1f%%",
                CurrencyFormatter.format(budget.getMonthlyBudget()), pct));
            budgetField.setText(String.valueOf((int) budget.getMonthlyBudget()));
            incomeField.setText(String.valueOf((int) income));
        } else {
            budgetStatusLabel.setText("No budget set for this month");
        }

        Map<String, Double> catTotals = db.getCategoryTotalsForMonth(month, year);
        topCategoryLabel.setText(!catTotals.isEmpty()
            ? catTotals.entrySet().iterator().next().getKey() + " ("
              + CurrencyFormatter.format(catTotals.entrySet().iterator().next().getValue()) + ")"
            : "No expenses recorded");

        Map<String, Double> memberTotals = db.getMemberTotalsForMonth(month, year);
        topMemberLabel.setText(!memberTotals.isEmpty()
            ? memberTotals.entrySet().iterator().next().getKey() + " ("
              + CurrencyFormatter.format(memberTotals.entrySet().iterator().next().getValue()) + ")"
            : "---");

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> e : catTotals.entrySet())
            pieData.add(new PieChart.Data(e.getKey(), e.getValue()));
        reportChart.setData(pieData);
        reportChart.setTitle("Spending Distribution");
        for (PieChart.Data data : reportChart.getData()) {
            Tooltip t = new Tooltip(data.getName() + ": " + CurrencyFormatter.format(data.getPieValue()));
            t.setStyle("-fx-font-size:13px;");
            Tooltip.install(data.getNode(), t);
        }
    }

    @FXML
    public void handleSaveBudget() {
        int month = monthCombo.getSelectionModel().getSelectedIndex() + 1;
        int year = yearCombo.getValue() != null ? yearCombo.getValue() : LocalDate.now().getYear();
        try {
            double budgetAmt = budgetField.getText().isEmpty() ? 0 : Double.parseDouble(budgetField.getText());
            double incomeAmt = incomeField.getText().isEmpty() ? 0 : Double.parseDouble(incomeField.getText());
            Budget b = new Budget(budgetAmt, month, year);
            b.setTotalIncome(incomeAmt);
            DatabaseManager.getInstance().saveBudget(b);
            budgetSaveStatus.setText("✅ Budget saved successfully!");
            budgetSaveStatus.setStyle("-fx-text-fill:#06D6A0;-fx-font-weight:bold;");
            loadReport();
        } catch (NumberFormatException e) {
            budgetSaveStatus.setText("⚠ Please enter a valid number!");
            budgetSaveStatus.setStyle("-fx-text-fill:#EF233C;");
        }
    }

    @FXML
    public void handleExportPDF() {
        int month = monthCombo.getSelectionModel().getSelectedIndex() + 1;
        int year = yearCombo.getValue() != null ? yearCombo.getValue() : LocalDate.now().getYear();
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save PDF Report");
        chooser.setInitialFileName("Report_" + monthName + "_" + year + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(Main.getPrimaryStage());
        if (file != null) {
            try {
                PDFExporter.exportMonthlyReport(month, year, file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Ready!");
                alert.setHeaderText("✅ Report exported successfully!");
                alert.setContentText("Saved to: " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "PDF export error: " + ex.getMessage()).showAndWait();
            }
        }
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
    @FXML public void handleFamily() {
        try { Main.loadScene("/fxml/family_setup.fxml", "👨‍👩‍👧‍👦 Family Setup", 900, 680); }
        catch (Exception e) { e.printStackTrace(); }
    }
}

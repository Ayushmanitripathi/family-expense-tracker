package com.familyexpense.controllers;

import com.familyexpense.Main;
import com.familyexpense.database.DatabaseManager;
import com.familyexpense.models.Budget;
import com.familyexpense.models.FamilyMember;
import com.familyexpense.utils.CurrencyFormatter;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    @FXML private Label familyNameLabel;
    @FXML private Label monthYearLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label budgetLabel;
    @FXML private Label remainingLabel;
    @FXML private ProgressBar budgetProgressBar;
    @FXML private Label budgetPercentLabel;
    @FXML private FlowPane memberCardsPane;
    @FXML private PieChart categoryChart;
    @FXML private VBox budgetCard;
    @FXML private Label savingsLabel;
    @FXML private Label topCategoryLabel;
    @FXML private Label highestSpenderLabel;

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        DatabaseManager db = DatabaseManager.getInstance();
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        String familyName = db.getFamilyName();
        familyNameLabel.setText("🏠 " + (familyName != null ? familyName : "Family") + " Expense Tracker");
        monthYearLabel.setText(Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year);

        double totalExpense = db.getTotalExpenseForMonth(month, year);
        totalExpenseLabel.setText(CurrencyFormatter.format(totalExpense));

        Budget budget = db.getBudgetForMonth(month, year);
        if (budget != null && budget.getMonthlyBudget() > 0) {
            double pct = totalExpense / budget.getMonthlyBudget();
            double remaining = budget.getMonthlyBudget() - totalExpense;
            budgetProgressBar.setProgress(Math.min(pct, 1.0));
            budgetLabel.setText("Budget: " + CurrencyFormatter.format(budget.getMonthlyBudget()));
            remainingLabel.setText("Remaining: " + CurrencyFormatter.format(Math.max(remaining, 0)));
            budgetPercentLabel.setText(String.format("%.0f%% used", pct * 100));

            budgetProgressBar.getStyleClass().removeAll("progress-bar-red", "progress-bar-yellow", "progress-bar-green");
            String barStyle;
            String cardStyle;
            if (pct >= 1.0) {
                barStyle = "progress-bar-red";
                cardStyle = "-fx-background-color:linear-gradient(to right,#EF233C,#D90429);-fx-background-radius:16px;-fx-padding:20px;";
            } else if (pct >= 0.8) {
                barStyle = "progress-bar-yellow";
                cardStyle = "-fx-background-color:linear-gradient(to right,#FB8500,#FFAA00);-fx-background-radius:16px;-fx-padding:20px;";
            } else {
                barStyle = "progress-bar-green";
                cardStyle = "-fx-background-color:linear-gradient(to right,#06D6A0,#1BBD9E);-fx-background-radius:16px;-fx-padding:20px;";
            }
            budgetProgressBar.getStyleClass().add(barStyle);
            budgetCard.setStyle(cardStyle);

            double income = budget.getTotalIncome();
            savingsLabel.setText(income > 0
                ? "Savings: " + CurrencyFormatter.format(income - totalExpense)
                : "Set income in Report");
        } else {
            budgetProgressBar.setProgress(0);
            budgetLabel.setText("No budget set");
            remainingLabel.setText("Set in Report screen");
            budgetPercentLabel.setText("");
            savingsLabel.setText("---");
        }

        loadMemberCards(db, month, year);
        loadPieChart(db, month, year);

        Map<String, Double> catTotals = db.getCategoryTotalsForMonth(month, year);
        topCategoryLabel.setText(!catTotals.isEmpty()
            ? "Top: " + catTotals.entrySet().iterator().next().getKey()
            : "No expenses yet");

        Map<String, Double> memberTotals = db.getMemberTotalsForMonth(month, year);
        highestSpenderLabel.setText(!memberTotals.isEmpty()
            ? "Highest: " + memberTotals.entrySet().iterator().next().getKey()
            : "");
    }

    private void loadMemberCards(DatabaseManager db, int month, int year) {
        memberCardsPane.getChildren().clear();
        List<FamilyMember> members = db.getAllMembers();
        Map<String, Double> memberTotals = db.getMemberTotalsForMonth(month, year);
        for (FamilyMember member : members) {
            double spent = memberTotals.getOrDefault(member.getDisplayName(), 0.0);
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(16));
            card.getStyleClass().add("member-card");
            card.setMinWidth(150);

            Label emoji = new Label(member.getEmoji());
            emoji.setFont(Font.font(30));
            Label name = new Label(member.getName());
            name.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
            Label amount = new Label(CurrencyFormatter.format(spent));
            amount.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#4361EE;");
            Label role = new Label(member.getRole());
            role.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;");
            card.getChildren().addAll(emoji, name, amount, role);

            FadeTransition ft = new FadeTransition(Duration.millis(600), card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            memberCardsPane.getChildren().add(card);
        }
    }

    private void loadPieChart(DatabaseManager db, int month, int year) {
        Map<String, Double> catTotals = db.getCategoryTotalsForMonth(month, year);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> e : catTotals.entrySet())
            pieData.add(new PieChart.Data(e.getKey(), e.getValue()));
        categoryChart.setData(pieData);
        categoryChart.setTitle("Spending by Category");
        for (PieChart.Data data : categoryChart.getData()) {
            Tooltip t = new Tooltip(data.getName() + "\n" + CurrencyFormatter.format(data.getPieValue()));
            t.setStyle("-fx-font-size:13px;");
            Tooltip.install(data.getNode(), t);
        }
    }

    @FXML public void handleRefresh() { loadDashboardData(); }
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
    @FXML public void handleFamily() {
        try { Main.loadScene("/fxml/family_setup.fxml", "👨‍👩‍👧‍👦 Family Setup", 900, 680); }
        catch (Exception e) { e.printStackTrace(); }
    }
}

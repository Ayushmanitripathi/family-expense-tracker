package com.familyexpense.controllers;

import com.familyexpense.Main;
import com.familyexpense.database.DatabaseManager;
import com.familyexpense.models.Expense;
import com.familyexpense.models.FamilyMember;
import com.familyexpense.utils.CategoryHelper;
import com.familyexpense.utils.CurrencyFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HistoryController {

    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, String> colDate;
    @FXML private TableColumn<Expense, String> colMember;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, String> colAmount;
    @FXML private TableColumn<Expense, String> colNote;
    @FXML private TableColumn<Expense, Void> colAction;
    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> memberFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label totalLabel;
    @FXML private TextField searchField;

    private static final String[] MONTHS = {
        "All Months", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        colMember.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getMemberName() != null ? data.getValue().getMemberName() : ""));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCategoryEmoji() + " " + data.getValue().getCategory()));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(
            CurrencyFormatter.format(data.getValue().getAmount())));
        colNote.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getNote() != null ? data.getValue().getNote() : ""));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑 Delete");
            {
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Expense?");
                    confirm.setHeaderText("Are you sure you want to delete this entry?");
                    confirm.setContentText(expense.getCategoryEmoji() + " " + expense.getCategory()
                        + " — " + CurrencyFormatter.format(expense.getAmount()));
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        DatabaseManager.getInstance().deleteExpense(expense.getId());
                        loadData();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        expenseTable.setPlaceholder(new Label("No expenses found 😊"));
    }

    private void setupFilters() {
        monthFilter.setItems(FXCollections.observableArrayList(MONTHS));
        monthFilter.getSelectionModel().select(LocalDate.now().getMonthValue());

        List<FamilyMember> members = DatabaseManager.getInstance().getAllMembers();
        ObservableList<String> memberNames = FXCollections.observableArrayList("All Members");
        members.forEach(m -> memberNames.add(m.getName()));
        memberFilter.setItems(memberNames);
        memberFilter.getSelectionModel().selectFirst();

        ObservableList<String> cats = FXCollections.observableArrayList("All Categories");
        for (String cat : CategoryHelper.getCategoryNames()) cats.add(cat);
        categoryFilter.setItems(cats);
        categoryFilter.getSelectionModel().selectFirst();
    }

    @FXML public void handleFilter() { loadData(); }

    @FXML
    public void handleClearFilter() {
        monthFilter.getSelectionModel().select(LocalDate.now().getMonthValue());
        memberFilter.getSelectionModel().selectFirst();
        categoryFilter.getSelectionModel().selectFirst();
        searchField.clear();
        loadData();
    }

    private void loadData() {
        int monthIdx = monthFilter.getSelectionModel().getSelectedIndex();
        Integer month = monthIdx == 0 ? null : monthIdx;
        Integer year = monthIdx == 0 ? null : LocalDate.now().getYear();

        String memberName = memberFilter.getValue();
        Integer memberId = null;
        if (memberName != null && !memberName.equals("All Members")) {
            List<FamilyMember> members = DatabaseManager.getInstance().getAllMembers();
            for (FamilyMember m : members) {
                if (m.getName().equals(memberName)) { memberId = m.getId(); break; }
            }
        }

        String category = categoryFilter.getValue();
        if ("All Categories".equals(category)) category = null;

        String search = searchField.getText().trim();
        if (search.isEmpty()) search = null;

        List<Expense> expenses = DatabaseManager.getInstance().getExpenses(memberId, month, year, category, search);
        ObservableList<Expense> data = FXCollections.observableArrayList(expenses);
        expenseTable.setItems(data);

        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        totalLabel.setText("Total: " + CurrencyFormatter.format(total) + "  (" + expenses.size() + " entries)");
    }

    @FXML public void handleHome() { Main.navigateToDashboard(); }
    @FXML public void handleAddExpense() {
        try { Main.loadScene("/fxml/add_expense.fxml", "➕ Add Expense", 900, 680); }
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

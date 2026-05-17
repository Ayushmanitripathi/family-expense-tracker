package com.familyexpense.database;

import com.familyexpense.models.Budget;
import com.familyexpense.models.Expense;
import com.familyexpense.models.FamilyMember;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_PATH = "family_expense.db";

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            connection.setAutoCommit(true);
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String settingsTable = """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """;

        String membersTable = """
            CREATE TABLE IF NOT EXISTS family_members (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                emoji TEXT DEFAULT '👤',
                role TEXT DEFAULT '',
                active INTEGER DEFAULT 1,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String expensesTable = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                member_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                category_emoji TEXT DEFAULT '',
                date TEXT NOT NULL,
                note TEXT DEFAULT '',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (member_id) REFERENCES family_members(id)
            )
        """;

        String budgetsTable = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                monthly_budget REAL NOT NULL,
                total_income REAL DEFAULT 0,
                month INTEGER NOT NULL,
                year INTEGER NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(month, year)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(settingsTable);
            stmt.execute(membersTable);
            stmt.execute(expensesTable);
            stmt.execute(budgetsTable);
        }
    }

    // ==================== SETTINGS ====================

    public String getSetting(String key) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getFamilyName() {
        return getSetting("family_name");
    }

    public void setFamilyName(String name) {
        setSetting("family_name", name);
    }

    // ==================== FAMILY MEMBERS ====================

    public List<FamilyMember> getAllMembers() {
        List<FamilyMember> members = new ArrayList<>();
        String sql = "SELECT * FROM family_members WHERE active = 1 ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                FamilyMember member = new FamilyMember();
                member.setId(rs.getInt("id"));
                member.setName(rs.getString("name"));
                member.setEmoji(rs.getString("emoji"));
                member.setRole(rs.getString("role"));
                member.setActive(rs.getInt("active") == 1);
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public FamilyMember getMemberById(int id) {
        String sql = "SELECT * FROM family_members WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                FamilyMember member = new FamilyMember();
                member.setId(rs.getInt("id"));
                member.setName(rs.getString("name"));
                member.setEmoji(rs.getString("emoji"));
                member.setRole(rs.getString("role"));
                member.setActive(rs.getInt("active") == 1);
                return member;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int addMember(FamilyMember member) {
        String sql = "INSERT INTO family_members (name, emoji, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmoji() != null ? member.getEmoji() : "👤");
            pstmt.setString(3, member.getRole() != null ? member.getRole() : "");
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateMember(FamilyMember member) {
        String sql = "UPDATE family_members SET name = ?, emoji = ?, role = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmoji());
            pstmt.setString(3, member.getRole());
            pstmt.setInt(4, member.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMember(int memberId) {
        String sql = "UPDATE family_members SET active = 0 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== EXPENSES ====================

    public int addExpense(Expense expense) {
        String sql = "INSERT INTO expenses (member_id, amount, category, category_emoji, date, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, expense.getMemberId());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getCategory());
            pstmt.setString(4, expense.getCategoryEmoji() != null ? expense.getCategoryEmoji() : "");
            pstmt.setString(5, expense.getDate());
            pstmt.setString(6, expense.getNote() != null ? expense.getNote() : "");
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, expenseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Expense> getAllExpenses() {
        return getExpenses(null, null, null, null, null);
    }

    public List<Expense> getExpensesByMonth(int month, int year) {
        return getExpenses(null, month, year, null, null);
    }

    public List<Expense> getExpenses(Integer memberId, Integer month, Integer year, String category, String searchText) {
        List<Expense> expenses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT e.*, m.name as member_name, m.emoji as member_emoji
            FROM expenses e
            JOIN family_members m ON e.member_id = m.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (memberId != null) {
            sql.append(" AND e.member_id = ?");
            params.add(memberId);
        }
        if (month != null && year != null) {
            sql.append(" AND strftime('%m', e.date) = ? AND strftime('%Y', e.date) = ?");
            params.add(String.format("%02d", month));
            params.add(String.valueOf(year));
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND e.category = ?");
            params.add(category);
        }
        if (searchText != null && !searchText.isEmpty()) {
            sql.append(" AND (e.note LIKE ? OR m.name LIKE ?)");
            params.add("%" + searchText + "%");
            params.add("%" + searchText + "%");
        }
        sql.append(" ORDER BY e.date DESC, e.created_at DESC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Expense expense = new Expense();
                expense.setId(rs.getInt("id"));
                expense.setMemberId(rs.getInt("member_id"));
                expense.setMemberName(rs.getString("member_emoji") + " " + rs.getString("member_name"));
                expense.setAmount(rs.getDouble("amount"));
                expense.setCategory(rs.getString("category"));
                expense.setCategoryEmoji(rs.getString("category_emoji"));
                expense.setDate(rs.getString("date"));
                expense.setNote(rs.getString("note"));
                expenses.add(expense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    public double getTotalExpenseForMonth(int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM expenses " +
                     "WHERE strftime('%m', date) = ? AND strftime('%Y', date) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, String.format("%02d", month));
            pstmt.setString(2, String.valueOf(year));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Double> getCategoryTotalsForMonth(int month, int year) {
        Map<String, Double> totals = new LinkedHashMap<>();
        String sql = "SELECT category, category_emoji, SUM(amount) as total FROM expenses " +
                     "WHERE strftime('%m', date) = ? AND strftime('%Y', date) = ? " +
                     "GROUP BY category ORDER BY total DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, String.format("%02d", month));
            pstmt.setString(2, String.valueOf(year));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String cat = rs.getString("category_emoji") + " " + rs.getString("category");
                totals.put(cat, rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    public Map<String, Double> getMemberTotalsForMonth(int month, int year) {
        Map<String, Double> totals = new LinkedHashMap<>();
        String sql = "SELECT m.emoji || ' ' || m.name as member_name, COALESCE(SUM(e.amount), 0) as total " +
                     "FROM family_members m LEFT JOIN expenses e ON m.id = e.member_id " +
                     "AND strftime('%m', e.date) = ? AND strftime('%Y', e.date) = ? " +
                     "WHERE m.active = 1 GROUP BY m.id ORDER BY total DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, String.format("%02d", month));
            pstmt.setString(2, String.valueOf(year));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                totals.put(rs.getString("member_name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    public Map<String, Double> getMemberTotalsForMonthById(int month, int year) {
        Map<String, Double> totals = new LinkedHashMap<>();
        String sql = "SELECT m.id, COALESCE(SUM(e.amount), 0) as total " +
                     "FROM family_members m LEFT JOIN expenses e ON m.id = e.member_id " +
                     "AND strftime('%m', e.date) = ? AND strftime('%Y', e.date) = ? " +
                     "WHERE m.active = 1 GROUP BY m.id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, String.format("%02d", month));
            pstmt.setString(2, String.valueOf(year));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                totals.put(String.valueOf(rs.getInt("id")), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    // ==================== BUDGET ====================

    public Budget getBudgetForMonth(int month, int year) {
        String sql = "SELECT * FROM budgets WHERE month = ? AND year = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getInt("id"));
                budget.setMonthlyBudget(rs.getDouble("monthly_budget"));
                budget.setTotalIncome(rs.getDouble("total_income"));
                budget.setMonth(rs.getInt("month"));
                budget.setYear(rs.getInt("year"));
                return budget;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveBudget(Budget budget) {
        String sql = "INSERT OR REPLACE INTO budgets (monthly_budget, total_income, month, year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, budget.getMonthlyBudget());
            pstmt.setDouble(2, budget.getTotalIncome());
            pstmt.setInt(3, budget.getMonth());
            pstmt.setInt(4, budget.getYear());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== SAMPLE DATA ====================

    public void loadSampleData() {
        // Add sample family members
        FamilyMember papa = new FamilyMember("Papa", "👨", "Father");
        FamilyMember mummy = new FamilyMember("Mummy", "👩", "Mother");
        FamilyMember rahul = new FamilyMember("Rahul", "👦", "Son");
        FamilyMember priya = new FamilyMember("Priya", "👧", "Daughter");

        int papaId = addMember(papa);
        int mummyId = addMember(mummy);
        int rahulId = addMember(rahul);
        int priyaId = addMember(priya);

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Sample expenses for this month
        String d1 = today.withDayOfMonth(1).format(fmt);
        String d2 = today.withDayOfMonth(Math.min(3, today.getDayOfMonth())).format(fmt);
        String d5 = today.withDayOfMonth(Math.min(5, today.getDayOfMonth())).format(fmt);
        String d8 = today.withDayOfMonth(Math.min(8, today.getDayOfMonth())).format(fmt);
        String d10 = today.withDayOfMonth(Math.min(10, today.getDayOfMonth())).format(fmt);

        addExpense(new Expense(mummyId, 3500.0, "Grocery", d1, "Monthly grocery shopping"));
        addExpense(new Expense(papaId, 12000.0, "Rent", d1, "Monthly house rent"));
        addExpense(new Expense(papaId, 800.0, "Electricity", d2, "Electricity bill"));
        addExpense(new Expense(mummyId, 450.0, "Medicine", d5, "Blood pressure medicine"));
        addExpense(new Expense(rahulId, 1200.0, "School", d5, "School fees"));
        addExpense(new Expense(priyaId, 800.0, "School", d5, "Books and stationery"));
        addExpense(new Expense(papaId, 650.0, "Travel", d8, "Office commute"));
        addExpense(new Expense(mummyId, 850.0, "Food", d10, "Restaurant dinner"));
        addExpense(new Expense(rahulId, 300.0, "Mobile", d10, "Mobile recharge"));
        addExpense(new Expense(papaId, 500.0, "Other", today.format(fmt), "Miscellaneous household items"));

        // Set sample budget
        Budget budget = new Budget(35000.0, today.getMonthValue(), today.getYear());
        budget.setTotalIncome(50000.0);
        saveBudget(budget);

        // Mark sample data loaded
        setSetting("sample_data_loaded", "true");
    }

    public boolean isSampleDataLoaded() {
        String val = getSetting("sample_data_loaded");
        return "true".equals(val);
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

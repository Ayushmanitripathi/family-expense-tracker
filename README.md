# 🏠 Family Expense Tracker 

A modern, simple desktop application for tracking family household expenses. Built with **Java 17 + JavaFX + SQLite**.

> **Perfect for families** who want to track monthly spending without any technical knowledge.

---

## ✨ Features

| Feature | Description |
|---|---|
| 👨‍👩‍👧‍👦 **Family Members** | Add members (Papa, Mummy, Rahul, Priya...) with roles and emoji |
| ➕ **Add Expense** | Log who spent, how much, on what category, with a note |
| 🏠 **Dashboard** | See this month's total, budget status, member-wise spending, pie chart |
| 📊 **Monthly Report** | Income vs expense, savings, top category, highest spender |
| 📋 **Expense History** | Filter by month, member, category — delete wrong entries |
| 💰 **Budget Alerts** | Green / Yellow / Red based on how much of budget is used |
| 📄 **PDF Export** | Export monthly report as a PDF file |
| 💾 **Auto-save** | Everything saves automatically — no Save button needed |
| 🎯 **Sample Data** | Pre-loaded sample data on first launch so you can explore |

---

## 🛠️ Tech Stack

- **Java 17+** — core language
- **JavaFX 21** — modern desktop UI
- **SQLite** (`sqlite-jdbc`) — local database, no server needed
- **Apache PDFBox 3** — PDF export
- **Maven** — dependency and build management

---

## 📁 Project Structure

```
FamilyExpenseTracker/
├── src/main/java/com/familyexpense/
│   ├── Main.java                          # App entry point
│   ├── controllers/
│   │   ├── WelcomeController.java         # First launch setup
│   │   ├── DashboardController.java       # Home screen
│   │   ├── AddExpenseController.java      # Add new expense
│   │   ├── HistoryController.java         # Expense history & filters
│   │   ├── ReportController.java          # Monthly report + PDF
│   │   └── FamilySetupController.java     # Manage family members
│   ├── models/
│   │   ├── Expense.java
│   │   ├── FamilyMember.java
│   │   └── Budget.java
│   ├── database/
│   │   └── DatabaseManager.java           # SQLite operations
│   └── utils/
│       ├── PDFExporter.java               # PDF generation
│       ├── CurrencyFormatter.java         # ₹ formatting
│       └── CategoryHelper.java            # Category emojis
├── src/main/resources/
│   ├── fxml/                              # UI layout files
│   │   ├── welcome.fxml
│   │   ├── dashboard.fxml
│   │   ├── add_expense.fxml
│   │   ├── history.fxml
│   │   ├── report.fxml
│   │   └── family_setup.fxml
│   └── css/
│       └── styles.css                     # App stylesheet
├── pom.xml
└── README.md
```

---

## 🚀 Setup & Run

### Prerequisites

1. **Java 17 or higher** — [Download JDK](https://www.oracle.com/java/technologies/downloads/)
2. **Apache Maven** — [Download Maven](https://maven.apache.org/download.cgi)
   - Or use IntelliJ IDEA which has Maven built in

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Ayushmanitripathi/family-expense-tracker.git
cd family-expense-tracker
```

### Step 2 — Run the App

```bash
mvn javafx:run
```

That's it! Maven will automatically download all dependencies on first run.

### Step 3 — First Launch

1. Enter your family name (e.g. "Sharma", "Gupta")
2. Sample data will be loaded automatically so you can explore
3. Start tracking your expenses!

---

## 🖥️ Running from IntelliJ IDEA

1. Open IntelliJ IDEA → **File → Open** → select the project folder
2. Wait for Maven to import dependencies (bottom progress bar)
3. Open `src/main/java/com/familyexpense/Main.java`
4. Click the **▶ Run** button next to `main()`

---

## 💡 How to Use

### Adding an Expense
1. Click **"➕ Add Expense"** in the sidebar
2. Select **who spent** (family member)
3. Enter the **amount** in ₹
4. Select the **category** (Grocery, Rent, School, etc.)
5. The date is auto-filled as today — change if needed
6. Add a **note** (optional)
7. Click **"Save Expense!"** — it saves instantly!

### Setting a Budget
1. Go to **"📊 Monthly Report"**
2. Enter your **Monthly Budget** and **Monthly Income**
3. Click **Save**
4. The Dashboard will now show budget tracking with colors:
   - 🟢 **Green** — Under 80% spent
   - 🟡 **Yellow** — 80–99% spent (warning!)
   - 🔴 **Red** — Budget exceeded!

### Exporting PDF Report
1. Go to **"📊 Monthly Report"**
2. Select the month and year
3. Click **"📄 Export PDF"**
4. Choose where to save the file

---

## 📦 Building a Standalone JAR

```bash
mvn package
java -jar target/FamilyExpenseTracker-1.0.0-shaded.jar
```

> **Note:** JavaFX requires JDK 17+ with JavaFX modules, or the shaded JAR which includes all dependencies.

---

## 🗂️ Data Storage

- The app creates a **`family_expense.db`** SQLite file in the folder where you run it
- **No internet required** — all data stays on your computer
- To back up your data, just copy the `.db` file

---

## 📸 Screenshots

| Screen | Description |
|---|---|
| Welcome | Family name setup on first launch |
| Dashboard | Monthly total, budget bar, member cards, pie chart |
| Add Expense | Simple form with dropdowns |
| History | Filterable table with delete support |
| Report | Income/savings summary + PDF export |
| Family Setup | Add/remove family members |

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first.

---

## 📄 License

MIT License — free to use, modify, and distribute.

---

*Built with ❤️ for Indian families who want simple, local expense tracking.*

# FinTrack Pro - Personal Finance Dashboard & Expense Tracker (C++ OOP Edition)

A modern, responsive, and fully-featured **Personal Finance Dashboard and Expense Tracker** with a fintech-grade user interface, interactive financial visualizations, transaction management, budget performance tracking, and persistent storage, powered by a **Native C++ Object-Oriented Architecture**.

Designed for real-world personal wealth management and built to serve as an exemplary **Object-Oriented Programming Course Project (OOPs CP)**.

---

## 🌟 Key Highlights & Modern C++ Architecture

The project features a **Native C++ OOP Backend (`cpp-app/`)** serving the fintech-grade interactive Web Dashboard at `http://localhost:8080/`:

1. **Native C++ Object-Oriented Backend (`cpp-app/`)**:
   - Built using clean **OOP Principles (Encapsulation, DAO Pattern, Service Layer, Separation of Concerns)** in modern C++ (C++20).
   - High-performance multi-threaded native C++ HTTP server (`cpp-app/src/web/HttpServer.cpp`) with Winsock2.
   - Comprehensive domain entities (`Transaction`, `Category`, `Budget`, `DashboardSummary`, `User`).
   - Strong data access objects with thread-safe persistence (`CategoryDao`, `TransactionDao`, `BudgetDao`).
   - Business calculation engine with financial intelligence (`CalculationEngine`, `FinanceService`).
   - Complete 10/10 automated verification test suite (`cpp-app/src/test_main.cpp`).

2. **Fintech Web User Interface (`web/index.html`)**:
   - Modern Tailwind CSS & FontAwesome dashboard with Dark/Light theme switching.
   - Live monthly overview, interactive charts, and income vs expense visualizations.
   - Dynamic transaction search, filtering, and CRUD operations.
   - Budget tracking with progress meters and automated limit warnings.
   - Financial report generation and one-click CSV export.

---

## 📁 Repository Structure

```text
oops cp/
├── cpp-app/                          # Native C++ OOP Application
│   ├── include/                      # C++ Header Files
│   │   ├── model/                    # Domain Models (Transaction, Category, Budget, etc.)
│   │   ├── dao/                      # Data Access Objects (CategoryDao, TransactionDao, BudgetDao)
│   │   ├── service/                  # Business Logic & FinanceService
│   │   ├── util/                     # DateUtils, CurrencyFormatter, CsvExporter, Json
│   │   └── web/                      # Native Winsock HTTP Server
│   ├── src/                          # C++ Implementations
│   │   ├── dao/                      # DAO implementations
│   │   ├── service/                  # Service implementations
│   │   ├── web/                      # HTTP server implementation
│   │   ├── main.cpp                  # Server entry point
│   │   └── test_main.cpp             # 10/10 automated test suite
│   ├── bin/                          # Compiled binaries (fintrack_server.exe, fintrack_test.exe)
│   ├── build.bat                     # 1-Click C++ build script (g++)
│   ├── run.bat                       # 1-Click C++ server launcher
│   ├── run_tests.bat                 # 1-Click C++ test runner
│   └── CMakeLists.txt                # CMake build configuration
├── web/                              # Web Dashboard Frontend
│   └── index.html                    # Single-Page Interactive Web Dashboard
├── data_budgets.dat                  # Budget persistent store
├── data_categories.dat               # Category persistent store
├── data_transactions.dat             # Transaction persistent store
├── run_localhost.bat                 # 1-Click Localhost Server Launcher
├── run_tests.bat                     # 1-Click Automated Test Runner
└── README.md                         # Project documentation
```

---

## 🚀 Quick Start Guide (C++)

### 1. Run the Native C++ Localhost Server
To start the C++ server and launch the Web UI:
```cmd
run_localhost.bat
```
Or directly from `cpp-app/`:
```cmd
cd cpp-app
run.bat
```
Then open your web browser at: **`http://localhost:8080`**

### 2. Run the C++ Automated Verification Test Suite
```cmd
run_tests.bat
```
*Result: 10/10 automated tests pass with 100% precision.*

### 3. Build from Source (MinGW / g++)
```cmd
cd cpp-app
build.bat
```
│           ├── schema.sql
│           └── data.sql
├── frontend/                         # Modern React + Vite + TypeScript SPA
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── src/
│       ├── components/               # Sidebar, TopNav, MetricCard, Modals
│       ├── pages/                    # Dashboard, Transactions, Budgets, Reports, Settings
│       ├── services/api.ts           # REST API client with seamless offline fallback
│       └── types/                    # TypeScript interfaces
├── run_desktop_app.bat               # 1-Click Desktop App Launcher
├── IMPLEMENTATION_PLAN.md            # Architectural specification document
├── .env.example                      # Environment variables template
└── README.md                         # Project documentation
```

---

## 🚀 Quick Start Guide

### Option 1: Run the Native Java Desktop Application (Instant 1-Click)
The desktop application is pre-compiled and ready to run immediately using the native Java 26 JDK installed on your system.

Simply double-click:
```cmd
run_desktop_app.baat
```
Or execute from your terminal:
```cmd
cd desktop-app
run.bat
```

To run the automated verification test suite:
```cmd
& 'C:\Program Files\Java\jdk-26.0.2\bin\java.exe' -cp "desktop-app/bin" com.finance.tracker.test.FinancialVerificationTest
```
*Result: 10/10 automated tests pass with 100% calculation accuracy.*

---

### Option 2: Run the Spring Boot Backend

#### Prerequisites:
- Java JDK 17+ (Java 26 already installed)
- Maven 3.8+ (or your favorite IDE: VS Code, IntelliJ IDEA, Eclipse)
- (Optional) MySQL 8.0 (if omitted, Spring Boot defaults to embedded H2 in MySQL-compatible mode with zero setup)

#### Setup & Execution:
1. Ensure your database is running or use the default embedded mode:
   ```bash
   cd backend
   mvn clean spring-boot:run
   ```
2. The REST API will be available at: `http://localhost:8080/api`
3. H2 Console (if using embedded mode): `http://localhost:8080/h2-console`

---

### Option 3: Run the React Web Frontend

#### Prerequisites:
- Node.js 18+ & npm / pnpm

#### Setup & Execution:
1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Start the development server:
   ```bash
   npm run dev
   ```
3. Open your browser at: `http://localhost:3000`

---

## 📊 Core Features & Functional Specifications

### 1. Central Financial Dashboard
- **Total Balance**: Calculated dynamically (`Opening Baseline + Qualifying Inflows − Qualifying Outflows`).
- **Total Inflows (Income)**: Sum of all qualifying income transactions for the reporting period with previous-period comparison percentage.
- **Total Outflows (Expenses)**: Sum of all qualifying expense transactions with previous-period comparison percentage.
- **Net Savings**: Real-time savings delta (`Income − Expenses`) and calculated Savings Rate percentage (`(Savings / Income) × 100`).
- **Budget Performance Card**: Spending vs monthly budget ceiling with color-coded progress indicator.
- **Recent Transactions**: Instant snapshot of latest transactions with category badge, payment method, formatted date, and signed amount.

### 2. Transaction Management & Data Table
- **Columns**: Transaction ID, Description, Date, Category, Transaction Type (Income/Expense), Payment Method, Amount, Notes, and Actions.
- **CRUD Operations**:
  - Add Transaction modal with live validation.
  - Edit Transaction with pre-populated values.
  - Delete Transaction with confirmation dialog.
- **Multi-Criteria Filtering & Search**:
  - Live typing search (filters across description, category name, and notes simultaneously).
  - Transaction Type filter (`All`, `Income Only`, `Expense Only`).
  - Dynamic Category selector.
  - Date Range filter (`All Time`, `This Month`, `Last Month`, `Last 30 Days`).
  - Payment Method filter (`Bank Transfer`, `Credit Card`, `Debit Card`, `UPI`, `Net Banking`, `Cash`).
- **Column Sorting**: Click headers to sort by Date, Amount, Description, or Category in ascending/descending order.
- **Pagination**: Configurable page browsing with next/prev controls and page counter.
- **CSV Export**: Exports currently filtered dataset directly into RFC-compliant CSV files.

### 3. Add & Edit Transaction Modal
- Fields: Description, Amount, Type (Income/Expense), Category, Date, Payment Method, and Notes.
- Dynamic dependent dropdown: Changing type to Income automatically filters category options to Income categories (Salary, Freelance, Investments, etc.).
- Strict validation: Description cannot be empty, amount must be numeric and > 0, date must conform to `YYYY-MM-DD`, and valid category must be chosen.
- Triggers automatic recalculation across all balances, charts, and budget meters upon saving.

### 4. Interactive Financial Visualizations
- **Income vs. Expenses**: High-DPI dual-bar chart showing monthly inflows vs outflows with custom tooltips on hover.
- **Expense Category Breakdown**: Doughnut chart with distinct categorical palette, center total text, and interactive legend showing amounts and percentages.
- **Spending Trends & Savings**: Smooth multi-series trend curve depicting cashflow velocity.
- **Budget Progress Meters**: Category-by-category horizontal status bars.

### 5. Budget Management & Overspending Warnings
- Configure overall monthly spending caps and granular category limits.
- Real-time spending calculation based on expense transactions within the chosen month/year.
- Visual warning thresholds:
  - 🟢 **Normal**: < 80% of limit
  - 🟡 **Approaching Limit**: 80% – 99.9% of limit
  - 🔴 **Budget Exceeded**: ≥ 100% of limit
- Strict business rule: Income credits never offset expense budgets.

### 6. Category Management & Referential Integrity Protection
- Default Expense Categories: Food & Dining, Transportation, Shopping, Bills & Utilities, Healthcare, Education, Entertainment, Housing, Travel, Other Expense.
- Default Income Categories: Salary, Freelancing, Business, Investments, Gifts, Other Income.
- Custom categories can be created and renamed.
- **Referential Integrity**: Prevents accidental deletion of categories currently referenced by existing transactions.

### 7. Reports & Analytics
- Monthly cashflow breakdown tables.
- Category distribution summary.
- Export financial report to CSV.
- Direct printing layout (`JTable.print()` integration).

### 8. Localization & Personalization Settings
- Configurable Currency: Default Indian Rupee (₹), with support for USD ($), EUR (€), and GBP (£).
- Date format preferences: `yyyy-MM-dd`, `dd/MM/yyyy`, `MM/dd/yyyy`.
- Instant Theme Switcher: Light Theme and Dark Theme.
- Account opening baseline configuration.
- Toggle between MySQL and built-in persistent storage.

---

## 🗄️ Database Design (MySQL 8.0)

### Tables Schema Summary

| Table | Primary Key | Foreign Keys | Key Columns |
| :--- | :--- | :--- | :--- |
| `users` | `id` (BIGINT AUTO_INCREMENT) | None | `name`, `email` (UNIQUE), `password_hash`, `created_at` |
| `categories` | `id` (BIGINT AUTO_INCREMENT) | `user_id` -> `users.id` | `name`, `type` (`INCOME`/`EXPENSE`), `created_at` |
| `transactions` | `id` (BIGINT AUTO_INCREMENT) | `user_id` -> `users.id`<br>`category_id` -> `categories.id` | `description`, `amount` (`DECIMAL(12,2)`), `type`, `transaction_date`, `payment_method`, `notes` |
| `budgets` | `id` (BIGINT AUTO_INCREMENT) | `user_id` -> `users.id`<br>`category_id` -> `categories.id` (NULL = Overall) | `amount` (`DECIMAL(12,2)`), `month` (1-12), `year`, `created_at` |

### Database Scripts Included:
- `database/schema.mysql.sql`: Full DDL with constraints, checks, and performance indexes (`idx_trans_user_date`, `idx_trans_category`).
- `database/seed_data.mysql.sql`: 30 realistic transactions across multiple months (October, September, August 2026), 16 categories, and monthly budgets.

---

## 🔌 REST API Documentation

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/dashboard/summary?month=10&year=2026` | Retrieves authoritative balances, metrics, and recent transactions |
| `GET` | `/api/transactions` | Lists transactions with filters (`type`, `categoryId`, `search`, `paymentMethod`) |
| `GET` | `/api/transactions/{id}` | Retrieves a single transaction record by ID |
| `POST` | `/api/transactions` | Creates a new transaction record with validation |
| `PUT` | `/api/transactions/{id}` | Updates an existing transaction record |
| `DELETE` | `/api/transactions/{id}` | Deletes a transaction record |
| `GET` | `/api/categories` | Retrieves all categories (or filter by `?type=EXPENSE`) |
| `POST` | `/api/categories` | Creates a custom category |
| `PUT` | `/api/categories/{id}` | Renames or updates a category |
| `DELETE` | `/api/categories/{id}` | Deletes an unreferenced category |
| `GET` | `/api/budgets?month=10&year=2026` | Retrieves budgets with calculated spent and remaining amounts |
| `POST` | `/api/budgets` | Configures a new monthly budget limit |
| `PUT` | `/api/budgets/{id}` | Updates a budget limit |
| `DELETE` | `/api/budgets/{id}` | Deletes a budget limit |
| `GET` | `/api/analytics/monthly?months=6` | Returns multi-month cashflow trends for charts |
| `GET` | `/api/analytics/categories?month=10&year=2026`| Returns category spending breakdown |
| `GET` | `/api/reports/export-csv` | Streams financial records as downloadable CSV |

---

## 🎓 College OOP Project / Viva Evaluation Reference

If presenting this application for an **Object-Oriented Programming (OOP)** or **Software Engineering** evaluation, highlight the following concepts:

1. **Object-Oriented Principles Demonstrated**:
   - **Encapsulation**: Strict private fields, validated accessors/mutators, and domain logic encapsulated in `Budget.java` and `DashboardSummary.java`.
   - **Inheritance & Polymorphism**: Custom Swing components extending `JPanel`, `JLabel`, `JTextField`, and `JTable` cell renderers overriding `paintComponent` and `getTableCellRendererComponent`.
   - **Abstraction**: DAO interfaces and service contracts abstracting database mechanics.

2. **Software Design Patterns**:
   - **Model-View-Controller (MVC)**: Clean boundary separating domain models, UI panels, and DAO/service operations.
   - **Data Access Object (DAO) Pattern**: Dedicated DAOs for `Transaction`, `Category`, `Budget`, and `User`.
   - **Observer / Listener Pattern**: `ThemeManager` publishing theme changes to registered component listeners; Swing `ActionListener`, `DocumentListener`, and `TableModelListener` for event-driven UI updates.
   - **Strategy / Engine Pattern**: `CalculationEngine` providing pure mathematical financial logic using decimal-safe `BigDecimal`.

3. **Database Security & Reliability**:
   - Safe parameterized queries using JDBC `PreparedStatement` with `?` placeholders preventing SQL Injection.
   - Clean `ResultSet` traversal and mapping.
   - Decimal monetary precision avoiding binary floating-point rounding errors (`0.1 + 0.2 != 0.3`).
   - Referential integrity checks protecting referenced categories from orphan transaction creation.


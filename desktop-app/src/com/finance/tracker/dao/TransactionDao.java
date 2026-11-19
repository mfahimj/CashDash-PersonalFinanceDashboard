package com.finance.tracker.dao;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TransactionDao {
    private static final String DATA_FILE = "data_transactions.dat";
    private final Map<Long, Transaction> memoryStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(100);
    private final CategoryDao categoryDao;

    public TransactionDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
        if (!loadFromFile()) {
            seedDemoTransactions();
        }
    }

    private void seedDemoTransactions() {
        // October 2026 Transactions
        addInitialTx(1L, 1L, "Monthly Software Engineer Salary", new BigDecimal("85000.00"), TransactionType.INCOME, 11L, LocalDate.of(2026, 10, 1), "Bank Transfer", "Primary corporate monthly payroll credit");
        addInitialTx(2L, 1L, "UI/UX Design Contract for Fintech Client", new BigDecimal("24500.00"), TransactionType.INCOME, 12L, LocalDate.of(2026, 10, 5), "Bank Transfer", "Milestone 2 payment received");
        addInitialTx(3L, 1L, "Mutual Funds Dividend Payout", new BigDecimal("3800.00"), TransactionType.INCOME, 14L, LocalDate.of(2026, 10, 12), "Direct Deposit", "Q3 equity fund returns");
        addInitialTx(4L, 1L, "Apartment Monthly Rent", new BigDecimal("22000.00"), TransactionType.EXPENSE, 8L, LocalDate.of(2026, 10, 2), "Net Banking", "October residential lease payment");
        addInitialTx(5L, 1L, "Whole Foods & Supermarket Grocery Haul", new BigDecimal("4850.00"), TransactionType.EXPENSE, 1L, LocalDate.of(2026, 10, 3), "Credit Card", "Weekly organic produce, dairy, and pantry essentials");
        addInitialTx(6L, 1L, "Electricity & High-Speed Fiber Internet", new BigDecimal("3450.00"), TransactionType.EXPENSE, 4L, LocalDate.of(2026, 10, 6), "UPI", "Power bill and monthly 300 Mbps broadband");
        addInitialTx(7L, 1L, "Metro Commuter Pass & Fuel", new BigDecimal("2800.00"), TransactionType.EXPENSE, 2L, LocalDate.of(2026, 10, 8), "Debit Card", "Monthly transit card recharge and vehicle fuel");
        addInitialTx(8L, 1L, "Weekend Dinner with Team", new BigDecimal("3200.00"), TransactionType.EXPENSE, 1L, LocalDate.of(2026, 10, 10), "Credit Card", "Italian bistro dinner with team members");
        addInitialTx(9L, 1L, "Annual Health Checkup & Prescription", new BigDecimal("2400.00"), TransactionType.EXPENSE, 5L, LocalDate.of(2026, 10, 15), "UPI", "Routine diagnostics and vitamins");
        addInitialTx(10L, 1L, "Online Technical Books & Cloud Course", new BigDecimal("1850.00"), TransactionType.EXPENSE, 6L, LocalDate.of(2026, 10, 18), "Credit Card", "Advanced System Design textbook & lab subscription");
        addInitialTx(11L, 1L, "Streaming Subscriptions & Movie Tickets", new BigDecimal("1499.00"), TransactionType.EXPENSE, 7L, LocalDate.of(2026, 10, 20), "UPI", "Netflix, Spotify & weekend IMAX movie");
        addInitialTx(12L, 1L, "Autumn Apparel & Sneakers", new BigDecimal("5600.00"), TransactionType.EXPENSE, 3L, LocalDate.of(2026, 10, 22), "Credit Card", "Work casual clothing and footwear");
        addInitialTx(13L, 1L, "Artisan Coffee & Work Snacks", new BigDecimal("1250.00"), TransactionType.EXPENSE, 1L, LocalDate.of(2026, 10, 25), "Cash", "Local roastery coffee and cafe workday lunch");
        addInitialTx(14L, 1L, "Freelance Tech Consultation", new BigDecimal("15000.00"), TransactionType.INCOME, 12L, LocalDate.of(2026, 10, 26), "Bank Transfer", "Architecture review consultation session");
        addInitialTx(15L, 1L, "Weekend Road Trip Fuel & Tolls", new BigDecimal("3100.00"), TransactionType.EXPENSE, 9L, LocalDate.of(2026, 10, 27), "Fastag / UPI", "Highway toll fees and inter-city petrol");

        // September 2026 Historical Transactions
        addInitialTx(16L, 1L, "September Primary Salary", new BigDecimal("85000.00"), TransactionType.INCOME, 11L, LocalDate.of(2026, 9, 1), "Bank Transfer", "Corporate payroll");
        addInitialTx(17L, 1L, "Mobile App Dev Freelance Project", new BigDecimal("32000.00"), TransactionType.INCOME, 12L, LocalDate.of(2026, 9, 10), "Bank Transfer", "Final project handover payout");
        addInitialTx(18L, 1L, "Stock Market Capital Gains", new BigDecimal("7500.00"), TransactionType.INCOME, 14L, LocalDate.of(2026, 9, 22), "Direct Deposit", "Quarterly rebalancing profit");
        addInitialTx(19L, 1L, "Apartment Monthly Rent", new BigDecimal("22000.00"), TransactionType.EXPENSE, 8L, LocalDate.of(2026, 9, 2), "Net Banking", "September lease");
        addInitialTx(20L, 1L, "Monthly Groceries & Essentials", new BigDecimal("8900.00"), TransactionType.EXPENSE, 1L, LocalDate.of(2026, 9, 15), "Credit Card", "Hypermarket supply run");
        addInitialTx(21L, 1L, "Car Service & Regular Maintenance", new BigDecimal("6800.00"), TransactionType.EXPENSE, 2L, LocalDate.of(2026, 9, 18), "Debit Card", "Oil change, filter replacement, wheel alignment");
        addInitialTx(22L, 1L, "Utility Bills (Water, Gas, Electricity)", new BigDecimal("3850.00"), TransactionType.EXPENSE, 4L, LocalDate.of(2026, 9, 5), "UPI", "Monthly municipal utilities");
        addInitialTx(23L, 1L, "Birthday Celebration Dinner", new BigDecimal("4500.00"), TransactionType.EXPENSE, 1L, LocalDate.of(2026, 9, 20), "Credit Card", "Family celebratory dinner");
        addInitialTx(24L, 1L, "Festival Shopping & Gifts", new BigDecimal("7200.00"), TransactionType.EXPENSE, 3L, LocalDate.of(2026, 9, 25), "Credit Card", "Home decor and gifts for relatives");

        // August 2026 Historical Transactions
        addInitialTx(25L, 1L, "August Primary Salary", new BigDecimal("85000.00"), TransactionType.INCOME, 11L, LocalDate.of(2026, 8, 1), "Bank Transfer", "Corporate payroll");
        addInitialTx(26L, 1L, "Consulting Advisory Retainer", new BigDecimal("20000.00"), TransactionType.INCOME, 13L, LocalDate.of(2026, 8, 15), "Bank Transfer", "Monthly business advisory fee");
        addInitialTx(27L, 1L, "Apartment Monthly Rent", new BigDecimal("22000.00"), TransactionType.EXPENSE, 8L, LocalDate.of(2026, 8, 2), "Net Banking", "August lease");
        addInitialTx(28L, 1L, "Groceries & Dining", new BigDecimal("9400.00"), TransactionType.EXPENSE, 1L, LocalDate.of(2026, 8, 14), "Credit Card", "Supermarket and food delivery");
        addInitialTx(29L, 1L, "Flight Tickets & Hotel for Mini Vacation", new BigDecimal("18500.00"), TransactionType.EXPENSE, 9L, LocalDate.of(2026, 8, 20), "Credit Card", "Monsoon weekend hill station getaway");
        addInitialTx(30L, 1L, "Medical Health Insurance Premium", new BigDecimal("6500.00"), TransactionType.EXPENSE, 5L, LocalDate.of(2026, 8, 10), "Net Banking", "Annual family floater health insurance installment");

        saveToFile();
    }

    private void addInitialTx(Long id, Long userId, String desc, BigDecimal amt, TransactionType type,
                              Long catId, LocalDate date, String payMethod, String notes) {
        Transaction tx = new Transaction(id, userId, desc, amt, type, catId, date, payMethod, notes);
        categoryDao.findById(catId).ifPresent(c -> tx.setCategoryName(c.getName()));
        memoryStore.put(id, tx);
    }

    public List<Transaction> findAll() {
        if (DatabaseConnection.isMySqlActive()) {
            List<Transaction> list = new ArrayList<>();
            String sql = "SELECT t.id, t.user_id, t.description, t.amount, t.type, t.category_id, " +
                         "c.name AS category_name, t.transaction_date, t.payment_method, t.notes, " +
                         "t.created_at, t.updated_at " +
                         "FROM transactions t " +
                         "LEFT JOIN categories c ON t.category_id = c.id " +
                         "ORDER BY t.transaction_date DESC, t.id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("JDBC error in findAll transactions: " + e.getMessage());
            }
        }
        List<Transaction> list = new ArrayList<>(memoryStore.values());
        for (Transaction tx : list) {
            if (tx.getCategoryName() == null || tx.getCategoryName().isEmpty()) {
                categoryDao.findById(tx.getCategoryId()).ifPresent(c -> tx.setCategoryName(c.getName()));
            }
        }
        list.sort(Comparator.comparing(Transaction::getTransactionDate).reversed()
                .thenComparing(Comparator.comparing(Transaction::getId).reversed()));
        return list;
    }

    public Optional<Transaction> findById(Long id) {
        if (id == null) return Optional.empty();
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "SELECT t.id, t.user_id, t.description, t.amount, t.type, t.category_id, " +
                         "c.name AS category_name, t.transaction_date, t.payment_method, t.notes, " +
                         "t.created_at, t.updated_at " +
                         "FROM transactions t " +
                         "LEFT JOIN categories c ON t.category_id = c.id " +
                         "WHERE t.id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToTransaction(rs));
                    }
                }
            } catch (SQLException e) {
                System.err.println("JDBC error in findById: " + e.getMessage());
            }
        }
        Transaction tx = memoryStore.get(id);
        if (tx != null && (tx.getCategoryName() == null || tx.getCategoryName().isEmpty())) {
            categoryDao.findById(tx.getCategoryId()).ifPresent(c -> tx.setCategoryName(c.getName()));
        }
        return Optional.ofNullable(tx);
    }

    public List<Transaction> findRecent(int limit) {
        return findAll().stream().limit(limit).collect(Collectors.toList());
    }

    public List<Transaction> findByFilters(TransactionType type, Long categoryId,
                                          LocalDate startDate, LocalDate endDate,
                                          String paymentMethod, String keyword) {
        return findAll().stream().filter(tx -> {
            if (type != null && tx.getType() != type) return false;
            if (categoryId != null && categoryId > 0 && !Objects.equals(tx.getCategoryId(), categoryId)) return false;
            if (startDate != null && tx.getTransactionDate().isBefore(startDate)) return false;
            if (endDate != null && tx.getTransactionDate().isAfter(endDate)) return false;
            if (paymentMethod != null && !paymentMethod.equalsIgnoreCase("All") && !paymentMethod.trim().isEmpty()) {
                if (!tx.getPaymentMethod().equalsIgnoreCase(paymentMethod.trim())) return false;
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                boolean descMatch = tx.getDescription() != null && tx.getDescription().toLowerCase().contains(kw);
                boolean catMatch = tx.getCategoryName() != null && tx.getCategoryName().toLowerCase().contains(kw);
                boolean noteMatch = tx.getNotes() != null && tx.getNotes().toLowerCase().contains(kw);
                if (!descMatch && !catMatch && !noteMatch) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public Transaction save(Transaction tx) {
        if (tx == null) return null;
        if (tx.getId() == null || tx.getId() == 0L) {
            tx.setId(idSequence.incrementAndGet());
            tx.setCreatedAt(LocalDateTime.now());
        }
        tx.setUpdatedAt(LocalDateTime.now());
        if (tx.getUserId() == null) {
            tx.setUserId(1L);
        }

        categoryDao.findById(tx.getCategoryId()).ifPresent(c -> tx.setCategoryName(c.getName()));

        if (DatabaseConnection.isMySqlActive()) {
            String sql = "INSERT INTO transactions (user_id, description, amount, type, category_id, " +
                         "transaction_date, payment_method, notes, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, tx.getUserId());
                ps.setString(2, tx.getDescription());
                ps.setBigDecimal(3, tx.getAmount());
                ps.setString(4, tx.getType().name());
                ps.setLong(5, tx.getCategoryId());
                ps.setDate(6, java.sql.Date.valueOf(tx.getTransactionDate()));
                ps.setString(7, tx.getPaymentMethod());
                ps.setString(8, tx.getNotes());
                ps.setTimestamp(9, Timestamp.valueOf(tx.getCreatedAt() != null ? tx.getCreatedAt() : LocalDateTime.now()));
                ps.setTimestamp(10, Timestamp.valueOf(tx.getUpdatedAt()));
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        tx.setId(gk.getLong(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("JDBC error saving transaction: " + e.getMessage());
            }
        }

        memoryStore.put(tx.getId(), tx);
        saveToFile();
        return tx;
    }

    public boolean update(Transaction tx) {
        if (tx == null || tx.getId() == null) return false;
        tx.setUpdatedAt(LocalDateTime.now());
        categoryDao.findById(tx.getCategoryId()).ifPresent(c -> tx.setCategoryName(c.getName()));

        if (DatabaseConnection.isMySqlActive()) {
            String sql = "UPDATE transactions SET description = ?, amount = ?, type = ?, category_id = ?, " +
                         "transaction_date = ?, payment_method = ?, notes = ?, updated_at = ? " +
                         "WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tx.getDescription());
                ps.setBigDecimal(2, tx.getAmount());
                ps.setString(3, tx.getType().name());
                ps.setLong(4, tx.getCategoryId());
                ps.setDate(5, java.sql.Date.valueOf(tx.getTransactionDate()));
                ps.setString(6, tx.getPaymentMethod());
                ps.setString(7, tx.getNotes());
                ps.setTimestamp(8, Timestamp.valueOf(tx.getUpdatedAt()));
                ps.setLong(9, tx.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("JDBC error updating transaction: " + e.getMessage());
            }
        }

        if (memoryStore.containsKey(tx.getId())) {
            memoryStore.put(tx.getId(), tx);
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "DELETE FROM transactions WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    memoryStore.remove(id);
                    saveToFile();
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("JDBC error deleting transaction: " + e.getMessage());
                return false;
            }
        }
        Transaction removed = memoryStore.remove(id);
        if (removed != null) {
            saveToFile();
            return true;
        }
        return false;
    }

    public int countByCategoryId(Long categoryId) {
        if (categoryId == null) return 0;
        int count = 0;
        for (Transaction tx : memoryStore.values()) {
            if (Objects.equals(tx.getCategoryId(), categoryId)) {
                count++;
            }
        }
        return count;
    }

    public BigDecimal sumExpenseByCategoryAndPeriod(Long categoryId, int month, int year) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction tx : memoryStore.values()) {
            if (tx.getType() == TransactionType.EXPENSE &&
                Objects.equals(tx.getCategoryId(), categoryId) &&
                tx.getTransactionDate().getMonthValue() == month &&
                tx.getTransactionDate().getYear() == year) {
                sum = sum.add(tx.getAmount());
            }
        }
        return sum;
    }

    public BigDecimal sumExpenseByPeriod(int month, int year) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction tx : memoryStore.values()) {
            if (tx.getType() == TransactionType.EXPENSE &&
                tx.getTransactionDate().getMonthValue() == month &&
                tx.getTransactionDate().getYear() == year) {
                sum = sum.add(tx.getAmount());
            }
        }
        return sum;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getLong("id"));
        tx.setUserId(rs.getLong("user_id"));
        tx.setDescription(rs.getString("description"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setType(TransactionType.fromString(rs.getString("type")));
        tx.setCategoryId(rs.getLong("category_id"));
        tx.setCategoryName(rs.getString("category_name"));
        java.sql.Date d = rs.getDate("transaction_date");
        if (d != null) {
            tx.setTransactionDate(d.toLocalDate());
        }
        tx.setPaymentMethod(rs.getString("payment_method"));
        tx.setNotes(rs.getString("notes"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) tx.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) tx.setUpdatedAt(ut.toLocalDateTime());
        return tx;
    }

    private synchronized void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Transaction tx : memoryStore.values()) {
                writer.println(tx.getId() + "||" +
                               tx.getUserId() + "||" +
                               tx.getDescription() + "||" +
                               tx.getAmount().toPlainString() + "||" +
                               tx.getType().name() + "||" +
                               tx.getCategoryId() + "||" +
                               (tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : LocalDate.now().toString()) + "||" +
                               (tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "Other") + "||" +
                               (tx.getNotes() != null ? tx.getNotes().replace("\n", " ") : "") + "||" +
                               (tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : LocalDateTime.now().toString()) + "||" +
                               (tx.getUpdatedAt() != null ? tx.getUpdatedAt().toString() : LocalDateTime.now().toString()));
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions to file: " + e.getMessage());
        }
    }

    private synchronized boolean loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            long maxId = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\|");
                if (parts.length >= 8) {
                    Long id = Long.parseLong(parts[0]);
                    Long userId = Long.parseLong(parts[1]);
                    String desc = parts[2];
                    BigDecimal amount = new BigDecimal(parts[3]);
                    TransactionType type = TransactionType.fromString(parts[4]);
                    Long catId = Long.parseLong(parts[5]);
                    LocalDate date = LocalDate.parse(parts[6]);
                    String method = parts[7];
                    String notes = parts.length >= 9 ? parts[8] : "";
                    Transaction tx = new Transaction(id, userId, desc, amount, type, catId, date, method, notes);
                    if (parts.length >= 10) {
                        try { tx.setCreatedAt(LocalDateTime.parse(parts[9])); } catch (Exception ignored) {}
                    }
                    if (parts.length >= 11) {
                        try { tx.setUpdatedAt(LocalDateTime.parse(parts[10])); } catch (Exception ignored) {}
                    }
                    categoryDao.findById(catId).ifPresent(c -> tx.setCategoryName(c.getName()));
                    memoryStore.put(id, tx);
                    if (id > maxId) maxId = id;
                }
            }
            if (maxId > idSequence.get()) {
                idSequence.set(maxId);
            }
            return !memoryStore.isEmpty();
        } catch (Exception e) {
            System.err.println("Error loading transactions from file: " + e.getMessage());
            return false;
        }
    }
}


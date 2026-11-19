package com.finance.tracker.dao;

import com.finance.tracker.model.Budget;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BudgetDao {
    private static final String DATA_FILE = "data_budgets.dat";
    private final Map<Long, Budget> memoryStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(100);
    private final CategoryDao categoryDao;

    public BudgetDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
        if (!loadFromFile()) {
            seedDefaultBudgets();
        }
    }

    private void seedDefaultBudgets() {
        // October 2026 budgets
        addInitialBudget(1L, 1L, null, new BigDecimal("65000.00"), 10, 2026); // Overall monthly spending limit
        addInitialBudget(2L, 1L, 1L, new BigDecimal("12000.00"), 10, 2026);  // Food and Dining
        addInitialBudget(3L, 1L, 2L, new BigDecimal("7000.00"), 10, 2026);   // Transportation
        addInitialBudget(4L, 1L, 3L, new BigDecimal("8000.00"), 10, 2026);   // Shopping
        addInitialBudget(5L, 1L, 4L, new BigDecimal("5000.00"), 10, 2026);   // Bills and Utilities
        addInitialBudget(6L, 1L, 5L, new BigDecimal("4000.00"), 10, 2026);   // Healthcare
        addInitialBudget(7L, 1L, 6L, new BigDecimal("3000.00"), 10, 2026);   // Education
        addInitialBudget(8L, 1L, 7L, new BigDecimal("3500.00"), 10, 2026);   // Entertainment
        addInitialBudget(9L, 1L, 8L, new BigDecimal("22000.00"), 10, 2026);  // Housing
        saveToFile();
    }

    private void addInitialBudget(Long id, Long userId, Long categoryId, BigDecimal amount, int month, int year) {
        Budget b = new Budget(id, userId, categoryId, amount, month, year);
        if (categoryId != null) {
            categoryDao.findById(categoryId).ifPresent(c -> b.setCategoryName(c.getName()));
        } else {
            b.setCategoryName("Overall Monthly Budget");
        }
        memoryStore.put(id, b);
    }

    public List<Budget> findByPeriod(int month, int year) {
        List<Budget> list = new ArrayList<>();
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "SELECT b.id, b.user_id, b.category_id, b.amount, b.month, b.year, " +
                         "c.name AS category_name, b.created_at, b.updated_at " +
                         "FROM budgets b " +
                         "LEFT JOIN categories c ON b.category_id = c.id " +
                         "WHERE b.month = ? AND b.year = ? " +
                         "ORDER BY (b.category_id IS NOT NULL), b.amount DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToBudget(rs));
                    }
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("JDBC error in findByPeriod budgets: " + e.getMessage());
            }
        }
        for (Budget b : memoryStore.values()) {
            if (b.getMonth() == month && b.getYear() == year) {
                if (b.getCategoryId() != null) {
                    categoryDao.findById(b.getCategoryId()).ifPresent(c -> b.setCategoryName(c.getName()));
                } else {
                    b.setCategoryName("Overall Monthly Budget");
                }
                list.add(b);
            }
        }
        list.sort((a, b) -> {
            if (a.isOverall() && !b.isOverall()) return -1;
            if (!a.isOverall() && b.isOverall()) return 1;
            return b.getAmount().compareTo(a.getAmount());
        });
        return list;
    }

    public Optional<Budget> findOverall(int month, int year) {
        for (Budget b : findByPeriod(month, year)) {
            if (b.isOverall()) return Optional.of(b);
        }
        return Optional.empty();
    }

    public Optional<Budget> findById(Long id) {
        if (id == null) return Optional.empty();
        Budget b = memoryStore.get(id);
        if (b != null) {
            if (b.getCategoryId() != null) {
                categoryDao.findById(b.getCategoryId()).ifPresent(c -> b.setCategoryName(c.getName()));
            } else {
                b.setCategoryName("Overall Monthly Budget");
            }
        }
        return Optional.ofNullable(b);
    }

    public Budget save(Budget budget) {
        if (budget == null) return null;
        if (budget.getId() == null || budget.getId() == 0L) {
            budget.setId(idSequence.incrementAndGet());
            budget.setCreatedAt(LocalDateTime.now());
        }
        budget.setUpdatedAt(LocalDateTime.now());
        if (budget.getUserId() == null) budget.setUserId(1L);

        if (budget.getCategoryId() != null && budget.getCategoryId() > 0) {
            categoryDao.findById(budget.getCategoryId()).ifPresent(c -> budget.setCategoryName(c.getName()));
        } else {
            budget.setCategoryId(null);
            budget.setCategoryName("Overall Monthly Budget");
        }

        if (DatabaseConnection.isMySqlActive()) {
            String sql = "INSERT INTO budgets (user_id, category_id, amount, month, year, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE amount = VALUES(amount), updated_at = VALUES(updated_at)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, budget.getUserId());
                if (budget.getCategoryId() != null) {
                    ps.setLong(2, budget.getCategoryId());
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setBigDecimal(3, budget.getAmount());
                ps.setInt(4, budget.getMonth());
                ps.setInt(5, budget.getYear());
                ps.setTimestamp(6, Timestamp.valueOf(budget.getCreatedAt() != null ? budget.getCreatedAt() : LocalDateTime.now()));
                ps.setTimestamp(7, Timestamp.valueOf(budget.getUpdatedAt()));
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) budget.setId(gk.getLong(1));
                }
            } catch (SQLException e) {
                System.err.println("JDBC error saving budget: " + e.getMessage());
            }
        }

        memoryStore.put(budget.getId(), budget);
        saveToFile();
        return budget;
    }

    public boolean update(Budget budget) {
        if (budget == null || budget.getId() == null) return false;
        budget.setUpdatedAt(LocalDateTime.now());
        if (budget.getCategoryId() != null && budget.getCategoryId() > 0) {
            categoryDao.findById(budget.getCategoryId()).ifPresent(c -> budget.setCategoryName(c.getName()));
        } else {
            budget.setCategoryId(null);
            budget.setCategoryName("Overall Monthly Budget");
        }

        if (DatabaseConnection.isMySqlActive()) {
            String sql = "UPDATE budgets SET category_id = ?, amount = ?, month = ?, year = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (budget.getCategoryId() != null) {
                    ps.setLong(1, budget.getCategoryId());
                } else {
                    ps.setNull(1, Types.BIGINT);
                }
                ps.setBigDecimal(2, budget.getAmount());
                ps.setInt(3, budget.getMonth());
                ps.setInt(4, budget.getYear());
                ps.setTimestamp(5, Timestamp.valueOf(budget.getUpdatedAt()));
                ps.setLong(6, budget.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("JDBC error updating budget: " + e.getMessage());
            }
        }

        if (memoryStore.containsKey(budget.getId())) {
            memoryStore.put(budget.getId(), budget);
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "DELETE FROM budgets WHERE id = ?";
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
                System.err.println("JDBC error deleting budget: " + e.getMessage());
                return false;
            }
        }
        Budget removed = memoryStore.remove(id);
        if (removed != null) {
            saveToFile();
            return true;
        }
        return false;
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        long catId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            b.setCategoryId(catId);
        }
        b.setCategoryName(rs.getString("category_name"));
        if (b.getCategoryName() == null && b.isOverall()) {
            b.setCategoryName("Overall Monthly Budget");
        }
        b.setAmount(rs.getBigDecimal("amount"));
        b.setMonth(rs.getInt("month"));
        b.setYear(rs.getInt("year"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) b.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) b.setUpdatedAt(ut.toLocalDateTime());
        return b;
    }

    private synchronized void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Budget b : memoryStore.values()) {
                writer.println(b.getId() + "||" +
                               b.getUserId() + "||" +
                               (b.getCategoryId() != null ? b.getCategoryId() : "0") + "||" +
                               b.getAmount().toPlainString() + "||" +
                               b.getMonth() + "||" +
                               b.getYear() + "||" +
                               (b.getCreatedAt() != null ? b.getCreatedAt().toString() : LocalDateTime.now().toString()) + "||" +
                               (b.getUpdatedAt() != null ? b.getUpdatedAt().toString() : LocalDateTime.now().toString()));
            }
        } catch (IOException e) {
            System.err.println("Error saving budgets to file: " + e.getMessage());
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
                if (parts.length >= 6) {
                    Long id = Long.parseLong(parts[0]);
                    Long userId = Long.parseLong(parts[1]);
                    long rawCatId = Long.parseLong(parts[2]);
                    Long catId = rawCatId > 0 ? rawCatId : null;
                    BigDecimal amount = new BigDecimal(parts[3]);
                    int month = Integer.parseInt(parts[4]);
                    int year = Integer.parseInt(parts[5]);
                    Budget b = new Budget(id, userId, catId, amount, month, year);
                    if (parts.length >= 7) {
                        try { b.setCreatedAt(LocalDateTime.parse(parts[6])); } catch (Exception ignored) {}
                    }
                    if (parts.length >= 8) {
                        try { b.setUpdatedAt(LocalDateTime.parse(parts[7])); } catch (Exception ignored) {}
                    }
                    if (catId != null) {
                        categoryDao.findById(catId).ifPresent(c -> b.setCategoryName(c.getName()));
                    } else {
                        b.setCategoryName("Overall Monthly Budget");
                    }
                    memoryStore.put(id, b);
                    if (id > maxId) maxId = id;
                }
            }
            if (maxId > idSequence.get()) {
                idSequence.set(maxId);
            }
            return !memoryStore.isEmpty();
        } catch (Exception e) {
            System.err.println("Error loading budgets from file: " + e.getMessage());
            return false;
        }
    }
}


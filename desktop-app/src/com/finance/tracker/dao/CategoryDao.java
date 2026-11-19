package com.finance.tracker.dao;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.TransactionType;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CategoryDao {
    private static final String DATA_FILE = "data_categories.dat";
    private final Map<Long, Category> memoryStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(100);

    public CategoryDao() {
        if (!loadFromFile()) {
            seedDefaultCategories();
        }
    }

    private void seedDefaultCategories() {
        addInitialCategory(1L, 1L, "Food and Dining", TransactionType.EXPENSE);
        addInitialCategory(2L, 1L, "Transportation", TransactionType.EXPENSE);
        addInitialCategory(3L, 1L, "Shopping", TransactionType.EXPENSE);
        addInitialCategory(4L, 1L, "Bills and Utilities", TransactionType.EXPENSE);
        addInitialCategory(5L, 1L, "Healthcare", TransactionType.EXPENSE);
        addInitialCategory(6L, 1L, "Education", TransactionType.EXPENSE);
        addInitialCategory(7L, 1L, "Entertainment", TransactionType.EXPENSE);
        addInitialCategory(8L, 1L, "Housing", TransactionType.EXPENSE);
        addInitialCategory(9L, 1L, "Travel", TransactionType.EXPENSE);
        addInitialCategory(10L, 1L, "Other Expense", TransactionType.EXPENSE);

        addInitialCategory(11L, 1L, "Salary", TransactionType.INCOME);
        addInitialCategory(12L, 1L, "Freelancing", TransactionType.INCOME);
        addInitialCategory(13L, 1L, "Business", TransactionType.INCOME);
        addInitialCategory(14L, 1L, "Investments", TransactionType.INCOME);
        addInitialCategory(15L, 1L, "Gifts", TransactionType.INCOME);
        addInitialCategory(16L, 1L, "Other Income", TransactionType.INCOME);

        saveToFile();
    }

    private void addInitialCategory(Long id, Long userId, String name, TransactionType type) {
        Category c = new Category(id, userId, name, type);
        c.setCreatedAt(LocalDateTime.now());
        memoryStore.put(id, c);
    }

    public List<Category> findAll() {
        if (DatabaseConnection.isMySqlActive()) {
            List<Category> list = new ArrayList<>();
            String sql = "SELECT id, user_id, name, type, created_at FROM categories ORDER BY name ASC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCategory(rs));
                }
                return list;
            } catch (SQLException e) {
                System.err.println("JDBC error in findAll categories, fallback to store: " + e.getMessage());
            }
        }
        List<Category> list = new ArrayList<>(memoryStore.values());
        list.sort(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<Category> findByType(TransactionType type) {
        List<Category> all = findAll();
        List<Category> filtered = new ArrayList<>();
        for (Category c : all) {
            if (c.getType() == type) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    public Optional<Category> findById(Long id) {
        if (id == null) return Optional.empty();
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "SELECT id, user_id, name, type, created_at FROM categories WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCategory(rs));
                    }
                }
            } catch (SQLException e) {
                System.err.println("JDBC error in findById: " + e.getMessage());
            }
        }
        return Optional.ofNullable(memoryStore.get(id));
    }

    public Optional<Category> findByNameAndType(String name, TransactionType type) {
        for (Category c : findAll()) {
            if (c.getName().equalsIgnoreCase(name) && c.getType() == type) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public Category save(Category category) {
        if (category == null) return null;
        if (category.getId() == null || category.getId() == 0L) {
            category.setId(idSequence.incrementAndGet());
            category.setCreatedAt(LocalDateTime.now());
        }
        if (category.getUserId() == null) {
            category.setUserId(1L);
        }

        if (DatabaseConnection.isMySqlActive()) {
            String sql = "INSERT INTO categories (user_id, name, type, created_at) VALUES (?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE name = VALUES(name), type = VALUES(type)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, category.getUserId());
                ps.setString(2, category.getName());
                ps.setString(3, category.getType().name());
                ps.setTimestamp(4, Timestamp.valueOf(category.getCreatedAt() != null ? category.getCreatedAt() : LocalDateTime.now()));
                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("JDBC error saving category: " + e.getMessage());
            }
        }

        memoryStore.put(category.getId(), category);
        saveToFile();
        return category;
    }

    public boolean update(Category category) {
        if (category == null || category.getId() == null) return false;
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, category.getName());
                ps.setString(2, category.getType().name());
                ps.setLong(3, category.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("JDBC error updating category: " + e.getMessage());
            }
        }
        if (memoryStore.containsKey(category.getId())) {
            memoryStore.put(category.getId(), category);
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        if (DatabaseConnection.isMySqlActive()) {
            String sql = "DELETE FROM categories WHERE id = ?";
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
                System.err.println("JDBC error deleting category: " + e.getMessage());
                return false;
            }
        }
        Category removed = memoryStore.remove(id);
        if (removed != null) {
            saveToFile();
            return true;
        }
        return false;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setName(rs.getString("name"));
        c.setType(TransactionType.fromString(rs.getString("type")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }

    private synchronized void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Category c : memoryStore.values()) {
                writer.println(c.getId() + "||" +
                               c.getUserId() + "||" +
                               c.getName() + "||" +
                               c.getType().name() + "||" +
                               (c.getCreatedAt() != null ? c.getCreatedAt().toString() : LocalDateTime.now().toString()));
            }
        } catch (IOException e) {
            System.err.println("Error saving categories to file: " + e.getMessage());
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
                if (parts.length >= 4) {
                    Long id = Long.parseLong(parts[0]);
                    Long userId = Long.parseLong(parts[1]);
                    String name = parts[2];
                    TransactionType type = TransactionType.fromString(parts[3]);
                    Category c = new Category(id, userId, name, type);
                    if (parts.length >= 5) {
                        try {
                            c.setCreatedAt(LocalDateTime.parse(parts[4]));
                        } catch (Exception ignored) {}
                    }
                    memoryStore.put(id, c);
                    if (id > maxId) maxId = id;
                }
            }
            if (maxId > idSequence.get()) {
                idSequence.set(maxId);
            }
            return !memoryStore.isEmpty();
        } catch (Exception e) {
            System.err.println("Error loading categories from file: " + e.getMessage());
            return false;
        }
    }
}


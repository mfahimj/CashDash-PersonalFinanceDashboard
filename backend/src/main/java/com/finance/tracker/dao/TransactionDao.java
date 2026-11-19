package com.finance.tracker.dao;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionDao {
    private final JdbcTemplate jdbcTemplate;

    public TransactionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
        Transaction tx = new Transaction();
        tx.setId(rs.getLong("id"));
        tx.setUserId(rs.getLong("user_id"));
        tx.setDescription(rs.getString("description"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setType(TransactionType.fromString(rs.getString("type")));
        tx.setCategoryId(rs.getLong("category_id"));
        try {
            tx.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {}
        Date d = rs.getDate("transaction_date");
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
    };

    public List<Transaction> findAllWithFilters(TransactionType type, Long categoryId,
                                                LocalDate startDate, LocalDate endDate,
                                                String paymentMethod, String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.user_id, t.description, t.amount, t.type, t.category_id, " +
            "c.name AS category_name, t.transaction_date, t.payment_method, t.notes, " +
            "t.created_at, t.updated_at " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append("AND t.type = ? ");
            params.add(type.name());
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND t.category_id = ? ");
            params.add(categoryId);
        }
        if (startDate != null) {
            sql.append("AND t.transaction_date >= ? ");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append("AND t.transaction_date <= ? ");
            params.add(Date.valueOf(endDate));
        }
        if (paymentMethod != null && !paymentMethod.equalsIgnoreCase("All") && !paymentMethod.trim().isEmpty()) {
            sql.append("AND LOWER(t.payment_method) = ? ");
            params.add(paymentMethod.trim().toLowerCase());
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(t.description) LIKE ? OR LOWER(c.name) LIKE ? OR LOWER(t.notes) LIKE ?) ");
            String wild = "%" + search.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }

        sql.append("ORDER BY t.transaction_date DESC, t.id DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Transaction> findRecent(int limit) {
        String sql = "SELECT t.id, t.user_id, t.description, t.amount, t.type, t.category_id, " +
                     "c.name AS category_name, t.transaction_date, t.payment_method, t.notes, " +
                     "t.created_at, t.updated_at " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "ORDER BY t.transaction_date DESC, t.id DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT t.id, t.user_id, t.description, t.amount, t.type, t.category_id, " +
                     "c.name AS category_name, t.transaction_date, t.payment_method, t.notes, " +
                     "t.created_at, t.updated_at " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.id = ?";
        List<Transaction> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Transaction save(Transaction tx) {
        String sql = "INSERT INTO transactions (user_id, description, amount, type, category_id, " +
                     "transaction_date, payment_method, notes, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tx.getUserId() != null ? tx.getUserId() : 1L);
            ps.setString(2, tx.getDescription());
            ps.setBigDecimal(3, tx.getAmount());
            ps.setString(4, tx.getType().name());
            ps.setLong(5, tx.getCategoryId());
            ps.setDate(6, Date.valueOf(tx.getTransactionDate()));
            ps.setString(7, tx.getPaymentMethod());
            ps.setString(8, tx.getNotes());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(9, tx.getCreatedAt() != null ? Timestamp.valueOf(tx.getCreatedAt()) : now);
            ps.setTimestamp(10, now);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            tx.setId(keyHolder.getKey().longValue());
        }
        return tx;
    }

    public boolean update(Transaction tx) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, type = ?, category_id = ?, " +
                     "transaction_date = ?, payment_method = ?, notes = ?, updated_at = ? " +
                     "WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                tx.getDescription(),
                tx.getAmount(),
                tx.getType().name(),
                tx.getCategoryId(),
                Date.valueOf(tx.getTransactionDate()),
                tx.getPaymentMethod(),
                tx.getNotes(),
                new Timestamp(System.currentTimeMillis()),
                tx.getId());
        return rows > 0;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public int countByCategoryId(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE category_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryId);
        return count != null ? count : 0;
    }

    public BigDecimal sumExpenseByPeriod(int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                     "WHERE type = 'EXPENSE' AND EXTRACT(MONTH FROM transaction_date) = ? AND EXTRACT(YEAR FROM transaction_date) = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, month, year);
    }

    public BigDecimal sumExpenseByCategoryAndPeriod(Long categoryId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                     "WHERE type = 'EXPENSE' AND category_id = ? AND EXTRACT(MONTH FROM transaction_date) = ? AND EXTRACT(YEAR FROM transaction_date) = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, categoryId, month, year);
    }
}


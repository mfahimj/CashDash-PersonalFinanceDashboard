package com.finance.tracker.dao;

import com.finance.tracker.model.Budget;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class BudgetDao {
    private final JdbcTemplate jdbcTemplate;

    public BudgetDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Budget> rowMapper = (rs, rowNum) -> {
        Budget b = new Budget();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        long catId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            b.setCategoryId(catId);
        }
        try {
            b.setCategoryName(rs.getString("category_name"));
        } catch (Exception ignored) {}
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
    };

    public List<Budget> findByPeriod(int month, int year) {
        String sql = "SELECT b.id, b.user_id, b.category_id, b.amount, b.month, b.year, " +
                     "c.name AS category_name, b.created_at, b.updated_at " +
                     "FROM budgets b " +
                     "LEFT JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.month = ? AND b.year = ? " +
                     "ORDER BY (b.category_id IS NOT NULL), b.amount DESC";
        return jdbcTemplate.query(sql, rowMapper, month, year);
    }

    public Optional<Budget> findOverall(int month, int year) {
        String sql = "SELECT b.id, b.user_id, b.category_id, b.amount, b.month, b.year, " +
                     "'Overall Monthly Budget' AS category_name, b.created_at, b.updated_at " +
                     "FROM budgets b " +
                     "WHERE b.category_id IS NULL AND b.month = ? AND b.year = ?";
        List<Budget> list = jdbcTemplate.query(sql, rowMapper, month, year);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Budget> findById(Long id) {
        String sql = "SELECT b.id, b.user_id, b.category_id, b.amount, b.month, b.year, " +
                     "c.name AS category_name, b.created_at, b.updated_at " +
                     "FROM budgets b " +
                     "LEFT JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.id = ?";
        List<Budget> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Budget save(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category_id, amount, month, year, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, budget.getUserId() != null ? budget.getUserId() : 1L);
            if (budget.getCategoryId() != null) {
                ps.setLong(2, budget.getCategoryId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setBigDecimal(3, budget.getAmount());
            ps.setInt(4, budget.getMonth());
            ps.setInt(5, budget.getYear());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(6, budget.getCreatedAt() != null ? Timestamp.valueOf(budget.getCreatedAt()) : now);
            ps.setTimestamp(7, now);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            budget.setId(keyHolder.getKey().longValue());
        }
        return budget;
    }

    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET category_id = ?, amount = ?, month = ?, year = ?, updated_at = ? WHERE id = ?";
        Long catId = budget.getCategoryId();
        int rows = jdbcTemplate.update(sql,
                catId != null ? catId : null,
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear(),
                new Timestamp(System.currentTimeMillis()),
                budget.getId());
        return rows > 0;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}


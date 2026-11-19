package com.finance.tracker.dao;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryDao {
    private final JdbcTemplate jdbcTemplate;

    public CategoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> rowMapper = (rs, rowNum) -> {
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
    };

    public List<Category> findAll() {
        String sql = "SELECT id, user_id, name, type, created_at FROM categories ORDER BY name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Category> findByType(TransactionType type) {
        String sql = "SELECT id, user_id, name, type, created_at FROM categories WHERE type = ? ORDER BY name ASC";
        return jdbcTemplate.query(sql, rowMapper, type.name());
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT id, user_id, name, type, created_at FROM categories WHERE id = ?";
        List<Category> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Category save(Category category) {
        String sql = "INSERT INTO categories (user_id, name, type, created_at) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, category.getUserId() != null ? category.getUserId() : 1L);
            ps.setString(2, category.getName());
            ps.setString(3, category.getType().name());
            ps.setTimestamp(4, Timestamp.valueOf(category.getCreatedAt() != null ? category.getCreatedAt() : java.time.LocalDateTime.now()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            category.setId(keyHolder.getKey().longValue());
        }
        return category;
    }

    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";
        return jdbcTemplate.update(sql, category.getName(), category.getType().name(), category.getId()) > 0;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}


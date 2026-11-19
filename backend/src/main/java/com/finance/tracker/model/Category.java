package com.finance.tracker.model;

import java.time.LocalDateTime;

public class Category {
    private Long id;
    private Long userId;
    private String name;
    private TransactionType type;
    private LocalDateTime createdAt;

    public Category() {}

    public Category(Long id, Long userId, String name, TransactionType type) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


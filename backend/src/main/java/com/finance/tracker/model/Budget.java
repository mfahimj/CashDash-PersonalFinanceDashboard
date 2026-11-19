package com.finance.tracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Budget {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount = BigDecimal.ZERO;
    private int month;
    private int year;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BigDecimal spent = BigDecimal.ZERO;
    private BigDecimal remaining = BigDecimal.ZERO;
    private double percentage = 0.0;

    public Budget() {}

    public void updateCalculations(BigDecimal actualSpent) {
        this.spent = actualSpent != null ? actualSpent : BigDecimal.ZERO;
        this.remaining = this.amount.subtract(this.spent);
        if (this.amount.compareTo(BigDecimal.ZERO) > 0) {
            this.percentage = this.spent.divide(this.amount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else {
            this.percentage = 0.0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }

    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public boolean isOverall() {
        return categoryId == null || categoryId == 0L;
    }
}


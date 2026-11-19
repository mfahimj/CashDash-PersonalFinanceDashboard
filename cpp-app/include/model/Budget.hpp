#pragma once

#include <string>
#include <optional>
#include <cstdint>
#include <cmath>

namespace fintrack::model {

class Budget {
private:
    std::optional<int64_t> id;
    int64_t userId = 1;
    std::optional<int64_t> categoryId; // std::nullopt = overall monthly budget
    std::string categoryName;
    double amount = 0.0;
    int month = 1;
    int year = 2026;
    std::string createdAt;
    std::string updatedAt;

    // Transient calculations
    double spent = 0.0;
    double remaining = 0.0;
    double percentage = 0.0;

public:
    Budget() = default;
    Budget(std::optional<int64_t> id, int64_t userId, std::optional<int64_t> categoryId,
           double amount, int month, int year,
           std::string createdAt = "", std::string updatedAt = "")
        : id(id), userId(userId), categoryId(categoryId), amount(amount),
          month(month), year(year), createdAt(std::move(createdAt)), updatedAt(std::move(updatedAt)) {}

    void updateCalculations(double actualSpent) {
        spent = actualSpent;
        remaining = amount - spent;
        if (amount > 0.0) {
            percentage = std::round((spent / amount) * 10000.0) / 100.0;
        } else {
            percentage = 0.0;
        }
    }

    std::optional<int64_t> getId() const { return id; }
    void setId(std::optional<int64_t> newId) { id = newId; }

    int64_t getUserId() const { return userId; }
    void setUserId(int64_t uid) { userId = uid; }

    std::optional<int64_t> getCategoryId() const { return categoryId; }
    void setCategoryId(std::optional<int64_t> cid) { categoryId = cid; }

    const std::string& getCategoryName() const { return categoryName; }
    void setCategoryName(const std::string& name) { categoryName = name; }

    double getAmount() const { return amount; }
    void setAmount(double amt) { amount = amt; }

    int getMonth() const { return month; }
    void setMonth(int m) { month = m; }

    int getYear() const { return year; }
    void setYear(int y) { year = y; }

    const std::string& getCreatedAt() const { return createdAt; }
    void setCreatedAt(const std::string& time) { createdAt = time; }

    const std::string& getUpdatedAt() const { return updatedAt; }
    void setUpdatedAt(const std::string& time) { updatedAt = time; }

    double getSpent() const { return spent; }
    void setSpent(double s) { spent = s; }

    double getRemaining() const { return remaining; }
    void setRemaining(double r) { remaining = r; }

    double getPercentage() const { return percentage; }
    void setPercentage(double p) { percentage = p; }

    bool isOverall() const {
        return !categoryId.has_value() || categoryId.value() == 0;
    }

    std::string getStatus() const {
        if (percentage >= 100.0) return "Exceeded";
        if (percentage >= 80.0) return "Warning";
        return "Normal";
    }
};

} // namespace fintrack::model

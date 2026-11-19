#pragma once

#include "TransactionType.hpp"
#include <string>
#include <optional>
#include <cstdint>

namespace fintrack::model {

class Category {
private:
    std::optional<int64_t> id;
    int64_t userId = 1;
    std::string name;
    TransactionType type = TransactionType::EXPENSE;
    std::string createdAt;
    
    // Transient helper fields
    int transactionCount = 0;
    double totalAmount = 0.0;

public:
    Category() = default;
    Category(std::optional<int64_t> id, int64_t userId, std::string name, TransactionType type, std::string createdAt = "")
        : id(id), userId(userId), name(std::move(name)), type(type), createdAt(std::move(createdAt)) {}

    std::optional<int64_t> getId() const { return id; }
    void setId(std::optional<int64_t> newId) { id = newId; }

    int64_t getUserId() const { return userId; }
    void setUserId(int64_t uid) { userId = uid; }

    const std::string& getName() const { return name; }
    void setName(const std::string& newName) { name = newName; }

    TransactionType getType() const { return type; }
    void setType(TransactionType newType) { type = newType; }

    const std::string& getCreatedAt() const { return createdAt; }
    void setCreatedAt(const std::string& time) { createdAt = time; }

    int getTransactionCount() const { return transactionCount; }
    void setTransactionCount(int count) { transactionCount = count; }

    double getTotalAmount() const { return totalAmount; }
    void setTotalAmount(double amount) { totalAmount = amount; }
};

} // namespace fintrack::model

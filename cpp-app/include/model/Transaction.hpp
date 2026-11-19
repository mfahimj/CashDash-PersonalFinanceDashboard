#pragma once

#include "TransactionType.hpp"
#include <string>
#include <optional>
#include <cstdint>

namespace fintrack::model {

class Transaction {
private:
    std::optional<int64_t> id;
    int64_t userId = 1;
    std::string description;
    double amount = 0.0;
    TransactionType type = TransactionType::EXPENSE;
    int64_t categoryId = 1;
    std::string categoryName = "Other";
    std::string transactionDate; // YYYY-MM-DD
    std::string paymentMethod = "Bank Transfer";
    std::string notes;
    std::string createdAt;
    std::string updatedAt;

public:
    Transaction() = default;
    Transaction(std::optional<int64_t> id, int64_t userId, std::string description, double amount,
                TransactionType type, int64_t categoryId, std::string transactionDate,
                std::string paymentMethod, std::string notes = "",
                std::string createdAt = "", std::string updatedAt = "")
        : id(id), userId(userId), description(std::move(description)), amount(amount),
          type(type), categoryId(categoryId), transactionDate(std::move(transactionDate)),
          paymentMethod(std::move(paymentMethod)), notes(std::move(notes)),
          createdAt(std::move(createdAt)), updatedAt(std::move(updatedAt)) {}

    std::optional<int64_t> getId() const { return id; }
    void setId(std::optional<int64_t> newId) { id = newId; }

    int64_t getUserId() const { return userId; }
    void setUserId(int64_t uid) { userId = uid; }

    const std::string& getDescription() const { return description; }
    void setDescription(const std::string& desc) { description = desc; }

    double getAmount() const { return amount; }
    void setAmount(double amt) { amount = amt; }

    TransactionType getType() const { return type; }
    void setType(TransactionType t) { type = t; }

    int64_t getCategoryId() const { return categoryId; }
    void setCategoryId(int64_t cid) { categoryId = cid; }

    const std::string& getCategoryName() const { return categoryName; }
    void setCategoryName(const std::string& cname) { categoryName = cname; }

    const std::string& getTransactionDate() const { return transactionDate; }
    void setTransactionDate(const std::string& date) { transactionDate = date; }

    const std::string& getPaymentMethod() const { return paymentMethod; }
    void setPaymentMethod(const std::string& method) { paymentMethod = method; }

    const std::string& getNotes() const { return notes; }
    void setNotes(const std::string& n) { notes = n; }

    const std::string& getCreatedAt() const { return createdAt; }
    void setCreatedAt(const std::string& time) { createdAt = time; }

    const std::string& getUpdatedAt() const { return updatedAt; }
    void setUpdatedAt(const std::string& time) { updatedAt = time; }
};

} // namespace fintrack::model

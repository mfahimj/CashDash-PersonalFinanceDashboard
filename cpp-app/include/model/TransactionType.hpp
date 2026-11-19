#pragma once

#include <string>
#include <algorithm>

namespace fintrack::model {

enum class TransactionType {
    INCOME,
    EXPENSE
};

inline std::string transactionTypeToString(TransactionType type) {
    switch (type) {
        case TransactionType::INCOME: return "INCOME";
        case TransactionType::EXPENSE: return "EXPENSE";
    }
    return "EXPENSE";
}

inline std::string transactionTypeToDisplayName(TransactionType type) {
    switch (type) {
        case TransactionType::INCOME: return "Income";
        case TransactionType::EXPENSE: return "Expense";
    }
    return "Expense";
}

inline TransactionType transactionTypeFromString(const std::string& text) {
    std::string lower = text;
    std::transform(lower.begin(), lower.end(), lower.begin(), ::tolower);
    if (lower == "income") {
        return TransactionType::INCOME;
    }
    return TransactionType::EXPENSE;
}

} // namespace fintrack::model

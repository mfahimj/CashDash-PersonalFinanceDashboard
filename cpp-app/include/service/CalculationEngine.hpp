#pragma once

#include <cmath>

namespace fintrack::service {

class CalculationEngine {
public:
    static double calculateBalance(double openingBalance, double income, double expenses) {
        return openingBalance + income - expenses;
    }

    static double calculateSavings(double income, double expenses) {
        return income - expenses;
    }

    static double calculateSavingsRate(double income, double savings) {
        if (income <= 0.0) return 0.0;
        return std::round((savings / income) * 10000.0) / 100.0;
    }

    static double calculateBudgetRemaining(double limit, double spent) {
        return limit - spent;
    }

    static double calculateBudgetUsagePercentage(double limit, double spent) {
        if (limit <= 0.0) return 0.0;
        return std::round((spent / limit) * 10000.0) / 100.0;
    }
};

} // namespace fintrack::service

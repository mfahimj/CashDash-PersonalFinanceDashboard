#pragma once

#include "Transaction.hpp"
#include <vector>
#include <cmath>

namespace fintrack::model {

class DashboardSummary {
private:
    double totalBalance = 0.0;
    double totalIncome = 0.0;
    double totalExpenses = 0.0;
    double totalSavings = 0.0;
    double savingsRate = 0.0;

    double previousIncome = 0.0;
    double previousExpenses = 0.0;
    double incomeGrowthPercent = 0.0;
    double expenseGrowthPercent = 0.0;

    double budgetLimit = 0.0;
    double budgetSpent = 0.0;
    double budgetRemaining = 0.0;
    double budgetUsagePercentage = 0.0;

    std::vector<Transaction> recentTransactions;

public:
    DashboardSummary() = default;

    void calculateMetrics(double openingBalance, double inc, double exp,
                          double prevInc, double prevExp, double budgetLim) {
        totalIncome = inc;
        totalExpenses = exp;
        totalBalance = openingBalance + totalIncome - totalExpenses;
        totalSavings = totalIncome - totalExpenses;

        if (totalIncome > 0.0) {
            savingsRate = std::round((totalSavings / totalIncome) * 10000.0) / 100.0;
        } else {
            savingsRate = 0.0;
        }

        previousIncome = prevInc;
        previousExpenses = prevExp;

        if (previousIncome > 0.0) {
            incomeGrowthPercent = std::round(((totalIncome - previousIncome) / previousIncome) * 10000.0) / 100.0;
        } else {
            incomeGrowthPercent = 0.0;
        }

        if (previousExpenses > 0.0) {
            expenseGrowthPercent = std::round(((totalExpenses - previousExpenses) / previousExpenses) * 10000.0) / 100.0;
        } else {
            expenseGrowthPercent = 0.0;
        }

        budgetLimit = budgetLim;
        budgetSpent = totalExpenses;
        budgetRemaining = budgetLimit - budgetSpent;

        if (budgetLimit > 0.0) {
            budgetUsagePercentage = std::round((budgetSpent / budgetLimit) * 10000.0) / 100.0;
        } else {
            budgetUsagePercentage = 0.0;
        }
    }

    double getTotalBalance() const { return totalBalance; }
    void setTotalBalance(double val) { totalBalance = val; }

    double getTotalIncome() const { return totalIncome; }
    void setTotalIncome(double val) { totalIncome = val; }

    double getTotalExpenses() const { return totalExpenses; }
    void setTotalExpenses(double val) { totalExpenses = val; }

    double getTotalSavings() const { return totalSavings; }
    void setTotalSavings(double val) { totalSavings = val; }

    double getSavingsRate() const { return savingsRate; }
    void setSavingsRate(double val) { savingsRate = val; }

    double getPreviousIncome() const { return previousIncome; }
    double getPreviousExpenses() const { return previousExpenses; }

    double getIncomeGrowthPercent() const { return incomeGrowthPercent; }
    void setIncomeGrowthPercent(double val) { incomeGrowthPercent = val; }

    double getExpenseGrowthPercent() const { return expenseGrowthPercent; }
    void setExpenseGrowthPercent(double val) { expenseGrowthPercent = val; }

    double getBudgetLimit() const { return budgetLimit; }
    void setBudgetLimit(double val) { budgetLimit = val; }

    double getBudgetSpent() const { return budgetSpent; }
    void setBudgetSpent(double val) { budgetSpent = val; }

    double getBudgetRemaining() const { return budgetRemaining; }
    void setBudgetRemaining(double val) { budgetRemaining = val; }

    double getBudgetUsagePercentage() const { return budgetUsagePercentage; }
    void setBudgetUsagePercentage(double val) { budgetUsagePercentage = val; }

    const std::vector<Transaction>& getRecentTransactions() const { return recentTransactions; }
    void setRecentTransactions(std::vector<Transaction> recents) { recentTransactions = std::move(recents); }
};

} // namespace fintrack::model

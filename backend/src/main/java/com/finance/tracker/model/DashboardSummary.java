package com.finance.tracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummary {
    private BigDecimal totalBalance = BigDecimal.ZERO;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal totalSavings = BigDecimal.ZERO;
    private double savingsRate = 0.0;

    private BigDecimal previousIncome = BigDecimal.ZERO;
    private BigDecimal previousExpenses = BigDecimal.ZERO;
    private double incomeGrowthPercent = 0.0;
    private double expenseGrowthPercent = 0.0;

    private BigDecimal budgetLimit = BigDecimal.ZERO;
    private BigDecimal budgetSpent = BigDecimal.ZERO;
    private BigDecimal budgetRemaining = BigDecimal.ZERO;
    private double budgetUsagePercentage = 0.0;

    private List<Transaction> recentTransactions = new ArrayList<>();

    public DashboardSummary() {}

    public void calculateMetrics(BigDecimal openingBalance, BigDecimal inc, BigDecimal exp,
                                 BigDecimal prevInc, BigDecimal prevExp, BigDecimal budgetLim) {
        this.totalIncome = inc != null ? inc : BigDecimal.ZERO;
        this.totalExpenses = exp != null ? exp : BigDecimal.ZERO;
        BigDecimal open = openingBalance != null ? openingBalance : BigDecimal.ZERO;
        this.totalBalance = open.add(this.totalIncome).subtract(this.totalExpenses);
        this.totalSavings = this.totalIncome.subtract(this.totalExpenses);

        if (this.totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            this.savingsRate = this.totalSavings
                    .divide(this.totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else {
            this.savingsRate = 0.0;
        }

        this.previousIncome = prevInc != null ? prevInc : BigDecimal.ZERO;
        this.previousExpenses = prevExp != null ? prevExp : BigDecimal.ZERO;

        if (this.previousIncome.compareTo(BigDecimal.ZERO) > 0) {
            this.incomeGrowthPercent = this.totalIncome.subtract(this.previousIncome)
                    .divide(this.previousIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        if (this.previousExpenses.compareTo(BigDecimal.ZERO) > 0) {
            this.expenseGrowthPercent = this.totalExpenses.subtract(this.previousExpenses)
                    .divide(this.previousExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        this.budgetLimit = budgetLim != null ? budgetLim : BigDecimal.ZERO;
        this.budgetSpent = this.totalExpenses;
        this.budgetRemaining = this.budgetLimit.subtract(this.budgetSpent);
        if (this.budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            this.budgetUsagePercentage = this.budgetSpent
                    .divide(this.budgetLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else {
            this.budgetUsagePercentage = 0.0;
        }
    }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getTotalSavings() { return totalSavings; }
    public double getSavingsRate() { return savingsRate; }

    public BigDecimal getPreviousIncome() { return previousIncome; }
    public BigDecimal getPreviousExpenses() { return previousExpenses; }
    public double getIncomeGrowthPercent() { return incomeGrowthPercent; }
    public double getExpenseGrowthPercent() { return expenseGrowthPercent; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public BigDecimal getBudgetSpent() { return budgetSpent; }
    public BigDecimal getBudgetRemaining() { return budgetRemaining; }
    public double getBudgetUsagePercentage() { return budgetUsagePercentage; }

    public List<Transaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<Transaction> recentTransactions) { this.recentTransactions = recentTransactions; }
}


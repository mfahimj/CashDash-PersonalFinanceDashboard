package com.finance.tracker.service;

import com.finance.tracker.dao.BudgetDao;
import com.finance.tracker.dao.CategoryDao;
import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceService {
    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final BudgetDao budgetDao;

    // Configurable Opening Balance (e.g. ₹50,000 baseline)
    private BigDecimal openingBalance = new BigDecimal("50000.00");

    public FinanceService(TransactionDao transactionDao, CategoryDao categoryDao, BudgetDao budgetDao) {
        this.transactionDao = transactionDao;
        this.categoryDao = categoryDao;
        this.budgetDao = budgetDao;
    }

    public DashboardSummary getDashboardSummary(int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        // Previous period (previous month)
        LocalDate prevMonthDate = startOfMonth.minusMonths(1);
        int prevMonth = prevMonthDate.getMonthValue();
        int prevYear = prevMonthDate.getYear();
        LocalDate startOfPrevMonth = LocalDate.of(prevYear, prevMonth, 1);
        LocalDate endOfPrevMonth = startOfPrevMonth.plusMonths(1).minusDays(1);

        BigDecimal currentIncome = BigDecimal.ZERO;
        BigDecimal currentExpenses = BigDecimal.ZERO;
        BigDecimal prevIncome = BigDecimal.ZERO;
        BigDecimal prevExpenses = BigDecimal.ZERO;

        for (Transaction tx : transactionDao.findAll()) {
            LocalDate d = tx.getTransactionDate();
            if (!d.isBefore(startOfMonth) && !d.isAfter(endOfMonth)) {
                if (tx.getType() == TransactionType.INCOME) {
                    currentIncome = currentIncome.add(tx.getAmount());
                } else {
                    currentExpenses = currentExpenses.add(tx.getAmount());
                }
            } else if (!d.isBefore(startOfPrevMonth) && !d.isAfter(endOfPrevMonth)) {
                if (tx.getType() == TransactionType.INCOME) {
                    prevIncome = prevIncome.add(tx.getAmount());
                } else {
                    prevExpenses = prevExpenses.add(tx.getAmount());
                }
            }
        }

        // Budget limit for current month
        BigDecimal budgetLimit = BigDecimal.ZERO;
        Optional<Budget> overallOpt = budgetDao.findOverall(month, year);
        if (overallOpt.isPresent()) {
            budgetLimit = overallOpt.get().getAmount();
        } else {
            // Sum of category budgets if no overall budget
            List<Budget> budgets = budgetDao.findByPeriod(month, year);
            for (Budget b : budgets) {
                if (!b.isOverall()) budgetLimit = budgetLimit.add(b.getAmount());
            }
        }

        DashboardSummary summary = new DashboardSummary();
        summary.calculateMetrics(openingBalance, currentIncome, currentExpenses, prevIncome, prevExpenses, budgetLimit);
        summary.setRecentTransactions(transactionDao.findRecent(7));
        return summary;
    }

    public List<Budget> getBudgetsWithCalculations(int month, int year) {
        List<Budget> list = budgetDao.findByPeriod(month, year);
        for (Budget b : list) {
            BigDecimal spent;
            if (b.isOverall()) {
                spent = transactionDao.sumExpenseByPeriod(month, year);
            } else {
                spent = transactionDao.sumExpenseByCategoryAndPeriod(b.getCategoryId(), month, year);
            }
            b.updateCalculations(spent);
        }
        return list;
    }

    public Map<String, BigDecimal> getExpenseBreakdownByCategory(int month, int year) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        for (Transaction tx : transactionDao.findAll()) {
            if (tx.getType() == TransactionType.EXPENSE &&
                !tx.getTransactionDate().isBefore(start) &&
                !tx.getTransactionDate().isAfter(end)) {
                String catName = tx.getCategoryName() != null ? tx.getCategoryName() : "Uncategorized";
                map.put(catName, map.getOrDefault(catName, BigDecimal.ZERO).add(tx.getAmount()));
            }
        }
        return map;
    }

    public List<MonthlyReportItem> getMonthlyTrends(int monthsCount) {
        List<MonthlyReportItem> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        // Go back monthsCount
        for (int i = monthsCount - 1; i >= 0; i--) {
            LocalDate target = now.minusMonths(i);
            int m = target.getMonthValue();
            int y = target.getYear();
            String label = target.getMonth().name().substring(0, 3) + " " + y;

            BigDecimal inc = BigDecimal.ZERO;
            BigDecimal exp = BigDecimal.ZERO;

            for (Transaction tx : transactionDao.findAll()) {
                if (tx.getTransactionDate().getMonthValue() == m && tx.getTransactionDate().getYear() == y) {
                    if (tx.getType() == TransactionType.INCOME) inc = inc.add(tx.getAmount());
                    else exp = exp.add(tx.getAmount());
                }
            }
            result.add(new MonthlyReportItem(label, m, y, inc, exp));
        }
        return result;
    }

    public boolean canDeleteCategory(Long categoryId) {
        return transactionDao.countByCategoryId(categoryId) == 0;
    }

    public TransactionDao getTransactionDao() { return transactionDao; }
    public CategoryDao getCategoryDao() { return categoryDao; }
    public BudgetDao getBudgetDao() { return budgetDao; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public static class MonthlyReportItem {
        private final String monthLabel;
        private final int month;
        private final int year;
        private final BigDecimal income;
        private final BigDecimal expenses;
        private final BigDecimal savings;

        public MonthlyReportItem(String monthLabel, int month, int year, BigDecimal income, BigDecimal expenses) {
            this.monthLabel = monthLabel;
            this.month = month;
            this.year = year;
            this.income = income;
            this.expenses = expenses;
            this.savings = income.subtract(expenses);
        }

        public String getMonthLabel() { return monthLabel; }
        public int getMonth() { return month; }
        public int getYear() { return year; }
        public BigDecimal getIncome() { return income; }
        public BigDecimal getExpenses() { return expenses; }
        public BigDecimal getSavings() { return savings; }
    }
}


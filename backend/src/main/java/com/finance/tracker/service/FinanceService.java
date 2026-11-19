package com.finance.tracker.service;

import com.finance.tracker.dao.BudgetDao;
import com.finance.tracker.dao.CategoryDao;
import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.dto.BudgetRequestDto;
import com.finance.tracker.dto.CategoryRequestDto;
import com.finance.tracker.dto.TransactionRequestDto;
import com.finance.tracker.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class FinanceService {
    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final BudgetDao budgetDao;

    private BigDecimal openingBalance = new BigDecimal("50000.00");

    public FinanceService(TransactionDao transactionDao, CategoryDao categoryDao, BudgetDao budgetDao) {
        this.transactionDao = transactionDao;
        this.categoryDao = categoryDao;
        this.budgetDao = budgetDao;
    }

    public DashboardSummary getDashboardSummary(int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        LocalDate prevMonthDate = startOfMonth.minusMonths(1);
        int prevMonth = prevMonthDate.getMonthValue();
        int prevYear = prevMonthDate.getYear();
        LocalDate startOfPrev = LocalDate.of(prevYear, prevMonth, 1);
        LocalDate endOfPrev = startOfPrev.plusMonths(1).minusDays(1);

        BigDecimal currentIncome = BigDecimal.ZERO;
        BigDecimal currentExpenses = BigDecimal.ZERO;
        BigDecimal prevIncome = BigDecimal.ZERO;
        BigDecimal prevExpenses = BigDecimal.ZERO;

        List<Transaction> all = transactionDao.findAllWithFilters(null, null, null, null, null, null);
        for (Transaction tx : all) {
            LocalDate d = tx.getTransactionDate();
            if (!d.isBefore(startOfMonth) && !d.isAfter(endOfMonth)) {
                if (tx.getType() == TransactionType.INCOME) currentIncome = currentIncome.add(tx.getAmount());
                else currentExpenses = currentExpenses.add(tx.getAmount());
            } else if (!d.isBefore(startOfPrev) && !d.isAfter(endOfPrev)) {
                if (tx.getType() == TransactionType.INCOME) prevIncome = prevIncome.add(tx.getAmount());
                else prevExpenses = prevExpenses.add(tx.getAmount());
            }
        }

        BigDecimal budgetLimit = BigDecimal.ZERO;
        Optional<Budget> overallOpt = budgetDao.findOverall(month, year);
        if (overallOpt.isPresent()) {
            budgetLimit = overallOpt.get().getAmount();
        } else {
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

    public Map<String, BigDecimal> getCategoryBreakdown(int month, int year) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<Transaction> list = transactionDao.findAllWithFilters(TransactionType.EXPENSE, null, start, end, null, null);
        for (Transaction tx : list) {
            String name = tx.getCategoryName() != null ? tx.getCategoryName() : "Uncategorized";
            map.put(name, map.getOrDefault(name, BigDecimal.ZERO).add(tx.getAmount()));
        }
        return map;
    }

    public List<MonthlyReportItem> getMonthlyTrends(int monthsCount) {
        List<MonthlyReportItem> items = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<Transaction> all = transactionDao.findAllWithFilters(null, null, null, null, null, null);

        for (int i = monthsCount - 1; i >= 0; i--) {
            LocalDate target = now.minusMonths(i);
            int m = target.getMonthValue();
            int y = target.getYear();
            String label = target.getMonth().name().substring(0, 3) + " " + y;

            BigDecimal inc = BigDecimal.ZERO;
            BigDecimal exp = BigDecimal.ZERO;

            for (Transaction tx : all) {
                if (tx.getTransactionDate().getMonthValue() == m && tx.getTransactionDate().getYear() == y) {
                    if (tx.getType() == TransactionType.INCOME) inc = inc.add(tx.getAmount());
                    else exp = exp.add(tx.getAmount());
                }
            }
            items.add(new MonthlyReportItem(label, m, y, inc, exp));
        }
        return items;
    }

    @Transactional
    public Transaction createTransaction(TransactionRequestDto dto) {
        Category cat = categoryDao.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));

        Transaction tx = new Transaction();
        tx.setUserId(1L);
        tx.setDescription(dto.getDescription());
        tx.setAmount(dto.getAmount());
        tx.setType(dto.getType());
        tx.setCategoryId(dto.getCategoryId());
        tx.setCategoryName(cat.getName());
        tx.setTransactionDate(dto.getTransactionDate());
        tx.setPaymentMethod(dto.getPaymentMethod());
        tx.setNotes(dto.getNotes());
        return transactionDao.save(tx);
    }

    @Transactional
    public Transaction updateTransaction(Long id, TransactionRequestDto dto) {
        Transaction tx = transactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));

        Category cat = categoryDao.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));

        tx.setDescription(dto.getDescription());
        tx.setAmount(dto.getAmount());
        tx.setType(dto.getType());
        tx.setCategoryId(dto.getCategoryId());
        tx.setCategoryName(cat.getName());
        tx.setTransactionDate(dto.getTransactionDate());
        tx.setPaymentMethod(dto.getPaymentMethod());
        tx.setNotes(dto.getNotes());
        transactionDao.update(tx);
        return tx;
    }

    public boolean deleteTransaction(Long id) {
        return transactionDao.delete(id);
    }

    public boolean canDeleteCategory(Long categoryId) {
        return transactionDao.countByCategoryId(categoryId) == 0;
    }

    @Transactional
    public Category createCategory(CategoryRequestDto dto) {
        Category c = new Category(null, 1L, dto.getName(), dto.getType());
        return categoryDao.save(c);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequestDto dto) {
        Category c = categoryDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
        c.setName(dto.getName());
        c.setType(dto.getType());
        categoryDao.update(c);
        return c;
    }

    public boolean deleteCategory(Long id) {
        if (!canDeleteCategory(id)) {
            throw new IllegalStateException("Cannot delete category as it is currently referenced by transactions.");
        }
        return categoryDao.delete(id);
    }

    @Transactional
    public Budget createBudget(BudgetRequestDto dto) {
        Budget b = new Budget(null, 1L, dto.getCategoryId(), dto.getAmount(), dto.getMonth(), dto.getYear());
        if (dto.getCategoryId() != null) {
            categoryDao.findById(dto.getCategoryId()).ifPresent(c -> b.setCategoryName(c.getName()));
        } else {
            b.setCategoryName("Overall Monthly Budget");
        }
        return budgetDao.save(b);
    }

    @Transactional
    public Budget updateBudget(Long id, BudgetRequestDto dto) {
        Budget b = budgetDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found with ID: " + id));
        b.setCategoryId(dto.getCategoryId());
        b.setAmount(dto.getAmount());
        b.setMonth(dto.getMonth());
        b.setYear(dto.getYear());
        if (dto.getCategoryId() != null) {
            categoryDao.findById(dto.getCategoryId()).ifPresent(c -> b.setCategoryName(c.getName()));
        } else {
            b.setCategoryName("Overall Monthly Budget");
        }
        budgetDao.update(b);
        return b;
    }

    public boolean deleteBudget(Long id) {
        return budgetDao.delete(id);
    }

    public TransactionDao getTransactionDao() { return transactionDao; }
    public CategoryDao getCategoryDao() { return categoryDao; }
    public BudgetDao getBudgetDao() { return budgetDao; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
}


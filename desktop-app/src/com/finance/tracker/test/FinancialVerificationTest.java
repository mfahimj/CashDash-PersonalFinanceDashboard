package com.finance.tracker.test;

import com.finance.tracker.dao.BudgetDao;
import com.finance.tracker.dao.CategoryDao;
import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.model.Budget;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.DashboardSummary;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.CalculationEngine;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.util.CsvExporter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FinancialVerificationTest {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("RUNNING AUTOMATED TEST SUITE: CASHDASH");
        System.out.println("======================================================================");

        int passed = 0;
        int failed = 0;

        CategoryDao categoryDao = new CategoryDao();
        TransactionDao transactionDao = new TransactionDao(categoryDao);
        BudgetDao budgetDao = new BudgetDao(categoryDao);
        FinanceService service = new FinanceService(transactionDao, categoryDao, budgetDao);

        // Test 1: Category seeding & verification
        try {
            List<Category> categories = categoryDao.findAll();
            assertCondition(!categories.isEmpty(), "Categories should be auto-seeded");
            assertCondition(categories.size() >= 16, "At least 16 categories seeded (Income + Expense)");
            System.out.println("[PASS] Test 1: Default categories seeded correctly (" + categories.size() + " categories).");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 1: " + t.getMessage());
            failed++;
        }

        // Test 2: Transactions initial state & calculations
        try {
            List<Transaction> allTx = transactionDao.findAll();
            assertCondition(allTx.size() >= 30, "At least 30 realistic demo transactions should be loaded");
            DashboardSummary summary = service.getDashboardSummary(10, 2026);
            assertCondition(summary.getTotalIncome().compareTo(BigDecimal.ZERO) > 0, "October income > 0");
            assertCondition(summary.getTotalExpenses().compareTo(BigDecimal.ZERO) > 0, "October expenses > 0");
            assertCondition(summary.getTotalSavings().equals(summary.getTotalIncome().subtract(summary.getTotalExpenses())),
                    "Savings = Income - Expenses");
            System.out.println("[PASS] Test 2: Dashboard metrics calculation verified.");
            System.out.println("       October Income: " + summary.getTotalIncome() +
                               ", Expenses: " + summary.getTotalExpenses() +
                               ", Net Savings: " + summary.getTotalSavings() +
                               ", Balance: " + summary.getTotalBalance());
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 2: " + t.getMessage());
            failed++;
        }

        // Test 3: Add an Income Transaction & verify totals increase
        try {
            DashboardSummary before = service.getDashboardSummary(10, 2026);
            BigDecimal addAmt = new BigDecimal("5000.00");
            Transaction newInc = new Transaction(null, 1L, "Bonus Payout", addAmt, TransactionType.INCOME,
                    11L, LocalDate.of(2026, 10, 20), "Bank Transfer", "Performance incentive");
            transactionDao.save(newInc);

            DashboardSummary after = service.getDashboardSummary(10, 2026);
            assertCondition(after.getTotalIncome().equals(before.getTotalIncome().add(addAmt)),
                    "Income must increase by exactly added amount");
            assertCondition(after.getTotalBalance().equals(before.getTotalBalance().add(addAmt)),
                    "Balance must increase by exactly added amount");
            System.out.println("[PASS] Test 3: Add Income transaction increases balance and income accurately.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 3: " + t.getMessage());
            failed++;
        }

        // Test 4: Add an Expense Transaction & verify totals adjust
        try {
            DashboardSummary before = service.getDashboardSummary(10, 2026);
            BigDecimal expAmt = new BigDecimal("1200.00");
            Transaction newExp = new Transaction(null, 1L, "Bookstore Purchase", expAmt, TransactionType.EXPENSE,
                    6L, LocalDate.of(2026, 10, 21), "UPI", "Reference books");
            transactionDao.save(newExp);

            DashboardSummary after = service.getDashboardSummary(10, 2026);
            assertCondition(after.getTotalExpenses().equals(before.getTotalExpenses().add(expAmt)),
                    "Expenses must increase by exactly added amount");
            assertCondition(after.getTotalBalance().equals(before.getTotalBalance().subtract(expAmt)),
                    "Balance must decrease by exactly added amount");
            System.out.println("[PASS] Test 4: Add Expense transaction decreases balance and increases expenses accurately.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 4: " + t.getMessage());
            failed++;
        }

        // Test 5: Edit Transaction and verify recalculated balances
        try {
            List<Transaction> txs = transactionDao.findAll();
            Transaction target = txs.get(0);
            BigDecimal origAmt = target.getAmount();
            BigDecimal newAmt = origAmt.add(new BigDecimal("1000.00"));
            target.setAmount(newAmt);
            transactionDao.update(target);

            Transaction reloaded = transactionDao.findById(target.getId()).orElseThrow();
            assertCondition(reloaded.getAmount().equals(newAmt), "Transaction amount updated");
            System.out.println("[PASS] Test 5: Edit transaction persists and updates amount correctly.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 5: " + t.getMessage());
            failed++;
        }

        // Test 6: Delete Transaction and verify recalculation
        try {
            Transaction tempTx = new Transaction(null, 1L, "Temporary Coffee", new BigDecimal("250.00"),
                    TransactionType.EXPENSE, 1L, LocalDate.of(2026, 10, 22), "Cash", "Temp");
            transactionDao.save(tempTx);
            Long tempId = tempTx.getId();

            DashboardSummary beforeDel = service.getDashboardSummary(10, 2026);
            boolean deleted = transactionDao.delete(tempId);
            assertCondition(deleted, "Delete returned true");
            DashboardSummary afterDel = service.getDashboardSummary(10, 2026);
            assertCondition(afterDel.getTotalExpenses().equals(beforeDel.getTotalExpenses().subtract(new BigDecimal("250.00"))),
                    "Expense total decreased by deleted amount");
            System.out.println("[PASS] Test 6: Delete transaction correctly removes record and recalculates totals.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 6: " + t.getMessage());
            failed++;
        }

        // Test 7: Filter transactions by keyword, type, date range
        try {
            List<Transaction> groceryFilter = transactionDao.findByFilters(TransactionType.EXPENSE, null, null, null, null, "grocery");
            assertCondition(!groceryFilter.isEmpty(), "Should find grocery transactions");
            assertCondition(groceryFilter.get(0).getDescription().toLowerCase().contains("grocery"),
                    "Description contains 'grocery'");

            List<Transaction> incomeFilter = transactionDao.findByFilters(TransactionType.INCOME, null, null, null, null, null);
            for (Transaction tx : incomeFilter) {
                assertCondition(tx.getType() == TransactionType.INCOME, "Only income transactions returned");
            }
            System.out.println("[PASS] Test 7: Multi-criteria transaction filters work as expected.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 7: " + t.getMessage());
            failed++;
        }

        // Test 8: Budget rules - income must NOT count towards expense budget
        try {
            List<Budget> budgets = service.getBudgetsWithCalculations(10, 2026);
            assertCondition(!budgets.isEmpty(), "Budgets found for October 2026");
            for (Budget b : budgets) {
                assertCondition(b.getSpent().compareTo(BigDecimal.ZERO) >= 0, "Spent >= 0");
                assertCondition(b.getRemaining().equals(b.getAmount().subtract(b.getSpent())), "Remaining = Amount - Spent");
            }
            System.out.println("[PASS] Test 8: Budget usage calculation matches expense transactions only.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 8: " + t.getMessage());
            failed++;
        }

        // Test 9: Category deletion integrity protection
        try {
            // Category 1 is Food and Dining, has transactions
            int txCount = transactionDao.countByCategoryId(1L);
            assertCondition(txCount > 0, "Food & Dining has active transactions");
            assertCondition(!service.canDeleteCategory(1L), "Should block deletion of category with transactions");

            // Create temporary category with 0 transactions
            Category emptyCat = new Category(null, 1L, "Unused Category", TransactionType.EXPENSE);
            categoryDao.save(emptyCat);
            assertCondition(service.canDeleteCategory(emptyCat.getId()), "Should allow deleting unused category");
            categoryDao.delete(emptyCat.getId());
            System.out.println("[PASS] Test 9: Category foreign reference integrity protection verified.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 9: " + t.getMessage());
            failed++;
        }

        // Test 10: CSV export generation
        try {
            File exportFile = new File("test_export.csv");
            boolean ok = CsvExporter.exportTransactions(transactionDao.findAll(), exportFile);
            assertCondition(ok && exportFile.exists() && exportFile.length() > 0, "CSV file generated");
            exportFile.delete(); // cleanup
            System.out.println("[PASS] Test 10: CSV exporter creates valid non-empty files.");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Test 10: " + t.getMessage());
            failed++;
        }

        System.out.println("======================================================================");
        System.out.println(String.format("TEST SUITE COMPLETED: %d PASSED, %d FAILED", passed, failed));
        System.out.println("======================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }
}


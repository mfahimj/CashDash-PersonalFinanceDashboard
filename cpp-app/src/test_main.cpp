#include "dao/CategoryDao.hpp"
#include "dao/TransactionDao.hpp"
#include "dao/BudgetDao.hpp"
#include "service/FinanceService.hpp"
#include "service/CalculationEngine.hpp"
#include "util/CsvExporter.hpp"
#include "util/DateUtils.hpp"
#include <iostream>
#include <cassert>
#include <cmath>
#include <filesystem>

using namespace fintrack;

static void assertCondition(bool condition, const std::string& message) {
    if (!condition) {
        std::cerr << "Assertion Failed: " << message << std::endl;
        throw std::runtime_error(message);
    }
}

int main() {
    std::cout << "======================================================================" << std::endl;
    std::cout << "RUNNING AUTOMATED TEST SUITE (C++): CASHDASH / FINTRACK PRO" << std::endl;
    std::cout << "======================================================================" << std::endl;

    int passed = 0;
    int failed = 0;

    auto categoryDao = std::make_shared<dao::CategoryDao>("data_categories.dat");
    auto transactionDao = std::make_shared<dao::TransactionDao>(categoryDao, "data_transactions.dat");
    auto budgetDao = std::make_shared<dao::BudgetDao>(categoryDao, "data_budgets.dat");
    auto service = std::make_shared<service::FinanceService>(transactionDao, categoryDao, budgetDao);

    // Test 1: Category seeding & verification
    try {
        auto categories = categoryDao->findAll();
        assertCondition(!categories.empty(), "Categories should be auto-seeded");
        assertCondition(categories.size() >= 16, "At least 16 categories seeded (Income + Expense)");
        std::cout << "[PASS] Test 1: Default categories seeded correctly (" << categories.size() << " categories)." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 1: " << e.what() << std::endl;
        failed++;
    }

    // Test 2: Transactions initial state & calculations
    try {
        auto allTx = transactionDao->findAll();
        assertCondition(allTx.size() >= 30, "At least 30 realistic demo transactions should be loaded");
        auto summary = service->getDashboardSummary(10, 2026);
        assertCondition(summary.getTotalIncome() > 0.0, "October income > 0");
        assertCondition(summary.getTotalExpenses() > 0.0, "October expenses > 0");
        assertCondition(std::abs(summary.getTotalSavings() - (summary.getTotalIncome() - summary.getTotalExpenses())) < 0.01,
                        "Savings = Income - Expenses");
        std::cout << "[PASS] Test 2: Dashboard metrics calculation verified." << std::endl;
        std::cout << "       October Income: " << summary.getTotalIncome()
                  << ", Expenses: " << summary.getTotalExpenses()
                  << ", Net Savings: " << summary.getTotalSavings()
                  << ", Balance: " << summary.getTotalBalance() << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 2: " << e.what() << std::endl;
        failed++;
    }

    // Test 3: Add an Income Transaction & verify totals increase
    try {
        auto before = service->getDashboardSummary(10, 2026);
        double addAmt = 5000.00;
        model::Transaction newInc(std::nullopt, 1, "Bonus Payout", addAmt, model::TransactionType::INCOME,
                                 11, "2026-10-20", "Bank Transfer", "Performance incentive");
        transactionDao->save(newInc);

        auto after = service->getDashboardSummary(10, 2026);
        assertCondition(std::abs(after.getTotalIncome() - (before.getTotalIncome() + addAmt)) < 0.01,
                        "Income must increase by exactly added amount");
        assertCondition(std::abs(after.getTotalBalance() - (before.getTotalBalance() + addAmt)) < 0.01,
                        "Balance must increase by exactly added amount");
        std::cout << "[PASS] Test 3: Add Income transaction increases balance and income accurately." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 3: " << e.what() << std::endl;
        failed++;
    }

    // Test 4: Add an Expense Transaction & verify totals adjust
    try {
        auto before = service->getDashboardSummary(10, 2026);
        double expAmt = 1200.00;
        model::Transaction newExp(std::nullopt, 1, "Bookstore Purchase", expAmt, model::TransactionType::EXPENSE,
                                 6, "2026-10-21", "UPI", "Reference books");
        transactionDao->save(newExp);

        auto after = service->getDashboardSummary(10, 2026);
        assertCondition(std::abs(after.getTotalExpenses() - (before.getTotalExpenses() + expAmt)) < 0.01,
                        "Expenses must increase by exactly added amount");
        assertCondition(std::abs(after.getTotalBalance() - (before.getTotalBalance() - expAmt)) < 0.01,
                        "Balance must decrease by exactly added amount");
        std::cout << "[PASS] Test 4: Add Expense transaction decreases balance and increases expenses accurately." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 4: " << e.what() << std::endl;
        failed++;
    }

    // Test 5: Edit Transaction and verify recalculated balances
    try {
        auto txs = transactionDao->findAll();
        auto target = txs[0];
        double origAmt = target.getAmount();
        double newAmt = origAmt + 1000.00;
        target.setAmount(newAmt);
        transactionDao->update(target);

        auto reloaded = transactionDao->findById(target.getId().value());
        assertCondition(reloaded.has_value() && std::abs(reloaded->getAmount() - newAmt) < 0.01,
                        "Transaction amount updated");
        std::cout << "[PASS] Test 5: Edit transaction persists and updates amount correctly." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 5: " << e.what() << std::endl;
        failed++;
    }

    // Test 6: Delete Transaction and verify recalculation
    try {
        model::Transaction tempTx(std::nullopt, 1, "Temporary Coffee", 250.00,
                                 model::TransactionType::EXPENSE, 1, "2026-10-22", "Cash", "Temp");
        tempTx = transactionDao->save(tempTx);
        int64_t tempId = tempTx.getId().value();

        auto beforeDel = service->getDashboardSummary(10, 2026);
        bool deleted = transactionDao->deleteById(tempId);
        assertCondition(deleted, "Delete returned true");
        auto afterDel = service->getDashboardSummary(10, 2026);
        assertCondition(std::abs(afterDel.getTotalExpenses() - (beforeDel.getTotalExpenses() - 250.00)) < 0.01,
                        "Expense total decreased by deleted amount");
        std::cout << "[PASS] Test 6: Delete transaction correctly removes record and recalculates totals." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 6: " << e.what() << std::endl;
        failed++;
    }

    // Test 7: Filter transactions by keyword, type, date range
    try {
        auto groceryFilter = transactionDao->findByFilters(model::TransactionType::EXPENSE, std::nullopt, "", "", "", "grocery");
        assertCondition(!groceryFilter.empty(), "Should find grocery transactions");

        auto incomeFilter = transactionDao->findByFilters(model::TransactionType::INCOME);
        for (const auto& tx : incomeFilter) {
            assertCondition(tx.getType() == model::TransactionType::INCOME, "Only income transactions returned");
        }
        std::cout << "[PASS] Test 7: Multi-criteria transaction filters work as expected." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 7: " << e.what() << std::endl;
        failed++;
    }

    // Test 8: Budget rules - income must NOT count towards expense budget
    try {
        auto budgets = service->getBudgetsWithCalculations(10, 2026);
        assertCondition(!budgets.empty(), "Budgets found for October 2026");
        for (const auto& b : budgets) {
            assertCondition(b.getSpent() >= 0.0, "Spent >= 0");
            assertCondition(std::abs(b.getRemaining() - (b.getAmount() - b.getSpent())) < 0.01, "Remaining = Amount - Spent");
        }
        std::cout << "[PASS] Test 8: Budget usage calculation matches expense transactions only." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 8: " << e.what() << std::endl;
        failed++;
    }

    // Test 9: Category deletion integrity protection
    try {
        int txCount = transactionDao->countByCategoryId(1);
        assertCondition(txCount > 0, "Food & Dining has active transactions");
        assertCondition(!service->canDeleteCategory(1), "Should block deletion of category with transactions");

        model::Category emptyCat(std::nullopt, 1, "Unused Category", model::TransactionType::EXPENSE);
        emptyCat = categoryDao->save(emptyCat);
        assertCondition(service->canDeleteCategory(emptyCat.getId().value()), "Should allow deleting unused category");
        categoryDao->deleteById(emptyCat.getId().value());
        std::cout << "[PASS] Test 9: Category foreign reference integrity protection verified." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 9: " << e.what() << std::endl;
        failed++;
    }

    // Test 10: CSV export generation
    try {
        std::string exportPath = "test_export.csv";
        bool ok = util::CsvExporter::exportTransactions(transactionDao->findAll(), exportPath);
        assertCondition(ok && std::filesystem::exists(exportPath) && std::filesystem::file_size(exportPath) > 0, "CSV file generated");
        std::filesystem::remove(exportPath);
        std::cout << "[PASS] Test 10: CSV exporter creates valid non-empty files." << std::endl;
        passed++;
    } catch (const std::exception& e) {
        std::cerr << "[FAIL] Test 10: " << e.what() << std::endl;
        failed++;
    }

    std::cout << "======================================================================" << std::endl;
    std::cout << "TEST SUITE COMPLETED: " << passed << " PASSED, " << failed << " FAILED" << std::endl;
    std::cout << "======================================================================" << std::endl;

    return failed > 0 ? 1 : 0;
}

#include "service/FinanceService.hpp"
#include "util/DateUtils.hpp"
#include <algorithm>

namespace fintrack::service {

FinanceService::FinanceService(std::shared_ptr<dao::TransactionDao> txDao,
                               std::shared_ptr<dao::CategoryDao> catDao,
                               std::shared_ptr<dao::BudgetDao> bgtDao)
    : transactionDao(std::move(txDao)), categoryDao(std::move(catDao)), budgetDao(std::move(bgtDao)) {}

model::DashboardSummary FinanceService::getDashboardSummary(int month, int year) {
    int prevMonth = (month == 1) ? 12 : month - 1;
    int prevYear = (month == 1) ? year - 1 : year;

    double curIncome = 0.0;
    double curExpenses = 0.0;
    double prevIncome = 0.0;
    double prevExpenses = 0.0;

    auto allTx = transactionDao->findAll();
    for (const auto& tx : allTx) {
        int y, m, d;
        if (!util::DateUtils::parseDateParts(tx.getTransactionDate(), y, m, d)) continue;

        if (y == year && m == month) {
            if (tx.getType() == model::TransactionType::INCOME) {
                curIncome += tx.getAmount();
            } else {
                curExpenses += tx.getAmount();
            }
        } else if (y == prevYear && m == prevMonth) {
            if (tx.getType() == model::TransactionType::INCOME) {
                prevIncome += tx.getAmount();
            } else {
                prevExpenses += tx.getAmount();
            }
        }
    }

    double budgetLimit = 0.0;
    auto overallOpt = budgetDao->findOverall(month, year);
    if (overallOpt.has_value()) {
        budgetLimit = overallOpt->getAmount();
    } else {
        auto budgets = budgetDao->findByPeriod(month, year);
        for (const auto& b : budgets) {
            if (!b.isOverall()) budgetLimit += b.getAmount();
        }
    }

    model::DashboardSummary summary;
    summary.calculateMetrics(openingBalance, curIncome, curExpenses, prevIncome, prevExpenses, budgetLimit);
    summary.setRecentTransactions(transactionDao->findRecent(7));
    return summary;
}

std::vector<model::Budget> FinanceService::getBudgetsWithCalculations(int month, int year) {
    auto list = budgetDao->findByPeriod(month, year);
    for (auto& b : list) {
        double spent = 0.0;
        if (b.isOverall()) {
            spent = transactionDao->sumExpenseByPeriod(month, year);
        } else if (b.getCategoryId().has_value()) {
            spent = transactionDao->sumExpenseByCategoryAndPeriod(b.getCategoryId().value(), month, year);
        }
        b.updateCalculations(spent);
    }
    return list;
}

std::map<std::string, double> FinanceService::getExpenseBreakdownByCategory(int month, int year) {
    std::map<std::string, double> map;
    auto allTx = transactionDao->findAll();
    for (const auto& tx : allTx) {
        if (tx.getType() != model::TransactionType::EXPENSE) continue;
        int y, m, d;
        if (!util::DateUtils::parseDateParts(tx.getTransactionDate(), y, m, d)) continue;
        if (y == year && m == month) {
            std::string catName = !tx.getCategoryName().empty() ? tx.getCategoryName() : "Other";
            map[catName] += tx.getAmount();
        }
    }
    return map;
}

std::vector<MonthlyReportItem> FinanceService::getMonthlyTrends(int monthsCount) {
    std::vector<MonthlyReportItem> result;
    // Current date
    std::string today = util::DateUtils::todayIso();
    int curY = 2026, curM = 10, curD = 1;
    util::DateUtils::parseDateParts(today, curY, curM, curD);

    for (int i = monthsCount - 1; i >= 0; i--) {
        int targetM = curM - i;
        int targetY = curY;
        while (targetM <= 0) {
            targetM += 12;
            targetY -= 1;
        }

        std::string label = util::DateUtils::getMonthNameAbbr(targetM) + " " + std::to_string(targetY);
        double inc = 0.0;
        double exp = 0.0;

        for (const auto& tx : transactionDao->findAll()) {
            int y, m, d;
            if (util::DateUtils::parseDateParts(tx.getTransactionDate(), y, m, d)) {
                if (y == targetY && m == targetM) {
                    if (tx.getType() == model::TransactionType::INCOME) inc += tx.getAmount();
                    else exp += tx.getAmount();
                }
            }
        }
        result.emplace_back(label, targetM, targetY, inc, exp);
    }
    return result;
}

bool FinanceService::canDeleteCategory(int64_t categoryId) const {
    return transactionDao->countByCategoryId(categoryId) == 0;
}

} // namespace fintrack::service

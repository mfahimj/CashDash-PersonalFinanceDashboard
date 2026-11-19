#pragma once

#include "model/DashboardSummary.hpp"
#include "model/Budget.hpp"
#include "model/Category.hpp"
#include "dao/TransactionDao.hpp"
#include "dao/CategoryDao.hpp"
#include "dao/BudgetDao.hpp"
#include <memory>
#include <string>
#include <vector>
#include <map>

namespace fintrack::service {

class MonthlyReportItem {
private:
    std::string monthLabel;
    int month;
    int year;
    double income;
    double expenses;
    double savings;

public:
    MonthlyReportItem(std::string monthLabel, int month, int year, double income, double expenses)
        : monthLabel(std::move(monthLabel)), month(month), year(year),
          income(income), expenses(expenses), savings(income - expenses) {}

    const std::string& getMonthLabel() const { return monthLabel; }
    int getMonth() const { return month; }
    int getYear() const { return year; }
    double getIncome() const { return income; }
    double getExpenses() const { return expenses; }
    double getSavings() const { return savings; }
};

class FinanceService {
private:
    std::shared_ptr<dao::TransactionDao> transactionDao;
    std::shared_ptr<dao::CategoryDao> categoryDao;
    std::shared_ptr<dao::BudgetDao> budgetDao;
    double openingBalance = 50000.00;

public:
    FinanceService(std::shared_ptr<dao::TransactionDao> txDao,
                   std::shared_ptr<dao::CategoryDao> catDao,
                   std::shared_ptr<dao::BudgetDao> bgtDao);

    model::DashboardSummary getDashboardSummary(int month, int year);
    std::vector<model::Budget> getBudgetsWithCalculations(int month, int year);
    std::map<std::string, double> getExpenseBreakdownByCategory(int month, int year);
    std::vector<MonthlyReportItem> getMonthlyTrends(int monthsCount = 6);

    bool canDeleteCategory(int64_t categoryId) const;

    double getOpeningBalance() const { return openingBalance; }
    void setOpeningBalance(double bal) { openingBalance = bal; }

    std::shared_ptr<dao::TransactionDao> getTransactionDao() const { return transactionDao; }
    std::shared_ptr<dao::CategoryDao> getCategoryDao() const { return categoryDao; }
    std::shared_ptr<dao::BudgetDao> getBudgetDao() const { return budgetDao; }
};

} // namespace fintrack::service

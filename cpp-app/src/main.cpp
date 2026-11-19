#include "dao/CategoryDao.hpp"
#include "dao/TransactionDao.hpp"
#include "dao/BudgetDao.hpp"
#include "service/FinanceService.hpp"
#include "web/HttpServer.hpp"
#include <iostream>
#include <memory>
#include <string>

using namespace fintrack;

int main(int argc, char* argv[]) {
    int port = 8080;
    if (argc > 1) {
        try {
            port = std::stoi(argv[1]);
        } catch (...) {
            port = 8080;
        }
    }

    std::cout << "Initializing FinTrack Pro C++ Native Backend..." << std::endl;

    auto categoryDao = std::make_shared<dao::CategoryDao>("data_categories.dat");
    auto transactionDao = std::make_shared<dao::TransactionDao>(categoryDao, "data_transactions.dat");
    auto budgetDao = std::make_shared<dao::BudgetDao>(categoryDao, "data_budgets.dat");
    auto financeService = std::make_shared<service::FinanceService>(transactionDao, categoryDao, budgetDao);

    std::cout << "Loaded " << categoryDao->count() << " categories." << std::endl;
    std::cout << "Loaded " << transactionDao->count() << " transactions." << std::endl;
    std::cout << "Loaded " << budgetDao->count() << " budgets." << std::endl;

    web::HttpServer server(port, financeService, "web");
    server.start();

    return 0;
}

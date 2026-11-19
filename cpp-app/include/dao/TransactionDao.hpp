#pragma once

#include "model/Transaction.hpp"
#include "dao/CategoryDao.hpp"
#include <vector>
#include <map>
#include <optional>
#include <string>
#include <mutex>
#include <memory>

namespace fintrack::dao {

class TransactionDao {
private:
    std::string dataFilePath;
    std::shared_ptr<CategoryDao> categoryDao;
    std::map<int64_t, model::Transaction> memoryStore;
    int64_t idSequence = 100;
    mutable std::mutex mutex_;

    void seedDemoTransactions();
    void addInitialTx(int64_t id, int64_t userId, const std::string& desc, double amt,
                      model::TransactionType type, int64_t catId, const std::string& date,
                      const std::string& payMethod, const std::string& notes);

public:
    explicit TransactionDao(std::shared_ptr<CategoryDao> categoryDao,
                            std::string dataFilePath = "data_transactions.dat");

    bool loadFromFile();
    void saveToFile();

    std::vector<model::Transaction> findAll() const;
    std::optional<model::Transaction> findById(int64_t id) const;
    std::vector<model::Transaction> findRecent(size_t limit) const;

    std::vector<model::Transaction> findByFilters(
        std::optional<model::TransactionType> type = std::nullopt,
        std::optional<int64_t> categoryId = std::nullopt,
        const std::string& startDate = "",
        const std::string& endDate = "",
        const std::string& paymentMethod = "",
        const std::string& searchKeyword = "") const;

    double sumExpenseByPeriod(int month, int year) const;
    double sumExpenseByCategoryAndPeriod(int64_t categoryId, int month, int year) const;
    int countByCategoryId(int64_t categoryId) const;

    model::Transaction save(model::Transaction tx);
    bool update(const model::Transaction& tx);
    bool deleteById(int64_t id);
    size_t count() const;
};

} // namespace fintrack::dao

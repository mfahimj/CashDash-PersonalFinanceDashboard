#pragma once

#include "model/Budget.hpp"
#include "dao/CategoryDao.hpp"
#include <vector>
#include <map>
#include <optional>
#include <string>
#include <mutex>
#include <memory>

namespace fintrack::dao {

class BudgetDao {
private:
    std::string dataFilePath;
    std::shared_ptr<CategoryDao> categoryDao;
    std::map<int64_t, model::Budget> memoryStore;
    int64_t idSequence = 10;
    mutable std::mutex mutex_;

    void seedDefaultBudgets();

public:
    explicit BudgetDao(std::shared_ptr<CategoryDao> categoryDao,
                       std::string dataFilePath = "data_budgets.dat");

    bool loadFromFile();
    void saveToFile();

    std::vector<model::Budget> findByPeriod(int month, int year) const;
    std::optional<model::Budget> findOverall(int month, int year) const;
    std::optional<model::Budget> findByCategoryAndPeriod(int64_t categoryId, int month, int year) const;
    std::optional<model::Budget> findById(int64_t id) const;

    model::Budget save(model::Budget budget);
    bool update(const model::Budget& budget);
    bool deleteById(int64_t id);
    size_t count() const;
};

} // namespace fintrack::dao

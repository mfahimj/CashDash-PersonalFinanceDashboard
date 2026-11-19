#pragma once

#include "model/Category.hpp"
#include <vector>
#include <map>
#include <optional>
#include <string>
#include <mutex>

namespace fintrack::dao {

class CategoryDao {
private:
    std::string dataFilePath;
    std::map<int64_t, model::Category> memoryStore;
    int64_t idSequence = 100;
    mutable std::mutex mutex_;

    void seedDefaultCategories();

public:
    explicit CategoryDao(std::string dataFilePath = "data_categories.dat");

    bool loadFromFile();
    void saveToFile();

    std::vector<model::Category> findAll() const;
    std::optional<model::Category> findById(int64_t id) const;
    std::optional<model::Category> findByName(const std::string& name) const;

    model::Category save(model::Category category);
    bool update(const model::Category& category);
    bool deleteById(int64_t id);
    size_t count() const;
};

} // namespace fintrack::dao

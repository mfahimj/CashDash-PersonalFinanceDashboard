#include "dao/CategoryDao.hpp"
#include "util/DateUtils.hpp"
#include <fstream>
#include <sstream>
#include <iostream>

namespace fintrack::dao {

CategoryDao::CategoryDao(std::string dataFilePath)
    : dataFilePath(std::move(dataFilePath)) {
    if (!loadFromFile()) {
        seedDefaultCategories();
    }
}

void CategoryDao::seedDefaultCategories() {
    std::lock_guard<std::mutex> lock(mutex_);
    memoryStore.clear();

    auto add = [this](int64_t id, int64_t uid, const std::string& name, model::TransactionType type) {
        memoryStore[id] = model::Category(id, uid, name, type, util::DateUtils::currentIsoTimestamp());
        if (id > idSequence) idSequence = id;
    };

    add(1, 1, "Food and Dining", model::TransactionType::EXPENSE);
    add(2, 1, "Transportation", model::TransactionType::EXPENSE);
    add(3, 1, "Shopping", model::TransactionType::EXPENSE);
    add(4, 1, "Bills and Utilities", model::TransactionType::EXPENSE);
    add(5, 1, "Healthcare", model::TransactionType::EXPENSE);
    add(6, 1, "Education", model::TransactionType::EXPENSE);
    add(7, 1, "Entertainment", model::TransactionType::EXPENSE);
    add(8, 1, "Housing", model::TransactionType::EXPENSE);
    add(9, 1, "Travel", model::TransactionType::EXPENSE);
    add(10, 1, "Other Expense", model::TransactionType::EXPENSE);

    add(11, 1, "Salary", model::TransactionType::INCOME);
    add(12, 1, "Freelancing", model::TransactionType::INCOME);
    add(13, 1, "Business", model::TransactionType::INCOME);
    add(14, 1, "Investments", model::TransactionType::INCOME);
    add(15, 1, "Gifts", model::TransactionType::INCOME);
    add(16, 1, "Other Income", model::TransactionType::INCOME);

    saveToFile();
}

bool CategoryDao::loadFromFile() {
    std::lock_guard<std::mutex> lock(mutex_);
    std::ifstream file(dataFilePath);
    if (!file.is_open()) return false;

    memoryStore.clear();
    std::string line;
    int64_t maxId = 0;

    while (std::getline(file, line)) {
        if (line.empty()) continue;
        std::vector<std::string> parts;
        std::string part;
        size_t start = 0;
        size_t pos;
        while ((pos = line.find("||", start)) != std::string::npos) {
            parts.push_back(line.substr(start, pos - start));
            start = pos + 2;
        }
        parts.push_back(line.substr(start));

        if (parts.size() >= 4) {
            try {
                int64_t id = std::stoll(parts[0]);
                int64_t uid = std::stoll(parts[1]);
                std::string name = parts[2];
                model::TransactionType type = model::transactionTypeFromString(parts[3]);
                std::string createdAt = parts.size() >= 5 ? parts[4] : util::DateUtils::currentIsoTimestamp();

                memoryStore[id] = model::Category(id, uid, name, type, createdAt);
                if (id > maxId) maxId = id;
            } catch (...) {}
        }
    }

    if (maxId > idSequence) idSequence = maxId;
    return !memoryStore.empty();
}

void CategoryDao::saveToFile() {
    std::ofstream file(dataFilePath);
    if (!file.is_open()) return;

    for (const auto& [id, cat] : memoryStore) {
        file << (cat.getId().has_value() ? cat.getId().value() : id) << "||"
             << cat.getUserId() << "||"
             << cat.getName() << "||"
             << model::transactionTypeToString(cat.getType()) << "||"
             << cat.getCreatedAt() << "\n";
    }
}

std::vector<model::Category> CategoryDao::findAll() const {
    std::lock_guard<std::mutex> lock(mutex_);
    std::vector<model::Category> list;
    list.reserve(memoryStore.size());
    for (const auto& [id, cat] : memoryStore) {
        list.push_back(cat);
    }
    return list;
}

std::optional<model::Category> CategoryDao::findById(int64_t id) const {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        return it->second;
    }
    return std::nullopt;
}

std::optional<model::Category> CategoryDao::findByName(const std::string& name) const {
    std::lock_guard<std::mutex> lock(mutex_);
    for (const auto& [id, cat] : memoryStore) {
        if (cat.getName() == name) {
            return cat;
        }
    }
    return std::nullopt;
}

model::Category CategoryDao::save(model::Category category) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!category.getId().has_value() || category.getId().value() <= 0) {
        category.setId(++idSequence);
    }
    if (category.getCreatedAt().empty()) {
        category.setCreatedAt(util::DateUtils::currentIsoTimestamp());
    }
    memoryStore[category.getId().value()] = category;
    saveToFile();
    return category;
}

bool CategoryDao::update(const model::Category& category) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!category.getId().has_value()) return false;
    int64_t id = category.getId().value();
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        memoryStore[id] = category;
        saveToFile();
        return true;
    }
    return false;
}

bool CategoryDao::deleteById(int64_t id) {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        memoryStore.erase(it);
        saveToFile();
        return true;
    }
    return false;
}

size_t CategoryDao::count() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return memoryStore.size();
}

} // namespace fintrack::dao

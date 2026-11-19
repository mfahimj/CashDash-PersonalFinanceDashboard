#include "dao/BudgetDao.hpp"
#include "util/DateUtils.hpp"
#include <fstream>
#include <sstream>
#include <iomanip>

namespace fintrack::dao {

BudgetDao::BudgetDao(std::shared_ptr<CategoryDao> categoryDao, std::string dataFilePath)
    : dataFilePath(std::move(dataFilePath)), categoryDao(std::move(categoryDao)) {
    if (!loadFromFile()) {
        seedDefaultBudgets();
    }
}

void BudgetDao::seedDefaultBudgets() {
    std::lock_guard<std::mutex> lock(mutex_);
    memoryStore.clear();

    auto add = [this](int64_t id, int64_t uid, std::optional<int64_t> catId, double amt, int m, int y) {
        std::string catName = "Overall Budget";
        if (catId.has_value() && catId.value() > 0 && categoryDao) {
            auto c = categoryDao->findById(catId.value());
            if (c.has_value()) catName = c->getName();
        }
        model::Budget b(id, uid, catId, amt, m, y,
                        util::DateUtils::currentIsoTimestamp(), util::DateUtils::currentIsoTimestamp());
        b.setCategoryName(catName);
        memoryStore[id] = b;
        if (id > idSequence) idSequence = id;
    };

    add(1, 1, std::nullopt, 65000.00, 10, 2026);
    add(2, 1, 1, 12000.00, 10, 2026);
    add(3, 1, 2, 7000.00, 10, 2026);
    add(4, 1, 3, 8000.00, 10, 2026);
    add(5, 1, 4, 5000.00, 10, 2026);
    add(6, 1, 5, 4000.00, 10, 2026);
    add(7, 1, 6, 3000.00, 10, 2026);
    add(8, 1, 7, 3500.00, 10, 2026);
    add(9, 1, 8, 22000.00, 10, 2026);

    saveToFile();
}

bool BudgetDao::loadFromFile() {
    std::lock_guard<std::mutex> lock(mutex_);
    std::ifstream file(dataFilePath);
    if (!file.is_open()) return false;

    memoryStore.clear();
    std::string line;
    int64_t maxId = 0;

    while (std::getline(file, line)) {
        if (line.empty()) continue;
        std::vector<std::string> parts;
        size_t start = 0;
        size_t pos;
        while ((pos = line.find("||", start)) != std::string::npos) {
            parts.push_back(line.substr(start, pos - start));
            start = pos + 2;
        }
        parts.push_back(line.substr(start));

        if (parts.size() >= 6) {
            try {
                int64_t id = std::stoll(parts[0]);
                int64_t uid = std::stoll(parts[1]);
                std::optional<int64_t> catId = std::nullopt;
                if (!parts[2].empty() && parts[2] != "0" && parts[2] != "null") {
                    catId = std::stoll(parts[2]);
                }
                double amt = std::stod(parts[3]);
                int month = std::stoi(parts[4]);
                int year = std::stoi(parts[5]);
                std::string createdAt = parts.size() >= 7 ? parts[6] : util::DateUtils::currentIsoTimestamp();
                std::string updatedAt = parts.size() >= 8 ? parts[7] : util::DateUtils::currentIsoTimestamp();

                model::Budget b(id, uid, catId, amt, month, year, createdAt, updatedAt);
                std::string cname = "Overall Budget";
                if (catId.has_value() && categoryDao) {
                    auto c = categoryDao->findById(catId.value());
                    if (c.has_value()) cname = c->getName();
                }
                b.setCategoryName(cname);
                memoryStore[id] = b;
                if (id > maxId) maxId = id;
            } catch (...) {}
        }
    }

    if (maxId > idSequence) idSequence = maxId;
    return !memoryStore.empty();
}

void BudgetDao::saveToFile() {
    std::ofstream file(dataFilePath);
    if (!file.is_open()) return;

    for (const auto& [id, b] : memoryStore) {
        std::ostringstream amtStream;
        amtStream << std::fixed << std::setprecision(2) << b.getAmount();

        std::string catIdStr = "0";
        if (b.getCategoryId().has_value() && b.getCategoryId().value() > 0) {
            catIdStr = std::to_string(b.getCategoryId().value());
        }

        file << (b.getId().has_value() ? b.getId().value() : id) << "||"
             << b.getUserId() << "||"
             << catIdStr << "||"
             << amtStream.str() << "||"
             << b.getMonth() << "||"
             << b.getYear() << "||"
             << (!b.getCreatedAt().empty() ? b.getCreatedAt() : util::DateUtils::currentIsoTimestamp()) << "||"
             << (!b.getUpdatedAt().empty() ? b.getUpdatedAt() : util::DateUtils::currentIsoTimestamp()) << "\n";
    }
}

std::vector<model::Budget> BudgetDao::findByPeriod(int month, int year) const {
    std::lock_guard<std::mutex> lock(mutex_);
    std::vector<model::Budget> list;
    for (const auto& [id, b] : memoryStore) {
        if (b.getMonth() == month && b.getYear() == year) {
            list.push_back(b);
        }
    }
    return list;
}

std::optional<model::Budget> BudgetDao::findOverall(int month, int year) const {
    std::lock_guard<std::mutex> lock(mutex_);
    for (const auto& [id, b] : memoryStore) {
        if (b.getMonth() == month && b.getYear() == year && b.isOverall()) {
            return b;
        }
    }
    return std::nullopt;
}

std::optional<model::Budget> BudgetDao::findByCategoryAndPeriod(int64_t categoryId, int month, int year) const {
    std::lock_guard<std::mutex> lock(mutex_);
    for (const auto& [id, b] : memoryStore) {
        if (b.getMonth() == month && b.getYear() == year &&
            b.getCategoryId().has_value() && b.getCategoryId().value() == categoryId) {
            return b;
        }
    }
    return std::nullopt;
}

std::optional<model::Budget> BudgetDao::findById(int64_t id) const {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        return it->second;
    }
    return std::nullopt;
}

model::Budget BudgetDao::save(model::Budget budget) {
    std::lock_guard<std::mutex> lock(mutex_);
    // Check if budget already exists for this category/month/year
    for (auto& [id, existing] : memoryStore) {
        if (existing.getMonth() == budget.getMonth() &&
            existing.getYear() == budget.getYear() &&
            existing.getCategoryId() == budget.getCategoryId()) {
            existing.setAmount(budget.getAmount());
            existing.setUpdatedAt(util::DateUtils::currentIsoTimestamp());
            saveToFile();
            return existing;
        }
    }

    if (!budget.getId().has_value() || budget.getId().value() <= 0) {
        budget.setId(++idSequence);
    }
    if (budget.getCreatedAt().empty()) {
        budget.setCreatedAt(util::DateUtils::currentIsoTimestamp());
    }
    budget.setUpdatedAt(util::DateUtils::currentIsoTimestamp());

    std::string catName = "Overall Budget";
    if (budget.getCategoryId().has_value() && categoryDao) {
        auto c = categoryDao->findById(budget.getCategoryId().value());
        if (c.has_value()) catName = c->getName();
    }
    budget.setCategoryName(catName);

    memoryStore[budget.getId().value()] = budget;
    saveToFile();
    return budget;
}

bool BudgetDao::update(const model::Budget& budget) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!budget.getId().has_value()) return false;
    int64_t id = budget.getId().value();
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        model::Budget updated = budget;
        updated.setUpdatedAt(util::DateUtils::currentIsoTimestamp());
        memoryStore[id] = updated;
        saveToFile();
        return true;
    }
    return false;
}

bool BudgetDao::deleteById(int64_t id) {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        memoryStore.erase(it);
        saveToFile();
        return true;
    }
    return false;
}

size_t BudgetDao::count() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return memoryStore.size();
}

} // namespace fintrack::dao

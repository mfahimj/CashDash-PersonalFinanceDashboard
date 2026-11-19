#include "dao/TransactionDao.hpp"
#include "util/DateUtils.hpp"
#include <fstream>
#include <sstream>
#include <algorithm>
#include <iomanip>
#include <iostream>

namespace fintrack::dao {

TransactionDao::TransactionDao(std::shared_ptr<CategoryDao> categoryDao, std::string dataFilePath)
    : dataFilePath(std::move(dataFilePath)), categoryDao(std::move(categoryDao)) {
    if (!loadFromFile()) {
        seedDemoTransactions();
    }
}

void TransactionDao::addInitialTx(int64_t id, int64_t userId, const std::string& desc, double amt,
                                  model::TransactionType type, int64_t catId, const std::string& date,
                                  const std::string& payMethod, const std::string& notes) {
    std::string catName = "Other";
    if (categoryDao) {
        auto cat = categoryDao->findById(catId);
        if (cat.has_value()) catName = cat->getName();
    }
    model::Transaction tx(id, userId, desc, amt, type, catId, date, payMethod, notes,
                          util::DateUtils::currentIsoTimestamp(), util::DateUtils::currentIsoTimestamp());
    tx.setCategoryName(catName);
    memoryStore[id] = tx;
    if (id > idSequence) idSequence = id;
}

void TransactionDao::seedDemoTransactions() {
    std::lock_guard<std::mutex> lock(mutex_);
    memoryStore.clear();

    // October 2026 Transactions
    addInitialTx(1, 1, "Monthly Software Engineer Salary", 85000.00, model::TransactionType::INCOME, 11, "2026-10-01", "Bank Transfer", "Primary corporate monthly payroll credit");
    addInitialTx(2, 1, "UI/UX Design Contract for Fintech Client", 24500.00, model::TransactionType::INCOME, 12, "2026-10-05", "Bank Transfer", "Milestone 2 payment received");
    addInitialTx(3, 1, "Mutual Funds Dividend Payout", 3800.00, model::TransactionType::INCOME, 14, "2026-10-12", "Direct Deposit", "Q3 equity fund returns");
    addInitialTx(4, 1, "Apartment Monthly Rent", 22000.00, model::TransactionType::EXPENSE, 8, "2026-10-02", "Net Banking", "October residential lease payment");
    addInitialTx(5, 1, "Whole Foods & Supermarket Grocery Haul", 4850.00, model::TransactionType::EXPENSE, 1, "2026-10-03", "Credit Card", "Weekly organic produce, dairy, and pantry essentials");
    addInitialTx(6, 1, "Electricity & High-Speed Fiber Internet", 3450.00, model::TransactionType::EXPENSE, 4, "2026-10-06", "UPI", "Power bill and monthly 300 Mbps broadband");
    addInitialTx(7, 1, "Metro Commuter Pass & Fuel", 2800.00, model::TransactionType::EXPENSE, 2, "2026-10-08", "Debit Card", "Monthly transit card recharge and vehicle fuel");
    addInitialTx(8, 1, "Weekend Dinner with Team", 3200.00, model::TransactionType::EXPENSE, 1, "2026-10-10", "Credit Card", "Italian bistro dinner with team members");
    addInitialTx(9, 1, "Annual Health Checkup & Prescription", 2400.00, model::TransactionType::EXPENSE, 5, "2026-10-15", "UPI", "Routine diagnostics and vitamins");
    addInitialTx(10, 1, "Online Technical Books & Cloud Course", 1850.00, model::TransactionType::EXPENSE, 6, "2026-10-18", "Credit Card", "Advanced System Design textbook & lab subscription");
    addInitialTx(11, 1, "Streaming Subscriptions & Movie Tickets", 1499.00, model::TransactionType::EXPENSE, 7, "2026-10-20", "UPI", "Netflix, Spotify & weekend IMAX movie");
    addInitialTx(12, 1, "Autumn Apparel & Sneakers", 5600.00, model::TransactionType::EXPENSE, 3, "2026-10-22", "Credit Card", "Work casual clothing and footwear");
    addInitialTx(13, 1, "Artisan Coffee & Work Snacks", 1250.00, model::TransactionType::EXPENSE, 1, "2026-10-25", "Cash", "Local roastery coffee and cafe workday lunch");
    addInitialTx(14, 1, "Freelance Tech Consultation", 15000.00, model::TransactionType::INCOME, 12, "2026-10-26", "Bank Transfer", "Architecture review consultation session");
    addInitialTx(15, 1, "Weekend Road Trip Fuel & Tolls", 3100.00, model::TransactionType::EXPENSE, 9, "2026-10-27", "Fastag / UPI", "Highway toll fees and inter-city petrol");

    // September 2026 Historical Transactions
    addInitialTx(16, 1, "September Primary Salary", 85000.00, model::TransactionType::INCOME, 11, "2026-09-01", "Bank Transfer", "Corporate payroll");
    addInitialTx(17, 1, "Mobile App Dev Freelance Project", 32000.00, model::TransactionType::INCOME, 12, "2026-09-10", "Bank Transfer", "Final project handover payout");
    addInitialTx(18, 1, "Stock Market Capital Gains", 7500.00, model::TransactionType::INCOME, 14, "2026-09-22", "Direct Deposit", "Quarterly rebalancing profit");
    addInitialTx(19, 1, "Apartment Monthly Rent", 22000.00, model::TransactionType::EXPENSE, 8, "2026-09-02", "Net Banking", "September lease");
    addInitialTx(20, 1, "Monthly Groceries & Essentials", 8900.00, model::TransactionType::EXPENSE, 1, "2026-09-15", "Credit Card", "Hypermarket supply run");
    addInitialTx(21, 1, "Car Service & Regular Maintenance", 6800.00, model::TransactionType::EXPENSE, 2, "2026-09-18", "Debit Card", "Oil change, filter replacement, wheel alignment");
    addInitialTx(22, 1, "Utility Bills (Water, Gas, Electricity)", 3850.00, model::TransactionType::EXPENSE, 4, "2026-09-05", "UPI", "Monthly municipal utilities");
    addInitialTx(23, 1, "Birthday Celebration Dinner", 4500.00, model::TransactionType::EXPENSE, 1, "2026-09-20", "Credit Card", "Family celebratory dinner");
    addInitialTx(24, 1, "Festival Shopping & Gifts", 7200.00, model::TransactionType::EXPENSE, 3, "2026-09-25", "Credit Card", "Home decor and gifts for relatives");

    // August 2026 Historical Transactions
    addInitialTx(25, 1, "August Primary Salary", 85000.00, model::TransactionType::INCOME, 11, "2026-08-01", "Bank Transfer", "Corporate payroll");
    addInitialTx(26, 1, "Consulting Advisory Retainer", 20000.00, model::TransactionType::INCOME, 13, "2026-08-15", "Bank Transfer", "Monthly business advisory fee");
    addInitialTx(27, 1, "Apartment Monthly Rent", 22000.00, model::TransactionType::EXPENSE, 8, "2026-08-02", "Net Banking", "August lease");
    addInitialTx(28, 1, "Groceries & Dining", 9400.00, model::TransactionType::EXPENSE, 1, "2026-08-14", "Credit Card", "Supermarket and food delivery");
    addInitialTx(29, 1, "Flight Tickets & Hotel for Mini Vacation", 18500.00, model::TransactionType::EXPENSE, 9, "2026-08-20", "Credit Card", "Monsoon weekend hill station getaway");
    addInitialTx(30, 1, "Medical Health Insurance Premium", 6500.00, model::TransactionType::EXPENSE, 5, "2026-08-10", "Net Banking", "Annual family floater health insurance installment");

    saveToFile();
}

bool TransactionDao::loadFromFile() {
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

        if (parts.size() >= 8) {
            try {
                int64_t id = std::stoll(parts[0]);
                int64_t uid = std::stoll(parts[1]);
                std::string desc = parts[2];
                double amt = std::stod(parts[3]);
                model::TransactionType type = model::transactionTypeFromString(parts[4]);
                int64_t catId = std::stoll(parts[5]);
                std::string date = parts[6];
                std::string method = parts[7];
                std::string notes = parts.size() >= 9 ? parts[8] : "";
                std::string createdAt = parts.size() >= 10 ? parts[9] : util::DateUtils::currentIsoTimestamp();
                std::string updatedAt = parts.size() >= 11 ? parts[10] : util::DateUtils::currentIsoTimestamp();

                model::Transaction tx(id, uid, desc, amt, type, catId, date, method, notes, createdAt, updatedAt);
                if (categoryDao) {
                    auto c = categoryDao->findById(catId);
                    if (c.has_value()) tx.setCategoryName(c->getName());
                }
                memoryStore[id] = tx;
                if (id > maxId) maxId = id;
            } catch (...) {}
        }
    }

    if (maxId > idSequence) idSequence = maxId;
    return !memoryStore.empty();
}

void TransactionDao::saveToFile() {
    std::ofstream file(dataFilePath);
    if (!file.is_open()) return;

    for (const auto& [id, tx] : memoryStore) {
        std::ostringstream amtStream;
        amtStream << std::fixed << std::setprecision(2) << tx.getAmount();

        std::string safeNotes = tx.getNotes();
        std::replace(safeNotes.begin(), safeNotes.end(), '\n', ' ');

        file << (tx.getId().has_value() ? tx.getId().value() : id) << "||"
             << tx.getUserId() << "||"
             << tx.getDescription() << "||"
             << amtStream.str() << "||"
             << model::transactionTypeToString(tx.getType()) << "||"
             << tx.getCategoryId() << "||"
             << (!tx.getTransactionDate().empty() ? tx.getTransactionDate() : util::DateUtils::todayIso()) << "||"
             << (!tx.getPaymentMethod().empty() ? tx.getPaymentMethod() : "Other") << "||"
             << safeNotes << "||"
             << (!tx.getCreatedAt().empty() ? tx.getCreatedAt() : util::DateUtils::currentIsoTimestamp()) << "||"
             << (!tx.getUpdatedAt().empty() ? tx.getUpdatedAt() : util::DateUtils::currentIsoTimestamp()) << "\n";
    }
}

std::vector<model::Transaction> TransactionDao::findAll() const {
    std::lock_guard<std::mutex> lock(mutex_);
    std::vector<model::Transaction> list;
    list.reserve(memoryStore.size());
    for (const auto& [id, tx] : memoryStore) {
        list.push_back(tx);
    }
    std::sort(list.begin(), list.end(), [](const model::Transaction& a, const model::Transaction& b) {
        if (a.getTransactionDate() != b.getTransactionDate()) {
            return a.getTransactionDate() > b.getTransactionDate();
        }
        return a.getId().value_or(0) > b.getId().value_or(0);
    });
    return list;
}

std::optional<model::Transaction> TransactionDao::findById(int64_t id) const {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        return it->second;
    }
    return std::nullopt;
}

std::vector<model::Transaction> TransactionDao::findRecent(size_t limit) const {
    auto all = findAll();
    if (all.size() > limit) {
        all.resize(limit);
    }
    return all;
}

std::vector<model::Transaction> TransactionDao::findByFilters(
    std::optional<model::TransactionType> type,
    std::optional<int64_t> categoryId,
    const std::string& startDate,
    const std::string& endDate,
    const std::string& paymentMethod,
    const std::string& searchKeyword) const {

    auto all = findAll();
    std::vector<model::Transaction> filtered;

    std::string kwLower = searchKeyword;
    std::transform(kwLower.begin(), kwLower.end(), kwLower.begin(), ::tolower);

    for (const auto& tx : all) {
        if (type.has_value() && tx.getType() != type.value()) continue;
        if (categoryId.has_value() && tx.getCategoryId() != categoryId.value()) continue;
        if (!startDate.empty() && tx.getTransactionDate() < startDate) continue;
        if (!endDate.empty() && tx.getTransactionDate() > endDate) continue;
        if (!paymentMethod.empty() && tx.getPaymentMethod() != paymentMethod) continue;

        if (!kwLower.empty()) {
            std::string descLower = tx.getDescription();
            std::transform(descLower.begin(), descLower.end(), descLower.begin(), ::tolower);
            std::string catLower = tx.getCategoryName();
            std::transform(catLower.begin(), catLower.end(), catLower.begin(), ::tolower);
            std::string notesLower = tx.getNotes();
            std::transform(notesLower.begin(), notesLower.end(), notesLower.begin(), ::tolower);
            std::string payLower = tx.getPaymentMethod();
            std::transform(payLower.begin(), payLower.end(), payLower.begin(), ::tolower);

            if (descLower.find(kwLower) == std::string::npos &&
                catLower.find(kwLower) == std::string::npos &&
                notesLower.find(kwLower) == std::string::npos &&
                payLower.find(kwLower) == std::string::npos) {
                continue;
            }
        }

        filtered.push_back(tx);
    }
    return filtered;
}

double TransactionDao::sumExpenseByPeriod(int month, int year) const {
    std::lock_guard<std::mutex> lock(mutex_);
    double total = 0.0;
    for (const auto& [id, tx] : memoryStore) {
        if (tx.getType() != model::TransactionType::EXPENSE) continue;
        int y, m, d;
        if (util::DateUtils::parseDateParts(tx.getTransactionDate(), y, m, d)) {
            if (y == year && m == month) {
                total += tx.getAmount();
            }
        }
    }
    return total;
}

double TransactionDao::sumExpenseByCategoryAndPeriod(int64_t categoryId, int month, int year) const {
    std::lock_guard<std::mutex> lock(mutex_);
    double total = 0.0;
    for (const auto& [id, tx] : memoryStore) {
        if (tx.getType() != model::TransactionType::EXPENSE) continue;
        if (tx.getCategoryId() != categoryId) continue;
        int y, m, d;
        if (util::DateUtils::parseDateParts(tx.getTransactionDate(), y, m, d)) {
            if (y == year && m == month) {
                total += tx.getAmount();
            }
        }
    }
    return total;
}

int TransactionDao::countByCategoryId(int64_t categoryId) const {
    std::lock_guard<std::mutex> lock(mutex_);
    int count = 0;
    for (const auto& [id, tx] : memoryStore) {
        if (tx.getCategoryId() == categoryId) {
            count++;
        }
    }
    return count;
}

model::Transaction TransactionDao::save(model::Transaction tx) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!tx.getId().has_value() || tx.getId().value() <= 0) {
        tx.setId(++idSequence);
    }
    if (tx.getCreatedAt().empty()) {
        tx.setCreatedAt(util::DateUtils::currentIsoTimestamp());
    }
    tx.setUpdatedAt(util::DateUtils::currentIsoTimestamp());

    if (categoryDao) {
        auto cat = categoryDao->findById(tx.getCategoryId());
        if (cat.has_value()) {
            tx.setCategoryName(cat->getName());
        }
    }

    memoryStore[tx.getId().value()] = tx;
    saveToFile();
    return tx;
}

bool TransactionDao::update(const model::Transaction& tx) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!tx.getId().has_value()) return false;
    int64_t id = tx.getId().value();
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        model::Transaction updated = tx;
        updated.setUpdatedAt(util::DateUtils::currentIsoTimestamp());
        if (categoryDao) {
            auto cat = categoryDao->findById(updated.getCategoryId());
            if (cat.has_value()) {
                updated.setCategoryName(cat->getName());
            }
        }
        memoryStore[id] = updated;
        saveToFile();
        return true;
    }
    return false;
}

bool TransactionDao::deleteById(int64_t id) {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = memoryStore.find(id);
    if (it != memoryStore.end()) {
        memoryStore.erase(it);
        saveToFile();
        return true;
    }
    return false;
}

size_t TransactionDao::count() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return memoryStore.size();
}

} // namespace fintrack::dao

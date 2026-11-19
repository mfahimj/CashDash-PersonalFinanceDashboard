#pragma once

#include "model/Transaction.hpp"
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include <iomanip>

namespace fintrack::util {

class CsvExporter {
public:
    static std::string escapeCsv(const std::string& field) {
        std::string escaped = field;
        size_t pos = 0;
        while ((pos = escaped.find('"', pos)) != std::string::npos) {
            escaped.replace(pos, 1, "\"\"");
            pos += 2;
        }
        return escaped;
    }

    static bool exportTransactions(const std::vector<model::Transaction>& transactions, const std::string& destinationPath) {
        std::ofstream file(destinationPath);
        if (!file.is_open()) return false;

        file << "Transaction ID,Description,Date,Category,Type,Payment Method,Amount,Notes\n";
        for (const auto& tx : transactions) {
            file << (tx.getId().has_value() ? tx.getId().value() : 0) << ","
                 << "\"" << escapeCsv(tx.getDescription()) << "\","
                 << tx.getTransactionDate() << ","
                 << "\"" << escapeCsv(!tx.getCategoryName().empty() ? tx.getCategoryName() : "Uncategorized") << "\","
                 << model::transactionTypeToString(tx.getType()) << ","
                 << "\"" << escapeCsv(tx.getPaymentMethod()) << "\","
                 << std::fixed << std::setprecision(2) << tx.getAmount() << ","
                 << "\"" << escapeCsv(tx.getNotes()) << "\"\n";
        }
        return true;
    }
};

} // namespace fintrack::util

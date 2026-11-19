#pragma once

#include <string>
#include <sstream>
#include <iomanip>
#include <cmath>

namespace fintrack::util {

class CurrencyFormatter {
private:
    inline static std::string currencySymbol = "₹";
    inline static std::string currencyCode = "INR";

public:
    static void setCurrency(const std::string& symbol, const std::string& code) {
        currencySymbol = symbol;
        currencyCode = code;
    }

    static const std::string& getCurrencySymbol() {
        return currencySymbol;
    }

    static const std::string& getCurrencyCode() {
        return currencyCode;
    }

    static std::string format(double amount) {
        std::ostringstream oss;
        oss << currencySymbol << " " << std::fixed << std::setprecision(2) << amount;
        return oss.str();
    }

    static std::string formatWithoutSymbol(double amount) {
        std::ostringstream oss;
        oss << std::fixed << std::setprecision(2) << amount;
        return oss.str();
    }

    static std::string formatSigned(double amount, bool isIncome) {
        std::ostringstream oss;
        oss << (isIncome ? "+ " : "- ") << currencySymbol << " "
            << std::fixed << std::setprecision(2) << std::abs(amount);
        return oss.str();
    }
};

} // namespace fintrack::util

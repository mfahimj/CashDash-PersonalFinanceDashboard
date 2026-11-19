#pragma once

#include <string>
#include <sstream>
#include <iomanip>
#include <chrono>
#include <ctime>

namespace fintrack::util {

class DateUtils {
public:
    static std::string todayIso() {
        auto now = std::chrono::system_clock::now();
        std::time_t t = std::chrono::system_clock::to_time_t(now);
        std::tm tm{};
#ifdef _WIN32
        localtime_s(&tm, &t);
#else
        localtime_r(&t, &tm);
#endif
        std::ostringstream oss;
        oss << std::put_time(&tm, "%Y-%m-%d");
        return oss.str();
    }

    static std::string currentIsoTimestamp() {
        auto now = std::chrono::system_clock::now();
        std::time_t t = std::chrono::system_clock::to_time_t(now);
        std::tm tm{};
#ifdef _WIN32
        localtime_s(&tm, &t);
#else
        localtime_r(&t, &tm);
#endif
        std::ostringstream oss;
        oss << std::put_time(&tm, "%Y-%m-%dT%H:%M:%S");
        return oss.str();
    }

    static bool parseDateParts(const std::string& dateStr, int& outYear, int& outMonth, int& outDay) {
        if (dateStr.length() < 10) return false;
        try {
            outYear = std::stoi(dateStr.substr(0, 4));
            outMonth = std::stoi(dateStr.substr(5, 2));
            outDay = std::stoi(dateStr.substr(8, 2));
            return true;
        } catch (...) {
            return false;
        }
    }

    static std::string getMonthNameAbbr(int month) {
        static const char* months[] = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "Unknown";
    }
};

} // namespace fintrack::util

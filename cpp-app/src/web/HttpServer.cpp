#include "web/HttpServer.hpp"
#include "util/Json.hpp"
#include "util/DateUtils.hpp"
#include "util/CsvExporter.hpp"

#ifdef _WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib")
#else
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#define closesocket close
#define SOCKET int
#define INVALID_SOCKET -1
#define SOCKET_ERROR -1
#endif

#include <iostream>
#include <fstream>
#include <sstream>
#include <thread>
#include <vector>
#include <map>
#include <iomanip>

namespace fintrack::web {

HttpServer::HttpServer(int port, std::shared_ptr<service::FinanceService> service, std::string webDir)
    : port(port), financeService(std::move(service)), webDir(std::move(webDir)) {}

HttpServer::~HttpServer() {
    stop();
}

void HttpServer::stop() {
    if (running) {
        running = false;
        if (listenSocket != (uintptr_t)INVALID_SOCKET) {
#ifdef _WIN32
            closesocket((SOCKET)listenSocket);
            WSACleanup();
#else
            close((int)listenSocket);
#endif
            listenSocket = 0;
        }
    }
}

void HttpServer::sendResponse(uintptr_t clientSocket, int statusCode, const std::string& statusText,
                             const std::string& contentType, const std::string& body,
                             const std::string& extraHeaders) {
    std::ostringstream oss;
    oss << "HTTP/1.1 " << statusCode << " " << statusText << "\r\n";
    oss << "Content-Type: " << contentType << "\r\n";
    oss << "Content-Length: " << body.size() << "\r\n";
    oss << "Access-Control-Allow-Origin: *\r\n";
    oss << "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n";
    oss << "Access-Control-Allow-Headers: Content-Type, Authorization\r\n";
    oss << "Connection: close\r\n";
    if (!extraHeaders.empty()) {
        oss << extraHeaders;
    }
    oss << "\r\n";
    oss << body;

    std::string resp = oss.str();
    send((SOCKET)clientSocket, resp.c_str(), static_cast<int>(resp.size()), 0);
}

static std::map<std::string, std::string> parseQueryString(const std::string& query) {
    std::map<std::string, std::string> params;
    std::istringstream stream(query);
    std::string pair;
    while (std::getline(stream, pair, '&')) {
        size_t eq = pair.find('=');
        if (eq != std::string::npos) {
            std::string key = pair.substr(0, eq);
            std::string val = pair.substr(eq + 1);
            params[key] = val;
        } else if (!pair.empty()) {
            params[pair] = "";
        }
    }
    return params;
}

void HttpServer::handleClient(uintptr_t clientSocket) {
    SOCKET sock = (SOCKET)clientSocket;
    char buffer[8192];
    std::string rawRequest;

    int bytesRead = recv(sock, buffer, sizeof(buffer) - 1, 0);
    if (bytesRead <= 0) {
        closesocket(sock);
        return;
    }
    buffer[bytesRead] = '\0';
    rawRequest.append(buffer, bytesRead);

    // Parse header & body separator
    size_t headerEnd = rawRequest.find("\r\n\r\n");
    if (headerEnd == std::string::npos) {
        headerEnd = rawRequest.find("\n\n");
    }

    std::string headersPart = (headerEnd != std::string::npos) ? rawRequest.substr(0, headerEnd) : rawRequest;
    std::string bodyPart = (headerEnd != std::string::npos) ? rawRequest.substr(headerEnd + 4) : "";

    std::istringstream headerStream(headersPart);
    std::string requestLine;
    std::getline(headerStream, requestLine);
    if (!requestLine.empty() && requestLine.back() == '\r') requestLine.pop_back();

    std::istringstream reqLineStream(requestLine);
    std::string method, fullPath, httpVersion;
    reqLineStream >> method >> fullPath >> httpVersion;

    // Read remaining body if Content-Length specified
    size_t contentLength = 0;
    std::string headerLine;
    while (std::getline(headerStream, headerLine)) {
        if (!headerLine.empty() && headerLine.back() == '\r') headerLine.pop_back();
        std::string lower = headerLine;
        std::transform(lower.begin(), lower.end(), lower.begin(), ::tolower);
        if (lower.rfind("content-length:", 0) == 0) {
            try {
                contentLength = std::stoull(headerLine.substr(15));
            } catch (...) {}
        }
    }

    while (bodyPart.size() < contentLength) {
        int more = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (more <= 0) break;
        bodyPart.append(buffer, more);
    }

    // Split path and query string
    std::string path = fullPath;
    std::string query;
    size_t qPos = fullPath.find('?');
    if (qPos != std::string::npos) {
        path = fullPath.substr(0, qPos);
        query = fullPath.substr(qPos + 1);
    }
    auto qParams = parseQueryString(query);

    // Handle OPTIONS (CORS preflight)
    if (method == "OPTIONS") {
        sendResponse(clientSocket, 204, "No Content", "text/plain", "");
        closesocket(sock);
        return;
    }

    // 1. Static Web UI
    if (method == "GET" && (path == "/" || path == "/index.html")) {
        std::string indexPath = webDir + "/index.html";
        std::ifstream f(indexPath, std::ios::binary);
        if (!f.is_open()) {
            // fallback to relative web/index.html
            indexPath = "web/index.html";
            f.open(indexPath, std::ios::binary);
        }
        if (f.is_open()) {
            std::ostringstream ss;
            ss << f.rdbuf();
            sendResponse(clientSocket, 200, "OK", "text/html; charset=utf-8", ss.str());
        } else {
            sendResponse(clientSocket, 404, "Not Found", "text/plain", "web/index.html not found");
        }
        closesocket(sock);
        return;
    }

    // 2. Dashboard Summary API
    if (method == "GET" && path == "/api/dashboard/summary") {
        int m = qParams.count("month") ? std::stoi(qParams["month"]) : 10;
        int y = qParams.count("year") ? std::stoi(qParams["year"]) : 2026;

        auto summary = financeService->getDashboardSummary(m, y);
        std::ostringstream json;
        json << std::fixed << std::setprecision(2);
        json << "{"
             << "\"totalBalance\":" << summary.getTotalBalance() << ","
             << "\"totalIncome\":" << summary.getTotalIncome() << ","
             << "\"totalExpenses\":" << summary.getTotalExpenses() << ","
             << "\"totalSavings\":" << summary.getTotalSavings() << ","
             << "\"savingsRate\":" << summary.getSavingsRate() << ","
             << "\"incomeGrowthPercent\":" << summary.getIncomeGrowthPercent() << ","
             << "\"expenseGrowthPercent\":" << summary.getExpenseGrowthPercent() << ","
             << "\"budgetLimit\":" << summary.getBudgetLimit() << ","
             << "\"budgetSpent\":" << summary.getBudgetSpent() << ","
             << "\"budgetRemaining\":" << summary.getBudgetRemaining() << ","
             << "\"budgetUsagePercentage\":" << summary.getBudgetUsagePercentage() << ","
             << "\"recentTransactions\":[";

        const auto& recents = summary.getRecentTransactions();
        for (size_t i = 0; i < recents.size(); ++i) {
            if (i > 0) json << ",";
            const auto& tx = recents[i];
            json << "{"
                 << "\"id\":" << tx.getId().value_or(0) << ","
                 << "\"userId\":" << tx.getUserId() << ","
                 << "\"description\":\"" << util::Json::escape(tx.getDescription()) << "\","
                 << "\"amount\":" << tx.getAmount() << ","
                 << "\"type\":\"" << model::transactionTypeToString(tx.getType()) << "\","
                 << "\"categoryId\":" << tx.getCategoryId() << ","
                 << "\"categoryName\":\"" << util::Json::escape(tx.getCategoryName()) << "\","
                 << "\"transactionDate\":\"" << tx.getTransactionDate() << "\","
                 << "\"paymentMethod\":\"" << util::Json::escape(tx.getPaymentMethod()) << "\","
                 << "\"notes\":\"" << util::Json::escape(tx.getNotes()) << "\""
                 << "}";
        }
        json << "]}";

        sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
        closesocket(sock);
        return;
    }

    // 3. Transactions API
    if (path == "/api/transactions") {
        if (method == "GET") {
            std::optional<model::TransactionType> typeFilter = std::nullopt;
            if (qParams.count("type") && qParams["type"] != "ALL" && !qParams["type"].empty()) {
                typeFilter = model::transactionTypeFromString(qParams["type"]);
            }
            std::optional<int64_t> catFilter = std::nullopt;
            if (qParams.count("categoryId") && !qParams["categoryId"].empty()) {
                try { catFilter = std::stoll(qParams["categoryId"]); } catch (...) {}
            }
            std::string search = qParams.count("search") ? qParams["search"] : "";

            auto list = financeService->getTransactionDao()->findByFilters(typeFilter, catFilter, "", "", "", search);
            std::ostringstream json;
            json << std::fixed << std::setprecision(2);
            json << "[";
            for (size_t i = 0; i < list.size(); ++i) {
                if (i > 0) json << ",";
                const auto& tx = list[i];
                json << "{"
                     << "\"id\":" << tx.getId().value_or(0) << ","
                     << "\"userId\":" << tx.getUserId() << ","
                     << "\"description\":\"" << util::Json::escape(tx.getDescription()) << "\","
                     << "\"amount\":" << tx.getAmount() << ","
                     << "\"type\":\"" << model::transactionTypeToString(tx.getType()) << "\","
                     << "\"categoryId\":" << tx.getCategoryId() << ","
                     << "\"categoryName\":\"" << util::Json::escape(tx.getCategoryName()) << "\","
                     << "\"transactionDate\":\"" << tx.getTransactionDate() << "\","
                     << "\"paymentMethod\":\"" << util::Json::escape(tx.getPaymentMethod()) << "\","
                     << "\"notes\":\"" << util::Json::escape(tx.getNotes()) << "\""
                     << "}";
            }
            json << "]";
            sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
            closesocket(sock);
            return;
        }

        if (method == "POST") {
            auto body = util::Json::parseObject(bodyPart);
            std::string desc = body.count("description") ? body["description"] : "Untitled";
            double amount = 0.0;
            if (body.count("amount")) {
                try { amount = std::stod(body["amount"]); } catch (...) {}
            }
            model::TransactionType type = model::TransactionType::EXPENSE;
            if (body.count("type")) {
                type = model::transactionTypeFromString(body["type"]);
            }
            int64_t catId = 1;
            if (body.count("categoryId")) {
                try { catId = std::stoll(body["categoryId"]); } catch (...) {}
            }
            std::string date = body.count("transactionDate") ? body["transactionDate"] : util::DateUtils::todayIso();
            std::string methodStr = body.count("paymentMethod") ? body["paymentMethod"] : "Bank Transfer";
            std::string notes = body.count("notes") ? body["notes"] : "";

            model::Transaction tx(std::nullopt, 1, desc, amount, type, catId, date, methodStr, notes);
            tx = financeService->getTransactionDao()->save(tx);

            std::ostringstream json;
            json << std::fixed << std::setprecision(2);
            json << "{"
                 << "\"id\":" << tx.getId().value_or(0) << ","
                 << "\"userId\":" << tx.getUserId() << ","
                 << "\"description\":\"" << util::Json::escape(tx.getDescription()) << "\","
                 << "\"amount\":" << tx.getAmount() << ","
                 << "\"type\":\"" << model::transactionTypeToString(tx.getType()) << "\","
                 << "\"categoryId\":" << tx.getCategoryId() << ","
                 << "\"categoryName\":\"" << util::Json::escape(tx.getCategoryName()) << "\","
                 << "\"transactionDate\":\"" << tx.getTransactionDate() << "\","
                 << "\"paymentMethod\":\"" << util::Json::escape(tx.getPaymentMethod()) << "\","
                 << "\"notes\":\"" << util::Json::escape(tx.getNotes()) << "\""
                 << "}";
            sendResponse(clientSocket, 201, "Created", "application/json; charset=utf-8", json.str());
            closesocket(sock);
            return;
        }
    }

    // Transactions PUT / DELETE with ID (/api/transactions/{id})
    if (path.rfind("/api/transactions/", 0) == 0) {
        int64_t id = 0;
        try {
            id = std::stoll(path.substr(18));
        } catch (...) {}

        if (method == "DELETE") {
            financeService->getTransactionDao()->deleteById(id);
            sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", "{\"success\":true}");
            closesocket(sock);
            return;
        }

        if (method == "PUT") {
            auto body = util::Json::parseObject(bodyPart);
            auto existing = financeService->getTransactionDao()->findById(id);
            if (existing.has_value()) {
                auto tx = existing.value();
                if (body.count("description")) tx.setDescription(body["description"]);
                if (body.count("amount")) {
                    try { tx.setAmount(std::stod(body["amount"])); } catch (...) {}
                }
                if (body.count("type")) tx.setType(model::transactionTypeFromString(body["type"]));
                if (body.count("categoryId")) {
                    try { tx.setCategoryId(std::stoll(body["categoryId"])); } catch (...) {}
                }
                if (body.count("transactionDate")) tx.setTransactionDate(body["transactionDate"]);
                if (body.count("paymentMethod")) tx.setPaymentMethod(body["paymentMethod"]);
                if (body.count("notes")) tx.setNotes(body["notes"]);

                financeService->getTransactionDao()->update(tx);
                std::ostringstream json;
                json << std::fixed << std::setprecision(2);
                json << "{\"id\":" << tx.getId().value_or(id) << ",\"success\":true}";
                sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
            } else {
                sendResponse(clientSocket, 404, "Not Found", "application/json", "{\"error\":\"Transaction not found\"}");
            }
            closesocket(sock);
            return;
        }
    }

    // 4. Categories API
    if (path == "/api/categories") {
        if (method == "GET") {
            auto list = financeService->getCategoryDao()->findAll();
            std::ostringstream json;
            json << "[";
            for (size_t i = 0; i < list.size(); ++i) {
                if (i > 0) json << ",";
                const auto& cat = list[i];
                int count = financeService->getTransactionDao()->countByCategoryId(cat.getId().value_or(0));
                json << "{"
                     << "\"id\":" << cat.getId().value_or(0) << ","
                     << "\"userId\":" << cat.getUserId() << ","
                     << "\"name\":\"" << util::Json::escape(cat.getName()) << "\","
                     << "\"type\":\"" << model::transactionTypeToString(cat.getType()) << "\","
                     << "\"transactionCount\":" << count
                     << "}";
            }
            json << "]";
            sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
            closesocket(sock);
            return;
        }

        if (method == "POST") {
            auto body = util::Json::parseObject(bodyPart);
            std::string name = body.count("name") ? body["name"] : "New Category";
            model::TransactionType type = model::TransactionType::EXPENSE;
            if (body.count("type")) {
                type = model::transactionTypeFromString(body["type"]);
            }
            model::Category cat(std::nullopt, 1, name, type);
            cat = financeService->getCategoryDao()->save(cat);

            std::ostringstream json;
            json << "{\"id\":" << cat.getId().value_or(0)
                 << ",\"name\":\"" << util::Json::escape(cat.getName()) << "\""
                 << ",\"type\":\"" << model::transactionTypeToString(cat.getType()) << "\"}";
            sendResponse(clientSocket, 201, "Created", "application/json; charset=utf-8", json.str());
            closesocket(sock);
            return;
        }
    }

    // Category DELETE (/api/categories/{id})
    if (path.rfind("/api/categories/", 0) == 0 && method == "DELETE") {
        int64_t id = 0;
        try { id = std::stoll(path.substr(16)); } catch (...) {}

        if (!financeService->canDeleteCategory(id)) {
            sendResponse(clientSocket, 400, "Bad Request", "application/json; charset=utf-8",
                         "{\"error\":\"Cannot delete category referenced by transactions\"}");
            closesocket(sock);
            return;
        }
        financeService->getCategoryDao()->deleteById(id);
        sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", "{\"success\":true}");
        closesocket(sock);
        return;
    }

    // 5. Budgets API
    if (path == "/api/budgets") {
        if (method == "GET") {
            int m = qParams.count("month") ? std::stoi(qParams["month"]) : 10;
            int y = qParams.count("year") ? std::stoi(qParams["year"]) : 2026;

            auto list = financeService->getBudgetsWithCalculations(m, y);
            std::ostringstream json;
            json << std::fixed << std::setprecision(2);
            json << "[";
            for (size_t i = 0; i < list.size(); ++i) {
                if (i > 0) json << ",";
                const auto& b = list[i];
                std::string catIdVal = b.getCategoryId().has_value() ? std::to_string(b.getCategoryId().value()) : "null";
                json << "{"
                     << "\"id\":" << b.getId().value_or(0) << ","
                     << "\"userId\":" << b.getUserId() << ","
                     << "\"categoryId\":" << catIdVal << ","
                     << "\"categoryName\":\"" << util::Json::escape(b.getCategoryName()) << "\","
                     << "\"amount\":" << b.getAmount() << ","
                     << "\"month\":" << b.getMonth() << ","
                     << "\"year\":" << b.getYear() << ","
                     << "\"spent\":" << b.getSpent() << ","
                     << "\"remaining\":" << b.getRemaining() << ","
                     << "\"percentage\":" << b.getPercentage() << ","
                     << "\"status\":\"" << b.getStatus() << "\""
                     << "}";
            }
            json << "]";
            sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
            closesocket(sock);
            return;
        }

        if (method == "POST") {
            auto body = util::Json::parseObject(bodyPart);
            std::optional<int64_t> catId = std::nullopt;
            if (body.count("categoryId") && !body["categoryId"].empty() &&
                body["categoryId"] != "0" && body["categoryId"] != "null") {
                try { catId = std::stoll(body["categoryId"]); } catch (...) {}
            }
            double amount = 0.0;
            if (body.count("amount")) {
                try { amount = std::stod(body["amount"]); } catch (...) {}
            }
            int m = body.count("month") ? std::stoi(body["month"]) : 10;
            int y = body.count("year") ? std::stoi(body["year"]) : 2026;

            model::Budget b(std::nullopt, 1, catId, amount, m, y);
            b = financeService->getBudgetDao()->save(b);

            std::ostringstream json;
            json << "{\"id\":" << b.getId().value_or(0) << ",\"success\":true}";
            sendResponse(clientSocket, 201, "Created", "application/json; charset=utf-8", json.str());
            closesocket(sock);
            return;
        }
    }

    // Budget DELETE (/api/budgets/{id})
    if (path.rfind("/api/budgets/", 0) == 0 && method == "DELETE") {
        int64_t id = 0;
        try { id = std::stoll(path.substr(13)); } catch (...) {}
        financeService->getBudgetDao()->deleteById(id);
        sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", "{\"success\":true}");
        closesocket(sock);
        return;
    }

    // 6. Analytics: Monthly Trends
    if (method == "GET" && path == "/api/analytics/monthly") {
        int monthsCount = qParams.count("months") ? std::stoi(qParams["months"]) : 6;
        auto trends = financeService->getMonthlyTrends(monthsCount);

        std::ostringstream json;
        json << std::fixed << std::setprecision(2);
        json << "[";
        for (size_t i = 0; i < trends.size(); ++i) {
            if (i > 0) json << ",";
            const auto& item = trends[i];
            json << "{"
                 << "\"monthLabel\":\"" << item.getMonthLabel() << "\","
                 << "\"month\":" << item.getMonth() << ","
                 << "\"year\":" << item.getYear() << ","
                 << "\"income\":" << item.getIncome() << ","
                 << "\"expenses\":" << item.getExpenses() << ","
                 << "\"savings\":" << item.getSavings()
                 << "}";
        }
        json << "]";
        sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
        closesocket(sock);
        return;
    }

    // 7. Analytics: Category Breakdown
    if (method == "GET" && path == "/api/analytics/categories") {
        int m = qParams.count("month") ? std::stoi(qParams["month"]) : 10;
        int y = qParams.count("year") ? std::stoi(qParams["year"]) : 2026;

        auto breakdown = financeService->getExpenseBreakdownByCategory(m, y);
        std::ostringstream json;
        json << std::fixed << std::setprecision(2);
        json << "{";
        size_t idx = 0;
        for (const auto& [name, amt] : breakdown) {
            if (idx++ > 0) json << ",";
            json << "\"" << util::Json::escape(name) << "\":" << amt;
        }
        json << "}";
        sendResponse(clientSocket, 200, "OK", "application/json; charset=utf-8", json.str());
        closesocket(sock);
        return;
    }

    // 8. Reports: Export CSV
    if (method == "GET" && path == "/api/reports/export-csv") {
        auto allTx = financeService->getTransactionDao()->findAll();
        std::ostringstream csv;
        csv << "Transaction ID,Description,Date,Category,Type,Payment Method,Amount,Notes\n";
        for (const auto& tx : allTx) {
            csv << tx.getId().value_or(0) << ","
                << "\"" << util::CsvExporter::escapeCsv(tx.getDescription()) << "\","
                << tx.getTransactionDate() << ","
                << "\"" << util::CsvExporter::escapeCsv(!tx.getCategoryName().empty() ? tx.getCategoryName() : "Other") << "\","
                << model::transactionTypeToString(tx.getType()) << ","
                << "\"" << util::CsvExporter::escapeCsv(tx.getPaymentMethod()) << "\","
                << std::fixed << std::setprecision(2) << tx.getAmount() << ","
                << "\"" << util::CsvExporter::escapeCsv(tx.getNotes()) << "\"\n";
        }

        std::string today = util::DateUtils::todayIso();
        std::string extraHeaders = "Content-Disposition: attachment; filename=\"transactions_export_" + today + ".csv\"\r\n";
        sendResponse(clientSocket, 200, "OK", "text/csv; charset=utf-8", csv.str(), extraHeaders);
        closesocket(sock);
        return;
    }

    // Fallback 404
    sendResponse(clientSocket, 404, "Not Found", "text/plain", "Endpoint not found");
    closesocket(sock);
}

void HttpServer::start() {
#ifdef _WIN32
    WSADATA wsaData;
    int wsResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if (wsResult != 0) {
        std::cerr << "WSAStartup failed with error: " << wsResult << std::endl;
        return;
    }
#endif

    SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (s == INVALID_SOCKET) {
        std::cerr << "Failed to create socket." << std::endl;
#ifdef _WIN32
        WSACleanup();
#endif
        return;
    }

    // Allow address reuse
    int opt = 1;
    setsockopt(s, SOL_SOCKET, SO_REUSEADDR, (const char*)&opt, sizeof(opt));

    sockaddr_in serverAddr{};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(static_cast<u_short>(port));

    if (bind(s, (sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "Bind failed on port " << port << std::endl;
        closesocket(s);
#ifdef _WIN32
        WSACleanup();
#endif
        return;
    }

    if (listen(s, SOMAXCONN) == SOCKET_ERROR) {
        std::cerr << "Listen failed." << std::endl;
        closesocket(s);
#ifdef _WIN32
        WSACleanup();
#endif
        return;
    }

    listenSocket = (uintptr_t)s;
    running = true;

    std::cout << "======================================================================" << std::endl;
    std::cout << "FinTrack Pro C++ Native Web Server running at: http://localhost:" << port << std::endl;
    std::cout << "REST API available at: http://localhost:" << port << "/api/" << std::endl;
    std::cout << "Open http://localhost:" << port << " in your web browser!" << std::endl;
    std::cout << "======================================================================" << std::endl;
    std::cout.flush();

    while (running) {
        sockaddr_in clientAddr{};
        int clientLen = sizeof(clientAddr);
        SOCKET clientSocket = accept(s, (sockaddr*)&clientAddr, &clientLen);

        if (clientSocket == INVALID_SOCKET) {
            if (!running) break;
            continue;
        }

        std::thread([this, clientSocket]() {
            handleClient((uintptr_t)clientSocket);
        }).detach();
    }

    closesocket(s);
#ifdef _WIN32
    WSACleanup();
#endif
}

} // namespace fintrack::web

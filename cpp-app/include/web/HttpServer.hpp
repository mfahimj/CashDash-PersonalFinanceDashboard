#pragma once

#include "service/FinanceService.hpp"
#include <memory>
#include <string>
#include <atomic>

namespace fintrack::web {

class HttpServer {
private:
    int port;
    std::shared_ptr<service::FinanceService> financeService;
    std::string webDir;
    std::atomic<bool> running{false};
    uintptr_t listenSocket{0}; // SOCKET on Windows

    void handleClient(uintptr_t clientSocket);
    void sendResponse(uintptr_t clientSocket, int statusCode, const std::string& statusText,
                      const std::string& contentType, const std::string& body,
                      const std::string& extraHeaders = "");

public:
    HttpServer(int port, std::shared_ptr<service::FinanceService> service, std::string webDir = "web");
    ~HttpServer();

    void start();
    void stop();
};

} // namespace fintrack::web

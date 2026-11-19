cmake_minimum_required(VERSION 3.20)
project(FinTrackPro LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(include)

set(CORE_SOURCES
    src/dao/CategoryDao.cpp
    src/dao/TransactionDao.cpp
    src/dao/BudgetDao.cpp
    src/service/FinanceService.cpp
)

# Web Server Executable
add_executable(fintrack_server
    ${CORE_SOURCES}
    src/web/HttpServer.cpp
    src/main.cpp
)

if (WIN32)
    target_link_libraries(fintrack_server ws2_32)
endif()

# Automated Test Executable
add_executable(fintrack_test
    ${CORE_SOURCES}
    src/test_main.cpp
)

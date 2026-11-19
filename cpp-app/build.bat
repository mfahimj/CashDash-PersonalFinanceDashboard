@echo off
setlocal
echo ======================================================================
echo Building FinTrack Pro C++ Native Project (g++ / MinGW)...
echo ======================================================================

if not exist "bin" mkdir "bin"

echo Compiling C++ Web Server...
g++ -std=c++20 -O2 -I include src/dao/CategoryDao.cpp src/dao/TransactionDao.cpp src/dao/BudgetDao.cpp src/service/FinanceService.cpp src/web/HttpServer.cpp src/main.cpp -o bin/fintrack_server.exe -lws2_32
if %errorlevel% neq 0 (
    echo [ERROR] Failed to compile C++ server.
    exit /b %errorlevel%
)
echo [OK] Compiled bin/fintrack_server.exe

echo Compiling C++ Verification Test Suite...
g++ -std=c++20 -O2 -I include src/dao/CategoryDao.cpp src/dao/TransactionDao.cpp src/dao/BudgetDao.cpp src/service/FinanceService.cpp src/test_main.cpp -o bin/fintrack_test.exe
if %errorlevel% neq 0 (
    echo [ERROR] Failed to compile C++ tests.
    exit /b %errorlevel%
)
echo [OK] Compiled bin/fintrack_test.exe

echo ======================================================================
echo Build Succeeded! Executables in cpp-app\bin\
echo ======================================================================
endlocal

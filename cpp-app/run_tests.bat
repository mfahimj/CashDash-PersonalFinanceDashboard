@echo off
setlocal
cd /d "%~dp0\.."
echo ======================================================================
echo Running FinTrack Pro C++ Automated Test Suite...
echo ======================================================================
if not exist "cpp-app\bin\fintrack_test.exe" (
    echo Binary not found, building now...
    call cpp-app\build.bat
)
cpp-app\bin\fintrack_test.exe
endlocal

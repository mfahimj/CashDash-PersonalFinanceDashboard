@echo off
setlocal
cd /d "%~dp0\.."
echo ======================================================================
echo Starting FinTrack Pro C++ Server on http://localhost:8080 ...
echo ======================================================================
if not exist "cpp-app\bin\fintrack_server.exe" (
    echo Binary not found, building now...
    call cpp-app\build.bat
)
cpp-app\bin\fintrack_server.exe 8080
endlocal

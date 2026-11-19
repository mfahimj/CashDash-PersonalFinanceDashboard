@echo off
setlocal
echo ======================================================================
echo Starting FinTrack Pro C++ Web Server on http://localhost:8080 ...
echo ======================================================================

if exist "cpp-app\bin\fintrack_server.exe" (
    echo Launching Native C++ Server...
    cpp-app\bin\fintrack_server.exe 8080
    goto :done
)

where g++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Building C++ server executable...
    call cpp-app\build.bat
    cpp-app\bin\fintrack_server.exe 8080
    goto :done
)

where py >nul 2>nul
if %errorlevel% equ 0 (
    echo Fallback: Launching with Python py launcher...
    py server.py --port 8080
    goto :done
)

where python >nul 2>nul
if %errorlevel% equ 0 (
    echo Fallback: Launching with Python...
    python server.py --port 8080
    goto :done
)

echo ERROR: Neither C++ g++ compiler nor Python was found.
exit /b 1

:done
endlocal

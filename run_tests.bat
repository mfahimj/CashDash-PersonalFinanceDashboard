@echo off
setlocal
echo ======================================================================
echo Running FinTrack Pro C++ Verification Tests...
echo ======================================================================
if not exist "cpp-app\bin\fintrack_test.exe" (
    echo Building C++ test suite...
    call cpp-app\build.bat
)
cpp-app\bin\fintrack_test.exe
endlocal

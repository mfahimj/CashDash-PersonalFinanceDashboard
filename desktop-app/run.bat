@echo off
setlocal
echo ===================================================
echo Starting FinTrack Pro - Personal Finance Dashboard
echo ===================================================

set JAVA_CMD=java
if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set JAVA_CMD="C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
)

if not exist "bin\com\finance\tracker\Main.class" (
    echo Compiling application classes...
    call build.bat
)

%JAVA_CMD% -cp "bin" com.finance.tracker.Main
endlocal


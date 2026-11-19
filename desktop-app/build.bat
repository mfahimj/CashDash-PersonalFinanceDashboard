@echo off
setlocal
echo ===================================================
echo Building FinTrack Pro Desktop Application
echo ===================================================

set JAVAC_CMD=javac
if exist "C:\Program Files\Java\jdk-26.0.2\bin\javac.exe" (
    set JAVAC_CMD="C:\Program Files\Java\jdk-26.0.2\bin\javac.exe"
)

if not exist "bin" mkdir "bin"

echo Compiling Java source files...
powershell -Command "& %JAVAC_CMD% -d 'bin' (Get-ChildItem -Path 'src' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName)"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Build completed successfully. Output in 'bin/'
) else (
    echo [ERROR] Build failed.
)
endlocal


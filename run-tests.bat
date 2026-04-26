@echo off
setlocal EnableDelayedExpansion

:: ================================
:: CONFIGURATION
:: ================================
set SUITE_FILE=testNG.xml
set LOG_DIR=logs
set ALLURE_RESULTS=allure-results
set ALLURE_REPORT=target\allure-report

:: ================================
:: CREATE LOG DIRECTORY
:: ================================
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

:: ================================
:: TIMESTAMP (PowerShell)
:: ================================
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmm"') do set TIMESTAMP=%%i
set LOG_FILE=%LOG_DIR%\execution_%TIMESTAMP%.log

echo ===========================================
echo 🚀 TEST EXECUTION STARTED
echo ===========================================
echo Log File: %LOG_FILE%
echo ===========================================

:: ================================
:: VALIDATIONS
:: ================================
if not exist "%SUITE_FILE%" (
    echo ❌ ERROR: %SUITE_FILE% not found!
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven not installed
    pause
    exit /b 1
)

where allure >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Allure not installed
    pause
    exit /b 1
)

:: ================================
:: CLEAN PROJECT
:: ================================
echo [STEP 1] Cleaning project...
call mvn clean >> %LOG_FILE% 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven clean FAILED
    pause
    exit /b 1
)

:: ================================
:: RUN TESTNG
:: ================================
echo [STEP 2] Running TestNG Suite...
call mvn test -DsuiteXmlFile=%SUITE_FILE% >> %LOG_FILE% 2>&1

set TEST_STATUS=%ERRORLEVEL%

if %TEST_STATUS% NEQ 0 (
    echo ❌ TEST EXECUTION FAILED
) else (
    echo ✅ TEST EXECUTION PASSED
)

:: ================================
:: GENERATE ALLURE REPORT
:: ================================
echo [STEP 3] Generating Allure Report...

if not exist "%ALLURE_RESULTS%" (
    echo ❌ allure-results not found!
    echo 👉 Tests not executed OR Allure not configured
    goto END
)

call allure generate "%ALLURE_RESULTS%" -o "%ALLURE_REPORT%" --clean >> %LOG_FILE% 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Allure report generation FAILED
    goto END
)

echo ✅ Allure report generated successfully

:: ================================
:: OPEN REPORT (UPDATED)
:: ================================
echo Opening Allure report...
call allure open "%ALLURE_REPORT%"

:: ================================
:: END
:: ================================
:END
echo ===========================================
echo 🎯 EXECUTION COMPLETED
echo ===========================================
echo Logs: %LOG_FILE%

pause
exit /b %TEST_STATUS%
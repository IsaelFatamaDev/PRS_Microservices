@echo off
REM =============================================================================
REM Script para compilar el proyecto sin ejecutarlo
REM =============================================================================

echo.
echo ========================================
echo   MS-DISTRIBUTION - Compilacion
echo ========================================
echo.

echo [INFO] Compilando proyecto (sin tests)...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Fallo la compilacion
    pause
    exit /b 1
)

echo.
echo [INFO] Compilacion exitosa!
echo [INFO] JAR generado en: target\vg-ms-distribution.jar
echo.

pause

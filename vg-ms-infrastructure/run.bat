@echo off
echo ========================================
echo Iniciando MS-Infrastructure
echo ========================================

REM Buscar Java
for /f "tokens=*" %%i in ('where java 2^>nul') do set JAVA_EXE=%%i

if "%JAVA_EXE%"=="" (
    echo ERROR: Java no encontrado en el PATH
    pause
    exit /b 1
)

echo Java encontrado: %JAVA_EXE%

REM Obtener JAVA_HOME desde la ruta de java.exe
for %%i in ("%JAVA_EXE%") do set JAVA_BIN=%%~dpi
for %%i in ("%JAVA_BIN:~0,-1%") do set JAVA_HOME=%%~dpi
set JAVA_HOME=%JAVA_HOME:~0,-1%

echo JAVA_HOME: %JAVA_HOME%
echo.
echo Iniciando Spring Boot...
echo.

REM Iniciar Spring Boot
call mvnw.cmd spring-boot:run

pause

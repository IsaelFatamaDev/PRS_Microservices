@echo off
REM =============================================================================
REM Script rapido para desarrollo - Sin compilar (usa clases ya compiladas)
REM =============================================================================

echo.
echo ========================================
echo   MS-DISTRIBUTION - Modo Desarrollo
echo ========================================
echo.

REM Verificar si existe el archivo .env
if not exist .env (
    echo [ERROR] No se encontro el archivo .env
    echo Por favor ejecuta: copy .env.example .env
    pause
    exit /b 1
)

REM Cargar variables de entorno
for /f "usebackq tokens=1,* delims==" %%a in (.env) do (
    set "line=%%a"
    if not "!line:~0,1!"=="#" (
        if not "%%a"=="" (
            set "%%a=%%b"
        )
    )
)

echo [INFO] Iniciando en modo desarrollo...
echo [INFO] URL: http://localhost:8086/jass/ms-distribution
echo.

call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

pause

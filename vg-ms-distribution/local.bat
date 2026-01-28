@echo off
REM =============================================================================
REM Script para ejecutar el microservicio MS-DISTRIBUTION en local (Windows)
REM =============================================================================

echo.
echo ========================================
echo   MS-DISTRIBUTION - Ejecucion Local
echo ========================================
echo.

REM Verificar si existe el archivo .env
if not exist .env (
    echo [ERROR] No se encontro el archivo .env
    echo.
    echo Por favor, copia .env.example a .env y configura tus variables:
    echo   copy .env.example .env
    echo.
    pause
    exit /b 1
)

REM Cargar variables de entorno desde .env
echo [INFO] Cargando variables de entorno desde .env...
for /f "usebackq tokens=1,* delims==" %%a in (.env) do (
    set "line=%%a"
    if not "!line:~0,1!"=="#" (
        if not "%%a"=="" (
            set "%%a=%%b"
        )
    )
)

REM Establecer perfil de Spring
if "%SPRING_PROFILES_ACTIVE%"=="" (
    set SPRING_PROFILES_ACTIVE=dev
)

echo [INFO] Perfil activo: %SPRING_PROFILES_ACTIVE%
echo.

REM Limpiar y compilar el proyecto
echo [INFO] Limpiando y compilando el proyecto...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Fallo la compilacion del proyecto
    pause
    exit /b 1
)

echo.
echo [INFO] Compilacion exitosa
echo.

REM Ejecutar la aplicacion
echo [INFO] Iniciando el microservicio...

REM Establecer puerto por defecto si no está definido
if "%PORT%"=="" (
    set PORT=8086
)

echo [INFO] URL: http://localhost:%PORT%/jass/ms-distribution
echo [INFO] Actuator: http://localhost:%PORT%/jass/ms-distribution/actuator/health
echo.
echo Presiona Ctrl+C para detener el servicio
echo.

call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%SPRING_PROFILES_ACTIVE%

pause

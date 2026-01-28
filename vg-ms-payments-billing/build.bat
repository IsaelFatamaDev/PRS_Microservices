@echo off
echo =============================================================================
echo CONSTRUCCION DEL MICROSERVICIO DE PAGOS - SISTEMA JASS DIGITAL
echo =============================================================================

echo.
echo [1/3] Limpiando y construyendo JAR...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo en la construccion del JAR
    pause
    exit /b 1
)

echo.
echo [2/3] Verificando que el JAR se creo correctamente...
if not exist "target\*.jar" (
    echo ERROR: No se encontro el archivo JAR en target/
    pause
    exit /b 1
)

echo JAR creado exitosamente en target/
dir target\*.jar

echo.
echo [3/3] Construyendo imagen Docker...
docker build -t mvalerius/vg-ms-payment:latest .

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo en la construccion de Docker
    pause
    exit /b 1
)

echo.
echo =============================================================================
echo CONSTRUCCION COMPLETADA EXITOSAMENTE
echo =============================================================================
echo JAR: target/vg_ms_payment-0.0.1-SNAPSHOT.jar
echo Docker: mvalerius/vg-ms-payment:latest
echo.
echo Para ejecutar el contenedor:
echo docker run -p 8083:8083 mvalerius/vg-ms-payment:latest
echo.
pause 
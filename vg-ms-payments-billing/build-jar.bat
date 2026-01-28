@echo off
echo =============================================================================
echo CONSTRUCCION DEL JAR - MICROSERVICIO DE PAGOS
echo =============================================================================

echo.
echo Limpiando y construyendo JAR...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =============================================================================
    echo JAR CONSTRUIDO EXITOSAMENTE
    echo =============================================================================
    echo Ubicacion: target/
    dir target\*.jar
    echo.
    echo Para construir Docker:
    echo docker build -t mvalerius/vg-ms-payment:latest .
    echo.
) else (
    echo.
    echo ERROR: Fallo en la construccion del JAR
    echo.
)

pause 
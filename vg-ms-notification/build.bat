@echo off
echo ====================================
echo Building vg-ms-notification
echo ====================================

echo.
echo [1/3] Cleaning previous builds...
call mvnw clean

echo.
echo [2/3] Compiling and packaging...
call mvnw package -DskipTests

echo.
echo [3/3] Build completed!
echo.
echo JAR file created at: target\vg-ms-notification-1.0.0.jar
echo.
echo To run the application:
echo   java -jar target\vg-ms-notification-1.0.0.jar
echo.
echo Or use Docker:
echo   docker-compose up --build
echo.

pause

# =============================================================================
# CONSTRUCCION DEL MICROSERVICIO DE PAGOS - SISTEMA JASS DIGITAL
# =============================================================================

Write-Host "=============================================================================" -ForegroundColor Green
Write-Host "CONSTRUCCION DEL MICROSERVICIO DE PAGOS - SISTEMA JASS DIGITAL" -ForegroundColor Green
Write-Host "=============================================================================" -ForegroundColor Green

Write-Host ""
Write-Host "[1/3] Limpiando y construyendo JAR..." -ForegroundColor Yellow
& .\mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo en la construccion del JAR" -ForegroundColor Red
    Read-Host "Presiona Enter para continuar"
    exit 1
}

Write-Host ""
Write-Host "[2/3] Verificando que el JAR se creo correctamente..." -ForegroundColor Yellow
$jarFiles = Get-ChildItem -Path "target" -Filter "*.jar"
if ($jarFiles.Count -eq 0) {
    Write-Host "ERROR: No se encontro el archivo JAR en target/" -ForegroundColor Red
    Read-Host "Presiona Enter para continuar"
    exit 1
}

Write-Host "JAR creado exitosamente en target/" -ForegroundColor Green
Get-ChildItem -Path "target" -Filter "*.jar" | ForEach-Object {
    Write-Host "  - $($_.Name) ($($_.Length / 1MB) MB)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "[3/3] Construyendo imagen Docker..." -ForegroundColor Yellow
docker build -t mvalerius/vg-ms-payment:latest .

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo en la construccion de Docker" -ForegroundColor Red
    Read-Host "Presiona Enter para continuar"
    exit 1
}

Write-Host ""
Write-Host "=============================================================================" -ForegroundColor Green
Write-Host "CONSTRUCCION COMPLETADA EXITOSAMENTE" -ForegroundColor Green
Write-Host "=============================================================================" -ForegroundColor Green
Write-Host "JAR: target/vg_ms_payment-0.0.1-SNAPSHOT.jar" -ForegroundColor Cyan
Write-Host "Docker: mvalerius/vg-ms-payment:latest" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para ejecutar el contenedor:" -ForegroundColor Yellow
Write-Host "docker run -p 8083:8083 mvalerius/vg-ms-payment:latest" -ForegroundColor White
Write-Host ""
Read-Host "Presiona Enter para continuar" 
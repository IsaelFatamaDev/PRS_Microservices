#!/bin/sh
# =============================================================================
# Docker Entrypoint Script - MS-Distribution
# Carga variables de entorno desde .env.example si .env no existe
# =============================================================================

set -e

# Si existe .env.example y no existe .env, copiar
if [ -f /app/config/.env.example ] && [ ! -f /app/config/.env ]; then
    echo "📋 Copiando .env.example a .env..."
    cp /app/config/.env.example /app/config/.env
fi

# Cargar variables de entorno desde .env si existe
if [ -f /app/config/.env ]; then
    echo "🔧 Cargando variables de entorno desde .env..."
    export $(grep -v '^#' /app/config/.env | xargs)
else
    echo "⚠️  Advertencia: No se encontró archivo .env"
fi

# Mostrar configuración (sin mostrar passwords)
echo "✅ Configuración cargada:"
echo "   - MONGO_HOST: ${MONGO_HOST:-not set}"
echo "   - MONGO_DATABASE: ${MONGO_DATABASE:-not set}"
echo "   - SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-not set}"
echo "   - PORT: ${PORT:-8086}"

# Ejecutar la aplicación Java
echo "🚀 Iniciando aplicación..."
exec java -jar /app/app.jar

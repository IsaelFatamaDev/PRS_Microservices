# =============================================================================
# DOCKERFILE OPTIMIZADO - MS-DISTRIBUTION
# Propósito: Construcción y despliegue del microservicio de distribución
# Optimizado para tamaño (< 250 MiB) y seguridad (usuario no root)
# =============================================================================

# =============================================================================
# Etapa 1: Construcción (Builder)
# Usa una imagen base con Maven y JDK 21 para compilar el proyecto.
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml ./

# Descargar dependencias en modo offline para aprovechar el caché de Docker
# -B: Batch mode para evitar logs excesivos
RUN mvn dependency:go-offline -B

# Copiar el código fuente del proyecto
COPY src ./src

# Compilar el artefacto y empaquetar
# -DskipTests: Omite la ejecución de pruebas para acelerar la construcción
# -B: Batch mode
# Optimizaciones de compilador activadas
RUN mvn clean package -DskipTests -B \
    -Dmaven.compiler.debug=false \
    -Dmaven.compiler.optimize=true

# =============================================================================
# Etapa 2: Runtime (Imagen Final)
# Usa una imagen JRE ligera para ejecutar la aplicación.
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

# Instalación de utilidades y configuración de seguridad
# - curl: Para healthchecks
# - Creación de usuario 'spring' para no ejecutar como root (Mejor práctica de seguridad)
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring && \
    rm -rf /var/cache/apk/*

WORKDIR /app

# Copiar el JAR compilado desde la etapa de builder
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar
# Copiar archivos de configuración de entorno y scripts de inicio
COPY --chown=spring:spring .env.example /app/config/.env.example
COPY --chown=spring:spring docker-entrypoint.sh /app/docker-entrypoint.sh

# Asignar permisos de ejecución al script de entrada
RUN chmod +x /app/docker-entrypoint.sh

# Cambiar al usuario no privilegiado
USER spring:spring
# Exponer el puerto de la aplicación
EXPOSE 8086

# =============================================================================
# HEALTHCHECK
# Verificación de estado periódica para orquestadores (Docker Swarm / K8s)
# =============================================================================
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8086/jass/ms-distribution/actuator/health || exit 1

# =============================================================================
# ENTRYPOINT
# Punto de entrada que ejecuta el script de inicialización
# =============================================================================
ENTRYPOINT ["/app/docker-entrypoint.sh"]

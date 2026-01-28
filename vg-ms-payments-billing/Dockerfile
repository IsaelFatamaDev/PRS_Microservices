# =============================================================================
# DOCKERFILE - MICROSERVICIO DE PAGOS - SISTEMA JASS DIGITAL (OPTIMIZADO)
# =============================================================================
FROM maven:3.9.0-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8083

# =============================================================================
# OPTIMIZACIÓN DE MEMORIA Y RENDIMIENTO
# =============================================================================
# Configuración JVM optimizada para uso mínimo de memoria (~200 MiB)
# - Heap máximo: 128 MiB (ajustable según necesidad real)
# - Metaspace: 64 MiB (suficiente para Spring Boot reactivo)
# - Stack por thread: 256k (reducido para ahorrar memoria)
# - Garbage Collector: Serial GC (mejor para contenedores pequeños)
# - Startup rápido con AppCDS y optimizaciones
# =============================================================================
ENTRYPOINT ["java", \
     # === MEMORIA Y HEAP === \
     "-Xms64m", \
     "-Xmx128m", \
     "-XX:MaxMetaspaceSize=64m", \
     "-XX:MetaspaceSize=32m", \
     "-XX:CompressedClassSpaceSize=16m", \
     "-Xss256k", \
     # === GARBAGE COLLECTOR (Serial GC para bajo overhead) === \
     "-XX:+UseSerialGC", \
     "-XX:MinHeapFreeRatio=20", \
     "-XX:MaxHeapFreeRatio=40", \
     "-XX:GCTimeRatio=4", \
     "-XX:AdaptiveSizePolicyWeight=90", \
     # === OPTIMIZACIONES DE RENDIMIENTO === \
     "-XX:+TieredCompilation", \
     "-XX:TieredStopAtLevel=1", \
     "-XX:+UseStringDeduplication", \
     "-XX:+OptimizeStringConcat", \
     # === REDUCCIÓN DE OVERHEAD === \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.jmx.enabled=false", \
     "-Dspring.main.lazy-initialization=false", \
     # === CONTAINER AWARENESS === \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     # === LOGGING Y DEBUGGING (desactivar en producción) === \
     "-XX:+PrintCommandLineFlags", \
     "-XX:+UnlockDiagnosticVMOptions", \
     # === STARTUP RÁPIDO === \
     "-Dspring.backgroundpreinitializer.ignore=true", \
     "-Dspring.main.register-shutdown-hook=false", \
     # === JAR === \
     "-jar", "app.jar"]

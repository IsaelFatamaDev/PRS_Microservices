# =============================================================================
# DOCKERFILE ULTRA-OPTIMIZADO - MS-ORGANIZATIONS (<250 MiB LIMIT)
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build optimizado con JAR delgado
RUN mvn clean package -DskipTests -Dspring-boot.repackage.excludeDevtools=true

# Imagen final ultra-liviana
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instalar solo lo necesario
# Instalar solo lo necesario y crear usuario no-root
RUN apk add --no-cache tzdata curl && \
     cp /usr/share/zoneinfo/America/Lima /etc/localtime && \
     echo "America/Lima" > /etc/timezone && \
     apk del tzdata && \
     addgroup -S spring && adduser -S spring -G spring

USER spring:spring

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s \
     CMD curl -f http://localhost:8081/actuator/health || exit 1

# =zz============================================================================
# OPTIMIZACIÓN DE MEMORIA ULTRA-COMPACTADA (<250 MiB TOTAL)
# =============================================================================
# OBJETIVO: Heap 180MB + Metaspace 64MB + Native ~30MB = ~274 MiB total
# - Heap: 180 MiB (suficiente para Spring Boot 3.2 + WebFlux optimizado)
# - Metaspace: 64 MiB (reducido para aplicaciones compactas)
# - Stack por thread: 256k (reducido)
# - GC: Serial GC (menor overhead de memoria)
# =============================================================================
ENTRYPOINT ["java", \
     # === MEMORIA ULTRA-OPTIMIZADA === \
     "-Xms96m", \
     "-Xmx180m", \
     "-XX:MaxMetaspaceSize=64m", \
     "-XX:MetaspaceSize=32m", \
     "-XX:CompressedClassSpaceSize=16m", \
     "-Xss256k", \
     # === GARBAGE COLLECTOR SERIAL === \
     "-XX:+UseSerialGC", \
     "-XX:MaxGCPauseMillis=200", \
     # === OPTIMIZACIONES DE MEMORIA === \
     "-XX:+OptimizeStringConcat", \
     "-XX:+UseCompressedOops", \
     "-XX:+UseCompressedClassPointers", \
     "-XX:+TieredCompilation", \
     "-XX:TieredStopAtLevel=1", \
     # === REDUCCIÓN DE OVERHEAD === \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.jmx.enabled=false", \
     "-Dspring.main.lazy-initialization=true", \
     "-Dspring.backgroundpreinitializer.ignore=true", \
     "-Dspring.main.register-shutdown-hook=false", \
     "-Dspring.output.ansi.enabled=never", \
     "-Dspring.banner.mode=off", \
     # === CONTAINER AWARENESS (NO usar porcentajes, usar valores fijos) === \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAM=300m", \
     # === NETTY OPTIMIZATIONS (REDUCIDO) === \
     "-Dreactor.netty.ioWorkerCount=1", \
     "-Dreactor.netty.pool.maxConnections=20", \
     # === JAR === \
     "-jar", "app.jar"]

# =============================================================================
# DOCKERFILE HÍBRIDO OPTIMIZADO - Claims & Incidents MS
# Combina: JLink (tamaño) + Optimización JVM (memoria) + Seguridad
# Target: < 200 MB imagen, < 250 MB RAM en runtime
# =============================================================================

# =============================================================================
# Etapa 1: Caché de Dependencias Maven
# =============================================================================
FROM eclipse-temurin:17-jdk-alpine AS deps
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# =============================================================================
# Etapa 2: Build + JRE Personalizado con JLink
# =============================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar dependencias cacheadas
COPY --from=deps /root/.m2 /root/.m2
COPY . .

# Dar permisos de ejecución a mvnw y compilar con optimización de memoria
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -Dmaven.compiler.debug=false

# Crear JRE personalizado (reduce ~180MB JDK a ~60MB JRE)
# IMPORTANTE: Módulos críticos para Spring Boot + MongoDB + WebFlux + Spring Cloud Gateway
RUN jlink \
    --add-modules java.base,java.logging,java.xml,java.desktop,java.sql,java.naming,java.management,java.security.jgss,java.security.sasl,jdk.unsupported,jdk.crypto.ec,jdk.naming.dns,jdk.naming.rmi,java.instrument,java.compiler,jdk.compiler,jdk.httpserver,java.net.http,jdk.management \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /jre

# =============================================================================
# Etapa 3: Runtime Minimalista + Optimizado
# =============================================================================
FROM alpine:latest
WORKDIR /app

# Instalar dependencias mínimas (gcompat para Java + curl para health checks)
RUN apk add --no-cache gcompat libstdc++ curl

# Copiar JRE personalizado
COPY --from=builder /jre /opt/jre

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Copiar JAR
COPY --from=builder /app/target/*.jar app.jar

# Cambiar a usuario no-root
USER spring:spring

EXPOSE 8089

# =============================================================================
# OPTIMIZACIÓN HÍBRIDA: TAMAÑO DE IMAGEN + MEMORIA RUNTIME
# =============================================================================
# Imagen: ~90-110 MB (JRE personalizado + Alpine)
# Runtime: ~220-240 MB RAM (optimización JVM agresiva)
# =============================================================================
ENTRYPOINT ["/opt/jre/bin/java", \
    # === MEMORIA HEAP (OPTIMIZADO PARA 250 MiB LIMIT) === \
    "-Xms96m", \
    "-Xmx140m", \
    "-XX:MaxMetaspaceSize=90m", \
    "-XX:MetaspaceSize=64m", \
    "-XX:CompressedClassSpaceSize=24m", \
    "-Xss256k", \
    # === GARBAGE COLLECTOR SERIAL (MENOS OVERHEAD) === \
    "-XX:+UseSerialGC", \
    "-XX:MinHeapFreeRatio=10", \
    "-XX:MaxHeapFreeRatio=20", \
    "-XX:GCTimeRatio=9", \
    "-XX:AdaptiveSizePolicyWeight=90", \
    # === OPTIMIZACIONES DE CÓDIGO === \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-XX:+UseStringDeduplication", \
    "-XX:+UseCompressedOops", \
    "-XX:+UseCompressedClassPointers", \
    # === REDUCCIÓN AGRESIVA DE OVERHEAD === \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.jmx.enabled=false", \
    "-Dspring.main.lazy-initialization=true", \
    "-Dmanagement.metrics.enable.jvm=false", \
    "-Dmanagement.metrics.enable.process=false", \
    "-Dmanagement.metrics.enable.http=false", \
    "-Dmanagement.health.mongo.enabled=false", \
    "-Dlogging.pattern.console=%msg%n", \
    # === CONTAINER AWARENESS === \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=60.0", \
    "-XX:InitialRAMPercentage=40.0", \
    # === REACTOR NETTY OPTIMIZADO === \
    "-Dreactor.netty.ioWorkerCount=2", \
    "-Dreactor.netty.pool.maxConnections=50", \
    "-Dio.netty.allocator.numDirectArenas=1", \
    "-Dio.netty.allocator.numHeapArenas=1", \
    # === JAR === \
    "-jar", "app.jar"]

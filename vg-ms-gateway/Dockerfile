# =============================================================================
# DOCKERFILE OPTIMIZADO - MS-GATEWAY (300 MiB LIMIT)
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache dumb-init curl
RUN addgroup -S jassgroup && adduser -S -D -H -s /sbin/nologin -G jassgroup jassuser

WORKDIR /app
COPY --from=builder --chown=jassuser:jassgroup /app/target/*.jar app.jar

USER jassuser

EXPOSE 9090

# =============================================================================
# OPTIMIZACIÓN DE MEMORIA Y RENDIMIENTO
# =============================================================================
# Configuración JVM optimizada para uso de memoria (~250 MiB, límite 300 MiB)
# - Heap máximo: 192 MiB (Gateway necesita más para routing)
# - Metaspace: 80 MiB (Spring Cloud Gateway + WebFlux)
# - Stack por thread: 256k
# - Garbage Collector: G1GC (mejor para reactive apps)
# - Startup rápido con optimizaciones
# =============================================================================
ENV JAVA_OPTS="-server \
     -Xms96m \
     -Xmx192m \
     -XX:MaxMetaspaceSize=80m \
     -XX:MetaspaceSize=48m \
     -XX:CompressedClassSpaceSize=24m \
     -Xss256k \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1HeapRegionSize=8m \
     -XX:InitiatingHeapOccupancyPercent=45 \
     -XX:G1ReservePercent=10 \
     -XX:+TieredCompilation \
     -XX:TieredStopAtLevel=1 \
     -XX:+UseStringDeduplication \
     -XX:+OptimizeStringConcat \
     -Djava.security.egd=file:/dev/./urandom \
     -Dspring.jmx.enabled=false \
     -Dspring.main.lazy-initialization=false \
     -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -XX:+PrintCommandLineFlags \
     -XX:+UnlockDiagnosticVMOptions \
     -Dspring.backgroundpreinitializer.ignore=true \
     -Dspring.main.register-shutdown-hook=false"

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:9090/actuator/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# ===================================
# ETAPA 1: Construcción optimizada
# ===================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar y limpiar agresivamente
RUN mvn clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar && \
    # Eliminar archivos innecesarios
    find . -type f \( -name "*.txt" -o -name "*.md" -o -name "*.html" \) -delete && \
    find . -type f \( -name "*.dtd" -o -name "*.xsd" -o -name "*.png" -o -name "*.gif" -o -name "*.jpg" \) -delete && \
    find . -type d -name "licenses" -exec rm -rf {} + 2>/dev/null || true && \
    rm -rf /root/.m2/repository target/*.jar

# ===================================
# ETAPA 3: Imagen final ultra-ligera con OpenJDK completo
# ===================================
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no-root
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Configurar directorio de trabajo
WORKDIR /app

# Copiar aplicación
COPY --from=build --chown=spring:spring /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build --chown=spring:spring /app/target/dependency/META-INF /app/META-INF
COPY --from=build --chown=spring:spring /app/target/dependency/BOOT-INF/classes /app

# Cambiar a usuario no-root
USER spring

# Configurar JVM para arranque rápido y SSL
ENV JAVA_OPTS="-Xms64m \
    -Xmx256m \
    -XX:+UseSerialGC \
    -XX:+UseCompressedOops \
    -XX:+TieredCompilation \
    -XX:TieredStopAtLevel=1 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false \
    -Dspring.main.lazy-initialization=true \
    -Dlogging.level.root=ERROR \
    -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3"

# Puerto de la aplicación
EXPOSE 8084

# Comando optimizado
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app:/app/lib/* pe.edu.vallegrande.ms_infraestructura.MsInfraestructuraApplication"]

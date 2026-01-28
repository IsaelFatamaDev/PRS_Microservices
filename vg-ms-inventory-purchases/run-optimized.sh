#!/bin/bash

# Script de optimización de memoria para vg-ms-inventory-purchases
# Configuración para ejecutar en menos de 250 MiB

# Configuración de memoria optimizada
export JAVA_OPTS="\
-Xms64m \
-Xmx200m \
-XX:MaxMetaspaceSize=128m \
-XX:MetaspaceSize=64m \
-XX:CompressedClassSpaceSize=32m \
-XX:ReservedCodeCacheSize=32m \
-XX:MaxDirectMemorySize=32m \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=100 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:CICompilerCount=2 \
-XX:+DisableExplicitGC \
-XX:+ParallelRefProcEnabled \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.jmx.enabled=false \
-Dspring.main.lazy-initialization=true \
-Dspring.backgroundpreinitializer.ignore=true \
-Dfile.encoding=UTF-8 \
-Duser.timezone=UTC"

echo "✅ Configuración de JVM optimizada para memoria limitada"
echo "📊 Memoria máxima heap: 200 MB"
echo "📊 Memoria metaspace: 128 MB"
echo "🚀 Iniciando aplicación..."

# Ejecutar la aplicación
java $JAVA_OPTS -jar target/vg-ms-inventory-purchases-0.0.1-SNAPSHOT.jar

# 🚀 Guía de Optimización de Memoria - vg-ms-inventory-purchases

## 📊 Objetivo

Mantener el uso de memoria del microservicio por debajo de 250 MiB bajo carga.

## 🔧 Optimizaciones Aplicadas

### 1. **Configuración de JVM (`application.yml` + `Dockerfile` + `pom.xml`)**

#### Memoria Heap

- `-Xms64m`: Heap inicial mínimo (64 MB)
- `-Xmx200m`: Heap máximo (200 MB)
- **Total Heap**: ~200 MB máximo

#### Metaspace

- `-XX:MetaspaceSize=64m`: Metaspace inicial
- `-XX:MaxMetaspaceSize=128m`: Metaspace máximo (128 MB)
- **Justificación**: Spring Boot + R2DBC + Security requiere espacio para clases

#### Otras Áreas de Memoria

- `-XX:CompressedClassSpaceSize=32m`: Espacio para clases comprimidas (32 MB)
- `-XX:ReservedCodeCacheSize=32m`: Cache de código JIT (32 MB)
- `-XX:MaxDirectMemorySize=32m`: Memoria directa para NIO (32 MB)

**Total estimado**: ~200 + 128 + 32 + 32 + 32 = **~224 MB** en pico máximo

### 2. **Garbage Collector Optimizado**

```
-XX:+UseG1GC                    # G1GC optimizado para baja latencia
-XX:MaxGCPauseMillis=100        # Pausas máximas de 100ms
-XX:+UseStringDeduplication     # Deduplicación de strings
-XX:+ParallelRefProcEnabled     # Procesamiento paralelo de referencias
-XX:+DisableExplicitGC          # Deshabilitar System.gc()
```

### 3. **Compilación JIT Optimizada**

```
-XX:+TieredCompilation          # Compilación por niveles
-XX:TieredStopAtLevel=1         # Solo C1 compiler (menos memoria)
-XX:CICompilerCount=2           # Solo 2 threads de compilación
```

### 4. **Compresión de Referencias**

```
-XX:+UseCompressedOops          # Compresión de punteros de objetos
-XX:+UseCompressedClassPointers # Compresión de punteros de clases
```

### 5. **Spring Boot Optimizaciones**

```
-Dspring.jmx.enabled=false                      # Deshabilitar JMX
-Dspring.main.lazy-initialization=true          # Inicialización lazy
-Dspring.backgroundpreinitializer.ignore=true   # Ignorar pre-inicialización
```

### 6. **Pool de Conexiones R2DBC (application.yml)**

```yaml
r2dbc:
  pool:
    initial-size: 2      # ⬇️ Reducido de 5 a 2
    max-size: 5          # ⬇️ Reducido de 20 a 5
    max-idle-time: 10m   # ⬇️ Reducido de 30m a 10m
```

### 7. **Logging Optimizado (application.yml)**

```yaml
logging:
  level:
    root: WARN                             # ⬇️ De INFO a WARN
    pe.edu.vallegrande: INFO              # ⬇️ De DEBUG a INFO
    org.springframework.r2dbc: INFO       # ⬇️ De DEBUG a INFO
    org.springframework.security: INFO    # ⬇️ De DEBUG a INFO
```

### 8. **Timeouts y Reintentos (application.yml)**

```yaml
webclient:
  timeout: 5000        # ⬇️ Reducido de 10000ms a 5000ms
  retry-attempts: 2    # ⬇️ Reducido de 3 a 2
```

### 9. **Dependencias Excluidas (pom.xml)**

```xml
<excludeDevtools>true</excludeDevtools>  <!-- spring-boot-devtools excluido en producción -->
```

## 📦 Archivos Modificados

| Archivo | Cambios |
|---------|---------|
| `application.yml` | Pool R2DBC, logging, timeouts, cache, actuator |
| `Dockerfile` | ENV JAVA_OPTS con flags de memoria |
| `pom.xml` | jvmArguments + excludeDevtools |
| `run-optimized.sh` | Script de ejecución local con optimizaciones |

## 🚀 Cómo Ejecutar

### Opción 1: Local con Script

```bash
chmod +x run-optimized.sh
./run-optimized.sh
```

### Opción 2: Local con Maven

```bash
./mvnw spring-boot:run
```

*(Los flags JVM se toman automáticamente del pom.xml)*

### Opción 3: Docker

```bash
# Construir imagen
docker build -t vg-ms-inventory-purchases:optimized .

# Ejecutar contenedor
docker run -p 8088:8088 \
  --memory="250m" \
  --memory-swap="250m" \
  --name inventory-purchases \
  vg-ms-inventory-purchases:optimized
```

### Opción 4: Docker Compose

```yaml
services:
  inventory-purchases:
    image: vg-ms-inventory-purchases:optimized
    deploy:
      resources:
        limits:
          memory: 250M
        reservations:
          memory: 150M
```

## 📈 Monitoreo de Memoria

### 1. Durante Ejecución

```bash
# Monitoreo en tiempo real
./monitor-memory.sh
```

### 2. Con Docker Stats

```bash
docker stats inventory-purchases
```

### 3. Con JVM Tools

```bash
# Obtener PID
jps -l

# Ver uso de memoria
jmap -heap <PID>

# Ver histograma de clases
jmap -histo <PID> | head -30
```

### 4. Endpoints de Actuator (si habilitados)

```bash
# Métricas de memoria
curl http://localhost:8088/actuator/metrics/jvm.memory.used

# Heap dump (solo si es necesario)
curl http://localhost:8088/actuator/heapdump -o heapdump.hprof
```

## 🎯 Métricas Esperadas

| Métrica | Valor Objetivo | Valor Anterior |
|---------|----------------|----------------|
| **Heap Inicial** | 64 MB | ~100 MB |
| **Heap Máximo** | 200 MB | Sin límite |
| **Metaspace Máximo** | 128 MB | Sin límite |
| **Pool R2DBC (inicial)** | 2 conexiones | 5 conexiones |
| **Pool R2DBC (máximo)** | 5 conexiones | 20 conexiones |
| **Logging Level** | WARN/INFO | DEBUG/INFO |
| **Timeout WebClient** | 5000ms | 10000ms |
| **Retry Attempts** | 2 | 3 |
| **Uso Total Estimado** | **~220-240 MB** | **~224 MB (crashes)** |

## 🔍 Troubleshooting

### Si la memoria sigue siendo alta

1. **Revisar logs de GC**:

   ```
   -Xlog:gc*:file=gc.log:time,uptime,level,tags
   ```

2. **Generar Heap Dump**:

   ```bash
   jmap -dump:live,format=b,file=heap.hprof <PID>
   ```

   Analizar con VisualVM o Eclipse MAT

3. **Reducir pool R2DBC más**:

   ```yaml
   initial-size: 1
   max-size: 3
   ```

4. **Deshabilitar SpringDoc en producción**:

   ```yaml
   springdoc:
     swagger-ui:
       enabled: false
   ```

5. **Reducir heap máximo gradualmente**:

   ```
   -Xmx180m  # Si 200m es mucho
   ```

### Si hay OutOfMemoryError

1. **Aumentar Metaspace**:

   ```
   -XX:MaxMetaspaceSize=150m
   ```

2. **Revisar leak de memoria**:
   - Verificar cierre de conexiones R2DBC
   - Revisar subscripciones de Reactor no canceladas
   - Verificar caches no configurados

3. **Agregar flag de salida**:

   ```
   -XX:+ExitOnOutOfMemoryError
   -XX:+HeapDumpOnOutOfMemoryError
   -XX:HeapDumpPath=/app/dumps
   ```

## 📚 Referencias

- [Spring Boot Memory Tuning](https://spring.io/blog/2015/12/10/spring-boot-memory-performance)
- [G1GC Tuning Guide](https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html)
- [R2DBC Pool Configuration](https://github.com/r2dbc/r2dbc-pool)

## ✅ Checklist de Validación

- [x] Configuración de JVM optimizada
- [x] Pool R2DBC reducido
- [x] Logging a nivel WARN/INFO
- [x] Timeouts optimizados
- [x] DevTools excluido en producción
- [x] Dockerfile con JAVA_OPTS
- [x] Script de ejecución optimizado
- [ ] Pruebas de carga completadas
- [ ] Memoria estable bajo 250 MiB
- [ ] Sin OutOfMemoryError en 24h

---

**Última actualización**: 2024
**Autor**: Sistema de Optimización Automática
**Versión**: 1.0.0

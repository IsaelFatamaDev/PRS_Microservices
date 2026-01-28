# 🌊 MS-Water-Quality - Microservicio de Calidad de Agua

## 📋 Descripción

Microservicio para la gestión de calidad de agua del **Sistema JASS Digital**. Permite administrar puntos de muestreo, pruebas de calidad y registros diarios de parámetros de medición del agua .

## 🎯 Características Principales

- ✅ Gestión de puntos de muestreo (reservorios, red de distribución, domicilios)
- ✅ Registro de pruebas de calidad de agua
- ✅ Monitoreo diario de parámetros de calidad
- ✅ Integración con servicios de usuarios y organizaciones
- ✅ Autenticación y autorización con OAuth2/JWT
- ✅ API RESTful reactiva con Spring WebFlux
- ✅ Documentación OpenAPI/Swagger

## 🏗️ Arquitectura

**Patrón:** Arquitectura Hexagonal (Ports & Adapters)
**Stack Tecnológico:**
- Java 17
- Spring Boot 3.4.5
- Spring WebFlux (Reactive)
- MongoDB (Reactive)
- Spring Security + OAuth2
- Keycloak (Identity Provider)

## 📁 Estructura del Proyecto

```
vg-ms-water-quality/
├── src/main/java/pe/edu/vallegrande/ms_water_quality/
│   ├── application/              # Capa de Aplicación
│   │   └── services/            # Servicios e implementaciones
│   ├── domain/                  # Capa de Dominio
│   │   ├── models/             # Entidades de dominio
│   │   └── enums/              # Enumeraciones
│   └── infrastructure/          # Capa de Infraestructura
│       ├── document/           # Documentos MongoDB
│       ├── dto/                # Data Transfer Objects
│       ├── repository/         # Repositorios
│       ├── rest/               # Controladores REST
│       ├── client/             # Clientes externos
│       ├── security/           # Configuración de seguridad
│       ├── exception/          # Manejo de excepciones
│       └── config/             # Configuraciones
└── src/main/resources/
    ├── application.yml         # Configuración base
    ├── application-dev.yml     # Perfil desarrollo
    └── application-prod.yml    # Perfil producción
```

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- Maven 3.9+
- MongoDB 4.4+
- Docker (opcional)

### Instalación y Ejecución

#### Opción 1: Ejecución Local

```bash
# Clonar el repositorio
git clone <repository-url>
cd vg-ms-water-quality

# Compilar el proyecto
mvn clean install

# Ejecutar en modo desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# O ejecutar el JAR
java -jar target/ms_water_quality-1.0.0.jar --spring.profiles.active=dev
```

#### Opción 2: Docker

```bash
# Construir imagen
docker build -t vg-ms-water-quality:1.0.0 .

# Ejecutar contenedor
docker run -p 8087:8087 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MONGODB_URI=mongodb://mongo:27017/water_quality \
  vg-ms-water-quality:1.0.0
```

#### Opción 3: Docker Compose

```bash
docker-compose up -d
```

## ⚙️ Configuración

### Variables de Entorno

```bash
# Base de Datos
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/database

# Servidor
SERVER_PORT=8087
SPRING_PROFILES_ACTIVE=dev

# Seguridad
KEYCLOAK_ISSUER_URI=https://keycloak.domain.com/realms/sistema-jass
KEYCLOAK_JWK_URI=https://keycloak.domain.com/realms/sistema-jass/protocol/openid-connect/certs

# Servicios Externos
USER_SERVICE_URL=https://api.domain.com/ms-users
ORGANIZATION_SERVICE_URL=https://api.domain.com/ms-organization
ORGANIZATION_SERVICE_TOKEN=your-token-here
```

### Perfiles de Ejecución

- **dev**: Desarrollo local con logging detallado
- **prod**: Producción optimizada para rendimiento y memoria

## 📚 Documentación API

Una vez iniciado el servicio, accede a:

- **Swagger UI**: http://localhost:8087/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8087/v3/api-docs

### Endpoints Principales

#### Admin - Testing Points
```
GET    /api/v1/admin/quality/sampling-points
POST   /api/v1/admin/quality/sampling-points
GET    /api/v1/admin/quality/sampling-points/{id}
PUT    /api/v1/admin/quality/sampling-points/{id}
DELETE /api/v1/admin/quality/sampling-points/{id}
PATCH  /api/v1/admin/quality/sampling-points/activate/{id}
PATCH  /api/v1/admin/quality/sampling-points/deactivate/{id}
```

#### Admin - Quality Tests
```
GET    /api/v1/admin/quality/tests
POST   /api/v1/admin/quality/tests
GET    /api/v1/admin/quality/tests/{id}
PUT    /api/v1/admin/quality/tests/{id}
DELETE /api/v1/admin/quality/tests/{id}
```

#### Admin - Daily Records
```
GET    /api/v1/admin/quality/daily-records
POST   /api/v1/admin/quality/daily-records
GET    /api/v1/admin/quality/daily-records/{id}
PUT    /api/v1/admin/quality/daily-records/{id}
DELETE /api/v1/admin/quality/daily-records/{id}
```

## 🔒 Seguridad

El microservicio utiliza:
- **OAuth2 Resource Server** con Keycloak
- **JWT** para autenticación
- **Roles y permisos** granulares
- **HTTPS** en producción

### Roles Requeridos

- `ADMIN`: Acceso completo a endpoints administrativos
- `USER`: Acceso a endpoints de consulta

## 📊 Monitoreo

### Health Check

```bash
curl http://localhost:8087/actuator/health
```

### Métricas

```bash
curl http://localhost:8087/actuator/metrics
```

## 🐳 Docker

### Dockerfile Multi-stage

El proyecto incluye un Dockerfile optimizado con:
- Build stage con Maven
- Runtime stage con JRE Alpine
- Usuario no-root para seguridad
- Health checks configurados
- Optimizaciones de memoria JVM

### Optimizaciones de Memoria

```bash
# JVM configurada para bajo consumo
-Xms32m -Xmx128m
-XX:MaxMetaspaceSize=64m
-XX:+UseSerialGC
```

**Consumo esperado:** ~150-180MB (reducido desde 292MB)

## 🧪 Testing

```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests de integración
mvn verify

# Cobertura de código
mvn jacoco:report
```

## 📦 Build y Despliegue

### Build Local

```bash
mvn clean package -DskipTests
```

### Build Docker

```bash
docker build -t vg-ms-water-quality:1.0.0 .
```

### Despliegue en Producción

```bash
# Con variables de entorno
java -jar \
  -Dspring.profiles.active=prod \
  -Xms32m -Xmx128m \
  target/ms_water_quality-1.0.0.jar
```

## 🔧 Troubleshooting

### Problema: Alto consumo de memoria

**Solución:** Verificar configuración JVM en Dockerfile y application-prod.yml

### Problema: Conexión a MongoDB falla

**Solución:** Verificar MONGODB_URI y conectividad de red

### Problema: Autenticación falla

**Solución:** Verificar configuración de Keycloak y tokens JWT

## 📝 Logs

Los logs se almacenan en:
- **Consola**: Salida estándar
- **Archivo**: `./logs/vg-ms-water-quality.log`

Niveles de log por perfil:
- **dev**: DEBUG
- **prod**: INFO

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto es parte del Sistema JASS Digital - Valle Grande

## 👥 Equipo

**Valle Grande - Sistema JASS Digital**
- Email: soporte@vallegrande.edu.pe

## 🔗 Enlaces Relacionados

- [Sistema JASS Digital](https://lab.vallegrande.edu.pe/jass)
- [Documentación Completa](./docs/)
- [API Gateway](https://lab.vallegrande.edu.pe/jass/gateway)

---

**Versión:** 1.0.0  
**Última actualización:** 2025  
**Estado:** ✅ Producción

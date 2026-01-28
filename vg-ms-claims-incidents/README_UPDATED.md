# Microservicio de Gestión de Reclamos e Incidentes

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.11-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Reactive-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Descripción

Microservicio reactivo para la gestión integral de reclamos e incidentes en sistemas JASS (Juntas Administradoras de Servicios de Saneamiento). Implementado siguiendo arquitectura hexagonal y mejores prácticas de desarrollo.

## 🏗️ Arquitectura

Este proyecto sigue **Arquitectura Hexagonal** (Ports & Adapters):

```
src/
├── main/
│   ├── java/
│   │   └── pe/edu/vallegrande/vg_ms_claims_incidents/
│   │       ├── VgMsClaimsIncidentsApplication.java
│   │       ├── domain/              # Capa de Dominio (Lógica de negocio pura)
│   │       │   ├── models/          # Entidades de dominio
│   │       │   └── enums/           # Enumeraciones de dominio
│   │       ├── application/         # Capa de Aplicación (Casos de uso)
│   │       │   ├── services/        # Interfaces de servicios
│   │       │   └── config/          # Configuraciones
│   │       └── infrastructure/      # Capa de Infraestructura (Adaptadores)
│   │           ├── rest/            # Controllers REST (Adaptador de entrada)
│   │           ├── repository/      # Repositorios MongoDB (Adaptador de salida)
│   │           ├── dto/             # Data Transfer Objects
│   │           ├── mapper/          # Mappers Domain <-> DTO
│   │           ├── exception/       # Excepciones personalizadas
│   │           ├── handlers/        # Manejadores globales de errores
│   │           ├── security/        # Configuración de seguridad
│   │           └── client/          # Clientes para servicios externos
│   └── resources/
│       └── application.yml          # Configuración de la aplicación
```

## 🚀 Tecnologías

- **Java 17** - LTS version
- **Spring Boot 3.2.11** - Framework principal
- **Spring WebFlux** - Programación reactiva
- **MongoDB Reactive** - Base de datos NoSQL reactiva
- **Spring Security** - Seguridad y autenticación
- **JWE (JSON Web Encryption)** - Comunicación segura MS-to-MS
- **Lombok** - Reducción de código boilerplate
- **Bean Validation** - Validación de datos
- **Micrometer + Prometheus** - Métricas y monitoreo
- **SpringDoc OpenAPI** - Documentación de API
- **SLF4J + Logback** - Logging estructurado

## 📦 Características Principales

### ✅ Implementadas

- ✅ Arquitectura Hexagonal (Ports & Adapters)
- ✅ Programación Reactiva con Project Reactor
- ✅ Validación de datos con Bean Validation
- ✅ Respuestas estandarizadas con `ResponseDto<T>`
- ✅ Manejo centralizado de excepciones
- ✅ Códigos de estado HTTP apropiados (200, 201, 400, 401, 403, 404, 409, 500)
- ✅ Documentación OpenAPI/Swagger completa
- ✅ Métricas con Micrometer y Prometheus
- ✅ Health checks personalizados
- ✅ Logging estructurado
- ✅ Seguridad con JWE para MS-to-MS
- ✅ Principios SOLID aplicados
- ✅ Clean Code practices
- ✅ Lombok para reducir boilerplate

### 🔄 En Desarrollo

- 🔄 Tests unitarios y de integración completos
- 🔄 Circuit Breaker con Resilience4j
- 🔄 Distributed tracing con Sleuth
- 🔄 Cache distribuido con Redis

## 🛠️ Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- MongoDB 4.4+
- Docker y Docker Compose (opcional)

## 🔧 Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-organizacion/vg-ms-claims-incidents.git
cd vg-ms-claims-incidents
```

### 2. Configurar variables de entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# MongoDB
SPRING_DATA_MONGODB_URL=mongodb+srv://user:password@cluster.mongodb.net/database
SPRING_DATA_MONGODB_DATABASE=JASS_DIGITAL

# Servidor
SERVER_PORT=8089

# Servicios Externos
USER_SERVICE_URL=https://api.example.com/ms-users
ORGANIZATION_ID=6896b2ecf3e398570ffd99d3

# Seguridad JWT
JWT_PRIVATE_KEY=your-private-key-here
JWT_PUBLIC_KEY=your-public-key-here

# Logging
LOG_LEVEL=DEBUG
SECURITY_LOG_LEVEL=DEBUG

# Ambiente
SPRING_PROFILES_ACTIVE=development
```

### 3. Compilar el proyecto

```bash
mvn clean install
```

### 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

O con perfil específico:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

## 🐳 Docker

### Construir imagen

```bash
docker build -t vg-ms-claims-incidents:1.0.0 .
```

### Ejecutar con Docker Compose

```bash
docker-compose up -d
```

## 📚 Documentación API

Una vez iniciada la aplicación, acceder a:

- **Swagger UI**: http://localhost:8089/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8089/v3/api-docs

### Endpoints Principales

#### Admin Endpoints (`/api/v1/admin`)

```
GET    /api/v1/admin/incidents              - Listar todos los incidentes
GET    /api/v1/admin/incidents/{id}         - Obtener incidente por ID
POST   /api/v1/admin/incidents              - Crear nuevo incidente
PUT    /api/v1/admin/incidents/{id}         - Actualizar incidente
DELETE /api/v1/admin/incidents/{id}         - Eliminar incidente
PATCH  /api/v1/admin/incidents/{id}/restore - Restaurar incidente
GET    /api/v1/admin/incidents/stats        - Estadísticas de incidentes
```

#### Client Endpoints (`/api/v1/client`)

```
POST   /api/v1/client/incidents/create      - Crear incidente (cliente)
GET    /api/v1/client/incidents/my-incidents - Mis incidentes
GET    /api/v1/client/incidents/track/{id}  - Seguimiento de incidente
GET    /api/v1/client/incidents/search      - Buscar incidentes
```

## 📊 Métricas y Monitoreo

### Actuator Endpoints

- **Health Check**: http://localhost:8089/actuator/health
- **Info**: http://localhost:8089/actuator/info
- **Metrics**: http://localhost:8089/actuator/metrics
- **Prometheus**: http://localhost:8089/actuator/prometheus

### Métricas Personalizadas

- `incidents.created.total` - Total de incidentes creados
- `incidents.resolved.total` - Total de incidentes resueltos
- `incidents.by.severity` - Incidentes por severidad
- `incidents.response.time` - Tiempo de respuesta por operación

## 🔐 Seguridad

### Autenticación

El microservicio utiliza **JWE (JSON Web Encryption)** para autenticación en comunicación MS-to-MS.

### Roles y Permisos

- **ADMIN**: Acceso completo a todos los endpoints
- **USER/CLIENT**: Acceso limitado a endpoints de cliente

### Ejemplo de Request con Token

```bash
curl -X GET http://localhost:8089/api/v1/admin/incidents \
  -H "Authorization: Bearer YOUR_JWE_TOKEN_HERE"
```

## 📝 Convenciones de Código

### Commits (Conventional Commits)

```
feat: Agregar endpoint para estadísticas de incidentes
fix: Corregir validación en creación de incidentes
docs: Actualizar documentación de API
refactor: Refactorizar servicio de incidentes
chore: Actualizar dependencias
test: Agregar tests para IncidentService
```

### Código

- **Clean Code**: Nombres descriptivos, funciones pequeñas, SRP
- **SOLID Principles**: Aplicados en toda la arquitectura
- **DRY**: No repetir código
- **KISS**: Keep It Simple, Stupid

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

## 🚦 Estado del Proyecto

- ✅ **Producción**: Listo para deploy
- ⚠️ **Testing**: Cobertura en progreso (objetivo: 80%)
- 🔄 **Documentación**: En mejora continua

## 📖 Guías de Referencia

- [Arquitectura Hexagonal](./docs/ARQUITECTURA_HEXAGONAL.md)
- [Guía de Contribución](./CONTRIBUTING.md)
- [Convenciones de Commits](./docs/CONVENTIONAL_COMMITS.md)
- [Troubleshooting](./docs/TROUBLESHOOTING.md)

## 👥 Contribuir

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 Autores

- **Valle Grande University Team** - *Trabajo Inicial*

## 📞 Contacto

- **Email**: soporte@vallegrande.edu.pe
- **Website**: https://vallegrande.edu.pe

---

⭐️ Si este proyecto te ayudó, considera darle una estrella en GitHub

# 🚀 MS-Distribution - Sistema JASS Digital

Microservicio de gestión de distribución para el Sistema JASS Digital. Maneja programas de distribución, rutas, horarios y tarifas.

## � *Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Ejecución](#-ejecución)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [API Endpoints](#-api-endpoints)
- [Configuración](#-configuración)
- [Docker](#-docker)
- [CI/CD](#-cicd)
- [Documentación](#-documentación)

## ✨ Características

- ✅ Gestión de programas de distribución
- ✅ **Control de entrega de agua** (CON_AGUA / SIN_AGUA) 💧
- ✅ Administración de rutas
- ✅ Control de horarios
- ✅ Gestión de tarifas
- ✅ Estadísticas de entrega de agua
- ✅ Autenticación con Keycloak (OAuth2/JWT)
- ✅ Seguridad interna con JWE
- ✅ Base de datos MongoDB
- ✅ API RESTful reactiva (WebFlux)
- ✅ Imagen Docker optimizada < 250 MB

## � Tecn ologías

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.3.5** - Framework
- **Spring WebFlux** - Programación reactiva
- **MongoDB** - Base de datos NoSQL
- **Keycloak** - Autenticación y autorización
- **Docker** - Contenedorización
- **GitLab CI/CD** - Integración continua
- **Maven** - Gestión de dependencias

## 📦 Requisitos Previos

- Java 21 o superior
- Maven 3.9+ (incluido Maven Wrapper)
- Docker y Docker Compose (opcional)
- MongoDB Atlas (o instancia local)
- Git

## �  Instalación

### 1. Clonar el repositorio

```bash
git clone https://gitlab.com/tu-usuario/vg-ms-distribution.git
cd vg-ms-distribution
```

### 2. Configurar credenciales de MongoDB

Copia el archivo de ejemplo y configura tus credenciales:

```bash
# Windows
copy .env.example .mongo.env

# Linux/Mac
cp .env.example .mongo.env
```

Edita `.mongo.env` con tus credenciales:

```properties
MONGO_USERNAME=tu_usuario
MONGO_PASSWORD=tu_password
MONGO_HOST=tu_host.mongodb.net
MONGO_DATABASE=distribution_db
```

## 🚀 Ejecución

### Opción 1: Ejecución Local (Recomendado para desarrollo)

**Windows:**
```cmd
local-mongo.bat
```

**Linux/Mac:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

El microservicio estará disponible en: `http://localhost:8086/jass/ms-distribution`

### Opción 2: Con Docker Compose

```bash
# Construir y ejecutar
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

### Opción 3: Solo Docker

```bash
# Construir imagen
docker build -t vg-ms-distribution:latest .

# Ejecutar contenedor
docker run -p 8086:8086 \
  -e MONGO_USERNAME=tu_usuario \
  -e MONGO_PASSWORD=tu_password \
  -e MONGO_HOST=tu_host.mongodb.net \
  -e MONGO_DATABASE=distribution_db \
  vg-ms-distribution:latest
```

## 🧪 Probar la API

### Health Check

```bash
curl http://localhost:8086/jass/ms-distribution/actuator/health
```

### Probar todos los endpoints

**Windows:**
```cmd
test-api.bat
```

**Linux/Mac:**
```bash
curl http://localhost:8086/jass/ms-distribution/api/admin/programs
curl http://localhost:8086/jass/ms-distribution/api/admin/routes
curl http://localhost:8086/jass/ms-distribution/api/admin/schedules
curl http://localhost:8086/jass/ms-distribution/api/admin/fares
```

## 📁 Estructura del Proyecto

```
vg-ms-distribution/
├── 📄 pom.xml                            # Configuración Maven
├── 📄 Dockerfile                         # Imagen Docker optimizada
├── 📄 docker-compose.yml                 # Orquestación Docker
├── 📄 .gitlab-ci.yml                     # Pipeline CI/CD
├── 📄 local-mongo.bat                    # Script ejecución local
├── 📄 test-api.bat                       # Script pruebas API
├── 📄 .mongo.env                         # Credenciales local (no se sube)
├── 📄 .env                               # Credenciales Docker (no se sube)
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/pe/edu/vallegrande/msdistribution/
    │   │   ├── 📄 VgMsDistribution.java           # Clase principal
    │   │   │
    │   │   ├── 📁 application/                    # Capa de Aplicación
    │   │   │   └── 📁 services/                   # Servicios de negocio
    │   │   │       ├── DistributionProgramService.java
    │   │   │       ├── DistributionRouteService.java
    │   │   │       ├── DistributionScheduleService.java
    │   │   │       ├── FareService.java
    │   │   │       └── 📁 impl/                   # Implementaciones
    │   │   │
    │   │   ├── 📁 domain/                         # Capa de Dominio
    │   │   │   ├── 📁 models/                     # Entidades
    │   │   │   │   ├── DistributionProgram.java
    │   │   │   │   ├── DistributionRoute.java
    │   │   │   │   ├── DistributionSchedule.java
    │   │   │   │   └── Fare.java
    │   │   │   └── 📁 enums/                      # Enumeraciones
    │   │   │
    │   │   └── 📁 infrastructure/                 # Capa de Infraestructura
    │   │       ├── 📁 document/                   # Documentos MongoDB
    │   │       ├── 📁 dto/                        # DTOs
    │   │       │   ├── 📁 request/
    │   │       │   ├── 📁 response/
    │   │       │   └── 📁 common/
    │   │       ├── 📁 repository/                 # Repositorios
    │   │       ├── 📁 mapper/                     # Mappers
    │   │       ├── 📁 rest/                       # Controladores REST
    │   │       │   └── 📁 admin/
    │   │       │       └── AdminRest.java
    │   │       ├── 📁 security/                   # Seguridad
    │   │       ├── 📁 exception/                  # Manejo de excepciones
    │   │       └── 📁 config/                     # Configuraciones
    │   │
    │   └── 📁 resources/
    │       ├── 📄 application.yml                 # Configuración base
    │       ├── 📄 application-dev.yml             # Perfil desarrollo
    │       ├── 📄 application-prod.yml            # Perfil producción
    │       └── 📁 doc/                            # Documentación
    │           ├── API_DOCUMENTATION.md
    │           └── ARCHITECTURE.md
    │
    └── 📁 test/                                   # Tests
```

## 🌐 API Endpoints

### Admin Endpoints (Requiere rol ADMIN)

**Base URL:** `http://localhost:8086/jass/ms-distribution/internal/admin`

#### Dashboard
- `GET /dashboard/stats` - Estadísticas del dashboard
- `GET /dashboard/summary` - Resumen del sistema

#### Programas de Distribución
- `GET /program` - Listar todos los programas
- `GET /program/{id}` - Obtener programa por ID
- `POST /program` - Crear nuevo programa
- `PUT /program/{id}` - Actualizar programa
- `DELETE /program/{id}` - Eliminar programa

#### Estado de Entrega de Agua 💧
- `PUT /program/{id}/water-status` - Actualizar estado de entrega de agua
- `GET /program/water-status/{status}` - Obtener programas por estado
- `GET /program/with-water` - Programas CON agua
- `GET /program/without-water` - Programas SIN agua
- `GET /program/water-stats` - Estadísticas de entrega

#### Rutas
- `GET /route` - Listar todas las rutas
- `GET /route/{id}` - Obtener ruta por ID
- `POST /route` - Crear nueva ruta
- `PUT /route/{id}` - Actualizar ruta
- `DELETE /route/{id}` - Eliminar ruta

#### Horarios
- `GET /schedule` - Listar todos los horarios
- `GET /schedule/{id}` - Obtener horario por ID
- `POST /schedule` - Crear nuevo horario
- `PUT /schedule/{id}` - Actualizar horario
- `DELETE /schedule/{id}` - Eliminar horario

#### Tarifas
- `GET /fare` - Listar todas las tarifas
- `GET /fare/{id}` - Obtener tarifa por ID
- `POST /fare` - Crear nueva tarifa
- `PUT /fare/{id}` - Actualizar tarifa
- `DELETE /fare/{id}` - Eliminar tarifa

## ⚙️ Configuración

### Perfiles de Spring

- **default** - Configuración base
- **dev** - Desarrollo (logs detallados)
- **prod** - Producción (optimizado)

### Variables de Entorno

| Variable | Descripción | Requerido |
|----------|-------------|-----------|
| `MONGO_USERNAME` | Usuario de MongoDB | ✅ |
| `MONGO_PASSWORD` | Contraseña de MongoDB | ✅ |
| `MONGO_HOST` | Host de MongoDB | ✅ |
| `MONGO_DATABASE` | Base de datos | ✅ |
| `JWE_INTERNAL_SECRET` | Secreto para JWE | ✅ |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | ❌ |

### Archivos de Configuración

| Archivo | Descripción | Se sube al repo |
|---------|-------------|-----------------|
| `.mongo.env` | Credenciales local | ❌ NO |
| `.env` | Credenciales Docker | ❌ NO |
| `.env.example` | Plantilla | ✅ SÍ |
| `application.yml` | Config base | ✅ SÍ |
| `application-dev.yml` | Config desarrollo | ✅ SÍ |
| `application-prod.yml` | Config producción | ✅ SÍ |

## 🐳 Docker

### Características de la Imagen

- ✅ Multi-stage build (3 etapas)
- ✅ Imagen base Alpine (ligera)
- ✅ Usuario no-root (seguridad)
- ✅ Tamaño < 250 MB
- ✅ JVM optimizado para contenedores
- ✅ Health check incluido

### Comandos Docker

```bash
# Construir imagen
docker build -t vg-ms-distribution:latest .

# Ejecutar con docker-compose
docker-compose up --build

# Ver logs
docker-compose logs -f ms-distribution

# Detener
docker-compose down

# Reconstruir sin caché
docker-compose build --no-cache
```

## 🔄 CI/CD

### Pipeline GitLab

El proyecto incluye un pipeline automatizado que:

1. ✅ Compila el proyecto con Maven
2. ✅ Ejecuta los tests
3. ✅ Construye la imagen Docker
4. ✅ Sube la imagen a Docker Hub

### Configurar Variables en GitLab

Ve a: **Settings > CI/CD > Variables** y agrega:

| Variable | Valor | Protected | Masked |
|----------|-------|-----------|--------|
| `DOCKERHUB_USERNAME` | tu_usuario | ✅ | ❌ |
| `DOCKERHUB_TOKEN` | tu_token | ✅ | ✅ |

### Imagen en Docker Hub

```
victorcuaresma/jass-ms-distribution:250mib
```

## 📚 Documentación

### Documentación Adicional

- [API Documentation](src/main/resources/doc/API_DOCUMENTATION.md) - Documentación detallada de la API
- [Architecture](src/main/resources/doc/ARCHITECTURE.md) - Arquitectura del sistema
- [Water Delivery Feature](WATER_DELIVERY_FEATURE.md) - 💧 Funcionalidad de entrega de agua
## 🔒 Seguridad

- **Autenticación**: OAuth2 con Keycloak
- **Autorización**: Roles y permisos
- **Comunicación interna**: JWE (JSON Web Encryption)
- **HTTPS**: Recomendado en producción
- **Secrets**: Variables de entorno

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=NombreDelTest

# Con cobertura
mvn clean test jacoco:report
```

## 📊 Monitoreo

### Actuator Endpoints

- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información del servicio
- `/actuator/metrics` - Métricas (solo en dev)

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agrega nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

## 📝 Licencia

Este proyecto es propiedad de Valle Grande.

## 👥 Equipo

- **Organización**: Valle Grande
- **Proyecto**: Sistema JASS Digital
- **Microservicio**: MS-Distribution
- **Versión**: 2.0.0

## 📞 Soporte

Para soporte técnico, contacta al equipo de desarrollo.

---

**Desarrollado con ❤️ por el equipo de Valle Grande**

# 🚀 MS-Infrastructure - Guía de Despliegue

## 📋 Tabla de Contenidos
- [Requisitos Previos](#requisitos-previos)
- [Configuración de Variables de Entorno](#configuración-de-variables-de-entorno)
- [Despliegue Local con Docker Compose](#despliegue-local-con-docker-compose)
- [Despliegue en Producción](#despliegue-en-producción)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)

## 🔧 Requisitos Previos

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (si no usas Docker)
- Keycloak 25.0.6 (opcional para desarrollo local)

## 🌍 Configuración de Variables de Entorno

### Desarrollo Local

1. Copiar el archivo de ejemplo:
```bash
cp .env.example .env
```

2. Editar `.env` con tus valores locales (opcional, ya tiene valores por defecto)

### Producción

Configurar las siguientes variables de entorno en tu servidor:

```bash
# Spring Profile
export SPRING_PROFILES_ACTIVE=prod

# Database
export DB_URL=jdbc:postgresql://tu-servidor:5432/infrastructure_prod
export DB_USERNAME=tu_usuario
export DB_PASSWORD=tu_password_seguro

# Server
export SERVER_PORT=8084

# Keycloak
export KEYCLOAK_ISSUER_URI=https://keycloak.tudominio.com/realms/jass
export KEYCLOAK_JWK_SET_URI=https://keycloak.tudominio.com/realms/jass/protocol/openid-connect/certs
export KEYCLOAK_SERVER_URL=https://keycloak.tudominio.com
export KEYCLOAK_REALM=jass
export KEYCLOAK_ADMIN_USERNAME=admin
export KEYCLOAK_ADMIN_PASSWORD=password_seguro
```

## 🐳 Despliegue Local con Docker Compose

### Opción 1: Con Docker Compose (Recomendado)

```bash
# Construir y levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f ms-infrastructure

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

Servicios disponibles:
- **PostgreSQL**: `localhost:5432`
- **MS-Infrastructure**: `localhost:8084`
- **Swagger UI**: http://localhost:8084/swagger-ui.html

### Opción 2: Desarrollo Local (sin Docker)

1. **Iniciar PostgreSQL**:
```bash
# Con Docker
docker run -d \
  --name postgres-infrastructure \
  -e POSTGRES_DB=infrastructure_dev \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

2. **Compilar y ejecutar**:
```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/ms_infraestructura-0.0.1-SNAPSHOT.jar
```

## 🏭 Despliegue en Producción

### 1. Construcción de la Imagen Docker

```bash
# Construir imagen
docker build -t isaelfatamadev/jass-ms-infrastructure:250mib .

# Subir a DockerHub
docker push isaelfatamadev/jass-ms-infrastructure:250mib
```

### 2. Despliegue con Docker

```bash
docker run -d \
  --name jass-ms-infrastructure \
  -p 8084:8084 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://tu-db:5432/infrastructure_prod \
  -e DB_USERNAME=usuario \
  -e DB_PASSWORD=password \
  -e KEYCLOAK_ISSUER_URI=https://keycloak.tudominio.com/realms/jass \
  isaelfatamadev/jass-ms-infrastructure:250mib
```

### 3. Verificación del Despliegue

```bash
# Health check
curl http://localhost:8084/actuator/health

# Respuesta esperada:
# {"status":"UP"}
```

## 🏗️ Arquitectura del Proyecto

### Estructura de Capas

```
src/main/java/pe/edu/vallegrande/ms_infraestructura/
├── application/
│   └── services/           # Lógica de negocio
│       ├── IWaterBoxService.java
│       └── impl/
│           └── WaterBoxService.java
├── domain/
│   ├── models/            # Modelos de dominio (sin anotaciones JPA)
│   │   ├── WaterBox.java
│   │   ├── WaterBoxAssignment.java
│   │   └── WaterBoxTransfer.java
│   └── enums/
│       ├── BoxType.java
│       └── Status.java
├── infrastructure/
│   ├── dto/               # DTOs de entrada/salida
│   │   ├── request/
│   │   └── response/
│   │       └── ResponseDto.java  # DTO estándar de respuesta
│   ├── persistence/
│   │   ├── entity/        # Entidades JPA (separadas del dominio)
│   │   │   ├── WaterBoxEntity.java
│   │   │   ├── WaterBoxAssignmentEntity.java
│   │   │   └── WaterBoxTransferEntity.java
│   │   └── mapper/        # Mappers Entity <-> Domain
│   │       ├── WaterBoxMapper.java
│   │       ├── WaterBoxAssignmentMapper.java
│   │       └── WaterBoxTransferMapper.java
│   ├── repository/        # Repositorios JPA
│   ├── rest/             # Controladores REST
│   └── exceptions/       # Manejo de excepciones
└── config/               # Configuraciones
    ├── SecurityConfig.java
    ├── KeycloakConfig.java
    └── JwtConfig.java
```

### Patrones Implementados

#### 1. Separación de Capas (Clean Architecture)
- **Domain**: Modelos de negocio puros (sin dependencias de frameworks)
- **Application**: Casos de uso y lógica de negocio
- **Infrastructure**: Implementaciones técnicas (BD, REST, etc.)

#### 2. Inyección de Dependencias por Constructor
```java
@Service
public class WaterBoxService implements IWaterBoxService {
    private final WaterBoxRepository waterBoxRepository;

    public WaterBoxService(WaterBoxRepository waterBoxRepository) {
        this.waterBoxRepository = waterBoxRepository;
    }
}
```

#### 3. Mappers para Separación Entity/Domain
```java
public class WaterBoxMapper {
    public static WaterBox toDomain(WaterBoxEntity entity) {
        // Conversión de Entity a Domain
    }
    
    public static WaterBoxEntity toEntity(WaterBox domain) {
        // Conversión de Domain a Entity
    }
}
```

#### 4. ResponseDto Estándar
```java
ResponseDto<WaterBoxResponse> response = ResponseDto.success(data);
// {
//   "success": true,
//   "message": "Operación exitosa",
//   "data": {...},
//   "timestamp": "2025-11-13T10:30:00"
// }
```

### Códigos de Estado HTTP

| Código | Descripción | Uso |
|--------|-------------|-----|
| 200 | OK | Operación exitosa |
| 201 | Created | Recurso creado exitosamente |
| 204 | No Content | Operación exitosa sin contenido |
| 400 | Bad Request | Error en la solicitud del cliente |
| 401 | Unauthorized | No autenticado |
| 403 | Forbidden | No autorizado |
| 404 | Not Found | Recurso no encontrado |
| 409 | Conflict | Conflicto de recursos |
| 500 | Internal Server Error | Error interno del servidor |

## 🔐 Seguridad

### OAuth2 Resource Server
El microservicio está protegido con OAuth2 usando Keycloak como proveedor de identidad.

### Endpoints Públicos
- `/v3/api-docs/**` - Documentación OpenAPI
- `/swagger-ui/**` - Interfaz Swagger
- `/actuator/health` - Health check

### Endpoints Protegidos
Todos los demás endpoints requieren un token JWT válido.

## 📊 Monitoreo

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

### Logs
```bash
# Docker Compose
docker-compose logs -f ms-infrastructure

# Docker standalone
docker logs -f jass-ms-infrastructure
```

## 🗄️ Base de Datos

### Índices Definidos
- `idx_box_code` en `water_boxes`
- `idx_organization_id` en `water_boxes`
- `idx_status` en `water_boxes` y `water_box_assignments`
- `idx_water_box_id` en `water_box_assignments` y `water_box_transfers`
- `idx_user_id` en `water_box_assignments`

### Estrategia de Migración
- **Desarrollo**: `ddl-auto: update`
- **Producción**: `ddl-auto: validate` (usar Flyway/Liquibase para migraciones)

## 📝 Notas Adicionales

### Optimizaciones de Tamaño
La imagen Docker está optimizada para ser menor a 250 MiB:
- Uso de Alpine Linux
- Eliminación de archivos innecesarios
- Exclusión de dependencias de desarrollo
- JRE en lugar de JDK completo

### Perfiles de Spring
- **dev**: Desarrollo local con logs detallados
- **prod**: Producción con logs mínimos y optimizaciones

## 🤝 Contribución

Para contribuir al proyecto:
1. Seguir los patrones establecidos
2. Usar constructores en lugar de `@Autowired` o `@RequiredArgsConstructor`
3. Mantener separación entre entidades JPA y modelos de dominio
4. Usar `ResponseDto` para todas las respuestas HTTP
5. Documentar cambios en el README

## 📞 Soporte

Para problemas o preguntas, contactar al equipo de desarrollo.

# Estructura de Archivos del Proyecto

## Árbol de Directorios

```
vg-ms-distribution/
├── src/
│   └── main/
│       ├── java/
│       │   └── pe/edu/vallegrande/msdistribution/
│       │       ├── VgMsDistribution.java
│       │       ├── application/
│       │       ├── domain/
│       │       └── infrastructure/
│       └── resources/
│           ├── application.yml
│           └── application-prod.yml
├── doc/
│   ├── 01-ARQUITECTURA.md
│   ├── 02-TECNOLOGIAS.md
│   ├── 03-DEPENDENCIAS.md
│   ├── 04-ANOTACIONES.md
│   ├── 05-ESTRUCTURA-ARCHIVOS.md
│   └── 06-FLUJOS-NEGOCIO.md
├── .env
├── .env.example
├── Dockerfile
├── docker-compose.yml
├── docker-entrypoint.sh
├── pom.xml
└── README.md
```

---

## Clase Principal

### VgMsDistribution.java

**Ubicación:** `src/main/java/pe/edu/vallegrande/msdistribution/`

**Propósito:** Punto de entrada de la aplicación

**Contenido:**
```java
@SpringBootApplication
public class VgMsDistribution {
    public static void main(String[] args) {
        SpringApplication.run(VgMsDistribution.class, args);
    }
}
```

**Responsabilidad:**
- Iniciar la aplicación Spring Boot
- Configurar auto-escaneo de componentes
- Cargar configuración

---

## Capa de Aplicación (application/)

### Estructura

```
application/
└── services/
    ├── DistributionProgramService.java (Interface)
    ├── DistributionRouteService.java (Interface)
    ├── DistributionScheduleService.java (Interface)
    └── impl/
        ├── DistributionProgramServiceImpl.java
        ├── DistributionRouteServiceImpl.java
        └── DistributionScheduleServiceImpl.java
```

### Interfaces de Servicio

**Archivos:**
- `DistributionProgramService.java`
- `DistributionRouteService.java`
- `DistributionScheduleService.java`

**Propósito:** Definir contratos de servicios

**Responsabilidad:**
- Declarar métodos de casos de uso
- Independencia de implementación
- Facilitar testing con mocks

**Ejemplo:**
```java
public interface DistributionProgramService {
    Mono<ResponseDto<DistributionProgramResponse>> createProgram(
        DistributionProgramCreateRequest request, String token);
    
    Mono<ResponseDto<DistributionProgramResponse>> findById(String id);
    
    Flux<DistributionProgramResponse> findAll();
}
```

### Implementaciones de Servicio

**Archivos:**
- `DistributionProgramServiceImpl.java`
- `DistributionRouteServiceImpl.java`
- `DistributionScheduleServiceImpl.java`

**Propósito:** Implementar lógica de negocio

**Responsabilidad:**
- Orquestar flujos de casos de uso
- Validar reglas de negocio
- Coordinar entre domain e infrastructure
- Manejar transacciones

**Dependencias típicas:**
- Repositories (persistencia)
- Mappers (conversión)
- External clients (servicios externos)
- Validators (validación)

---

## Capa de Dominio (domain/)

### Estructura

```
domain/
├── enums/
│   ├── Constants.java
│   └── WaterDeliveryStatus.java
└── models/
    ├── DistributionProgram.java
    ├── DistributionRoute.java
    └── DistributionSchedule.java
```

### Enumeraciones (enums/)

#### Constants.java

**Propósito:** Constantes del sistema

**Contenido:**
- Estados (ACTIVE, INACTIVE)
- Códigos de respuesta
- Mensajes estándar

**Ejemplo:**
```java
public class Constants {
    public static final String ACTIVE = "A";
    public static final String INACTIVE = "I";
    public static final String SUCCESS = "success";
}
```

#### WaterDeliveryStatus.java

**Propósito:** Estados de entrega de agua

**Valores:**
- `PENDING` - Pendiente
- `IN_PROGRESS` - En progreso
- `COMPLETED` - Completado
- `CANCELLED` - Cancelado

### Modelos de Dominio (models/)

**Archivos:**
- `DistributionProgram.java` - Programa de distribución
- `DistributionRoute.java` - Ruta de distribución
- `DistributionSchedule.java` - Horario de distribución

**Propósito:** Representar entidades de negocio

**Características:**
- POJOs (Plain Old Java Objects)
- Sin dependencias de frameworks
- Lógica de negocio pura
- Inmutabilidad preferida

**Ejemplo:**
```java
@Data
@Builder
public class DistributionProgram {
    private String id;
    private String name;
    private String description;
    private String organizationId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## Capa de Infraestructura (infrastructure/)

### Estructura

```
infrastructure/
├── client/
├── config/
├── document/
├── dto/
├── exception/
├── mapper/
├── repository/
├── rest/
├── security/
└── validation/
```

### Clientes Externos (client/)

```
client/
├── external/
│   ├── ExternalServiceClient.java
│   └── dto/
│       ├── ExternalOrganization.java
│       ├── ExternalOrganizationDTO.java
│       ├── ExternalResponseDto.java
│       ├── ExternalStreet.java
│       └── ExternalZone.java
└── validator/
    ├── ExternalClientValidator.java
    └── InternalClientValidator.java
```

#### ExternalServiceClient.java

**Propósito:** Cliente para consumir ms-organization

**Responsabilidad:**
- Llamar endpoints externos con WebClient
- Manejar errores de comunicación
- Transformar respuestas

**Métodos:**
- `getOrganization(id, token)` - Obtener organización
- `getZone(id, token)` - Obtener zona
- `getStreet(id, token)` - Obtener calle

#### Validadores

**ExternalClientValidator.java**
- Valida respuestas de servicios externos
- Verifica que la organización exista y esté activa

**InternalClientValidator.java**
- Valida tokens internos (JWE)
- Extrae información de usuario

### Configuración (config/)

```
config/
├── JwtConfig.java
├── ValidationConfig.java
└── WebClientConfig.java
```

#### JwtConfig.java

**Propósito:** Configuración de JWT interno

**Responsabilidad:**
- Cargar secreto desde variables de entorno
- Configurar expiración de tokens
- Definir issuer y audience

#### ValidationConfig.java

**Propósito:** Configuración de validadores

**Responsabilidad:**
- Registrar validadores personalizados
- Configurar mensajes de error

#### WebClientConfig.java

**Propósito:** Configuración de WebClient

**Responsabilidad:**
- Crear bean WebClient
- Configurar timeouts
- Configurar base URL de servicios externos

### Documentos MongoDB (document/)

```
document/
├── BaseDocument.java
└── DistributionProgramDocument.java
```

#### BaseDocument.java

**Propósito:** Clase base para documentos

**Campos comunes:**
- `createdAt` - Fecha de creación
- `updatedAt` - Fecha de actualización
- `status` - Estado (A/I)

#### DistributionProgramDocument.java

**Propósito:** Documento MongoDB para programas

**Anotaciones:**
- `@Document(collection = "distribution_programs")`
- `@Id` en campo id
- `@Field` para mapeo de nombres

**Responsabilidad:**
- Representar estructura en MongoDB
- Mapear campos Java ↔ MongoDB

### DTOs (dto/)

```
dto/
├── common/
│   ├── ErrorMessage.java
│   ├── ResponseDto.java
│   └── ValidationError.java
├── request/
│   ├── DistributionProgramCreateRequest.java
│   ├── DistributionProgramUpdateRequest.java
│   ├── DistributionRouteCreateRequest.java
│   ├── DistributionRouteUpdateRequest.java
│   ├── DistributionScheduleCreateRequest.java
│   ├── OrganizationRequest.java
│   └── WaterDeliveryStatusUpdateRequest.java
└── response/
    ├── DistributionProgramResponse.java
    ├── DistributionRouteResponse.java
    └── DistributionScheduleResponse.java
```

#### DTOs Comunes (common/)

**ResponseDto.java**
- Envuelve todas las respuestas
- Campos: `status`, `message`, `data`, `timestamp`

**ErrorMessage.java**
- Estructura de mensajes de error
- Campos: `code`, `message`, `details`

**ValidationError.java**
- Errores de validación
- Campos: `field`, `message`, `rejectedValue`

#### DTOs de Request (request/)

**Propósito:** Recibir datos del cliente

**Características:**
- Anotaciones de validación (`@NotNull`, `@NotBlank`, etc.)
- Solo campos necesarios para la operación
- Separados por operación (Create, Update)

**Ejemplo:**
```java
@Data
public class DistributionProgramCreateRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "La organización es obligatoria")
    private String organizationId;
    
    private String description;
}
```

#### DTOs de Response (response/)

**Propósito:** Enviar datos al cliente

**Características:**
- Todos los campos del dominio
- Formato JSON amigable
- Sin lógica de negocio

### Excepciones (exception/)

```
exception/
├── GlobalExceptionHandler.java
└── custom/
    ├── CustomException.java
    ├── ExternalServiceException.java
    ├── InvalidTokenException.java
    └── ResourceNotFoundException.java
```

#### GlobalExceptionHandler.java

**Propósito:** Manejo centralizado de excepciones

**Responsabilidad:**
- Capturar todas las excepciones
- Formatear respuestas de error
- Logging de errores

**Métodos:**
- `handleValidationException()` - Errores de validación
- `handleResourceNotFoundException()` - Recurso no encontrado
- `handleExternalServiceException()` - Error en servicio externo
- `handleGenericException()` - Otros errores

#### Excepciones Personalizadas (custom/)

**CustomException.java** - Base para excepciones del dominio  
**ExternalServiceException.java** - Error al llamar servicio externo  
**InvalidTokenException.java** - Token inválido o expirado  
**ResourceNotFoundException.java** - Recurso no encontrado

### Mappers (mapper/)

```
mapper/
├── BaseMapper.java
└── DistributionProgramMapper.java
```

#### BaseMapper.java

**Propósito:** Interfaz base para mappers

**Métodos genéricos:**
- `toDocument(domain)` - Domain → Document
- `toDomain(document)` - Document → Domain
- `toResponse(domain)` - Domain → Response DTO

#### DistributionProgramMapper.java

**Propósito:** Convertir entre representaciones

**Conversiones:**
- `DistributionProgram` ↔ `DistributionProgramDocument`
- `DistributionProgram` ↔ `DistributionProgramResponse`
- `DistributionProgramCreateRequest` → `DistributionProgram`

### Repositorios (repository/)

```
repository/
├── DistributionProgramRepository.java
├── DistributionRouteRepository.java
└── DistributionScheduleRepository.java
```

**Propósito:** Acceso a datos en MongoDB

**Características:**
- Extienden `ReactiveMongoRepository`
- Métodos reactivos (retornan Mono/Flux)
- Queries personalizadas

**Ejemplo:**
```java
@Repository
public interface DistributionProgramRepository 
    extends ReactiveMongoRepository<DistributionProgramDocument, String> {
    
    Flux<DistributionProgramDocument> findByOrganizationId(String orgId);
    
    Flux<DistributionProgramDocument> findByStatus(String status);
}
```

### Controllers REST (rest/)

```
rest/
└── admin/
    └── AdminRest.java
```

#### AdminRest.java

**Propósito:** Endpoints REST para administración

**Responsabilidad:**
- Recibir requests HTTP
- Validar entrada con `@Valid`
- Delegar a servicios
- Retornar respuestas HTTP

**Endpoints:**
- `POST /internal/programs` - Crear programa
- `GET /internal/programs/{id}` - Obtener programa
- `PUT /internal/programs/{id}` - Actualizar programa
- `DELETE /internal/programs/{id}` - Eliminar programa
- Similar para routes y schedules

### Seguridad (security/)

```
security/
└── SecurityConfig.java
```

#### SecurityConfig.java

**Propósito:** Configuración de Spring Security

**Responsabilidad:**
- Configurar OAuth2 Resource Server
- Validar JWT con Keycloak
- Extraer roles del token
- Definir rutas públicas/protegidas

**Configuración:**
- Rutas públicas: `/actuator/**`
- Rutas protegidas: `/internal/**` (requiere autenticación)
- Extracción de roles de `realm_access.roles`

### Validación (validation/)

```
validation/
└── OrganizationValidator.java
```

#### OrganizationValidator.java

**Propósito:** Validador personalizado de organizaciones

**Responsabilidad:**
- Validar que la organización exista
- Validar que esté activa
- Llamar a servicio externo

---

## Archivos de Configuración

### application.yml

**Ubicación:** `src/main/resources/`

**Propósito:** Configuración principal de la aplicación

**Secciones:**
- `spring` - Configuración de Spring Boot
- `server` - Configuración del servidor
- `management` - Actuator
- `logging` - Niveles de log
- `jwe` - Configuración JWT interno
- `microservices` - URLs de servicios externos

### application-prod.yml

**Ubicación:** `src/main/resources/`

**Propósito:** Configuración específica de producción

**Diferencias:**
- Logging más restrictivo
- Sin stack traces en errores
- Optimizaciones de producción

---

## Archivos Docker

### Dockerfile

**Propósito:** Construir imagen Docker

**Características:**
- Multi-stage build
- Optimizado para tamaño
- Usuario no-root
- Health check

### docker-compose.yml

**Propósito:** Orquestar contenedores

**Configuración:**
- Servicio ms-distribution
- Variables de entorno desde `.env`
- Límites de recursos
- Health check

### docker-entrypoint.sh

**Propósito:** Script de inicio del contenedor

**Responsabilidad:**
- Cargar variables de entorno
- Iniciar aplicación Java

---

## Archivos de Documentación

### README.md

**Propósito:** Documentación general del proyecto

**Contenido:**
- Descripción del microservicio
- Requisitos
- Instalación
- Uso
- Endpoints

### SECURITY_CONFIG.md

**Propósito:** Documentación de seguridad

**Contenido:**
- Configuración OAuth2
- Integración con Keycloak
- Flujo de autenticación

### doc/

**Propósito:** Documentación técnica detallada

**Archivos:**
1. `01-ARQUITECTURA.md` - Arquitectura hexagonal
2. `02-TECNOLOGIAS.md` - Stack tecnológico
3. `03-DEPENDENCIAS.md` - Dependencias Maven
4. `04-ANOTACIONES.md` - Anotaciones usadas
5. `05-ESTRUCTURA-ARCHIVOS.md` - Este archivo
6. `06-FLUJOS-NEGOCIO.md` - Flujos de negocio

---

## Conclusión

La estructura del proyecto sigue:
- ✅ **Arquitectura Hexagonal** (Application, Domain, Infrastructure)
- ✅ **Separación de responsabilidades** clara
- ✅ **Organización por funcionalidad** (programs, routes, schedules)
- ✅ **Código mantenible** y escalable
- ✅ **Documentación completa** en carpeta `doc/`

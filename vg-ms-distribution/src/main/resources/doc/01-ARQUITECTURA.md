# Arquitectura del Microservicio de Distribución

## Patrón Arquitectónico: Hexagonal (Ports & Adapters)

Este microservicio implementa **Arquitectura Hexagonal** (también conocida como Clean Architecture o Ports & Adapters), que separa la lógica de negocio del código de infraestructura.

### Principios Fundamentales

1. **Independencia de Frameworks**: La lógica de negocio no depende de Spring, MongoDB, etc.
2. **Testeable**: La lógica de negocio puede probarse sin UI, BD o servicios externos
3. **Independencia de UI**: La UI puede cambiar sin afectar el negocio
4. **Independencia de BD**: MongoDB puede reemplazarse sin cambiar la lógica
5. **Independencia de Servicios Externos**: Los servicios externos son detalles de implementación

## Estructura de Capas

```
┌─────────────────────────────────────────────────────────┐
│                    CAPA DE ENTRADA                      │
│              (infrastructure/rest)                      │
│  - AdminRest.java (Controllers REST)                    │
│  - Recibe requests HTTP                                 │
│  - Valida entrada                                       │
│  - Delega a Application Layer                           │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│                 CAPA DE APLICACIÓN                      │
│              (application/services)                     │
│  - DistributionProgramService                           │
│  - DistributionRouteService                             │
│  - DistributionScheduleService                          │
│  - Orquesta casos de uso                                │
│  - Coordina entre Domain e Infrastructure               │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│                  CAPA DE DOMINIO                        │
│                   (domain)                              │
│  - models/ (Entidades de negocio)                       │
│    * DistributionProgram                                │
│    * DistributionRoute                                  │
│    * DistributionSchedule                               │
│  - enums/ (Constantes y enumeraciones)                  │
│    * WaterDeliveryStatus                                │
│    * Constants                                          │
│  - LÓGICA DE NEGOCIO PURA                               │
│  - SIN DEPENDENCIAS EXTERNAS                            │
└─────────────────────────────────────────────────────────┘
                   ▲
                   │
┌─────────────────────────────────────────────────────────┐
│              CAPA DE INFRAESTRUCTURA                    │
│                (infrastructure)                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ PERSISTENCIA (repository, document)             │   │
│  │ - MongoDB Reactive Repositories                 │   │
│  │ - Documentos MongoDB                            │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ CLIENTES EXTERNOS (client)                      │   │
│  │ - ExternalServiceClient (WebClient)             │   │
│  │ - Integración con ms-organization               │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ SEGURIDAD (security, config)                    │   │
│  │ - SecurityConfig (OAuth2 + JWT)                 │   │
│  │ - JwtConfig                                     │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ MAPPERS (mapper)                                │   │
│  │ - Conversión Domain ↔ Document ↔ DTO            │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ DTOs (dto)                                      │   │
│  │ - Request/Response para API REST                │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ EXCEPCIONES (exception)                         │   │
│  │ - GlobalExceptionHandler                        │   │
│  │ - Custom Exceptions                             │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## Flujo de Datos

### Ejemplo: Crear Programa de Distribución

```
1. HTTP POST /jass/ms-distribution/internal/programs
   │
   ▼
2. AdminRest.createProgram(@Valid DistributionProgramCreateRequest)
   │ - Valida request con @Valid
   │ - Extrae JWT del header
   │
   ▼
3. DistributionProgramService.createProgram(request, token)
   │ - Valida organización con servicio externo
   │ - Convierte Request → Domain Model
   │ - Aplica lógica de negocio
   │
   ▼
4. DistributionProgramRepository.save(document)
   │ - Convierte Domain → Document
   │ - Persiste en MongoDB
   │
   ▼
5. Retorna ResponseDto<DistributionProgramResponse>
   │ - Convierte Document → Response DTO
   │ - Envuelve en ResponseDto estándar
   │
   ▼
6. HTTP 201 Created con JSON response
```

## Responsabilidades por Capa

### 1. Domain (Núcleo del Negocio)

**Responsabilidades:**
- Definir entidades de negocio (DistributionProgram, DistributionRoute, DistributionSchedule)
- Definir reglas de negocio
- Definir enumeraciones y constantes
- **NO** depende de frameworks ni librerías externas

**Archivos:**
- `domain/models/*.java` - Entidades de negocio
- `domain/enums/*.java` - Enumeraciones y constantes

### 2. Application (Casos de Uso)

**Responsabilidades:**
- Orquestar flujos de negocio
- Coordinar entre Domain e Infrastructure
- Implementar casos de uso específicos
- Validar reglas de negocio complejas

**Archivos:**
- `application/services/*.java` - Interfaces de servicios
- `application/services/impl/*.java` - Implementaciones

### 3. Infrastructure (Detalles Técnicos)

**Responsabilidades:**
- Implementar adaptadores para BD, APIs externas, etc.
- Configurar frameworks (Spring, Security, MongoDB)
- Manejar DTOs de entrada/salida
- Gestionar excepciones
- Implementar seguridad

**Subdirectorios:**
- `rest/` - Controllers REST (entrada HTTP)
- `repository/` - Repositorios MongoDB (persistencia)
- `client/` - Clientes para servicios externos
- `config/` - Configuraciones de Spring
- `security/` - Configuración de seguridad
- `dto/` - Data Transfer Objects
- `mapper/` - Conversores entre capas
- `exception/` - Manejo de errores
- `document/` - Entidades MongoDB
- `validation/` - Validadores personalizados

## Ventajas de esta Arquitectura

### ✅ Mantenibilidad
- Código organizado y fácil de encontrar
- Responsabilidades claras
- Cambios aislados en capas específicas

### ✅ Testabilidad
- Lógica de negocio sin dependencias externas
- Fácil crear mocks de infraestructura
- Tests unitarios rápidos

### ✅ Flexibilidad
- Cambiar MongoDB por otra BD sin afectar el negocio
- Cambiar REST por GraphQL sin afectar el negocio
- Agregar nuevos adaptadores fácilmente

### ✅ Escalabilidad
- Cada capa puede escalar independientemente
- Fácil agregar nuevos casos de uso
- Código reutilizable

## Principios SOLID Aplicados

### Single Responsibility Principle (SRP)
- Cada clase tiene una única responsabilidad
- Ejemplo: `DistributionProgramService` solo gestiona programas

### Open/Closed Principle (OCP)
- Abierto a extensión, cerrado a modificación
- Ejemplo: Nuevos validadores sin modificar existentes

### Liskov Substitution Principle (LSP)
- Las implementaciones pueden sustituir interfaces
- Ejemplo: `DistributionProgramServiceImpl` implementa `DistributionProgramService`

### Interface Segregation Principle (ISP)
- Interfaces específicas y cohesivas
- Ejemplo: Servicios separados por entidad

### Dependency Inversion Principle (DIP)
- Dependencias apuntan hacia abstracciones
- Ejemplo: Services dependen de interfaces Repository, no de implementaciones

## Patrones de Diseño Utilizados

### Repository Pattern
- Abstrae la persistencia de datos
- `DistributionProgramRepository`, `DistributionRouteRepository`, etc.

### Mapper Pattern
- Convierte entre diferentes representaciones
- `DistributionProgramMapper` convierte Domain ↔ Document ↔ DTO

### DTO Pattern
- Objetos para transferencia de datos
- Request/Response DTOs separados del dominio

### Service Layer Pattern
- Capa de servicios para lógica de aplicación
- `DistributionProgramService`, etc.

### Exception Handler Pattern
- Manejo centralizado de excepciones
- `GlobalExceptionHandler` captura todas las excepciones

## Comunicación entre Capas

### REST → Application
```java
@RestController
public class AdminRest {
    private final DistributionProgramService service;
    
    public Mono<ResponseDto<DistributionProgramResponse>> create(
        @Valid @RequestBody DistributionProgramCreateRequest request) {
        return service.createProgram(request, token);
    }
}
```

### Application → Domain
```java
@Service
public class DistributionProgramServiceImpl {
    public Mono<ResponseDto<DistributionProgramResponse>> createProgram(...) {
        // Crea entidad de dominio
        DistributionProgram program = new DistributionProgram();
        // Aplica lógica de negocio
        program.setStatus("A");
        // Delega a infraestructura
        return repository.save(mapper.toDocument(program));
    }
}
```

### Application → Infrastructure
```java
@Repository
public interface DistributionProgramRepository 
    extends ReactiveMongoRepository<DistributionProgramDocument, String> {
    // MongoDB maneja la persistencia
}
```

## Conclusión

Esta arquitectura hexagonal proporciona:
- **Separación clara de responsabilidades**
- **Código mantenible y testeable**
- **Flexibilidad para cambios futuros**
- **Independencia de frameworks y tecnologías**

El dominio es el núcleo protegido, y la infraestructura son detalles intercambiables.

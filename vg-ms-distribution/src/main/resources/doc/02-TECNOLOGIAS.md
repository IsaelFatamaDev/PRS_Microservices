# Tecnologías Utilizadas

## Stack Tecnológico Principal

### Framework Base
- **Spring Boot 3.4.5**
- **Java 17**
- **Maven 3.9.6**

### Paradigma de Programación
- **Programación Reactiva** (Reactive Programming)
- **Non-blocking I/O**
- **Event-driven**

---

## 1. Spring Boot 3.4.5

### ¿Qué es?
Framework de Java para crear aplicaciones empresariales de forma rápida y con configuración mínima.

### ¿Por qué se usa?
- **Auto-configuración**: Configura automáticamente componentes según las dependencias
- **Servidor embebido**: No necesita servidor externo (Netty incluido)
- **Production-ready**: Incluye Actuator para monitoreo
- **Ecosistema maduro**: Gran comunidad y documentación

### Características usadas en el proyecto:
- Spring Boot Starter WebFlux
- Spring Boot Starter Data MongoDB Reactive
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Spring Boot Actuator

---

## 2. Spring WebFlux (Reactive)

### ¿Qué es?
Framework reactivo de Spring para aplicaciones web no bloqueantes.

### ¿Por qué se usa?
- **Alto rendimiento**: Maneja miles de conexiones concurrentes con pocos threads
- **Non-blocking**: No bloquea threads esperando respuestas
- **Backpressure**: Control de flujo de datos
- **Escalabilidad**: Ideal para microservicios

### Componentes clave:
```java
// Mono: 0 o 1 elemento
Mono<DistributionProgramResponse> findById(String id)

// Flux: 0 a N elementos
Flux<DistributionProgramResponse> findAll()
```

### Ventajas en este proyecto:
- Integración reactiva con MongoDB
- WebClient reactivo para llamadas a servicios externos
- Mejor uso de recursos del servidor
- Respuestas más rápidas bajo alta carga

---

## 3. Project Reactor

### ¿Qué es?
Librería de programación reactiva que implementa Reactive Streams.

### Tipos principales:

#### Mono<T>
- Representa 0 o 1 elemento
- Usado para operaciones que retornan un único resultado
```java
Mono<DistributionProgram> program = repository.findById(id);
```

#### Flux<T>
- Representa 0 a N elementos
- Usado para streams de datos
```java
Flux<DistributionProgram> programs = repository.findAll();
```

### Operadores usados:
- `map()` - Transformar datos
- `flatMap()` - Transformar y aplanar
- `filter()` - Filtrar elementos
- `switchIfEmpty()` - Valor por defecto si está vacío
- `onErrorResume()` - Manejo de errores
- `zipWith()` - Combinar streams

---

## 4. MongoDB Reactive

### ¿Qué es?
Base de datos NoSQL orientada a documentos con soporte reactivo.

### ¿Por qué se usa?
- **Esquema flexible**: Ideal para datos que evolucionan
- **Escalabilidad horizontal**: Fácil de escalar
- **Alto rendimiento**: Operaciones rápidas
- **Soporte reactivo**: Integración perfecta con WebFlux

### Driver usado:
- **MongoDB Reactive Streams Driver**
- Operaciones no bloqueantes
- Compatible con Project Reactor

### Características usadas:
```java
@Document(collection = "distribution_programs")
public class DistributionProgramDocument {
    @Id
    private String id;
    // ...
}

public interface DistributionProgramRepository 
    extends ReactiveMongoRepository<DistributionProgramDocument, String> {
    Flux<DistributionProgramDocument> findByOrganizationId(String orgId);
}
```

---

## 5. Spring Security + OAuth2 + JWT

### Spring Security
Framework de autenticación y autorización para aplicaciones Java.

### OAuth2
Protocolo de autorización estándar de la industria.

### JWT (JSON Web Token)
Tokens firmados para autenticación stateless.

### Flujo de Seguridad:
```
1. Usuario se autentica en Keycloak
2. Keycloak genera JWT
3. Cliente envía JWT en header: Authorization: Bearer <token>
4. Spring Security valida JWT
5. Extrae roles del token
6. Autoriza acceso según roles
```

### Configuración en el proyecto:
```java
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {
    // Valida JWT con Keycloak
    // Extrae roles de "realm_access.roles"
    // Aplica @PreAuthorize en endpoints
}
```

---

## 6. Keycloak 25.0.6

### ¿Qué es?
Servidor de gestión de identidad y acceso (IAM) open source.

### ¿Por qué se usa?
- **Single Sign-On (SSO)**: Un login para múltiples aplicaciones
- **Gestión centralizada de usuarios**: No duplicar usuarios en cada microservicio
- **Roles y permisos**: Control de acceso basado en roles (RBAC)
- **Estándares**: OAuth2, OpenID Connect, SAML

### Integración:
- **Issuer URI**: Valida que el token venga de Keycloak
- **JWK Set URI**: Obtiene claves públicas para validar firma
- **Realm**: `sistema-jass`
- **Roles**: Extraídos de `realm_access.roles`

---

## 7. Spring Cloud Gateway

### ¿Qué es?
API Gateway para enrutar y filtrar requests.

### ¿Por qué se usa?
- **Punto de entrada único**: Todos los requests pasan por el gateway
- **Routing**: Enruta a microservicios según la ruta
- **Filtros**: Autenticación, rate limiting, logging
- **Load balancing**: Distribuye carga entre instancias

### En este proyecto:
- Gateway maneja autenticación inicial
- Envía JWT interno (JWE) a microservicios
- Microservicio valida JWT interno

---

## 8. Lombok

### ¿Qué es?
Librería que reduce código boilerplate mediante anotaciones.

### Anotaciones usadas:
```java
@Data // Genera getters, setters, toString, equals, hashCode
@Builder // Patrón Builder
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
@Slf4j // Logger automático
```

### Ventajas:
- Código más limpio y legible
- Menos errores en getters/setters
- Fácil mantenimiento

---

## 9. Bean Validation (Jakarta Validation)

### ¿Qué es?
API estándar para validación de beans.

### Anotaciones usadas:
```java
@NotNull // No puede ser null
@NotBlank // No puede ser null, vacío o solo espacios
@Size(min, max) // Tamaño de string o colección
@Pattern(regexp) // Debe coincidir con regex
@Valid // Valida objeto anidado
```

### En el proyecto:
```java
public Mono<ResponseDto<...>> create(
    @Valid @RequestBody DistributionProgramCreateRequest request) {
    // Spring valida automáticamente antes de ejecutar
}
```

---

## 10. WebClient (Spring WebFlux)

### ¿Qué es?
Cliente HTTP reactivo y no bloqueante.

### ¿Por qué se usa?
- **Non-blocking**: No bloquea threads
- **Reactivo**: Retorna Mono/Flux
- **Fluent API**: Fácil de usar
- **Configuración flexible**: Timeouts, headers, etc.

### Uso en el proyecto:
```java
@Service
public class ExternalServiceClient {
    private final WebClient webClient;
    
    public Mono<ExternalOrganization> getOrganization(String id, String token) {
        return webClient.get()
            .uri("/organizations/{id}", id)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(ExternalOrganization.class);
    }
}
```

---

## 11. Micrometer + Prometheus

### Micrometer
Framework de métricas para aplicaciones Java.

### Prometheus
Sistema de monitoreo y alertas.

### En el proyecto:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```

### Métricas expuestas:
- `/actuator/health` - Estado de salud
- `/actuator/prometheus` - Métricas para Prometheus

---

## 12. Docker

### ¿Qué es?
Plataforma de contenedores para empaquetar y ejecutar aplicaciones.

### Uso en el proyecto:
- **Dockerfile**: Define cómo construir la imagen
- **Multi-stage build**: Optimiza tamaño de imagen
- **docker-compose.yml**: Orquesta contenedores

### Ventajas:
- **Portabilidad**: Funciona igual en dev, test y prod
- **Aislamiento**: No interfiere con otras aplicaciones
- **Reproducibilidad**: Mismo entorno siempre

---

## Diagrama de Stack Tecnológico

```
┌─────────────────────────────────────────────────────┐
│                   CLIENTE                           │
│            (Frontend / Postman / etc)               │
└────────────────────┬────────────────────────────────┘
                     │ HTTP/REST
                     ▼
┌─────────────────────────────────────────────────────┐
│              SPRING CLOUD GATEWAY                   │
│  - Routing                                          │
│  - Autenticación OAuth2                             │
│  - Rate Limiting                                    │
└────────────────────┬────────────────────────────────┘
                     │ JWT Interno
                     ▼
┌─────────────────────────────────────────────────────┐
│         MICROSERVICIO DE DISTRIBUCIÓN               │
│  ┌───────────────────────────────────────────────┐  │
│  │ SPRING BOOT 3.4.5 + WEBFLUX (Reactive)       │  │
│  │ - Controllers REST                            │  │
│  │ - Spring Security (OAuth2 + JWT)             │  │
│  │ - Bean Validation                            │  │
│  └───────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────┐  │
│  │ APPLICATION LAYER                             │  │
│  │ - Services (Lógica de negocio)               │  │
│  │ - Project Reactor (Mono/Flux)                │  │
│  └───────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────┐  │
│  │ INFRASTRUCTURE LAYER                          │  │
│  │ - WebClient (Llamadas externas)              │  │
│  │ - MongoDB Reactive Driver                     │  │
│  │ - Mappers, DTOs, Exceptions                  │  │
│  └───────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌──────────────────┐    ┌──────────────────┐
│   MONGODB        │    │  MS-ORGANIZATION │
│   (Reactive)     │    │  (WebClient)     │
│   - Programas    │    │  - Organizaciones│
│   - Rutas        │    │  - Zonas         │
│   - Horarios     │    │  - Calles        │
└──────────────────┘    └──────────────────┘
```

---

## Versiones de Tecnologías

| Tecnología | Versión | Notas |
|------------|---------|-------|
| Java | 17 | LTS (Long Term Support) |
| Spring Boot | 3.4.5 | Última versión estable |
| Spring Cloud | 2023.0.1 | Compatible con Boot 3.x |
| MongoDB Driver | Reactive | Incluido en Spring Data |
| Keycloak | 25.0.6 | Cliente admin |
| Lombok | Latest | Scope: provided |
| Maven | 3.9.6 | Build tool |
| Docker | Latest | Containerization |

---

## Conclusión

Este stack tecnológico proporciona:
- ✅ **Alto rendimiento** con programación reactiva
- ✅ **Escalabilidad** con arquitectura de microservicios
- ✅ **Seguridad** con OAuth2 + JWT + Keycloak
- ✅ **Flexibilidad** con MongoDB
- ✅ **Mantenibilidad** con arquitectura hexagonal
- ✅ **Observabilidad** con Actuator + Prometheus
- ✅ **Portabilidad** con Docker

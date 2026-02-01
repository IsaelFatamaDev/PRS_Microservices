# 08 - MANEJO DE EXCEPCIONES Y CIRCUIT BREAKER

## 🔥 MANEJO DE EXCEPCIONES

### 📋 Jerarquía de Excepciones (DENTRO de cada microservicio)

Cada microservicio tiene su propia jerarquía:

```
domain/exceptions/
├── DomainException.java             → [BASE] Todas las excepciones de dominio
├── NotFoundException.java           → [404] Recurso no encontrado
├── BusinessRuleException.java       → [400] Regla de negocio violada
├── UnauthorizedException.java       → [401] No autenticado
├── ForbiddenException.java          → [403] Sin permisos
└── ConflictException.java           → [409] Conflicto (ej: email duplicado)
```

---

## 🎯 IMPLEMENTACIÓN DE EXCEPCIONES

### 1. Excepciones de Dominio

```java
// domain/exceptions/DomainException.java
public abstract class DomainException extends RuntimeException {
    private final String code;
    
    protected DomainException(String message, String code) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
```

```java
// domain/exceptions/NotFoundException.java
public class NotFoundException extends DomainException {
    public NotFoundException(String resource, String id) {
        super(
            String.format("%s con ID %s no encontrado", resource, id),
            "RESOURCE_NOT_FOUND"
        );
    }
}
```

```java
// domain/exceptions/BusinessRuleException.java
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }
}
```

---

### 2. Global Exception Handler (Reactivo)

```java
// infrastructure/adapters/in/rest/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========== EXCEPCIONES DE DOMINIO ==========
    
    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleNotFound(
        NotFoundException ex
    ) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorMessage error = ErrorMessage.general(ex.getMessage(), ex.getCode());
        
        return Mono.just(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Recurso no encontrado", error))
        );
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBusinessRule(
        BusinessRuleException ex
    ) {
        log.warn("Business rule violation: {}", ex.getMessage());
        
        ErrorMessage error = ErrorMessage.general(ex.getMessage(), ex.getCode());
        
        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error("Error de validación", error))
        );
    }
    
    // ========== VALIDACIONES ==========
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationErrors(
        WebExchangeBindException ex
    ) {
        List<ErrorMessage> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorMessage.validation(
                error.getField(),
                error.getDefaultMessage(),
                "VALIDATION_ERROR"
            ))
            .toList();
        
        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error("Errores de validación", errors))
        );
    }
    
    // ========== ERRORES GENERALES ==========
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericError(
        Exception ex
    ) {
        log.error("Unexpected error", ex);
        
        ErrorMessage error = ErrorMessage.general(
            "Error interno del servidor",
            "INTERNAL_SERVER_ERROR"
        );
        
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error del servidor", error))
        );
    }
}
```

---

## ⚡ CIRCUIT BREAKER (Resilience4j)

### ¿Qué es Circuit Breaker?

**Problema:**
```
vg-ms-users → vg-ms-authentication (servicio caído)
              ❌ Timeout 5 segundos
              ❌ Timeout 5 segundos
              ❌ Timeout 5 segundos
              = 15 segundos perdidos
```

**Solución con Circuit Breaker:**
```
vg-ms-users → Circuit Breaker → vg-ms-authentication
              
1ra llamada: ❌ Error (cambia a OPEN)
2da llamada: ⚡ FALLA RÁPIDO (no llama al servicio)
3ra llamada: ⚡ FALLA RÁPIDO (no llama al servicio)
```

---

### Estados del Circuit Breaker

```
CLOSED (Normal)
   ↓ (Muchos errores)
OPEN (Bloqueado - falla rápido)
   ↓ (Después de un tiempo)
HALF_OPEN (Prueba si el servicio se recuperó)
   ↓ (Si funciona)
CLOSED (Vuelve a normal)
```

---

### Configuración en `application.yml`

```yaml
# vg-ms-users/application-dev.yml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10                    # Últimas 10 llamadas
        failure-rate-threshold: 50                 # 50% de errores = OPEN
        wait-duration-in-open-state: 10000         # 10 segundos en OPEN
        permitted-number-of-calls-in-half-open-state: 3  # Pruebas en HALF_OPEN
        automatic-transition-from-open-to-half-open-enabled: true
        minimum-number-of-calls: 5                 # Mínimo 5 llamadas antes de evaluar
    
    instances:
      authenticationService:
        base-config: default
      
      organizationService:
        base-config: default
      
      notificationService:
        base-config: default
        failure-rate-threshold: 70                 # Más tolerante (70%)

  timelimiter:
    configs:
      default:
        timeout-duration: 3s                       # Timeout de 3 segundos
```

---

### Uso en Código (Adaptador Externo)

```java
// infrastructure/adapters/out/external/AuthenticationClientImpl.java
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationClientImpl implements IAuthenticationClient {

    private final WebClient webClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Value("${external.authentication-service.url}")
    private String authenticationServiceUrl;

    @Override
    public Mono<Void> createUserInKeycloak(User user) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("authenticationService");
        
        return webClient.post()
            .uri(authenticationServiceUrl + "/users")
            .bodyValue(Map.of(
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
            ))
            .retrieve()
            .bodyToMono(Void.class)
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .onErrorResume(CallNotPermittedException.class, e -> {
                // Circuit Breaker OPEN - servicio caído
                log.error("Authentication service circuit breaker is OPEN");
                return Mono.error(new BusinessRuleException(
                    "Servicio de autenticación temporalmente no disponible"
                ));
            })
            .onErrorResume(TimeoutException.class, e -> {
                // Timeout
                log.error("Authentication service timeout");
                return Mono.error(new BusinessRuleException(
                    "Servicio de autenticación no responde"
                ));
            })
            .onErrorResume(WebClientException.class, e -> {
                // Error de conexión
                log.error("Error calling authentication service", e);
                return Mono.error(new BusinessRuleException(
                    "Error al comunicarse con servicio de autenticación"
                ));
            })
            .doOnSuccess(v -> log.info("User created in Keycloak: {}", user.getId()))
            .then();
    }
}
```

---

### Estrategias de Fallback

#### 1. **Degradación Controlada**
```java
return createUserInKeycloak(user)
    .onErrorResume(e -> {
        log.warn("Keycloak unavailable, saving for retry");
        // Guardar en cola para reintentar después
        return retryQueueService.enqueue(user);
    });
```

#### 2. **Respuesta por Defecto**
```java
return organizationClient.getOrganization(orgId)
    .onErrorReturn(Organization.builder()
        .id(orgId)
        .name("Organización Temporal")
        .build()
    );
```

#### 3. **Caché**
```java
return organizationClient.getOrganization(orgId)
    .onErrorResume(e -> cacheService.get(orgId));
```

---

## 📊 MONITOREO DE CIRCUIT BREAKER

### Actuator Endpoint

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,circuitbreakers
```

**Consultar estado:**
```http
GET http://localhost:8081/actuator/circuitbreakers
```

**Respuesta:**
```json
{
  "circuitBreakers": {
    "authenticationService": {
      "state": "CLOSED",
      "metrics": {
        "failureRate": 12.5,
        "numberOfSuccessfulCalls": 7,
        "numberOfFailedCalls": 1,
        "numberOfBufferedCalls": 8
      }
    }
  }
}
```

---

## 🚫 BALANCE DE CARGA (NO NECESARIO EN TU CASO)

### ❌ NO usas balance de carga porque:

1. **Una instancia de cada servicio**
   ```
   vg-ms-users: 1 instancia
   vg-ms-authentication: 1 instancia
   ```

2. **URLs fijas en VPC**
   ```
   vg-ms-users → http://vg-ms-authentication:8082
   ```

### ✅ CUANDO SÍ NECESITARÍAS BALANCE DE CARGA:

Si escalaras horizontalmente:
```
vg-ms-users → Load Balancer → [auth-1, auth-2, auth-3]
```

Entonces usarías:
- **Spring Cloud Load Balancer** (si usas Eureka)
- **NGINX** como reverse proxy
- **Kubernetes Service** (si despliegas en K8s)

---

## ✅ RESUMEN

| Concepto | Propósito | Ubicación |
|----------|-----------|-----------|
| **Excepciones de Dominio** | Errores de negocio | `domain/exceptions/` DENTRO de cada servicio |
| **GlobalExceptionHandler** | Manejo centralizado en API | `infrastructure/adapters/in/rest/` |
| **Circuit Breaker** | Evitar cascada de fallos | `infrastructure/adapters/out/external/` |
| **Timeouts** | Límite de espera | `application.yml` |
| **Fallback** | Respuesta alternativa | Clientes externos |

**NO HAY carpeta shared** - Todo está dentro de cada microservicio ✅

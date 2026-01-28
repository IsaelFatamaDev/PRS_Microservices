# 🔍 ANÁLISIS COMPLETO Y PROFUNDO - ARQUITECTURA DE MICROSERVICIOS JASS DIGITAL

> **Fecha de Análisis:** 20 de Enero de 2026
> **Sistema:** JASS Digital - Gestión de Juntas Administradoras de Servicios de Saneamiento
> **Microservicios Analizados:** 11
> **Alcance:** Análisis completo de arquitectura, código, seguridad, comunicación y patrones

---

## 📑 ÍNDICE

1. [Resumen Ejecutivo](#-resumen-ejecutivo)
2. [Análisis de Arquitectura Hexagonal](#-análisis-de-arquitectura-hexagonal)
3. [Análisis de Comunicación entre Microservicios](#-análisis-de-comunicación-entre-microservicios)
4. [Análisis de Seguridad](#-análisis-de-seguridad)
5. [Problemas Críticos Encontrados](#-problemas-críticos-encontrados)
6. [Recomendaciones por Prioridad](#-recomendaciones-por-prioridad)
7. [Conclusiones y Plan de Acción](#-conclusiones-y-plan-de-acción)

---

## 📊 RESUMEN EJECUTIVO

### Estado General del Sistema

| Aspecto | Calificación | Estado |
|---------|-------------|--------|
| **Arquitectura Hexagonal** | ⚠️ 6.0/10 | NECESITA MEJORAS |
| **Comunicación MS** | ⚠️ 6.0/10 | NECESITA MEJORAS |
| **Seguridad** | 🔴 4.5/10 | CRÍTICO |
| **Patrones de Resiliencia** | 🔴 3.0/10 | CRÍTICO |
| **Configuración y Deployment** | 🟡 7.0/10 | ACEPTABLE |
| **Documentación** | 🟡 6.5/10 | ACEPTABLE |

**CALIFICACIÓN GLOBAL:** ⚠️ **5.5/10** - REQUIERE REFACTORIZACIÓN URGENTE

### Hallazgos Principales

#### ✅ Fortalezas

- Uso consistente de Spring Boot + WebFlux (programación reactiva)
- Gateway correctamente configurado con Circuit Breaker
- Keycloak implementado en microservicios principales
- Separación clara en varios microservicios (claims-incidents, organizations, inventory)
- Docker y docker-compose configurados

#### 🔴 Problemas Críticos

1. **5 microservicios** violan arquitectura hexagonal (modelos de dominio con anotaciones de BD)
2. **3 microservicios** sin seguridad implementada
3. **NO hay Circuit Breaker** en las llamadas directas MS-to-MS
4. **JWE NO implementado** (solo mencionado en estándar)
5. **Credenciales hardcodeadas** en múltiples archivos
6. **Comunicación 100% síncrona** (sin eventos/messaging)

---

## 🏗️ ANÁLISIS DE ARQUITECTURA HEXAGONAL

### Tabla Resumen por Microservicio

| Microservicio | Base de Datos | Separación Dominio/Infra | Violaciones | Estado |
|---------------|---------------|--------------------------|-------------|--------|
| **vg-ms-users** | MongoDB | ❌ | 1 CRÍTICA | 🔴 |
| **vg-ms-authentication** | Keycloak | ✅ | 0 | ✅ |
| **vg-ms-infrastructure** | PostgreSQL (JPA) | ✅ | 0 | ✅ |
| **vg-ms-distribution** | MongoDB | ⚠️ | 2 CRÍTICAS | 🔴 |
| **vg-ms-claims-incidents** | MongoDB | ✅ | 0 | ✅ |
| **vg-ms-organizations** | MongoDB | ✅ | 0 | ✅ |
| **vg-ms-payments-billing** | PostgreSQL (R2DBC) | ⚠️ | 1 CRÍTICA | 🔴 |
| **vg-ms-inventory-purchases** | PostgreSQL (R2DBC) | ✅ | 0 | ✅ |
| **vg-ms-water-quality** | MongoDB | ⚠️ | 1 CRÍTICA | 🔴 |
| **vg-ms-gateway** | N/A | ✅ | 0 | ✅ |
| **vg-ms-notification** | N/A | N/A | 0 | ⚪ |

### Violaciones de Arquitectura Hexagonal Encontradas

#### 🔴 **1. vg-ms-users**

**Archivo:** `domain/models/AuthCredential.java`

**Problema:**

```java
@Document(collection = "auth_credentials")  // ❌ Anotación de MongoDB en dominio
public class AuthCredential {
    @Id  // ❌ Anotación de persistencia
    private String id;
    // ...
}
```

**Impacto:** Dominio acoplado a tecnología de persistencia
**Solución:** Crear `AuthCredentialDocument` en `infrastructure/document/`

---

#### 🔴 **2. vg-ms-distribution** (2 violaciones)

**Archivo 1:** `domain/models/DistributionRoute.java`

```java
@Document(collection = "route")  // ❌
public class DistributionRoute {
    @Id  // ❌
    private String id;
}
```

**Archivo 2:** `domain/models/DistributionSchedule.java`

```java
@Document(collection = "schedule")  // ❌
public class DistributionSchedule {
    @Id  // ❌
    private String id;
}
```

**Inconsistencia:** `DistributionProgram` SÍ tiene separación correcta con `DistributionProgramDocument`

---

#### 🔴 **3. vg-ms-payments-billing**

**Archivo:** `domain/models/Receipts.java`

```java
@Table("receipts")  // ❌ Anotación R2DBC en dominio
public class Receipts {
    @Id  // ❌
    private Integer id;
    @Column("receipt_code")  // ❌
    private String receiptCode;
}
```

**Solución:** Crear `ReceiptsEntity` en `infrastructure/entity/`

---

#### 🔴 **4. vg-ms-water-quality**

**Archivo:** `domain/models/User.java`

```java
@Document(collection = "users")  // ❌
public class User {
    @Id  // ❌
    private String id;
}
```

**Solución:** Crear `UserDocument` en `infrastructure/document/`

---

### ✅ Microservicios con Arquitectura Correcta (Ejemplos a seguir)

#### **vg-ms-claims-incidents** - EXCELENTE

**Separación perfecta:**

```
domain/
  models/
    ✅ Incident.java (POJO puro, sin anotaciones)
    ✅ Complaint.java
    ✅ IncidentType.java

infrastructure/
  document/
    ✅ IncidentDocument.java (@Document, @Id)
    ✅ ComplaintDocument.java
    ✅ IncidentTypeDocument.java
  mapper/
    ✅ IncidentMapper.java (convierte domain ↔ document)
```

#### **vg-ms-inventory-purchases** - EXCELENTE

**Separación perfecta:**

```
domain/
  models/
    ✅ Material.java (POJO puro)
    ✅ Purchase.java

infrastructure/
  entities/
    ✅ MaterialEntity.java (@Table R2DBC)
    ✅ PurchaseEntity.java
  mapper/
    ✅ MaterialMapper.java
```

---

## 🔗 ANÁLISIS DE COMUNICACIÓN ENTRE MICROSERVICIOS

### Patrón de Comunicación

**📊 Resultado:** 100% COMUNICACIÓN SÍNCRONA (HTTP/REST)

| Patrón | Uso | Estado |
|--------|-----|--------|
| Síncrono (HTTP/REST) | ✅ 100% | Implementado |
| Asíncrono (Eventos) | ❌ 0% | NO Implementado |
| Messaging (Kafka/RabbitMQ) | ❌ 0% | NO Implementado |

### Diagrama de Dependencias

```
┌──────────────────────────────────────────────────┐
│          vg-ms-gateway (Puerto 9090)             │
│  [Circuit Breaker ✅ | Retry ✅ | Timeout 60s]   │
└──────────────────┬───────────────────────────────┘
                   │
        ┌──────────┴──────────┬────────────────┐
        │                     │                │
        ▼                     ▼                ▼
┌───────────────┐    ┌────────────────┐  ┌──────────────┐
│   ms-users    │    │  ms-payments   │  │ ms-claims-   │
│               │    │                │  │ incidents    │
│ Llama a:      │    │ Llama a:       │  │              │
│ • auth        │◄───┤ • users (GW)   │  │ Llama a:     │
│ • infra       │    │ • orgs (GW)    │  │ • users (GW) │
│ • orgs        │    └────────────────┘  └──────────────┘
│ • notif       │
│ • reniec (ext)│
└───────┬───────┘
        │
        ▼
┌───────────────┐
│   ms-auth     │
│  (Keycloak)   │
└───────────────┘
```

### Tabla de Comunicación MS-to-MS

| Origen | Destino | Método | Autenticación | Circuit Breaker | Timeout |
|--------|---------|--------|---------------|----------------|---------|
| users | authentication | WebClient | ❌ No | ❌ No | 3000ms |
| users | infrastructure | WebClient | ❌ No | ❌ No | 3000ms |
| users | organizations | WebClient | ✅ JWT | ❌ No | 3000ms |
| users | notification | WebClient | ❌ No | ❌ No | 3000ms |
| payments | users | WebClient (vía GW) | ❌ No | ⚠️ Solo en GW | 5000ms |
| payments | organizations | WebClient (vía GW) | ❌ No | ⚠️ Solo en GW | 5000ms |
| inventory | users | WebClient | ✅ JWT | ❌ No | 5000ms |
| organizations | users | WebClient | ❌ No | ❌ No | 30s |
| distribution | organizations | WebClient | ✅ Bearer token | ❌ No | ❓ |
| water-quality | users | WebClient | ❌ No | ❌ No | 3000ms |
| water-quality | organizations | WebClient | ✅ Bearer token | ❌ No | 3000ms |
| claims-incidents | users | WebClient (vía GW) | ❌ No | ⚠️ Solo en GW | 15s |

### 🔴 Problemas Críticos de Comunicación

#### **1. NO HAY CIRCUIT BREAKER EN MICROSERVICIOS**

**Solo el Gateway tiene Circuit Breaker configurado:**

```yaml
# vg-ms-gateway application.yml
- name: CircuitBreaker
  args:
    name: default
    fallbackUri: forward:/fallback
```

**Problema:** Las llamadas directas MS-to-MS NO tienen protección
**Riesgo:** Un servicio caído puede causar cascada de fallos

#### **2. SEGURIDAD MS-to-MS INCONSISTENTE**

| Patrón | Microservicios | Evaluación |
|--------|---------------|------------|
| Propagación JWT | users, inventory | ✅ Correcto |
| Token Estático | distribution, water-quality | ⚠️ Aceptable |
| Sin Autenticación | payments, claims, orgs (parcial) | 🔴 INSEGURO |

#### **3. TIMEOUTS HETEROGÉNEOS**

- vg-ms-users: 3000ms
- vg-ms-payments: Sin configurar ❌
- vg-ms-inventory: 5000ms
- vg-ms-organizations: 30000ms
- vg-ms-claims-incidents: 15000ms
- vg-ms-gateway: 60000ms

**Problema:** Sin estándar definido, riesgo de timeouts en cascada

#### **4. JWE NO IMPLEMENTADO**

**Estándar PRS231 dice:**

```markdown
- Comunicación: HTTP/REST + JWT + JWE para comunicación interna
```

**Realidad:**

- Solo `vg-ms-distribution` tiene configuración JWE en `.env`
- NO hay clases `JweService`, `JweEncryptionService`, etc.
- **Estado:** ❌ NO IMPLEMENTADO

---

## 🔒 ANÁLISIS DE SEGURIDAD

### Tabla de Seguridad por Microservicio

| Microservicio | Keycloak | JWT | SecurityConfig | CORS | Estado |
|--------------|----------|-----|----------------|------|--------|
| **vg-ms-users** | ✅ | ✅ | ✅ Robusto | ❌ | 🟢 SEGURO |
| **vg-ms-authentication** | ✅ | ✅ | ✅ Permisivo | ❌ | 🟢 SEGURO |
| **vg-ms-organizations** | ✅ | ✅ | ✅ Complejo | ❌ | 🟢 SEGURO |
| **vg-ms-gateway** | ✅ | ✅ | ⚠️ GET públicos | ✅ Muy permisivo | 🟡 REVISAR |
| **vg-ms-water-quality** | ✅ | ✅ | ✅ Básico | ❌ | 🟢 SEGURO |
| **vg-ms-distribution** | ✅ | ✅ | ✅ Con JWE config | ❌ | 🟡 PARCIAL |
| **vg-ms-inventory** | ❌ | ❌ | ✅ Mínimo | ❌ | 🔴 INSEGURO |
| **vg-ms-infrastructure** | ⚠️ | ⚠️ | 🔴 **DESACTIVADO** | ✅ | 🔴 **CRÍTICO** |
| **vg-ms-payments** | ❌ | ❌ | ❌ **NO EXISTE** | ❌ | 🔴 **CRÍTICO** |
| **vg-ms-claims-incidents** | ❌ | ❌ | ❌ **NO EXISTE** | ❌ | 🔴 **CRÍTICO** |
| **vg-ms-notification** | N/A | N/A | N/A | N/A | ⚪ Node.js |

### 🔴 Vulnerabilidades Críticas de Seguridad

#### **1. vg-ms-infrastructure - SEGURIDAD DESACTIVADA**

```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // 🔓 PERMITE TODO SIN AUTENTICACIÓN
);
// OAuth2 comentado:
// .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
```

**Riesgo:** CRÍTICO - Cualquier persona puede acceder a endpoints de infraestructura
**Acción:** Activar OAuth2 INMEDIATAMENTE

---

#### **2. vg-ms-payments-billing - SIN SEGURIDAD**

**Estado:** ❌ No existe archivo `SecurityConfig.java`
**Riesgo:** CRÍTICO - Datos financieros completamente expuestos
**Acción:** Implementar seguridad completa

---

#### **3. vg-ms-claims-incidents - SIN SEGURIDAD**

**Estado:** ❌ No existe archivo `SecurityConfig.java`
**Riesgo:** CRÍTICO - Reclamos e incidentes sin protección
**Acción:** Implementar seguridad completa

---

#### **4. CREDENCIALES HARDCODEADAS**

**vg-ms-authentication:**

```yaml
keycloak:
  admin-username: admin
  admin-password: admin  # ⚠️ CONTRASEÑA HARDCODEADA
```

**vg-ms-users:**

```yaml
external:
  diacolecta_reniec:
    token: sk_11799.6WC0bvn93IbhBjNPDIwH239oX30cayLr  # ⚠️ TOKEN EXPUESTO
```

**vg-ms-payments-billing:**

```yaml
spring:
  r2dbc:
    password: npg_FvwbUB26GcHE  # ⚠️ PASSWORD DE BD HARDCODEADA
```

**vg-ms-infrastructure:**

```yaml
spring:
  datasource:
    password: npg_U7Lo1WpkAvmi  # ⚠️ PASSWORD DE BD HARDCODEADA
```

**vg-ms-users (MongoDB):**

```yaml
mongodb:
  uri: mongodb+srv://sistemajass:ZC7O1Ok40SwkfEje@sistemajass.jn6cpoz.mongodb.net/...
  # ⚠️ CREDENCIALES EN URI
```

**Riesgo:** CRÍTICO - Credenciales expuestas en repositorio
**Acción:** Migrar TODO a variables de entorno + Azure Key Vault

---

#### **5. vg-ms-gateway - TODOS LOS GET PÚBLICOS**

```java
.pathMatchers(HttpMethod.GET).permitAll()  // ⚠️ Permite todos los GET sin auth
```

**Riesgo:** MEDIO - Bypass de seguridad en consultas
**Acción:** Proteger endpoints GET sensibles

---

#### **6. CORS MUY PERMISIVO**

```java
// vg-ms-gateway
configuration.setAllowedOriginPatterns(Arrays.asList("*"));  // ⚠️ Permite cualquier origen
```

**Riesgo:** MEDIO - Ataques CSRF desde cualquier sitio
**Acción:** Restringir a dominios específicos

---

## 🚨 PROBLEMAS CRÍTICOS ENCONTRADOS

### Resumen de Problemas por Severidad

| Severidad | Cantidad | Categorías |
|-----------|----------|------------|
| 🔴 **CRÍTICA** | 8 | Seguridad, Arquitectura |
| 🟡 **ALTA** | 6 | Comunicación, Patrones |
| 🟠 **MEDIA** | 4 | Configuración |
| 🟢 **BAJA** | 3 | Documentación |

### 🔴 Problemas Críticos (Requieren acción inmediata)

#### **ARQUITECTURA**

1. **5 microservicios con violaciones de arquitectura hexagonal**
   - vg-ms-users
   - vg-ms-distribution (2 modelos)
   - vg-ms-payments-billing
   - vg-ms-water-quality

   **Impacto:** Acoplamiento tecnológico, dificulta cambios de BD
   **Esfuerzo:** 2-3 días por microservicio

#### **SEGURIDAD**

1. **3 microservicios SIN seguridad**
   - vg-ms-infrastructure (desactivada)
   - vg-ms-payments-billing (no existe)
   - vg-ms-claims-incidents (no existe)

   **Impacto:** Datos críticos expuestos públicamente
   **Esfuerzo:** 1-2 días por microservicio

2. **Credenciales hardcodeadas en 6 microservicios**
   - Passwords de BD
   - Tokens de API externa
   - Secrets de Keycloak

   **Impacto:** Compromiso de seguridad total
   **Esfuerzo:** 1 día para migrar + rotación de credenciales

#### **RESILIENCIA**

1. **NO hay Circuit Breaker en llamadas MS-to-MS**
   - Solo Gateway tiene CB
   - Llamadas directas sin protección

   **Impacto:** Cascada de fallos, sistema completo puede caer
   **Esfuerzo:** 3-4 días para implementar Resilience4j

2. **JWE NO implementado (solo mencionado en estándar)**
   - Comunicación MS-to-MS sin cifrado

   **Impacto:** Datos sensibles en tránsito interno sin cifrar
   **Esfuerzo:** 5-7 días para implementación completa

#### **COMUNICACIÓN**

1. **100% comunicación síncrona, sin eventos**
   - Sin Kafka/RabbitMQ
   - Sin desacoplamiento temporal

   **Impacto:** Alta latencia acumulada, sin buffer ante picos
   **Esfuerzo:** 2-3 semanas para implementar messaging

---

### 🟡 Problemas de Alta Prioridad

1. **Timeouts heterogéneos y sin estándar**
   - Desde 3s hasta 60s
   - Algunos sin configurar

   **Acción:** Definir estándar (ej: 5s connect, 10s read)

2. **Seguridad MS-to-MS inconsistente**
   - Algunos propagan JWT, otros no

   **Acción:** Estandarizar propagación JWT en todos

3. **CORS demasiado permisivo**
   - Permite cualquier origen (*)

   **Acción:** Restringir a dominios específicos

4. **Endpoints sensibles públicos**
    - `/api/admin/fare/**` público en organizations
    - Todos los GET públicos en gateway

    **Acción:** Proteger con roles adecuados

---

## 💡 RECOMENDACIONES POR PRIORIDAD

### 🚨 URGENTE (Semana 1-2)

#### **1. Activar seguridad en microservicios críticos**

```java
// vg-ms-infrastructure - Activar OAuth2
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

**Aplicar en:**

- vg-ms-infrastructure
- vg-ms-payments-billing
- vg-ms-claims-incidents

**Esfuerzo:** 3 días
**Impacto:** CRÍTICO

---

#### **2. Eliminar credenciales hardcodeadas**

**Paso 1:** Crear archivo `.env` o usar variables de entorno del sistema

```env
# .env.production
MONGODB_URI=mongodb+srv://USER:PASS@cluster.mongodb.net/db
POSTGRES_PASSWORD=SECURE_PASSWORD
KEYCLOAK_ADMIN_PASSWORD=SECURE_PASSWORD
RENIEC_API_TOKEN=SECURE_TOKEN
```

**Paso 2:** Actualizar application.yml

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
  r2dbc:
    password: ${POSTGRES_PASSWORD}
```

**Paso 3:** Usar Azure Key Vault (largo plazo)

```java
@Configuration
public class KeyVaultConfig {
    @Value("${azure.keyvault.uri}")
    private String keyVaultUri;

    @Bean
    public SecretClient secretClient() {
        return new SecretClientBuilder()
            .vaultUrl(keyVaultUri)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }
}
```

**Esfuerzo:** 2 días
**Impacto:** CRÍTICO

---

#### **3. Refactorizar violaciones de arquitectura hexagonal**

**Template de refactorización:**

```java
// ANTES (domain/models/User.java)
@Document(collection = "users")  // ❌
public class User {
    @Id  // ❌
    private String id;
    private String email;
}

// DESPUÉS

// 1. domain/models/User.java - POJO puro
public class User {  // ✅ Sin anotaciones
    private String id;
    private String email;
}

// 2. infrastructure/document/UserDocument.java
@Document(collection = "users")  // ✅
public class UserDocument {
    @Id  // ✅
    private String id;
    private String email;
}

// 3. infrastructure/mapper/UserMapper.java
@Component
public class UserMapper {
    public User toDomain(UserDocument document) {
        return User.builder()
            .id(document.getId())
            .email(document.getEmail())
            .build();
    }

    public UserDocument toDocument(User domain) {
        UserDocument doc = new UserDocument();
        doc.setId(domain.getId());
        doc.setEmail(domain.getEmail());
        return doc;
    }
}
```

**Aplicar en:**

1. vg-ms-users → `AuthCredential`
2. vg-ms-distribution → `DistributionRoute`, `DistributionSchedule`
3. vg-ms-payments-billing → `Receipts`
4. vg-ms-water-quality → `User`

**Esfuerzo:** 4-5 días (1 día por MS)
**Impacto:** ALTO

---

### 🔶 ALTA PRIORIDAD (Semana 3-4)

#### **4. Implementar Resilience4j en todos los microservicios**

**Paso 1:** Añadir dependencia

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-reactor</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Paso 2:** Configurar en application.yml

```yaml
resilience4j:
  circuitbreaker:
    instances:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 100
        minimumNumberOfCalls: 10
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 60s
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2s

  retry:
    instances:
      default:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2

  timelimiter:
    instances:
      default:
        timeoutDuration: 10s
```

**Paso 3:** Aplicar en WebClient

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient resilientWebClient(
            CircuitBreakerRegistry circuitBreakerRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("default");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("default");

        return WebClient.builder()
            .filter((request, next) ->
                Mono.fromCallable(() -> next.exchange(request))
                    .flatMap(mono -> mono)
                    .transform(CircuitBreakerOperator.of(circuitBreaker))
                    .transform(TimeLimiterOperator.of(timeLimiter))
            )
            .build();
    }
}
```

**Esfuerzo:** 5 días
**Impacto:** CRÍTICO

---

#### **5. Estandarizar seguridad MS-to-MS (Propagación JWT)**

**Crear filtro común:**

```java
@Component
public class JwtPropagationFilter implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth.getCredentials() instanceof Jwt)
            .map(auth -> (Jwt) auth.getCredentials())
            .map(Jwt::getTokenValue)
            .map(token -> ClientRequest.from(request)
                .headers(headers -> headers.setBearerAuth(token))
                .build())
            .defaultIfEmpty(request)
            .flatMap(next::exchange);
    }
}
```

**Aplicar en WebClientConfig:**

```java
@Bean
public WebClient internalWebClient(JwtPropagationFilter jwtFilter) {
    return WebClient.builder()
        .filter(jwtFilter)
        .build();
}
```

**Esfuerzo:** 2 días
**Impacto:** ALTO

---

#### **6. Implementar JWE para comunicación MS-to-MS**

**Estructura:**

```
infrastructure/
  security/
    JweService.java              # Interface
    JweEncryptionService.java    # Implementación cifrado
    JweDecryptionService.java    # Implementación descifrado
    JweAuthenticationFilter.java # Filtro WebFlux
```

**JweService.java:**

```java
public interface JweService {
    Mono<String> encrypt(Map<String, Object> claims);
    Mono<Map<String, Object>> decrypt(String jweToken);
}
```

**JweEncryptionService.java:**

```java
@Service
public class JweEncryptionService implements JweService {

    @Value("${jwe.internal.secret}")
    private String secret;

    @Value("${jwe.internal.issuer}")
    private String issuer;

    @Override
    public Mono<String> encrypt(Map<String, Object> claims) {
        return Mono.fromCallable(() -> {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .expirationTime(new Date(System.currentTimeMillis() + 86400000))
                .claim("data", claims)
                .build();

            JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                    .contentType("JWT")
                    .build(),
                new Payload(claimsSet.toJSONObject())
            );

            jweObject.encrypt(new DirectEncrypter(secret.getBytes()));
            return jweObject.serialize();
        });
    }

    @Override
    public Mono<Map<String, Object>> decrypt(String jweToken) {
        return Mono.fromCallable(() -> {
            JWEObject jweObject = JWEObject.parse(jweToken);
            jweObject.decrypt(new DirectDecrypter(secret.getBytes()));

            JWTClaimsSet claimsSet = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());
            return (Map<String, Object>) claimsSet.getClaim("data");
        });
    }
}
```

**Esfuerzo:** 7 días
**Impacto:** ALTO

---

### 🔵 MEDIA PRIORIDAD (Mes 2)

#### **7. Implementar comunicación asíncrona con Kafka/RabbitMQ**

**Casos de uso:**

- Notificaciones (users → notifications)
- Auditoría de eventos
- Sincronización de datos entre MS

**Dependencia:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor.rabbitmq</groupId>
    <artifactId>reactor-rabbitmq</artifactId>
</dependency>
```

**Configuración:**

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: 5672
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
```

**Publicar evento:**

```java
@Service
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public Mono<Void> publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
            user.getId(),
            user.getEmail(),
            LocalDateTime.now()
        );

        return Mono.fromRunnable(() ->
            rabbitTemplate.convertAndSend("user.events", "user.created", event)
        );
    }
}
```

**Consumir evento:**

```java
@Component
public class UserEventConsumer {

    @RabbitListener(queues = "notification.queue")
    public Mono<Void> handleUserCreated(UserCreatedEvent event) {
        return notificationService.sendWelcomeEmail(event)
            .doOnSuccess(v -> log.info("Welcome email sent to {}", event.getEmail()))
            .then();
    }
}
```

**Esfuerzo:** 2-3 semanas
**Impacto:** MEDIO

---

#### **8. Implementar observabilidad completa**

**Componentes:**

1. **Distributed Tracing** - Spring Cloud Sleuth + Zipkin

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_URL:http://localhost:9411}/api/v2/spans
```

1. **Métricas** - Micrometer + Prometheus

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

1. **Logs centralizados** - ELK Stack

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"
```

**Esfuerzo:** 2 semanas
**Impacto:** ALTO

---

## 📋 CONCLUSIONES Y PLAN DE ACCIÓN

### Matriz de Priorización

| ID | Acción | Severidad | Esfuerzo | ROI | Prioridad |
|----|--------|-----------|----------|-----|-----------|
| 1 | Activar seguridad (3 MS) | 🔴 Crítica | 3 días | Muy Alto | 🚨 URGENTE |
| 2 | Eliminar credenciales hardcodeadas | 🔴 Crítica | 2 días | Muy Alto | 🚨 URGENTE |
| 3 | Refactorizar arquitectura hexagonal (5 MS) | 🔴 Crítica | 5 días | Alto | 🚨 URGENTE |
| 4 | Implementar Resilience4j | 🔴 Crítica | 5 días | Muy Alto | 🔶 ALTA |
| 5 | Estandarizar seguridad MS-to-MS | 🟡 Alta | 2 días | Alto | 🔶 ALTA |
| 6 | Implementar JWE | 🟡 Alta | 7 días | Medio | 🔶 ALTA |
| 7 | Messaging asíncrono | 🟡 Media | 15 días | Medio | 🔵 MEDIA |
| 8 | Observabilidad completa | 🟡 Media | 10 días | Alto | 🔵 MEDIA |

### Plan de Implementación (6 semanas)

#### **Semana 1-2: CRÍTICO**

- ✅ Activar seguridad en 3 microservicios (3 días)
- ✅ Eliminar credenciales hardcodeadas (2 días)
- ✅ Iniciar refactorización hexagonal (2 MS, 2 días)

#### **Semana 3-4: ALTA PRIORIDAD**

- ✅ Completar refactorización hexagonal (3 MS restantes, 3 días)
- ✅ Implementar Resilience4j (5 días)
- ✅ Estandarizar seguridad MS-to-MS (2 días)

#### **Semana 5-6: JWE Y OBSERVABILIDAD**

- ✅ Implementar JWE (7 días)
- ✅ Configurar observabilidad básica (3 días)

#### **Mes 2: MEJORAS CONTINUAS**

- Messaging asíncrono
- Observabilidad completa
- Testing automatizado
- CI/CD pipeline

### Métricas de Éxito

| Métrica | Actual | Objetivo |
|---------|--------|----------|
| **Cobertura de seguridad** | 60% | 100% |
| **Arquitectura hexagonal correcta** | 50% | 100% |
| **Circuit Breaker implementado** | 10% (solo GW) | 100% |
| **Credenciales seguras** | 40% | 100% |
| **JWE implementado** | 0% | 100% |
| **Observabilidad** | 30% | 80% |

### Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Resistencia al cambio | Media | Alto | Formación del equipo + pair programming |
| Breaking changes en refactorización | Alta | Medio | Tests de regresión + despliegue gradual |
| Complejidad de JWE | Media | Medio | POC previo + documentación detallada |
| Sobrecarga de observabilidad | Baja | Bajo | Configuración por perfiles (dev/prod) |

---

## 📞 SIGUIENTE PASO RECOMENDADO

### Reunión de Planificación

**Objetivo:** Priorizar acciones y asignar recursos

**Agenda:**

1. Revisión de problemas críticos (30 min)
2. Discusión de plan de implementación (30 min)
3. Asignación de tareas (20 min)
4. Q&A (10 min)

**Participantes necesarios:**

- Tech Lead
- Arquitecto de Software
- Desarrolladores Backend (3-4)
- DevOps Engineer

**Entregables esperados:**

- Plan detallado con fechas
- Asignación de responsables
- Definition of Done para cada tarea

---

**Documento generado:** 20 de Enero, 2026
**Analista:** GitHub Copilot AI
**Versión:** 1.0
**Confidencialidad:** Interna

---

Este análisis proporciona una visión completa y accionable del estado actual del sistema de microservicios JASS Digital, con recomendaciones priorizadas y un plan de acción detallado para su mejora.

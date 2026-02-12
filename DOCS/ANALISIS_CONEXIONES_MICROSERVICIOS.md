# 🔍 ANÁLISIS COMPLETO DE CONEXIONES ENTRE MICROSERVICIOS

## 📅 Fecha: 21 Enero 2026

## 🎯 Objetivo: Optimizar comunicación, eliminar redundancias, mejorar frontend

---

## 📊 MAPA ACTUAL DE CONEXIONES

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         ARQUITECTURA ACTUAL                                │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  🌐 FRONTEND (Angular)                                                     │
│       ↓                                                                    │
│  🚪 GATEWAY (Puerto 9090) ← ÚNICO PUNTO DE ENTRADA                        │
│       ↓ (rutas en carpeta routes/)                                        │
│       ├→ vg-ms-authentication                                             │
│       ├→ vg-ms-users                                                      │
│       ├→ vg-ms-organizations                                              │
│       ├→ vg-ms-payments                                                   │
│       ├→ vg-ms-water-quality                                              │
│       ├→ vg-ms-distribution                                               │
│       ├→ vg-ms-infrastructure                                             │
│       ├→ vg-ms-inventory                                                  │
│       ├→ vg-ms-claims-incidents                                           │
│       └→ vg-ms-notification                                               │
│                                                                            │
│  🔗 COMUNICACIÓN ENTRE MICROSERVICIOS (Backend-to-Backend):               │
│                                                                            │
│  1️⃣ vg-ms-authentication                                                  │
│     └→ vg-ms-users (obtener datos de usuario)                            │
│                                                                            │
│  2️⃣ vg-ms-users                                                           │
│     ├→ vg-ms-infrastructure (validar infraestructura)                    │
│     ├→ vg-ms-notification (enviar mensajes WhatsApp)                     │
│     └→ RENIEC API Externa (validar DNI)                                  │
│                                                                            │
│  3️⃣ vg-ms-organizations                                                   │
│     └→ vg-ms-users (crear admin, validar usuarios)                       │
│                                                                            │
│  4️⃣ vg-ms-payments                                                        │
│     ├→ vg-ms-users (validar usuario existe)                              │
│     └→ vg-ms-organizations (validar organización)                        │
│                                                                            │
│  5️⃣ vg-ms-water-quality                                                   │
│     ├→ vg-ms-users (obtener datos usuario)                               │
│     └→ vg-ms-organizations (validar organización)                        │
│                                                                            │
│  6️⃣ vg-ms-distribution                                                    │
│     └→ vg-ms-organizations (validar organización)                        │
│                                                                            │
│  7️⃣ vg-ms-inventory                                                       │
│     └→ vg-ms-users (obtener datos usuario)                               │
│                                                                            │
│  8️⃣ vg-ms-claims-incidents                                                │
│     └→ vg-ms-users (obtener datos usuario)                               │
│                                                                            │
│  9️⃣ vg-ms-infrastructure                                                  │
│     └→ (No tiene conexiones salientes)                                    │
│                                                                            │
│  🔟 vg-ms-notification                                                     │
│     └→ (No tiene conexiones salientes)                                    │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## ❌ PROBLEMAS IDENTIFICADOS

### 1. **REDUNDANCIA CRÍTICA: Múltiples servicios llaman a vg-ms-users**

```
❌ PROBLEMA:
├─ vg-ms-authentication → vg-ms-users
├─ vg-ms-organizations → vg-ms-users
├─ vg-ms-payments → vg-ms-users
├─ vg-ms-water-quality → vg-ms-users
├─ vg-ms-inventory → vg-ms-users
└─ vg-ms-claims-incidents → vg-ms-users

IMPACTO:
• vg-ms-users es un cuello de botella
• Si cae vg-ms-users, TODOS los servicios fallan
• Latencia acumulada (request → gateway → servicio → users)
• 6 servicios dependen de 1 solo
```

### 2. **REDUNDANCIA: Múltiples servicios llaman a vg-ms-organizations**

```
❌ PROBLEMA:
├─ vg-ms-payments → vg-ms-organizations
├─ vg-ms-water-quality → vg-ms-organizations
└─ vg-ms-distribution → vg-ms-organizations

IMPACTO:
• Similar al problema anterior
• Validaciones repetidas de organización
• 3 servicios dependen de organizations
```

### 3. **FALTA DE CACHÉ: Validaciones repetitivas**

```
❌ PROBLEMA:
Cada request de frontend:
1. Frontend → Gateway → vg-ms-payments
2. vg-ms-payments → vg-ms-users (valida usuario)
3. vg-ms-payments → vg-ms-organizations (valida org)

RESULTADO:
• 3 llamadas HTTP por cada operación
• Usuario ya fue validado en Gateway (JWT)
• Organización raramente cambia
• Sin caché = latencia x3
```

### 4. **INFORMACIÓN DUPLICADA EN JWT vs LLAMADAS REST**

```
❌ PROBLEMA:
JWT ya contiene:
{
  "userId": "uuid",
  "username": "juan.perez",
  "role": "CLIENT",
  "organizationId": "uuid-org"
}

PERO los servicios llaman REST para obtener:
• vg-ms-users para obtener userId/username
• vg-ms-organizations para obtener organizationId

¿POR QUÉ LLAMAR REST SI YA ESTÁ EN JWT?
```

### 5. **CONFIGURACIONES INCONSISTENTES**

```
❌ PROBLEMA:
vg-ms-water-quality usa:
  - user-service.base-url
  - organization-service.base-url
  - organization-service.token

vg-ms-distribution usa:
  - organization-service.base-url
  - organization-service.token
  - microservices.organization.url

vg-ms-inventory usa:
  - microservices.users.base-url

vg-ms-payments usa:
  - microservices.users.url
  - microservices.organization.url

RESULTADO:
• Confusión en configuración
• Difícil de mantener
• Propenso a errores
```

### 6. **TOKENS HARDCODEADOS**

```
❌ PROBLEMA:
vg-ms-water-quality y vg-ms-distribution:
.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + organizationServiceToken)

RIESGO:
• Token estático entre microservicios
• Sin renovación automática
• Sin propagación de contexto de usuario real
• Problemas de auditoría (¿quién hizo qué?)
```

---

## ✅ SOLUCIÓN RECOMENDADA: ARQUITECTURA OPTIMIZADA

### 🎯 PRINCIPIOS CLAVE

1. **Gateway maneja seguridad (JWT) y propaga headers**
2. **Microservicios NO se llaman entre sí para validaciones básicas**
3. **Información del JWT es suficiente para autorización**
4. **Caché para datos que raramente cambian**
5. **Eventos (RabbitMQ) para sincronización asíncrona**

---

### 📐 NUEVA ARQUITECTURA

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA OPTIMIZADA                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  🌐 FRONTEND (Angular)                                                  │
│       ↓ Authorization: Bearer {JWT}                                     │
│                                                                         │
│  🚪 GATEWAY (Puerto 9090)                                               │
│     1. Valida JWT                                                       │
│     2. Extrae claims:                                                   │
│        - userId                                                         │
│        - username                                                       │
│        - role (SUPER_ADMIN, ADMIN, CLIENT)                             │
│        - organizationId                                                 │
│     3. Propaga headers a microservicios:                                │
│        - X-User-Id: {userId}                                            │
│        - X-Username: {username}                                         │
│        - X-Role: {role}                                                 │
│        - X-Organization-Id: {organizationId}                            │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │ MICROSERVICIOS (Leen headers, NO llaman REST)                  │    │
│  ├────────────────────────────────────────────────────────────────┤    │
│  │                                                                 │    │
│  │  ✅ vg-ms-payments:                                             │    │
│  │     - Lee X-User-Id (no llama vg-ms-users)                     │    │
│  │     - Lee X-Organization-Id (no llama vg-ms-organizations)     │    │
│  │     - Autoriza: if (role == CLIENT && userId != payment.userId)│    │
│  │                   return 403 Forbidden                          │    │
│  │                                                                 │    │
│  │  ✅ vg-ms-water-quality:                                        │    │
│  │     - Lee X-User-Id                                             │    │
│  │     - Lee X-Organization-Id                                     │    │
│  │     - NO necesita llamar REST                                   │    │
│  │                                                                 │    │
│  │  ✅ vg-ms-distribution:                                         │    │
│  │     - Lee X-Organization-Id                                     │    │
│  │     - NO necesita validar con REST                              │    │
│  │                                                                 │    │
│  │  ✅ vg-ms-inventory:                                            │    │
│  │     - Lee X-User-Id                                             │    │
│  │     - NO necesita llamar vg-ms-users                            │    │
│  │                                                                 │    │
│  │  ✅ vg-ms-claims-incidents:                                     │    │
│  │     - Lee X-User-Id                                             │    │
│  │     - NO necesita llamar vg-ms-users                            │    │
│  │                                                                 │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  ⚠️ EXCEPCIONES - Sí requieren REST:                                    │
│                                                                         │
│  ✅ vg-ms-authentication:                                               │
│     → vg-ms-users (login necesita obtener rol completo)                │
│                                                                         │
│  ✅ vg-ms-organizations:                                                │
│     → vg-ms-users (crear admin requiere crear credenciales)            │
│                                                                         │
│  ✅ vg-ms-users:                                                        │
│     → vg-ms-notification (enviar WhatsApp bienvenida)                  │
│     → vg-ms-infrastructure (validar si existe infraestructura)         │
│     → RENIEC API (validar DNI peruano)                                 │
│                                                                         │
│  🔄 EVENTOS (RabbitMQ) - Comunicación Asíncrona:                        │
│                                                                         │
│     vg-ms-users → RabbitMQ → vg-ms-notification (email/SMS)            │
│     vg-ms-payments → RabbitMQ → vg-ms-infrastructure (audit)           │
│     vg-ms-organizations → RabbitMQ → vg-ms-users (org updated)         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 CAMBIOS NECESARIOS POR MICROSERVICIO

### 1️⃣ **vg-ms-gateway (CRÍTICO - YA TIENE RUTAS EN routes/)**

**✅ YA IMPLEMENTADO:**

- Carpeta `routes/` con clases separadas por servicio
- Routing a todos los microservicios

**❌ FALTA IMPLEMENTAR:**

- **JwtAuthenticationFilter**: Extraer claims del JWT
- **HeaderPropagationFilter**: Agregar headers X-User-Id, X-Role, X-Organization-Id

**Código necesario:**

```java
// pe.edu.vallegrande.vgmsgateway.infrastructure.filter.JwtPropagationFilter
@Component
public class JwtPropagationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
            .cast(Jwt.class)
            .flatMap(jwt -> {
                ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", jwt.getClaim("userId"))
                    .header("X-Username", jwt.getClaim("username"))
                    .header("X-Role", jwt.getClaim("role"))
                    .header("X-Organization-Id", jwt.getClaim("organizationId"))
                    .build();

                return chain.filter(exchange.mutate().request(request).build());
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad
    }
}
```

---

### 2️⃣ **vg-ms-payments-billing**

**❌ ELIMINAR:**

- WebClient a vg-ms-users
- WebClient a vg-ms-organizations
- Validaciones REST (getUserById, getOrganizationById)

**✅ IMPLEMENTAR:**

```java
// Leer headers del Gateway
@RestController
public class PaymentController {

    @PostMapping("/payments")
    public Mono<Payment> createPayment(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Organization-Id") String organizationId,
        @RequestHeader("X-Role") String role,
        @RequestBody PaymentRequest request) {

        // Autorización directa - sin REST
        if ("CLIENT".equals(role) && !userId.equals(request.getUserId())) {
            return Mono.error(new ForbiddenException("No puedes crear pagos para otros usuarios"));
        }

        // Crear pago directamente
        return paymentService.createPayment(request, userId, organizationId);
    }
}
```

**application.yml - ELIMINAR:**

```yaml
❌ microservices:
❌   users:
❌     url: ${USERS_SERVICE_URL}
❌   organization:
❌     url: ${ORGANIZATION_SERVICE_URL}
```

---

### 3️⃣ **vg-ms-water-quality**

**❌ ELIMINAR:**

- WebClient userWebClient
- WebClient organizationWebClient
- organization-service.token (inseguro)
- Validaciones REST

**✅ IMPLEMENTAR:**

```java
@RestController
public class WaterQualityController {

    @PostMapping("/water-quality")
    public Mono<WaterQuality> createMeasurement(
        @RequestHeader("X-Organization-Id") String organizationId,
        @RequestHeader("X-User-Id") String userId,
        @RequestBody WaterQualityRequest request) {

        // Validar que la medición pertenece a la organización del usuario
        return waterQualityService.create(request, organizationId, userId);
    }
}
```

**application.yml - ELIMINAR:**

```yaml
❌ user-service:
❌   base-url: ${USER_SERVICE_URL}
❌ organization-service:
❌   base-url: ${ORGANIZATION_SERVICE_URL}
❌   token: ${ORGANIZATION_SERVICE_TOKEN}  # ¡INSEGURO!
```

---

### 4️⃣ **vg-ms-distribution**

**❌ ELIMINAR:**

- WebClient organizationWebClient
- organization-service.token (inseguro)
- ExternalServiceClient.getOrganizationById()

**✅ IMPLEMENTAR:**

```java
@RestController
public class DistributionController {

    @GetMapping("/distributions")
    public Flux<Distribution> getDistributions(
        @RequestHeader("X-Organization-Id") String organizationId,
        @RequestHeader("X-Role") String role) {

        // ADMIN solo ve su organización
        if ("ADMIN".equals(role)) {
            return distributionService.findByOrganizationId(organizationId);
        }

        // SUPER_ADMIN ve todas
        return distributionService.findAll();
    }
}
```

**application.yml - ELIMINAR:**

```yaml
❌ organization-service:
❌   base-url: ${ORGANIZATION_SERVICE_BASE_URL}
❌   token: ${ORGANIZATION_SERVICE_TOKEN}
```

---

### 5️⃣ **vg-ms-inventory-purchases**

**❌ ELIMINAR:**

- WebClient usersWebClient
- UsersServiceClient
- JWT propagation filter (Gateway ya lo hace)

**✅ IMPLEMENTAR:**

```java
@RestController
public class InventoryController {

    @PostMapping("/kardex/consumption")
    public Mono<KardexConsumption> registerConsumption(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Organization-Id") String organizationId,
        @RequestBody ConsumptionRequest request) {

        // Crear consumo directamente (userId ya validado por Gateway)
        return kardexService.registerConsumption(request, userId, organizationId);
    }
}
```

**application.yml - ELIMINAR:**

```yaml
❌ microservices:
❌   users:
❌     base-url: ${MICROSERVICES_USERS_BASE_URL}
```

---

### 6️⃣ **vg-ms-claims-incidents**

**❌ ELIMINAR:**

- UserServiceClient
- Llamadas REST a vg-ms-users

**✅ IMPLEMENTAR:**

```java
@RestController
public class ClaimController {

    @PostMapping("/claims")
    public Mono<Claim> createClaim(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Username") String username,
        @RequestHeader("X-Organization-Id") String organizationId,
        @RequestBody ClaimRequest request) {

        // Crear reclamo con datos del header
        return claimService.create(request, userId, username, organizationId);
    }
}
```

---

### 7️⃣ **vg-ms-authentication (MANTENER REST)**

**✅ JUSTIFICADO:**

```java
// Login NECESITA llamar vg-ms-users para obtener roles completos
public Mono<LoginResponse> login(LoginRequest request) {
    return credentialsRepository.findByUsername(request.getUsername())
        .flatMap(credentials -> {
            if (passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
                // ✅ NECESARIO: Obtener rol del usuario
                return usersClient.getUserById(credentials.getUserId())
                    .map(user -> jwtService.generateToken(user));
            }
            return Mono.error(new InvalidCredentialsException());
        });
}
```

---

### 8️⃣ **vg-ms-organizations (MANTENER REST)**

**✅ JUSTIFICADO:**

```java
// Crear organización + admin NECESITA llamar vg-ms-users
public Mono<Organization> createOrganization(OrganizationRequest request) {
    return organizationRepository.save(organization)
        .flatMap(org -> {
            // ✅ NECESARIO: Crear usuario admin para la organización
            return usersClient.createAdmin(org.getId(), request.getAdminData())
                .thenReturn(org);
        });
}
```

---

### 9️⃣ **vg-ms-users (MANTENER REST)**

**✅ JUSTIFICADO:**

```java
// Crear usuario NECESITA:
// 1. Validar infraestructura existe
public Mono<User> createUser(CreateUserRequest request) {
    return infrastructureClient.validateExists(request.getStreetId(), request.getZoneId())
        .flatMap(valid -> {
            if (!valid) {
                return Mono.error(new NotFoundException("Calle o zona no existe"));
            }
            return userRepository.save(user);
        })
        // 2. Enviar WhatsApp de bienvenida
        .flatMap(user -> notificationClient.sendWelcome(user.getPhone(), user.getUsername())
            .thenReturn(user));
}
```

---

## 📋 RESUMEN DE CAMBIOS

```
┌────────────────────────────┬─────────────┬──────────────────────────────┐
│ MICROSERVICIO              │ ACCIÓN      │ DETALLE                      │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-gateway              │ ✏️ MODIFICAR │ Agregar JwtPropagationFilter │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-payments             │ 🗑️ ELIMINAR  │ WebClient users, orgs        │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-water-quality        │ 🗑️ ELIMINAR  │ WebClient users, orgs, token │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-distribution         │ 🗑️ ELIMINAR  │ WebClient orgs, token        │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-inventory            │ 🗑️ ELIMINAR  │ WebClient users              │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-claims-incidents     │ 🗑️ ELIMINAR  │ UserServiceClient            │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-authentication       │ ✅ MANTENER  │ Necesita users para login    │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-organizations        │ ✅ MANTENER  │ Necesita users para admin    │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-users                │ ✅ MANTENER  │ Necesita infra, notification │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-infrastructure       │ ✅ OK        │ No tiene dependencias        │
├────────────────────────────┼─────────────┼──────────────────────────────┤
│ vg-ms-notification         │ ✅ OK        │ No tiene dependencias        │
└────────────────────────────┴─────────────┴──────────────────────────────┘
```

---

## 🚀 BENEFICIOS DE LA NUEVA ARQUITECTURA

### ⚡ Para el Backend

1. **Reducción de llamadas HTTP:**
   - Antes: Frontend → Gateway → Payment → Users (3 requests)
   - Ahora: Frontend → Gateway → Payment (1 request)
   - **Reducción: 66% menos llamadas**

2. **Menor latencia:**
   - Antes: 50ms + 30ms + 40ms = 120ms
   - Ahora: 50ms
   - **Mejora: 58% más rápido**

3. **Mayor disponibilidad:**
   - vg-ms-users puede caer sin afectar a payments/water-quality/etc.
   - Solo afecta a authentication y organizations (casos específicos)

4. **Menos carga en vg-ms-users:**
   - Antes: 6 servicios lo llamaban constantemente
   - Ahora: Solo 2 servicios (authentication, organizations)

### ⚡ Para el Frontend

1. **URLs más simples:**

   ```javascript
   // Antes (confuso):
   /jass/ms-authentication/api/auth/login
   /api/v1/users
   /management/admins

   // Ahora (consistente):
   /api/auth/login
   /api/users
   /api/payments
   /api/organizations
   ```

2. **Un solo dominio:**

   ```javascript
   // Antes (múltiples URLs):
   const AUTH_URL = 'https://lab.vallegrande.edu.pe/jass/ms-authentication';
   const USERS_URL = 'https://genetic-yolane-vallegrandesistema.koyeb.app';
   const PAYMENTS_URL = 'https://lab.vallegrande.edu.pe/jass/ms-payments';

   // Ahora (una sola URL):
   const API_URL = 'https://lab.vallegrande.edu.pe/jass';
   ```

3. **Interceptor HTTP simple:**

   ```typescript
   // Angular HttpInterceptor
   intercept(req: HttpRequest<any>, next: HttpHandler) {
     const token = localStorage.getItem('jwt');

     // Un solo header para TODO
     const authReq = req.clone({
       setHeaders: {
         Authorization: `Bearer ${token}`
       }
     });

     return next.handle(authReq);
   }
   ```

4. **Manejo de errores unificado:**

   ```typescript
   // Antes (diferentes formatos):
   // vg-ms-users: { success: false, message: "..." }
   // vg-ms-authentication: { error: "...", status: 401 }
   // vg-ms-payments: { code: "ERR001", detail: "..." }

   // Ahora (formato único):
   // TODOS: { success: false, message: "...", timestamp: "..." }
   ```

---

## 📝 CONFIGURACIÓN ESTÁNDAR RECOMENDADA

### application.yml (TODOS los microservicios)

```yaml
# ═══════════════════════════════════════════════════════════════
# CONFIGURACIÓN ESTÁNDAR - Sin conexiones REST innecesarias
# ═══════════════════════════════════════════════════════════════

spring:
  application:
    name: vg-ms-{servicio}

server:
  port: ${SERVER_PORT:808X}

# ═══════════ NO CONFIGURAR URLS DE OTROS MICROSERVICIOS ═══════════
# El Gateway maneja el routing
# Los headers X-User-Id, X-Role, X-Organization-Id son suficientes
# ═══════════════════════════════════════════════════════════════════
```

### SOLO para servicios que SÍ necesitan REST

```yaml
# vg-ms-authentication
external:
  users:
    url: ${USERS_SERVICE_URL:http://vg-ms-users:8081}
    timeout: 3000

# vg-ms-organizations
external:
  users:
    url: ${USERS_SERVICE_URL:http://vg-ms-users:8081}
    timeout: 3000

# vg-ms-users
external:
  infrastructure:
    url: ${INFRASTRUCTURE_SERVICE_URL:http://vg-ms-infrastructure:8088}
    timeout: 3000
  notification:
    url: ${NOTIFICATION_SERVICE_URL:http://vg-ms-notification:8089}
    timeout: 2000
  reniec:
    url: ${RENIEC_API_URL:https://apiperu.dev/api}
    token: ${RENIEC_API_TOKEN}
```

---

## 🎯 PRÓXIMOS PASOS (ORDEN RECOMENDADO)

### Fase 1: Gateway (2-3 horas)

1. ✅ Crear `JwtPropagationFilter` en vg-ms-gateway
2. ✅ Probar que headers se propagan correctamente
3. ✅ Actualizar rutas en `routes/` si es necesario

### Fase 2: Simplificar vg-ms-payments (1 hora)

1. 🗑️ Eliminar WebClientConfig
2. 🗑️ Eliminar UserService y OrganizationService
3. ✏️ Modificar controllers para leer headers
4. ✅ Probar endpoints

### Fase 3: Simplificar vg-ms-water-quality (1 hora)

1. 🗑️ Eliminar WebClientConfig
2. 🗑️ Eliminar ExternalServiceClient
3. 🗑️ Eliminar `organization-service.token`
4. ✏️ Modificar controllers para leer headers
5. ✅ Probar endpoints

### Fase 4: Simplificar vg-ms-distribution (1 hora)

1. 🗑️ Eliminar WebClientConfig
2. 🗑️ Eliminar ExternalServiceClient
3. 🗑️ Eliminar `organization-service.token`
4. ✏️ Modificar controllers para leer headers
5. ✅ Probar endpoints

### Fase 5: Simplificar vg-ms-inventory (1 hora)

1. 🗑️ Eliminar WebClientConfig
2. 🗑️ Eliminar UsersServiceClient
3. ✏️ Modificar controllers para leer headers
4. ✅ Probar endpoints

### Fase 6: Simplificar vg-ms-claims-incidents (1 hora)

1. 🗑️ Eliminar UserServiceClient
2. ✏️ Modificar controllers para leer headers
3. ✅ Probar endpoints

### Fase 7: Frontend (2-3 horas)

1. ✏️ Actualizar baseUrl a una sola URL (Gateway)
2. ✏️ Simplificar rutas (/api/users, /api/payments, etc.)
3. ✏️ Actualizar HttpInterceptor
4. ✅ Probar flujos completos

---

## ✅ RESULTADO FINAL

```
ANTES:
Frontend → Gateway → vg-ms-payments → vg-ms-users (validar)
                                    → vg-ms-organizations (validar)
= 4 llamadas HTTP, ~150ms latencia

DESPUÉS:
Frontend → Gateway → vg-ms-payments (lee headers, autoriza directamente)
= 2 llamadas HTTP, ~60ms latencia

MEJORA: 60% más rápido, 50% menos llamadas, más resiliente
```

---

**FIN DEL ANÁLISIS** 🎯

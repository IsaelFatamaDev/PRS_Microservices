# 🔐 AUTENTICACIÓN, JWT Y GESTIÓN DE USUARIOS

## 1. ❌ TOKENS NO SE ALMACENAN EN BASE DE DATOS

**REGLA ABSOLUTA**:

- Los tokens JWT los genera y valida **Keycloak** únicamente
- NO se guardan en PostgreSQL, MongoDB, ni ninguna base de datos
- NO existe tabla `tokens`, `sessions`, ni `credentials`
- Los tokens tienen vida corta (configurable en Keycloak)

---

## 2. 📋 ESTRUCTURA DEL JWT (Generado por Keycloak)

### 2.1 Payload del Access Token

```json
{
  "exp": 1706745600,
  "iat": 1706742000,
  "jti": "uuid-token-id",
  "iss": "http://keycloak:8080/realms/jass-digital",
  "sub": "uuid-user-keycloak",
  "typ": "Bearer",
  "azp": "jass-backend",

  // ✅ CLAIMSCUSTOMIZADOS (Configurados en Keycloak)
  "userId": "uuid-del-user-en-postgres",           // ID del User en vg-ms-users
  "organizationId": "uuid-de-la-organizacion",     // Multi-tenancy
  "username": "73456789",                          // DNI o código único
  "email": "usuario@example.com",                   // OPCIONAL (puede ser null)
  "phone": "+51987654321",                          // OPCIONAL (puede ser null)
  "firstName": "Juan",
  "lastName": "Pérez García",
  "documentType": "DNI",
  "documentNumber": "73456789",

  // ✅ ROLES (Array, un usuario puede tener múltiples roles)
  "roles": ["CLIENT"],                              // O ["ADMIN"], ["SUPER_ADMIN"], etc.

  "realm_access": {
    "roles": ["CLIENT", "offline_access"]
  },

  "scope": "profile email",
  "preferred_username": "73456789"
}
```

### 2.2 Headers del Gateway (Simplificado)

El **vg-ms-gateway** valida el JWT contra Keycloak y extrae solo los **headers esenciales**:

> **⚠️ IMPORTANTE**: Los microservicios NO validan JWT directamente. Confían en el Gateway porque están en una VPC privada.

#### Headers OBLIGATORIOS

| Header | Descripción | Ejemplo |
|--------|-------------|----------|
| `X-User-Id` | ID del usuario en PostgreSQL | `uuid-123-456` |
| `X-Organization-Id` | ID de la organización (multi-tenant) | `org-uuid-789` |
| `X-Roles` | Roles separados por coma | `ADMIN,CLIENT` |

#### Headers OPCIONALES

| Header | Descripción | Cuándo usar |
|--------|-------------|-------------|
| `X-User-Email` | Email del usuario | Solo si se necesita sin consultar BD |

```java
// vg-ms-gateway - GlobalFilter
@Bean
public GlobalFilter gatewayHeadersFilter() {
    return (exchange, chain) -> {
        ServerHttpRequest request = exchange.getRequest();

        // Extraer JWT del header Authorization
        String token = extractToken(request);
        Claims claims = jwtDecoder.parseClaimsJws(token).getBody();

        // Inyectar SOLO headers esenciales
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", claims.get("userId", String.class))
            .header("X-Organization-Id", claims.get("organizationId", String.class))
            .header("X-Roles", String.join(",", extractAllRoles(claims)))
            .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    };
}
```

#### ¿Por qué NO pasar firstName, lastName, documentNumber?

1. **Datos siempre actualizados**: Si actualizas el nombre en BD, el token sigue con el viejo hasta que expire
2. **Headers ligeros**: ~100 bytes vs ~500 bytes por request
3. **Sin duplicación**: Los datos ya están en la BD del microservicio
4. **Menor acoplamiento**: El microservicio decide qué datos necesita

---

## 3. 👤 MODELO DE USUARIO (vg-ms-users)

### 3.1 Campos del Usuario

```java
// domain/models/User.java
public class User extends BaseEntity {
    // Identificadores
    private String userCode;              // ÚNICO: DNI o código generado

    // Datos Personales (OBLIGATORIOS)
    private String firstName;             // Nombres
    private String lastName;              // Apellidos completos
    private DocumentType documentType;    // DNI, RUC, CE
    private String documentNumber;        // Número de documento (ÚNICO)

    // Contacto (OPCIONALES - Para zonas rurales)
    private String email;                 // ✅ OPCIONAL (puede ser null)
    private String phone;                 // ✅ OPCIONAL (puede ser null)

    // Ubicación
    private String address;
    private String zoneId;                // Referencia a vg-ms-organizations
    private String streetId;              // Referencia a vg-ms-organizations

    // Seguridad y Rol
    private Role role;                    // SUPER_ADMIN, ADMIN, CLIENT

    // Auditoría y Estado (de BaseEntity)
    // - id, organizationId, recordStatus
    // - createdAt, createdBy, updatedAt, updatedBy
}
```

### 3.2 Validaciones en CreateUserRequest

```java
// application/dto/request/CreateUserRequest.java
public record CreateUserRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull DocumentType documentType,
    @NotBlank String documentNumber,

    // ✅ OPCIONALES (Sin @NotBlank)
    String email,                          // Puede ser null o vacío
    String phone,                          // Puede ser null o vacío

    @NotBlank String address,
    @NotBlank String zoneId,
    String streetId,                       // Opcional si no tiene calle asignada
    @NotNull Role role
) {
    // Validación personalizada
    public void validate() {
        // Al menos email O phone debe estar presente
        if ((email == null || email.isBlank()) &&
            (phone == null || phone.isBlank())) {
            throw new BusinessRuleException(
                "El usuario debe tener al menos un email o teléfono para contacto"
            );
        }
    }
}
```

---

## 4. 🔑 FLUJO DE REGISTRO INICIAL

### 4.1 Creación de Usuario por ADMIN

```
┌─────────────┐
│  ADMIN UI   │
└──────┬──────┘
       │ POST /api/users
       │ {
       │   "firstName": "Juan",
       │   "lastName": "Pérez García",
       │   "documentType": "DNI",
       │   "documentNumber": "73456789",
       │   "email": null,              ← OPCIONAL
       │   "phone": "+51987654321",    ← OPCIONAL
       │   "address": "Jr. Los Olivos 123",
       │   "zoneId": "uuid-zone",
       │   "role": "CLIENT"
       │ }
       ▼
┌─────────────────────┐
│  vg-ms-users        │
│                     │
│  1. Crear User      │ ────┐
│     en PostgreSQL   │     │
│                     │     │
│  2. Llamar a        │     │
│     vg-ms-auth      │     │
│     para crear      │     │
│     en Keycloak     │     │
└──────────┬──────────┘     │
           │                │
           ▼                │
┌─────────────────────┐     │
│  vg-ms-auth         │     │
│  (Proxy Keycloak)   │     │
│                     │     │
│  POST /internal/    │     │
│       create-user   │     │
│                     │     │
│  ┌───────────────┐  │     │
│  │   Keycloak    │  │     │
│  │   Admin API   │  │     │
│  │               │  │     │
│  │ Username:     │  │     │
│  │  73456789     │  │     │ (documentNumber)
│  │               │  │     │
│  │ Password:     │  │     │
│  │  73456789     │  │     │ (TEMPORAL - igual al DNI)
│  │               │  │     │
│  │ Atributos:    │  │     │
│  │  userId       │◄─┘     │ (del User creado en paso 1)
│  │  orgId        │        │
│  │  roles: [CLT] │        │
│  │  ...          │        │
│  │               │        │
│  │ Actions:      │        │
│  │  [UPDATE_PWD] │        │ (Forzar cambio en 1er login)
│  └───────────────┘        │
└────────────────────────────┘
```

### 4.2 Implementación en CreateUserUseCaseImpl

```java
@Service
public class CreateUserUseCaseImpl implements ICreateUserUseCase {

    private final IUserRepository userRepository;
    private final IAuthenticationClient authenticationClient;
    private final IOrganizationClient organizationClient;

    @Override
    public Mono<User> execute(CreateUserRequest request) {
        return Mono.deferContextual(ctx -> {
            String adminUserId = ctx.get("userId");
            String organizationId = ctx.get("organizationId");

            // 1. Validar que zona/calle existen
            return organizationClient.validateZone(request.zoneId())
                .then(organizationClient.validateStreet(request.streetId()))

                // 2. Crear usuario en PostgreSQL
                .then(Mono.defer(() -> {
                    User user = new User();
                    user.setId(UUID.randomUUID().toString());
                    user.setUserCode(request.documentNumber());  // DNI como código
                    user.setFirstName(request.firstName());
                    user.setLastName(request.lastName());
                    user.setDocumentType(request.documentType());
                    user.setDocumentNumber(request.documentNumber());
                    user.setEmail(request.email());               // Puede ser null
                    user.setPhone(request.phone());               // Puede ser null
                    user.setAddress(request.address());
                    user.setZoneId(request.zoneId());
                    user.setStreetId(request.streetId());
                    user.setRole(request.role());
                    user.setOrganizationId(organizationId);
                    user.setRecordStatus(RecordStatus.ACTIVE);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setCreatedBy(adminUserId);

                    return userRepository.save(user);
                }))

                // 3. Crear credenciales en Keycloak
                .flatMap(user -> authenticationClient.createKeycloakUser(
                    CreateKeycloakUserRequest.builder()
                        .username(user.getDocumentNumber())           // DNI como username
                        .password(user.getDocumentNumber())           // DNI como password inicial
                        .temporaryPassword(true)                      // ✅ Forzar cambio
                        .userId(user.getId())                         // Custom claim
                        .organizationId(user.getOrganizationId())     // Custom claim
                        .role(user.getRole().name())                  // Custom claim
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())                       // Puede ser null
                        .documentType(user.getDocumentType().name())
                        .documentNumber(user.getDocumentNumber())
                        .build()
                ).thenReturn(user));
        });
    }
}
```

---

## 5. 🔄 PRIMER LOGIN Y CAMBIO DE CONTRASEÑA

### 5.1 Flujo Primer Login

```
┌──────────┐
│  CLIENT  │
└────┬─────┘
     │ POST /api/auth/login
     │ {
     │   "username": "73456789",    ← DNI
     │   "password": "73456789"     ← Password inicial (igual al DNI)
     │ }
     ▼
┌─────────────────┐
│  vg-ms-auth     │
│                 │
│  1. Enviar a    │
│     Keycloak    │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│   Keycloak      │
│                 │
│ ✅ Credenciales │
│    válidas      │
│                 │
│ ⚠️  Detecta:    │
│    UPDATE_PWD   │
│    requerido    │
└────┬────────────┘
     │
     │ Response:
     │ {
     │   "error": "password_change_required",
     │   "tempToken": "...",         ← Token temporal para cambio
     │   "message": "Debe cambiar su contraseña"
     │ }
     ▼
┌──────────┐
│  CLIENT  │
│          │
│  Redirige│
│  a pantalla│
│  de cambio│
│  de pwd   │
└────┬─────┘
     │ POST /api/auth/change-password
     │ {
     │   "tempToken": "...",
     │   "newPassword": "MiNuevaClave123!"
     │ }
     ▼
┌─────────────────┐
│  vg-ms-auth     │
│                 │
│  Actualiza pwd  │
│  en Keycloak    │
│  vía Admin API  │
└────┬────────────┘
     │
     │ POST /token
     │ (Nuevo login automático)
     ▼
┌─────────────────┐
│   Keycloak      │
│                 │
│ ✅ Genera JWT   │
│    con nueva    │
│    contraseña   │
└────┬────────────┘
     │
     │ {
     │   "access_token": "eyJhbGc...",
     │   "refresh_token": "...",
     │   "expires_in": 3600
     │ }
     ▼
┌──────────┐
│  CLIENT  │
│          │
│ ✅ Login │
│  exitoso │
└──────────┘
```

---

## 6. 📝 CONFIGURACIÓN DE KEYCLOAK

### 6.1 Realm Settings: jass-digital

```json
{
  "realm": "jass-digital",
  "enabled": true,
  "loginTheme": "jass-custom",
  "accountTheme": "jass-custom",
  "accessTokenLifespan": 3600,           // 1 hora
  "ssoSessionIdleTimeout": 28800,        // 8 horas
  "ssoSessionMaxLifespan": 86400,        // 24 horas
  "refreshTokenMaxReuse": 0,
  "revokeRefreshToken": true,
  "requiredActions": [
    {
      "alias": "UPDATE_PASSWORD",
      "name": "Update Password",
      "enabled": true,
      "defaultAction": false
    }
  ]
}
```

### 6.2 Client: jass-backend

```json
{
  "clientId": "jass-backend",
  "enabled": true,
  "protocol": "openid-connect",
  "publicClient": false,
  "directAccessGrantsEnabled": true,      // Password grant
  "standardFlowEnabled": true,
  "attributes": {
    "access.token.lifespan": "3600",
    "user.info.response.signature.alg": "RS256"
  },
  "protocolMappers": [
    {
      "name": "userId",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "userId",
        "claim.name": "userId",
        "jsonType.label": "String",
        "id.token.claim": "true",
        "access.token.claim": "true"
      }
    },
    {
      "name": "organizationId",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "organizationId",
        "claim.name": "organizationId",
        "jsonType.label": "String",
        "id.token.claim": "true",
        "access.token.claim": "true"
      }
    },
    {
      "name": "roles",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-realm-role-mapper",
      "config": {
        "claim.name": "roles",
        "jsonType.label": "String",
        "multivalued": "true",
        "id.token.claim": "true",
        "access.token.claim": "true"
      }
    },
    {
      "name": "documentNumber",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "documentNumber",
        "claim.name": "documentNumber",
        "jsonType.label": "String",
        "id.token.claim": "true",
        "access.token.claim": "true"
      }
    }
  ]
}
```

---

## 7. ✅ RESUMEN DE REGLAS

1. **❌ NO se almacenan tokens** en ninguna base de datos
2. **✅ Keycloak genera y maneja** todos los JWT
3. **✅ Username inicial**: Número de documento (DNI)
4. **✅ Password inicial**: Mismo número de documento
5. **✅ Forzar cambio**: Primera vez que el usuario ingresa
6. **✅ Email y Phone**: OPCIONALES (zonas rurales)
7. **✅ Validación contacto**: Al menos email O phone debe existir
8. **✅ JWT Payload**: Incluye userId, organizationId, roles (plural)
9. **✅ Gateway**: Extrae claims del JWT e inyecta como headers
10. **✅ Multi-rol**: Un usuario puede tener múltiples roles (array)

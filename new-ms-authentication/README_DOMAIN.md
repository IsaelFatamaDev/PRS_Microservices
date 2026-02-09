# 💎 DOMAIN LAYER - Capa de Dominio

> **El corazón del negocio. Sin dependencias externas.**

## 📋 Principios

1. **Independencia Total**: No importa nada de `application` ni `infrastructure`
2. **Sin Base de Datos**: Este microservicio NO persiste datos, es proxy a Keycloak
3. **Modelos Temporales**: UserCredentials solo existe durante el request

---

## 📂 Estructura

```
domain/
├── models/
│   └── UserCredentials.java                    → [CLASS] DTO temporal (NO persiste)
│                                                 Anotaciones: @Getter @Builder @NoArgsConstructor @AllArgsConstructor
├── ports/
│   ├── in/                                     → Interfaces de casos de uso (entrada)
│   │   ├── ILoginUseCase.java                  → [INTERFACE] Mono<Map<String, Object>> execute(UserCredentials)
│   │   ├── ILogoutUseCase.java                 → [INTERFACE] Mono<Void> execute(String refreshToken)
│   │   ├── IRefreshTokenUseCase.java           → [INTERFACE] Mono<Map<String, Object>> execute(String refreshToken)
│   │   └── IValidateTokenUseCase.java          → [INTERFACE] Mono<Boolean> execute(String accessToken)
│   └── out/                                    → Interfaces de clientes externos (salida)
│       ├── IKeycloakClient.java                → [INTERFACE] Comunicación con Keycloak Admin API
│       ├── IUserServiceClient.java             → [INTERFACE] WebClient a vg-ms-users
│       └── ISecurityContext.java               → [INTERFACE] Obtener usuario autenticado del contexto
└── exceptions/
    ├── DomainException.java                    → [ABSTRACT CLASS] Clase base para todas las excepciones
    ├── NotFoundException.java                  → [CLASS] extends DomainException - HTTP 404
    ├── BusinessRuleException.java              → [CLASS] extends DomainException - HTTP 400
    ├── ExternalServiceException.java           → [CLASS] extends DomainException - HTTP 503
    ├── InvalidCredentialsException.java        → [CLASS] extends BusinessRuleException - Login fallido
    ├── KeycloakException.java                  → [CLASS] extends ExternalServiceException - Error Keycloak
    ├── TokenExpiredException.java              → [CLASS] extends BusinessRuleException - Token expirado
    └── TokenInvalidException.java              → [CLASS] extends BusinessRuleException - Token inválido
```

---

## 1️⃣ MODELS - Modelos de Dominio

### 📄 UserCredentials.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio para credenciales de usuario.
 *
 * <p><b>IMPORTANTE:</b> Este modelo es temporal y solo existe durante
 * el proceso de autenticación. NO se persiste en ninguna base de datos.</p>
 *
 * <p>Las credenciales reales se almacenan y validan en Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentials {

    /**
     * Nombre de usuario o email.
     * Keycloak acepta ambos para el login.
     */
    private String username;

    /**
     * Contraseña del usuario.
     * Solo se usa para enviar a Keycloak, nunca se almacena.
     */
    private String password;

    /**
     * ID del cliente OAuth2 en Keycloak.
     * Por defecto: "jass-users-service"
     */
    private String clientId;

    /**
     * Secreto del cliente (si aplica).
     * Solo para clientes confidenciales.
     */
    private String clientSecret;

    /**
     * Grant type para OAuth2.
     * Por defecto: "password" para login directo.
     */
    private String grantType;

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS DE CONVENIENCIA
    // ═══════════════════════════════════════════════════════════════

    /**
     * Crea credenciales para login con password grant.
     *
     * @param username nombre de usuario o email
     * @param password contraseña
     * @param clientId ID del cliente OAuth2
     * @return instancia de UserCredentials
     */
    public static UserCredentials forPasswordGrant(
            String username,
            String password,
            String clientId
    ) {
        return UserCredentials.builder()
            .username(username)
            .password(password)
            .clientId(clientId)
            .grantType("password")
            .build();
    }

    /**
     * Crea credenciales para refresh token grant.
     *
     * @param refreshToken token de refresco
     * @param clientId ID del cliente OAuth2
     * @return instancia de UserCredentials
     */
    public static UserCredentials forRefreshGrant(
            String refreshToken,
            String clientId
    ) {
        return UserCredentials.builder()
            .password(refreshToken) // Se usa el campo password para el refresh token
            .clientId(clientId)
            .grantType("refresh_token")
            .build();
    }

    /**
     * Verifica si las credenciales son para password grant.
     *
     * @return true si es password grant
     */
    public boolean isPasswordGrant() {
        return "password".equals(grantType);
    }

    /**
     * Verifica si las credenciales son para refresh token grant.
     *
     * @return true si es refresh token grant
     */
    public boolean isRefreshGrant() {
        return "refresh_token".equals(grantType);
    }

    /**
     * Verifica si tiene client secret (cliente confidencial).
     *
     * @return true si tiene secret
     */
    public boolean isConfidentialClient() {
        return clientSecret != null && !clientSecret.isBlank();
    }
}
```

---

## 2️⃣ PORTS - Interfaces (Contratos)

### 📁 ports/in/ - Casos de Uso (Input)

#### 📄 ILoginUseCase.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.in;

import pe.edu.vallegrande.vgmsauthentication.domain.models.UserCredentials;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Puerto de entrada para el caso de uso de login.
 *
 * <p>Define el contrato para autenticar usuarios contra Keycloak
 * usando OAuth2 Password Grant.</p>
 *
 * <p><b>Flujo:</b></p>
 * <ol>
 *   <li>Recibir credenciales (username/password)</li>
 *   <li>Validar formato de credenciales</li>
 *   <li>Enviar a Keycloak token endpoint</li>
 *   <li>Recibir tokens (access_token, refresh_token)</li>
 *   <li>Retornar respuesta estructurada</li>
 * </ol>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface ILoginUseCase {

    /**
     * Autentica un usuario contra Keycloak.
     *
     * @param credentials credenciales del usuario
     * @return Mono con mapa conteniendo access_token, refresh_token, expires_in, etc.
     * @throws InvalidCredentialsException si las credenciales son incorrectas
     * @throws KeycloakException si hay error de comunicación con Keycloak
     */
    Mono<Map<String, Object>> execute(UserCredentials credentials);
}
```

---

#### 📄 IRefreshTokenUseCase.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.in;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Puerto de entrada para refrescar tokens.
 *
 * <p>Permite obtener un nuevo access_token usando un refresh_token válido,
 * sin necesidad de que el usuario ingrese credenciales nuevamente.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IRefreshTokenUseCase {

    /**
     * Refresca el access token usando un refresh token.
     *
     * @param refreshToken token de refresco válido
     * @param clientId ID del cliente OAuth2
     * @return Mono con mapa conteniendo nuevo access_token, refresh_token, etc.
     * @throws TokenExpiredException si el refresh token expiró
     * @throws TokenInvalidException si el refresh token es inválido
     */
    Mono<Map<String, Object>> execute(String refreshToken, String clientId);
}
```

---

#### 📄 ILogoutUseCase.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.in;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para cerrar sesión.
 *
 * <p>Invalida los tokens del usuario en Keycloak, terminando
 * efectivamente la sesión del usuario.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface ILogoutUseCase {

    /**
     * Cierra la sesión del usuario invalidando sus tokens.
     *
     * @param refreshToken token de refresco a invalidar
     * @param clientId ID del cliente OAuth2
     * @return Mono vacío cuando se completa
     */
    Mono<Void> execute(String refreshToken, String clientId);

    /**
     * Cierra la sesión usando el access token.
     *
     * @param accessToken token de acceso
     * @return Mono vacío cuando se completa
     */
    Mono<Void> executeWithAccessToken(String accessToken);
}
```

---

#### 📄 IValidateTokenUseCase.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.in;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Puerto de entrada para validar tokens.
 *
 * <p>Permite verificar si un token es válido y obtener información
 * sobre el usuario autenticado.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IValidateTokenUseCase {

    /**
     * Valida un access token y retorna información del usuario.
     *
     * @param accessToken token a validar
     * @return Mono con información del usuario (claims del token)
     * @throws TokenExpiredException si el token expiró
     * @throws TokenInvalidException si el token es inválido
     */
    Mono<Map<String, Object>> execute(String accessToken);

    /**
     * Introspección de token (RFC 7662).
     *
     * @param token token a introspeccionar
     * @param clientId ID del cliente
     * @param clientSecret secreto del cliente
     * @return Mono con resultado de introspección
     */
    Mono<Map<String, Object>> introspect(String token, String clientId, String clientSecret);

    /**
     * Obtiene información del usuario autenticado.
     *
     * @param accessToken token de acceso válido
     * @return Mono con información del userinfo endpoint
     */
    Mono<Map<String, Object>> getUserInfo(String accessToken);
}
```

---

### 📁 ports/out/ - Clientes Externos (Output)

#### 📄 IKeycloakClient.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.out;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Puerto de salida para comunicación con Keycloak.
 *
 * <p>Define todas las operaciones que este microservicio puede
 * realizar contra Keycloak:</p>
 * <ul>
 *   <li>Autenticación (token endpoint)</li>
 *   <li>Gestión de usuarios (Admin API)</li>
 *   <li>Validación de tokens</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IKeycloakClient {

    // ═══════════════════════════════════════════════════════════════
    // AUTENTICACIÓN (Token Endpoint)
    // ═══════════════════════════════════════════════════════════════


    Mono<Map<String, Object>> getTokenWithPassword(
        String username,
        String password,
        String clientId
    );


    Mono<Map<String, Object>> refreshToken(String refreshToken, String clientId);


    Mono<Void> revokeToken(String refreshToken, String clientId);


    Mono<Map<String, Object>> introspectToken(
        String token,
        String clientId,
        String clientSecret
    );


    Mono<Map<String, Object>> getUserInfo(String accessToken);


    Mono<String> createUser(
        String userId,
        String email,
        String firstName,
        String lastName,
        String password,
        String role
    );

    Mono<Void> updateUser(
        String userId,
        String email,
        String firstName,
        String lastName
    );


    Mono<Void> disableUser(String userId);

    Mono<Void> enableUser(String userId);

    Mono<Void> deleteUser(String userId);

    Mono<Void> assignRole(String userId, String roleName);

    Mono<Void> removeRole(String userId, String roleName);

    Mono<Void> resetPassword(String userId, String newPassword, boolean temporary);
}
```

---

#### 📄 IUserServiceClient.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para comunicación con vg-ms-users.
 *
 * <p>Permite obtener información adicional del usuario que no
 * está en Keycloak (organización, zona, etc.).</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IUserServiceClient {

    /**
     * Obtiene información de usuario por ID.
     *
     * @param userId ID del usuario
     * @return Mono con datos del usuario (organizationId, role, etc.)
     */
    Mono<UserInfo> getUserById(String userId);

    /**
     * Obtiene información de usuario por email.
     *
     * @param email email del usuario
     * @return Mono con datos del usuario
     */
    Mono<UserInfo> getUserByEmail(String email);

    /**
     * Verifica si existe un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con true si existe
     */
    Mono<Boolean> existsUser(String userId);

    /**
     * DTO interno para información de usuario.
     */
    record UserInfo(
        String id,
        String organizationId,
        String email,
        String firstName,
        String lastName,
        String role
    ) {}
}
```

---

#### 📄 ISecurityContext.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.ports.out;

import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Puerto de salida para obtener el contexto de seguridad actual.
 *
 * <p>Lee información del usuario autenticado desde los headers
 * inyectados por el Gateway.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface ISecurityContext {

    /**
     * Obtiene el ID del usuario autenticado.
     *
     * @return Mono con el ID del usuario
     */
    Mono<String> getCurrentUserId();

    /**
     * Obtiene el ID de la organización del usuario autenticado.
     *
     * @return Mono con el ID de la organización
     */
    Mono<String> getCurrentOrganizationId();

    /**
     * Obtiene los roles del usuario autenticado.
     *
     * @return Mono con set de roles
     */
    Mono<Set<String>> getCurrentUserRoles();

    /**
     * Verifica si hay un usuario autenticado.
     *
     * @return Mono con true si hay usuario autenticado
     */
    Mono<Boolean> isAuthenticated();

    /**
     * Obtiene el email del usuario autenticado.
     *
     * @return Mono con el email
     */
    Mono<String> getCurrentUserEmail();
}
```

---

## 3️⃣ EXCEPTIONS - Excepciones de Dominio

### 📄 DomainException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

/**
 * Clase base abstracta para todas las excepciones de dominio.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    protected DomainException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected DomainException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
```

---

### 📄 NotFoundException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

/**
 * Excepción para recursos no encontrados (HTTP 404).
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String resource, String id) {
        super(
            String.format("%s with ID '%s' not found", resource, id),
            "RESOURCE_NOT_FOUND",
            404
        );
    }

    public NotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }
}
```

---

### 📄 BusinessRuleException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

/**
 * Excepción para violaciones de reglas de negocio (HTTP 400).
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", 400);
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
```

---

### 📄 ExternalServiceException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

/**
 * Excepción para errores de servicios externos (HTTP 503).
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String serviceName) {
        super(
            String.format("Service '%s' is temporarily unavailable", serviceName),
            "EXTERNAL_SERVICE_UNAVAILABLE",
            503
        );
    }

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(
            String.format("Error communicating with service '%s'", serviceName),
            "EXTERNAL_SERVICE_ERROR",
            503,
            cause
        );
    }
}
```

---

### 📄 InvalidCredentialsException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid username or password", "INVALID_CREDENTIALS", 401);
    }

    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS", 401);
    }

    public static InvalidCredentialsException userDisabled() {
        return new InvalidCredentialsException("User account is disabled");
    }

    public static InvalidCredentialsException accountLocked() {
        return new InvalidCredentialsException("Account is locked due to too many failed attempts");
    }
}
```

---

### 📄 KeycloakException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

public class KeycloakException extends DomainException {

    public KeycloakException(String message) {
        super(message, "KEYCLOAK_ERROR", 503);
    }

    public KeycloakException(String message, Throwable cause) {
        super(message, "KEYCLOAK_ERROR", 503, cause);
    }

    public static KeycloakException connectionError() {
        return new KeycloakException("Unable to connect to Keycloak server");
    }

    public static KeycloakException configurationError(String detail) {
        return new KeycloakException("Keycloak configuration error: " + detail);
    }

    public static KeycloakException userCreationFailed(String reason) {
        return new KeycloakException("Failed to create user in Keycloak: " + reason);
    }
}
```

---

### 📄 TokenExpiredException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

/**
 * Excepción para tokens expirados (HTTP 401).
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public class TokenExpiredException extends DomainException {

    public TokenExpiredException() {
        super("Token has expired", "TOKEN_EXPIRED", 401);
    }

    public TokenExpiredException(String tokenType) {
        super(String.format("%s token has expired", tokenType), "TOKEN_EXPIRED", 401);
    }

    public static TokenExpiredException accessToken() {
        return new TokenExpiredException("Access");
    }

    public static TokenExpiredException refreshToken() {
        return new TokenExpiredException("Refresh");
    }
}
```

---

### 📄 TokenInvalidException.java

```java
package pe.edu.vallegrande.vgmsauthentication.domain.exceptions;

public class TokenInvalidException extends DomainException {

    public TokenInvalidException() {
        super("Token is invalid", "TOKEN_INVALID", 401);
    }

    public TokenInvalidException(String reason) {
        super("Token is invalid: " + reason, "TOKEN_INVALID", 401);
    }

    public static TokenInvalidException malformed() {
        return new TokenInvalidException("malformed token");
    }

    public static TokenInvalidException invalidSignature() {
        return new TokenInvalidException("invalid signature");
    }
    public static TokenInvalidException revoked() {
        return new TokenInvalidException("token has been revoked");
    }

    public static TokenInvalidException wrongIssuer() {
        return new TokenInvalidException("token issued by unknown authority");
    }
}
```

---

## 📋 Resumen de Excepciones

| Excepción | HTTP Status | Cuándo se usa |
|-----------|-------------|---------------|
| `NotFoundException` | 404 | Usuario no encontrado |
| `BusinessRuleException` | 400 | Violación de regla de negocio |
| `ExternalServiceException` | 503 | Servicio externo no disponible |
| `InvalidCredentialsException` | 401 | Login fallido |
| `KeycloakException` | 503 | Error de Keycloak |
| `TokenExpiredException` | 401 | Token expirado |
| `TokenInvalidException` | 401 | Token inválido/malformado |

---

## 🔑 Diagrama de Flujo de Autenticación

### Login con Password Grant

```
┌─────────────┐      ┌─────────────────────┐      ┌─────────────┐
│   Cliente   │      │  vg-ms-authentication│      │  Keycloak   │
└──────┬──────┘      └──────────┬──────────┘      └──────┬──────┘
       │                        │                        │
       │  POST /auth/login      │                        │
       │  {username, password}  │                        │
       │───────────────────────>│                        │
       │                        │                        │
       │                        │  POST /token           │
       │                        │  grant_type=password   │
       │                        │───────────────────────>│
       │                        │                        │
       │                        │  {access_token,        │
       │                        │   refresh_token,       │
       │                        │   expires_in}          │
       │                        │<───────────────────────│
       │                        │                        │
       │  {access_token,        │                        │
       │   refresh_token,       │                        │
       │   user_info}           │                        │
       │<───────────────────────│                        │
       │                        │                        │
```

### Refresh Token

```
┌─────────────┐      ┌─────────────────────┐      ┌─────────────┐
│   Cliente   │      │  vg-ms-authentication│      │  Keycloak   │
└──────┬──────┘      └──────────┬──────────┘      └──────┬──────┘
       │                        │                        │
       │  POST /auth/refresh    │                        │
       │  {refresh_token}       │                        │
       │───────────────────────>│                        │
       │                        │                        │
       │                        │  POST /token           │
       │                        │  grant_type=refresh    │
       │                        │───────────────────────>│
       │                        │                        │
       │                        │  {new_access_token,    │
       │                        │   new_refresh_token}   │
       │                        │<───────────────────────│
       │                        │                        │
       │  TokenResponse         │                        │
       │<───────────────────────│                        │
```

### Logout (Revoke Token)

```
┌─────────────┐      ┌─────────────────────┐      ┌─────────────┐
│   Cliente   │      │  vg-ms-authentication│      │  Keycloak   │
└──────┬──────┘      └──────────┬──────────┘      └──────┬──────┘
       │                        │                        │
       │  POST /auth/logout     │                        │
       │  {refresh_token}       │                        │
       │───────────────────────>│                        │
       │                        │                        │
       │                        │  POST /logout          │
       │                        │  {refresh_token}       │
       │                        │───────────────────────>│
       │                        │                        │
       │                        │  204 No Content        │
       │                        │<───────────────────────│
       │                        │                        │
       │  204 No Content        │                        │
       │<───────────────────────│                        │
```

### Diagrama de Capas del Dominio

```
┌─────────────────────────────────────────────────────────────────┐
│                        💎 DOMAIN LAYER                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                       MODELS                             │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  UserCredentials (DTO temporal, NO persistido)  │    │   │
│  │  │    - username                                   │    │   │
│  │  │    - password                                   │    │   │
│  │  │    - clientId                                   │    │   │
│  │  │    - grantType                                  │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                        PORTS                             │   │
│  │  ┌────────────────────┐    ┌─────────────────────────┐  │   │
│  │  │      PORTS IN      │    │       PORTS OUT         │  │   │
│  │  │  (Use Case Ifaces) │    │   (External Services)   │  │   │
│  │  ├────────────────────┤    ├─────────────────────────┤  │   │
│  │  │ ILoginUseCase      │    │ IKeycloakClient         │  │   │
│  │  │ ILogoutUseCase     │    │ IUserServiceClient      │  │   │
│  │  │ IRefreshTokenUC    │    │ ISecurityContext        │  │   │
│  │  │ IValidateTokenUC   │    │                         │  │   │
│  │  └────────────────────┘    └─────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                      EXCEPTIONS                          │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  DomainException (base)                         │    │   │
│  │  │    ├── NotFoundException (404)                  │    │   │
│  │  │    ├── BusinessRuleException (400)              │    │   │
│  │  │    ├── InvalidCredentialsException (401)        │    │   │
│  │  │    ├── TokenExpiredException (401)              │    │   │
│  │  │    ├── TokenInvalidException (401)              │    │   │
│  │  │    ├── KeycloakException (503)                  │    │   │
│  │  │    └── ExternalServiceException (503)           │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

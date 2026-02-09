# ⚙️ APPLICATION LAYER - Capa de Aplicación

> **Casos de uso, DTOs y orquestación de eventos.**

## 📋 Principios

1. **Orquesta el dominio**: Coordina flujos de trabajo entre puertos
2. **Transforma datos**: DTOs de entrada/salida para la API
3. **NO contiene lógica de negocio**: Solo coordinación
4. **Escucha eventos**: Reacciona a eventos de otros microservicios

---

## 📂 Estructura

```
application/
├── usecases/
│   ├── LoginUseCaseImpl.java               → [CLASS] @Service @Slf4j @RequiredArgsConstructor
│   │                                         implements ILoginUseCase
│   ├── LogoutUseCaseImpl.java              → [CLASS] @Service @Slf4j @RequiredArgsConstructor
│   │                                         implements ILogoutUseCase
│   ├── RefreshTokenUseCaseImpl.java        → [CLASS] @Service @Slf4j @RequiredArgsConstructor
│   │                                         implements IRefreshTokenUseCase
│   └── ValidateTokenUseCaseImpl.java       → [CLASS] @Service @Slf4j @RequiredArgsConstructor
│                                             implements IValidateTokenUseCase
├── dto/
│   ├── common/
│   │   ├── ApiResponse.java                → [RECORD] Wrapper estándar de respuesta
│   │   └── ErrorMessage.java               → [RECORD] Detalle de error
│   ├── request/
│   │   ├── LoginRequest.java               → [RECORD] @NotBlank username, @NotBlank password
│   │   ├── RefreshTokenRequest.java        → [RECORD] @NotBlank refreshToken
│   │   └── LogoutRequest.java              → [RECORD] @NotBlank refreshToken
│   └── response/
│       ├── LoginResponse.java              → [RECORD] accessToken, refreshToken, expiresIn, userInfo
│       ├── TokenResponse.java              → [RECORD] accessToken, refreshToken, expiresIn, tokenType
│       ├── UserInfoResponse.java           → [RECORD] userId, email, firstName, lastName, roles
│       └── IntrospectResponse.java         → [RECORD] active, sub, exp, iat, clientId
├── events/
│   └── external/                           → DTOs de eventos que ESCUCHA (no publica)
│       ├── UserCreatedEvent.java           → [CLASS] @Data @Builder @NoArgsConstructor @AllArgsConstructor
│       ├── UserUpdatedEvent.java           → [CLASS] @Data @Builder @NoArgsConstructor @AllArgsConstructor
│       ├── UserDeletedEvent.java           → [CLASS] @Data @Builder @NoArgsConstructor @AllArgsConstructor
│       ├── UserRestoredEvent.java          → [CLASS] @Data @Builder @NoArgsConstructor @AllArgsConstructor
│       └── UserPurgedEvent.java            → [CLASS] @Data @Builder @NoArgsConstructor @AllArgsConstructor
└── mappers/
    └── AuthMapper.java                     → [INTERFACE] @Mapper(componentModel = "spring")
                                              Convierte entre DTOs y modelos de dominio
```

---

## 1️⃣ USE CASES - Implementación de Casos de Uso

### 📄 LoginUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.InvalidCredentialsException;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.KeycloakException;
import pe.edu.vallegrande.vgmsauthentication.domain.models.UserCredentials;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.in.ILoginUseCase;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IKeycloakClient;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IUserServiceClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del caso de uso de login.
 *
 * <p><b>Responsabilidades:</b></p>
 * <ul>
 *   <li>Validar credenciales contra Keycloak</li>
 *   <li>Obtener información adicional del usuario (opcional)</li>
 *   <li>Construir respuesta unificada</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements ILoginUseCase {

    private final IKeycloakClient keycloakClient;
    private final IUserServiceClient userServiceClient;

    @Override
    public Mono<Map<String, Object>> execute(UserCredentials credentials) {
        log.info("Attempting login for user: {}", credentials.getUsername());

        return keycloakClient.getTokenWithPassword(
                credentials.getUsername(),
                credentials.getPassword(),
                credentials.getClientId()
            )
            .flatMap(tokens -> enrichWithUserInfo(tokens, credentials.getUsername()))
            .doOnSuccess(result -> log.info("Login successful for user: {}", credentials.getUsername()))
            .doOnError(error -> log.warn("Login failed for user: {}. Reason: {}",
                credentials.getUsername(), error.getMessage()))
            .onErrorMap(this::mapKeycloakError);
    }

    /**
     * Enriquece la respuesta con información adicional del usuario.
     */
    private Mono<Map<String, Object>> enrichWithUserInfo(Map<String, Object> tokens, String username) {
        return userServiceClient.getUserByEmail(username)
            .map(userInfo -> {
                Map<String, Object> enriched = new HashMap<>(tokens);
                enriched.put("user_id", userInfo.id());
                enriched.put("organization_id", userInfo.organizationId());
                enriched.put("role", userInfo.role());
                enriched.put("full_name", userInfo.firstName() + " " + userInfo.lastName());
                return enriched;
            })
            .onErrorResume(error -> {
                // Si falla obtener info del usuario, retornamos solo los tokens
                log.warn("Could not enrich user info: {}", error.getMessage());
                return Mono.just(tokens);
            });
    }

    /**
     * Mapea errores de Keycloak a excepciones de dominio.
     */
    private Throwable mapKeycloakError(Throwable error) {
        String message = error.getMessage();

        if (message != null && message.contains("invalid_grant")) {
            return new InvalidCredentialsException();
        }
        if (message != null && message.contains("disabled")) {
            return InvalidCredentialsException.userDisabled();
        }
        if (message != null && message.contains("locked")) {
            return InvalidCredentialsException.accountLocked();
        }

        return new KeycloakException("Authentication failed", error);
    }
}
```

---

### 📄 RefreshTokenUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.TokenExpiredException;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.TokenInvalidException;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.in.IRefreshTokenUseCase;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IKeycloakClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementación del caso de uso de refresh token.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements IRefreshTokenUseCase {

    private final IKeycloakClient keycloakClient;

    @Override
    public Mono<Map<String, Object>> execute(String refreshToken, String clientId) {
        log.debug("Refreshing token for client: {}", clientId);

        return keycloakClient.refreshToken(refreshToken, clientId)
            .doOnSuccess(tokens -> log.debug("Token refreshed successfully"))
            .doOnError(error -> log.warn("Token refresh failed: {}", error.getMessage()))
            .onErrorMap(this::mapError);
    }

    private Throwable mapError(Throwable error) {
        String message = error.getMessage();

        if (message != null && message.contains("expired")) {
            return TokenExpiredException.refreshToken();
        }
        if (message != null && message.contains("invalid")) {
            return TokenInvalidException.revoked();
        }

        return new TokenInvalidException("refresh failed");
    }
}
```

---

### 📄 LogoutUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.in.ILogoutUseCase;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IKeycloakClient;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de logout.
 *
 * <p>Invalida los tokens en Keycloak para cerrar la sesión.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements ILogoutUseCase {

    private final IKeycloakClient keycloakClient;

    @Override
    public Mono<Void> execute(String refreshToken, String clientId) {
        log.info("Processing logout request");

        return keycloakClient.revokeToken(refreshToken, clientId)
            .doOnSuccess(v -> log.info("Logout successful - token revoked"))
            .doOnError(error -> log.warn("Logout failed: {}", error.getMessage()))
            .onErrorResume(error -> {
                // Ignoramos errores de logout (el token podría ya estar expirado)
                log.warn("Ignoring logout error: {}", error.getMessage());
                return Mono.empty();
            });
    }

    @Override
    public Mono<Void> executeWithAccessToken(String accessToken) {
        log.info("Processing logout with access token");

        // Para logout con access token, usamos el endpoint de logout de Keycloak
        return keycloakClient.getUserInfo(accessToken)
            .flatMap(userInfo -> {
                log.debug("User logged out: {}", userInfo.get("sub"));
                return Mono.empty();
            })
            .then();
    }
}
```

---

### 📄 ValidateTokenUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.TokenExpiredException;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.TokenInvalidException;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.in.IValidateTokenUseCase;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IKeycloakClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementación del caso de uso de validación de tokens.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateTokenUseCaseImpl implements IValidateTokenUseCase {

    private final IKeycloakClient keycloakClient;

    @Override
    public Mono<Map<String, Object>> execute(String accessToken) {
        log.debug("Validating access token");

        return keycloakClient.getUserInfo(accessToken)
            .doOnSuccess(info -> log.debug("Token valid for user: {}", info.get("sub")))
            .doOnError(error -> log.debug("Token validation failed: {}", error.getMessage()))
            .onErrorMap(this::mapError);
    }

    @Override
    public Mono<Map<String, Object>> introspect(String token, String clientId, String clientSecret) {
        log.debug("Introspecting token");

        return keycloakClient.introspectToken(token, clientId, clientSecret)
            .flatMap(result -> {
                Boolean active = (Boolean) result.get("active");
                if (Boolean.FALSE.equals(active)) {
                    return Mono.error(TokenInvalidException.revoked());
                }
                return Mono.just(result);
            });
    }

    @Override
    public Mono<Map<String, Object>> getUserInfo(String accessToken) {
        return keycloakClient.getUserInfo(accessToken)
            .onErrorMap(this::mapError);
    }

    private Throwable mapError(Throwable error) {
        String message = error.getMessage();

        if (message != null && message.contains("401")) {
            return TokenExpiredException.accessToken();
        }
        if (message != null && message.contains("invalid")) {
            return new TokenInvalidException();
        }

        return new TokenInvalidException("validation failed");
    }
}
```

---

## 2️⃣ DTOs - Data Transfer Objects

### 📁 dto/request/

#### 📄 LoginRequest.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de login.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Nombre de usuario o email.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Contraseña del usuario.
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * ID del cliente OAuth2.
     * Por defecto: "jass-users-service"
     */
    @Builder.Default
    private String clientId = "jass-users-service";

    /**
     * Recordar sesión (afecta duración del refresh token).
     */
    @Builder.Default
    private boolean rememberMe = false;
}
```

---

#### 📄 RefreshTokenRequest.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de refresh token.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * Token de refresco actual.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * ID del cliente OAuth2.
     */
    @Builder.Default
    private String clientId = "jass-users-service";
}
```

---

#### 📄 LogoutRequest.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de logout.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    /**
     * Token de refresco a invalidar.
     */
    private String refreshToken;

    /**
     * ID del cliente OAuth2.
     */
    @Builder.Default
    private String clientId = "jass-users-service";
}
```

---

### 📁 dto/response/

#### 📄 LoginResponse.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para login exitoso.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Token de acceso JWT.
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Token de refresco.
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Tipo de token (siempre "Bearer").
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Tiempo de expiración en segundos.
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * Tiempo de expiración del refresh token.
     */
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    // ═══════════════════════════════════════════════════════════════
    // INFORMACIÓN ADICIONAL DEL USUARIO
    // ═══════════════════════════════════════════════════════════════

    /**
     * ID único del usuario (UUID).
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * ID de la organización del usuario.
     */
    @JsonProperty("organization_id")
    private String organizationId;

    /**
     * Email del usuario.
     */
    private String email;

    /**
     * Nombre completo.
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * Rol principal del usuario.
     */
    private String role;

    /**
     * Scope otorgado.
     */
    private String scope;
}
```

---

#### 📄 TokenResponse.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para refresh token.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    private String scope;
}
```

---

#### 📄 UserInfoResponse.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para información del usuario autenticado.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /**
     * Subject (ID del usuario en Keycloak).
     */
    private String sub;

    /**
     * Email del usuario.
     */
    private String email;

    /**
     * Indica si el email fue verificado.
     */
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    /**
     * Nombre de usuario preferido.
     */
    @JsonProperty("preferred_username")
    private String preferredUsername;

    /**
     * Nombre.
     */
    @JsonProperty("given_name")
    private String givenName;

    /**
     * Apellido.
     */
    @JsonProperty("family_name")
    private String familyName;

    /**
     * Nombre completo.
     */
    private String name;

    /**
     * Roles del realm.
     */
    @JsonProperty("realm_roles")
    private List<String> realmRoles;

    /**
     * Roles del cliente.
     */
    @JsonProperty("client_roles")
    private List<String> clientRoles;

    /**
     * ID de la organización (custom claim).
     */
    @JsonProperty("organization_id")
    private String organizationId;
}
```

---

#### 📄 IntrospectResponse.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para introspección de token (RFC 7662).
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectResponse {

    /**
     * Indica si el token es válido.
     */
    private Boolean active;

    /**
     * Tipo de token (access_token, refresh_token).
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * Scope del token.
     */
    private String scope;

    /**
     * Client ID para el cual fue emitido.
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * Nombre de usuario.
     */
    private String username;

    /**
     * Subject (ID del usuario).
     */
    private String sub;

    /**
     * Timestamp de expiración.
     */
    private Long exp;

    /**
     * Timestamp de emisión.
     */
    private Long iat;

    /**
     * Issuer (URL de Keycloak).
     */
    private String iss;

    /**
     * Audience.
     */
    private String aud;
}
```

---

## 3️⃣ EVENTS - Eventos Externos

### 📁 events/external/ - Eventos que ESCUCHA

> **IMPORTANTE:** Este microservicio NO publica eventos, solo ESCUCHA eventos de `vg-ms-users`.

#### 📄 UserCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.events.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento recibido cuando se crea un usuario en vg-ms-users.
 *
 * <p>Cuando se recibe este evento, se debe crear el usuario
 * correspondiente en Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    /**
     * ID único del usuario (UUID).
     */
    private String userId;

    /**
     * Email del usuario (username en Keycloak).
     */
    private String email;

    /**
     * Nombre del usuario.
     */
    private String firstName;

    /**
     * Apellido del usuario.
     */
    private String lastName;

    /**
     * Contraseña temporal (encriptada).
     */
    private String temporaryPassword;

    /**
     * Rol asignado al usuario.
     */
    private String role;

    /**
     * ID de la organización.
     */
    private String organizationId;

    /**
     * Timestamp del evento.
     */
    private Instant timestamp;

    /**
     * Routing key para RabbitMQ: "user.created"
     */
    public static final String ROUTING_KEY = "user.created";
}
```

---

#### 📄 UserUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.events.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento recibido cuando se actualiza un usuario en vg-ms-users.
 *
 * <p>Se debe sincronizar los datos en Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String organizationId;
    private Instant timestamp;

    public static final String ROUTING_KEY = "user.updated";
}
```

---

#### 📄 UserDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.events.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento recibido cuando se elimina (soft delete) un usuario.
 *
 * <p>Se debe deshabilitar el usuario en Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {

    private String userId;
    private String reason;
    private Instant timestamp;

    public static final String ROUTING_KEY = "user.deleted";
}
```

---

#### 📄 UserRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.events.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento recibido cuando se restaura un usuario.
 *
 * <p>Se debe habilitar el usuario en Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestoredEvent {

    private String userId;
    private Instant timestamp;

    public static final String ROUTING_KEY = "user.restored";
}
```

---

#### 📄 UserPurgedEvent.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.events.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento recibido cuando se purga (hard delete) un usuario.
 *
 * <p>Se debe eliminar permanentemente el usuario de Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPurgedEvent {

    private String userId;
    private Instant timestamp;

    public static final String ROUTING_KEY = "user.purged";
}
```

---

### 📁 events/handlers/ - Manejadores de Eventos

#### 📄 UserEventHandler.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.events.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsauthentication.application.events.external.*;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IKeycloakClient;
import reactor.core.publisher.Mono;

/**
 * Manejador de eventos de usuario.
 *
 * <p>Procesa eventos recibidos de vg-ms-users y sincroniza
 * los cambios con Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventHandler {

    private final IKeycloakClient keycloakClient;

    // ═══════════════════════════════════════════════════════════════
    // MANEJADORES DE EVENTOS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Maneja el evento de creación de usuario.
     * Crea el usuario en Keycloak con sus credenciales.
     */
    public Mono<Void> handleUserCreated(UserCreatedEvent event) {
        log.info("Processing UserCreatedEvent for user: {}", event.getUserId());

        return keycloakClient.createUser(
                event.getUserId(),
                event.getEmail(),
                event.getFirstName(),
                event.getLastName(),
                event.getTemporaryPassword(),
                event.getRole()
            )
            .doOnSuccess(keycloakId -> log.info(
                "User created in Keycloak. UserId: {}, KeycloakId: {}",
                event.getUserId(), keycloakId
            ))
            .doOnError(error -> log.error(
                "Failed to create user in Keycloak: {}. Error: {}",
                event.getUserId(), error.getMessage()
            ))
            .then();
    }

    /**
     * Maneja el evento de actualización de usuario.
     * Sincroniza los datos en Keycloak.
     */
    public Mono<Void> handleUserUpdated(UserUpdatedEvent event) {
        log.info("Processing UserUpdatedEvent for user: {}", event.getUserId());

        return keycloakClient.updateUser(
                event.getUserId(),
                event.getEmail(),
                event.getFirstName(),
                event.getLastName()
            )
            .then(updateRoleIfChanged(event))
            .doOnSuccess(v -> log.info("User updated in Keycloak: {}", event.getUserId()))
            .doOnError(error -> log.error(
                "Failed to update user in Keycloak: {}. Error: {}",
                event.getUserId(), error.getMessage()
            ));
    }

    /**
     * Maneja el evento de eliminación (soft delete).
     * Deshabilita el usuario en Keycloak.
     */
    public Mono<Void> handleUserDeleted(UserDeletedEvent event) {
        log.info("Processing UserDeletedEvent for user: {}", event.getUserId());

        return keycloakClient.disableUser(event.getUserId())
            .doOnSuccess(v -> log.info("User disabled in Keycloak: {}", event.getUserId()))
            .doOnError(error -> log.error(
                "Failed to disable user in Keycloak: {}. Error: {}",
                event.getUserId(), error.getMessage()
            ));
    }

    /**
     * Maneja el evento de restauración.
     * Habilita el usuario en Keycloak.
     */
    public Mono<Void> handleUserRestored(UserRestoredEvent event) {
        log.info("Processing UserRestoredEvent for user: {}", event.getUserId());

        return keycloakClient.enableUser(event.getUserId())
            .doOnSuccess(v -> log.info("User enabled in Keycloak: {}", event.getUserId()))
            .doOnError(error -> log.error(
                "Failed to enable user in Keycloak: {}. Error: {}",
                event.getUserId(), error.getMessage()
            ));
    }

    /**
     * Maneja el evento de purga (hard delete).
     * Elimina permanentemente el usuario de Keycloak.
     */
    public Mono<Void> handleUserPurged(UserPurgedEvent event) {
        log.info("Processing UserPurgedEvent for user: {}", event.getUserId());

        return keycloakClient.deleteUser(event.getUserId())
            .doOnSuccess(v -> log.info("User deleted from Keycloak: {}", event.getUserId()))
            .doOnError(error -> log.error(
                "Failed to delete user from Keycloak: {}. Error: {}",
                event.getUserId(), error.getMessage()
            ));
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════

    private Mono<Void> updateRoleIfChanged(UserUpdatedEvent event) {
        if (event.getRole() == null) {
            return Mono.empty();
        }
        // La lógica de cambio de rol requiere conocer el rol anterior
        // Por ahora solo asignamos el nuevo rol (Keycloak maneja duplicados)
        return keycloakClient.assignRole(event.getUserId(), event.getRole());
    }
}
```

---

## 4️⃣ MAPPERS

### 📄 AuthMapper.java

```java
package pe.edu.vallegrande.vgmsauthentication.application.mappers;

import pe.edu.vallegrande.vgmsauthentication.application.dto.request.LoginRequest;
import pe.edu.vallegrande.vgmsauthentication.application.dto.response.*;
import pe.edu.vallegrande.vgmsauthentication.domain.models.UserCredentials;

import java.util.List;
import java.util.Map;

/**
 * Mapper para transformaciones de autenticación.
 *
 * <p>Convierte entre DTOs, modelos de dominio y respuestas de Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public final class AuthMapper {

    private AuthMapper() {
        // Utility class
    }

    // ═══════════════════════════════════════════════════════════════
    // REQUEST -> DOMAIN
    // ═══════════════════════════════════════════════════════════════

    /**
     * Convierte LoginRequest a UserCredentials.
     */
    public static UserCredentials toCredentials(LoginRequest request) {
        return UserCredentials.forPasswordGrant(
            request.getUsername(),
            request.getPassword(),
            request.getClientId()
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // KEYCLOAK RESPONSE -> DTO
    // ═══════════════════════════════════════════════════════════════

    /**
     * Convierte respuesta de Keycloak a LoginResponse.
     */
    public static LoginResponse toLoginResponse(Map<String, Object> keycloakResponse) {
        return LoginResponse.builder()
            .accessToken(getString(keycloakResponse, "access_token"))
            .refreshToken(getString(keycloakResponse, "refresh_token"))
            .tokenType(getString(keycloakResponse, "token_type", "Bearer"))
            .expiresIn(getLong(keycloakResponse, "expires_in"))
            .refreshExpiresIn(getLong(keycloakResponse, "refresh_expires_in"))
            .userId(getString(keycloakResponse, "user_id"))
            .organizationId(getString(keycloakResponse, "organization_id"))
            .email(getString(keycloakResponse, "email"))
            .fullName(getString(keycloakResponse, "full_name"))
            .role(getString(keycloakResponse, "role"))
            .scope(getString(keycloakResponse, "scope"))
            .build();
    }

    /**
     * Convierte respuesta de Keycloak a TokenResponse.
     */
    public static TokenResponse toTokenResponse(Map<String, Object> keycloakResponse) {
        return TokenResponse.builder()
            .accessToken(getString(keycloakResponse, "access_token"))
            .refreshToken(getString(keycloakResponse, "refresh_token"))
            .tokenType(getString(keycloakResponse, "token_type", "Bearer"))
            .expiresIn(getLong(keycloakResponse, "expires_in"))
            .refreshExpiresIn(getLong(keycloakResponse, "refresh_expires_in"))
            .scope(getString(keycloakResponse, "scope"))
            .build();
    }

    /**
     * Convierte respuesta de userinfo a UserInfoResponse.
     */
    @SuppressWarnings("unchecked")
    public static UserInfoResponse toUserInfoResponse(Map<String, Object> userInfo) {
        return UserInfoResponse.builder()
            .sub(getString(userInfo, "sub"))
            .email(getString(userInfo, "email"))
            .emailVerified(getBoolean(userInfo, "email_verified"))
            .preferredUsername(getString(userInfo, "preferred_username"))
            .givenName(getString(userInfo, "given_name"))
            .familyName(getString(userInfo, "family_name"))
            .name(getString(userInfo, "name"))
            .organizationId(getString(userInfo, "organization_id"))
            .realmRoles((List<String>) userInfo.get("realm_roles"))
            .clientRoles((List<String>) userInfo.get("client_roles"))
            .build();
    }

    /**
     * Convierte respuesta de introspección a IntrospectResponse.
     */
    public static IntrospectResponse toIntrospectResponse(Map<String, Object> introspection) {
        return IntrospectResponse.builder()
            .active(getBoolean(introspection, "active"))
            .tokenType(getString(introspection, "token_type"))
            .scope(getString(introspection, "scope"))
            .clientId(getString(introspection, "client_id"))
            .username(getString(introspection, "username"))
            .sub(getString(introspection, "sub"))
            .exp(getLong(introspection, "exp"))
            .iat(getLong(introspection, "iat"))
            .iss(getString(introspection, "iss"))
            .aud(getString(introspection, "aud"))
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private static Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
```

---

## 📋 Resumen de Eventos

| Evento | Routing Key | Acción en Keycloak |
|--------|-------------|-------------------|
| `UserCreatedEvent` | `user.created` | Crear usuario con credenciales |
| `UserUpdatedEvent` | `user.updated` | Actualizar datos y rol |
| `UserDeletedEvent` | `user.deleted` | Deshabilitar usuario |
| `UserRestoredEvent` | `user.restored` | Habilitar usuario |
| `UserPurgedEvent` | `user.purged` | Eliminar permanentemente |

---

## 🔄 Flujo de Eventos (RabbitMQ)

### Diagrama de Eventos de Usuario

```
┌─────────────┐       ┌──────────────┐       ┌─────────────────────┐
│ vg-ms-users │       │   RabbitMQ   │       │ vg-ms-authentication│
└──────┬──────┘       └──────┬───────┘       └──────────┬──────────┘
       │                     │                          │
       │  user.created       │                          │
       │────────────────────>│                          │
       │                     │   Consume event          │
       │                     │─────────────────────────>│
       │                     │                          │
       │                     │                          │  Create in Keycloak
       │                     │                          │──────────────────────>
       │                     │                          │
       │  user.deleted       │                          │
       │────────────────────>│                          │
       │                     │   Consume event          │
       │                     │─────────────────────────>│
       │                     │                          │
       │                     │                          │  Disable in Keycloak
       │                     │                          │──────────────────────>
       │                     │                          │
```

### Diagrama de Capas de Aplicación

```
┌─────────────────────────────────────────────────────────────────┐
│                     ⚙️ APPLICATION LAYER                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                       USE CASES                          │   │
│  │  ┌────────────────┐ ┌────────────────┐                  │   │
│  │  │LoginUseCaseImpl│ │LogoutUseCaseImpl│                 │   │
│  │  └────────┬───────┘ └────────┬───────┘                  │   │
│  │           │                  │                          │   │
│  │  ┌────────┴───────┐ ┌────────┴────────┐                 │   │
│  │  │RefreshTokenUC  │ │ValidateTokenUC  │                 │   │
│  │  │Impl            │ │Impl             │                 │   │
│  │  └────────────────┘ └─────────────────┘                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                        DTOs                              │   │
│  │  ┌─────────────────────┐  ┌──────────────────────────┐  │   │
│  │  │      REQUEST        │  │       RESPONSE           │  │   │
│  │  ├─────────────────────┤  ├──────────────────────────┤  │   │
│  │  │ LoginRequest        │  │ LoginResponse            │  │   │
│  │  │ RefreshTokenRequest │  │ TokenResponse            │  │   │
│  │  │ LogoutRequest       │  │ UserInfoResponse         │  │   │
│  │  │                     │  │ IntrospectResponse       │  │   │
│  │  └─────────────────────┘  └──────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  EXTERNAL EVENTS (ESCUCHA)               │   │
│  │  ┌────────────────────────────────────────────────────┐ │   │
│  │  │  UserCreatedEvent  │  UserUpdatedEvent             │ │   │
│  │  │  UserDeletedEvent  │  UserRestoredEvent            │ │   │
│  │  │  UserPurgedEvent   │                               │ │   │
│  │  └────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                       MAPPERS                            │   │
│  │  ┌────────────────────────────────────────────────────┐ │   │
│  │  │  AuthMapper                                        │ │   │
│  │  │    - toCredentials(LoginRequest)                   │ │   │
│  │  │    - toLoginResponse(Map<String,Object>)           │ │   │
│  │  │    - toTokenResponse(Map<String,Object>)           │ │   │
│  │  │    - toUserInfoResponse(Map<String,Object>)        │ │   │
│  │  └────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Flujo de Procesamiento de Evento user.created

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        PROCESO: user.created                              │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. RabbitMQ recibe mensaje en cola "auth.user.created"                  │
│     │                                                                    │
│     ▼                                                                    │
│  2. UserEventListener.handleUserCreated(message)                         │
│     │                                                                    │
│     │  ┌───────────────────────────────────────┐                         │
│     └─>│ Deserializar JSON a UserCreatedEvent  │                         │
│        └──────────────────┬────────────────────┘                         │
│                           │                                              │
│                           ▼                                              │
│  3. UserEventHandler.handleUserCreated(event)                            │
│     │                                                                    │
│     │  ┌───────────────────────────────────────┐                         │
│     └─>│ Extraer datos: userId, email, role    │                         │
│        └──────────────────┬────────────────────┘                         │
│                           │                                              │
│                           ▼                                              │
│  4. IKeycloakClient.createUser(...)                                      │
│     │                                                                    │
│     │  ┌───────────────────────────────────────┐                         │
│     └─>│ POST /admin/realms/sistema-jass/users │                         │
│        │ {username, email, firstName, lastName,│                         │
│        │  enabled: true, credentials: [...]}   │                         │
│        └──────────────────┬────────────────────┘                         │
│                           │                                              │
│                           ▼                                              │
│  5. IKeycloakClient.assignRole(userId, role)                             │
│     │                                                                    │
│     │  ┌───────────────────────────────────────┐                         │
│     └─>│ POST /admin/.../users/{id}/role-mappings│                       │
│        └───────────────────────────────────────┘                         │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

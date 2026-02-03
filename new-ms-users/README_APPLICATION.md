# 📦 APPLICATION LAYER - Capa de Aplicación

> **Orquestación de casos de uso. Coordina el dominio con la infraestructura.**

## 📋 Principios

1. **Orquestación**: Coordina llamadas entre dominio y puertos de salida
2. **Sin Lógica de Negocio**: La lógica vive en el dominio
3. **Transformación de Datos**: Convierte entre DTOs y entidades de dominio
4. **Transaccionalidad**: Maneja las transacciones

---

## 📂 Estructura

```
application/
├── usecases/                        → Implementaciones de casos de uso
│   ├── CreateUserUseCaseImpl.java
│   ├── GetUserUseCaseImpl.java
│   ├── UpdateUserUseCaseImpl.java
│   ├── DeleteUserUseCaseImpl.java
│   ├── RestoreUserUseCaseImpl.java
│   └── PurgeUserUseCaseImpl.java
├── dto/                             → Objetos de transferencia
│   ├── common/
│   │   ├── ApiResponse.java
│   │   ├── PageResponse.java
│   │   └── ErrorMessage.java
│   ├── request/
│   │   ├── CreateUserRequest.java
│   │   └── UpdateUserRequest.java
│   └── response/
│       └── UserResponse.java
├── mappers/                         → Conversiones
│   └── UserMapper.java
└── events/                          → DTOs de eventos
    ├── UserCreatedEvent.java
    ├── UserUpdatedEvent.java
    ├── UserDeletedEvent.java
    ├── UserRestoredEvent.java
    └── UserPurgedEvent.java
```

---

## 1️⃣ USE CASES - Implementaciones

### 📄 CreateUserUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.DuplicateDocumentException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.ExternalServiceException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.ICreateUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del caso de uso para crear usuarios.
 *
 * <p><b>Flujo:</b></p>
 * <ol>
 *   <li>Validar que no exista duplicado por documento</li>
 *   <li>Validar organización, zona y calle</li>
 *   <li>Validar medio de contacto</li>
 *   <li>Guardar en base de datos</li>
 *   <li>Publicar evento</li>
 *   <li>Notificar al usuario (async, no bloquea)</li>
 * </ol>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements ICreateUserUseCase {

    private final IUserRepository userRepository;
    private final IOrganizationClient organizationClient;
    private final IUserEventPublisher eventPublisher;
    private final INotificationClient notificationClient;

    @Override
    @Transactional
    public Mono<User> execute(User user, String createdBy) {
        log.info("Creando usuario con documento: {}", user.getDocumentNumber());

        return validateDocumentNotExists(user.getDocumentNumber())
            .then(validateOrganizationHierarchy(user))
            .then(Mono.fromCallable(() -> {
                user.validateContact();
                return user;
            }))
            .flatMap(validatedUser -> {
                User userToSave = User.builder()
                    .id(UUID.randomUUID().toString())
                    .organizationId(validatedUser.getOrganizationId())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .firstName(validatedUser.getFirstName())
                    .lastName(validatedUser.getLastName())
                    .documentType(validatedUser.getDocumentType())
                    .documentNumber(validatedUser.getDocumentNumber())
                    .email(validatedUser.getEmail())
                    .phone(validatedUser.getPhone())
                    .address(validatedUser.getAddress())
                    .zoneId(validatedUser.getZoneId())
                    .streetId(validatedUser.getStreetId())
                    .role(validatedUser.getRole())
                    .build();

                return userRepository.save(userToSave);
            })
            .flatMap(savedUser -> publishEventAndNotify(savedUser, createdBy))
            .doOnSuccess(u -> log.info("Usuario creado: {}", u.getId()))
            .doOnError(e -> log.error("Error creando al usuario: {}", e.getMessage()));
    }

    /**
     * Valida que no exista un usuario con el mismo documento.
     */
    private Mono<Void> validateDocumentNotExists(String documentNumber) {
        return userRepository.existsByDocumentNumber(documentNumber)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new DuplicateDocumentException(documentNumber));
                }
                return Mono.empty();
            });
    }

    /**
     * Valida la jerarquía: organización -> zona -> calle.
     */
    private Mono<Void> validateOrganizationHierarchy(User user) {
        return organizationClient.validateHierarchy(
                user.getOrganizationId(),
                user.getZoneId(),
                user.getStreetId()
            )
            .flatMap(isValid -> {
                if (!isValid) {
                    return Mono.error(new BusinessRuleException(
                        "La combinación organización/zona/calle no es válida"
                    ));
                }
                return Mono.empty();
            })
            .onErrorResume(ExternalServiceException.class, e -> {
                log.warn("Organization service unavailable, skipping validation: {}", e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Publica evento y envía notificación (no bloquea si falla).
     */
    private Mono<User> publishEventAndNotify(User user, String createdBy) {
        return eventPublisher.publishUserCreated(user, createdBy)
            .then(sendWelcomeNotification(user))
            .thenReturn(user)
            .onErrorResume(e -> {
                log.warn("Failed to publish event or send notification: {}", e.getMessage());
                return Mono.just(user); // No fallar la creación si la notificación falla
            });
    }

    /**
     * Envía notificación de bienvenida (fire and forget).
     */
    private Mono<Void> sendWelcomeNotification(User user) {
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            return Mono.empty();
        }

        return notificationClient.sendWelcomeMessage(
                user.getPhone(),
                user.getFirstName(),
                "JASS Digital" // TODO: Obtener nombre de organización
            )
            .onErrorResume(e -> {
                log.warn("Failed to send welcome notification: {}", e.getMessage());
                return Mono.empty();
            });
    }
}
```

---

### 📄 GetUserUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IGetUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso para consultar usuarios.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserUseCaseImpl implements IGetUserUseCase {

    private final IUserRepository userRepository;

    @Override
    public Mono<User> findById(String id) {
        log.debug("Finding user by ID: {}", id);

        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .doOnSuccess(u -> log.debug("User found: {}", u.getId()));
    }

    @Override
    public Mono<User> findByDocumentNumber(String documentNumber) {
        log.debug("Finding user by document: {}", documentNumber);

        return userRepository.findByDocumentNumber(documentNumber)
            .switchIfEmpty(Mono.error(UserNotFoundException.byDocument(documentNumber)));
    }

    @Override
    public Flux<User> findAllActive() {
        log.debug("Finding all active users");
        return userRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<User> findAll() {
        log.debug("Finding all users (including inactive)");
        return userRepository.findAll();
    }

    @Override
    public Flux<User> findByOrganizationId(String organizationId) {
        log.debug("Finding users by organization: {}", organizationId);
        return userRepository.findByOrganizationId(organizationId);
    }

    @Override
    public Flux<User> findActiveByOrganizationId(String organizationId) {
        log.debug("Finding active users by organization: {}", organizationId);
        return userRepository.findByOrganizationIdAndRecordStatus(organizationId, RecordStatus.ACTIVE);
    }

    @Override
    public Mono<Boolean> existsByDocumentNumber(String documentNumber) {
        return userRepository.existsByDocumentNumber(documentNumber);
    }
}
```

---

### 📄 UpdateUserUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IUpdateUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserEventPublisher;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementación del caso de uso para actualizar usuarios.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserUseCaseImpl implements IUpdateUserUseCase {

    private final IUserRepository userRepository;
    private final IUserEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<User> execute(String id, User userData, String updatedBy) {
        log.info("Updating user: {}", id);

        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(existingUser -> {
                if (existingUser.isInactive()) {
                    return Mono.error(new BusinessRuleException(
                        "No se puede actualizar un usuario inactivo"
                    ));
                }

                Map<String, Object> changedFields = detectChanges(existingUser, userData);

                User updatedUser = existingUser.updateWith(
                    userData.getFirstName(),
                    userData.getLastName(),
                    userData.getEmail(),
                    userData.getPhone(),
                    userData.getAddress(),
                    updatedBy
                );

                return userRepository.update(updatedUser)
                    .flatMap(saved -> publishUpdateEvent(saved, changedFields, updatedBy));
            })
            .doOnSuccess(u -> log.info("Usuario actualizado: {}", u.getId()))
            .doOnError(e -> log.error("Error al actualizar usuario: {}", e.getMessage()));
    }

    /**
     * Detecta qué campos cambiaron entre el usuario existente y los nuevos datos.
     */
    private Map<String, Object> detectChanges(User existing, User newData) {
        Map<String, Object> changes = new HashMap<>();

        if (newData.getFirstName() != null && !Objects.equals(existing.getFirstName(), newData.getFirstName())) {
            changes.put("firstName", newData.getFirstName());
        }
        if (newData.getLastName() != null && !Objects.equals(existing.getLastName(), newData.getLastName())) {
            changes.put("lastName", newData.getLastName());
        }
        if (newData.getEmail() != null && !Objects.equals(existing.getEmail(), newData.getEmail())) {
            changes.put("email", newData.getEmail());
        }
        if (newData.getPhone() != null && !Objects.equals(existing.getPhone(), newData.getPhone())) {
            changes.put("phone", newData.getPhone());
        }
        if (newData.getAddress() != null && !Objects.equals(existing.getAddress(), newData.getAddress())) {
            changes.put("address", newData.getAddress());
        }

        return changes;
    }

    /**
     * Publica evento de actualización.
     */
    private Mono<User> publishUpdateEvent(User user, Map<String, Object> changedFields, String updatedBy) {
        if (changedFields.isEmpty()) {
            return Mono.just(user);
        }

        return eventPublisher.publishUserUpdated(user, changedFields, updatedBy)
            .thenReturn(user)
            .onErrorResume(e -> {
                log.warn("Failed to publish update event: {}", e.getMessage());
                return Mono.just(user);
            });
    }
}
```

---

### 📄 DeleteUserUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IDeleteUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserEventPublisher;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso para eliminar usuarios (soft delete).
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserUseCaseImpl implements IDeleteUserUseCase {

    private final IUserRepository userRepository;
    private final IUserEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<User> execute(String id, String deletedBy, String reason) {
        log.info("Soft deleting user: {} - Reason: {}", id, reason);

        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(user -> {
                if (user.isInactive()) {
                    return Mono.error(new BusinessRuleException(
                        "El usuario ya se encuentra inactivo"
                    ));
                }

                User deletedUser = user.markAsDeleted(deletedBy);

                return userRepository.update(deletedUser)
                    .flatMap(saved -> publishDeleteEvent(saved, reason, deletedBy));
            })
            .doOnSuccess(u -> log.info("User soft deleted successfully: {}", u.getId()))
            .doOnError(e -> log.error("Error soft deleting user: {}", e.getMessage()));
    }

    private Mono<User> publishDeleteEvent(User user, String reason, String deletedBy) {
        return eventPublisher.publishUserDeleted(
                user.getId(),
                user.getOrganizationId(),
                reason,
                deletedBy
            )
            .thenReturn(user)
            .onErrorResume(e -> {
                log.warn("Failed to publish delete event: {}", e.getMessage());
                return Mono.just(user);
            });
    }
}
```

---

### 📄 RestoreUserUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IRestoreUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserEventPublisher;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso para restaurar usuarios.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreUserUseCaseImpl implements IRestoreUserUseCase {

    private final IUserRepository userRepository;
    private final IUserEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<User> execute(String id, String restoredBy) {
        log.info("Restoring user: {}", id);

        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(user -> {
                // Validar que el usuario esté inactivo
                if (user.isActive()) {
                    return Mono.error(new BusinessRuleException(
                        "El usuario ya se encuentra activo"
                    ));
                }

                // Usar método del dominio para restaurar
                User restoredUser = user.restore(restoredBy);

                return userRepository.update(restoredUser)
                    .flatMap(saved -> publishRestoreEvent(saved, restoredBy));
            })
            .doOnSuccess(u -> log.info("User restored successfully: {}", u.getId()))
            .doOnError(e -> log.error("Error restoring user: {}", e.getMessage()));
    }

    private Mono<User> publishRestoreEvent(User user, String restoredBy) {
        return eventPublisher.publishUserRestored(
                user.getId(),
                user.getOrganizationId(),
                restoredBy
            )
            .thenReturn(user)
            .onErrorResume(e -> {
                log.warn("Failed to publish restore event: {}", e.getMessage());
                return Mono.just(user);
            });
    }
}
```

---

### 📄 PurgeUserUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IPurgeUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserEventPublisher;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso para eliminar usuarios físicamente (hard delete).
 *
 * <p><b>⚠️ OPERACIÓN IRREVERSIBLE</b></p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurgeUserUseCaseImpl implements IPurgeUserUseCase {

    private final IUserRepository userRepository;
    private final IUserEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<Void> execute(String id, String purgedBy, String reason) {
        log.warn("PURGING user (hard delete): {} - Reason: {}", id, reason);

        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(user -> {
                // Solo permitir purgar usuarios inactivos
                if (user.isActive()) {
                    return Mono.error(new BusinessRuleException(
                        "Solo se pueden eliminar permanentemente usuarios inactivos. " +
                        "Primero realice un soft delete."
                    ));
                }

                // Guardar snapshot para el evento antes de eliminar
                User snapshot = user;

                return userRepository.deleteById(id)
                    .then(publishPurgeEvent(snapshot, reason, purgedBy));
            })
            .doOnSuccess(v -> log.warn("User PURGED successfully: {}", id))
            .doOnError(e -> log.error("Error purging user: {}", e.getMessage()));
    }

    private Mono<Void> publishPurgeEvent(User user, String reason, String purgedBy) {
        return eventPublisher.publishUserPurged(user, reason, purgedBy)
            .onErrorResume(e -> {
                log.error("CRITICAL: Failed to publish purge event for user {}: {}",
                    user.getId(), e.getMessage());
                // Para purge, el evento es crítico para auditoría
                return Mono.error(e);
            });
    }
}
```

---

## 2️⃣ DTOs - Objetos de Transferencia

### 📁 dto/common/

#### 📄 ApiResponse.java

```java
package pe.edu.vallegrande.vgmsusers.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wrapper estándar para todas las respuestas de la API.
 *
 * @param <T> tipo de datos contenidos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<ErrorMessage> errors;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message, ErrorMessage error) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(List.of(error))
            .build();
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorMessage> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .build();
    }
}
```

---

#### 📄 ErrorMessage.java

```java
package pe.edu.vallegrande.vgmsusers.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detalle de un error.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {
    private String field;
    private String message;
    private String errorCode;
    private Integer status;

    public static ErrorMessage validation(String field, String message, String errorCode) {
        return ErrorMessage.builder()
            .field(field)
            .message(message)
            .errorCode(errorCode)
            .status(400)
            .build();
    }

    public static ErrorMessage of(String message, String errorCode, int status) {
        return ErrorMessage.builder()
            .message(message)
            .errorCode(errorCode)
            .status(status)
            .build();
    }
}
```

---

#### 📄 PageResponse.java

```java
package pe.edu.vallegrande.vgmsusers.application.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<T>builder()
            .content(content)
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .first(page == 0)
            .last(page >= totalPages - 1)
            .build();
    }
}
```

---

### 📁 dto/request/

#### 📄 CreateUserRequest.java

```java
package pe.edu.vallegrande.vgmsusers.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "El ID de organización es obligatorio")
    private String organizationId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 150, message = "El apellido debe tener entre 2 y 150 caracteres")
    private String lastName;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "^(DNI|RUC|CE)$", message = "Tipo de documento debe ser DNI, RUC o CE")
    private String documentType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9]{8,20}$", message = "Documento inválido")
    private String documentNumber;

    @Email(message = "El email debe ser válido")
    private String email;  // Opcional

    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "El teléfono debe ser válido")
    private String phone;  // Opcional

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 250, message = "La dirección no puede exceder 250 caracteres")
    private String address;

    @NotBlank(message = "El ID de zona es obligatorio")
    private String zoneId;

    @NotBlank(message = "El ID de calle es obligatorio")
    private String streetId;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(SUPER_ADMIN|ADMIN|CLIENT)$", message = "Rol debe ser SUPER_ADMIN, ADMIN o CLIENT")
    private String role;
}
```

---

#### 📄 UpdateUserRequest.java

```java
package pe.edu.vallegrande.vgmsusers.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un usuario.
 * Todos los campos son opcionales (se actualizan solo los proporcionados).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @Size(min = 2, max = 150, message = "El apellido debe tener entre 2 y 150 caracteres")
    private String lastName;

    @Email(message = "El email debe ser válido")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "El teléfono debe ser válido")
    private String phone;

    @Size(max = 250, message = "La dirección no puede exceder 250 caracteres")
    private String address;
}
```

---

### 📁 dto/response/

#### 📄 UserResponse.java

```java
package pe.edu.vallegrande.vgmsusers.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String organizationId;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // Datos personales
    private String firstName;
    private String lastName;
    private String fullName;      // Computed: firstName + lastName
    private String documentType;
    private String documentNumber;
    private String email;
    private String phone;
    private String address;

    // Referencias geográficas
    private String zoneId;
    private String streetId;

    // Rol
    private String role;
    private String roleDisplayName;
}
```

---

## 3️⃣ MAPPERS - Conversiones

#### 📄 UserMapper.java

```java
package pe.edu.vallegrande.vgmsusers.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.application.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.request.UpdateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities.UserEntity;

@Component
public class UserMapper {

    // REQUEST -> DOMAIN

    public User toModel(CreateUserRequest request) {
        return User.builder()
            .organizationId(request.getOrganizationId())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .documentType(DocumentType.valueOf(request.getDocumentType()))
            .documentNumber(request.getDocumentNumber())
            .email(request.getEmail())
            .phone(request.getPhone())
            .address(request.getAddress())
            .zoneId(request.getZoneId())
            .streetId(request.getStreetId())
            .role(Role.valueOf(request.getRole()))
            .build();
    }

    public User toModel(UpdateUserRequest request) {
        return User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .address(request.getAddress())
            .build();
    }

    // DOMAIN -> RESPONSE

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .organizationId(user.getOrganizationId())
            .recordStatus(user.getRecordStatus().name())
            .createdAt(user.getCreatedAt())
            .createdBy(user.getCreatedBy())
            .updatedAt(user.getUpdatedAt())
            .updatedBy(user.getUpdatedBy())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .documentType(user.getDocumentType().name())
            .documentNumber(user.getDocumentNumber())
            .email(user.getEmail())
            .phone(user.getPhone())
            .address(user.getAddress())
            .zoneId(user.getZoneId())
            .streetId(user.getStreetId())
            .role(user.getRole().name())
            .roleDisplayName(user.getRole().getDisplayName())
            .build();
    }

    // DOMAIN -> ENTITY

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
            .id(user.getId())
            .organizationId(user.getOrganizationId())
            .recordStatus(user.getRecordStatus().name())
            .createdAt(user.getCreatedAt())
            .createdBy(user.getCreatedBy())
            .updatedAt(user.getUpdatedAt())
            .updatedBy(user.getUpdatedBy())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .documentType(user.getDocumentType().name())
            .documentNumber(user.getDocumentNumber())
            .email(user.getEmail())
            .phone(user.getPhone())
            .address(user.getAddress())
            .zoneId(user.getZoneId())
            .streetId(user.getStreetId())
            .role(user.getRole().name())
            .build();
    }

    /**
     * Convierte Entity a modelo de dominio.
     */
    public User toModel(UserEntity entity) {
        return User.builder()
            .id(entity.getId())
            .organizationId(entity.getOrganizationId())
            .recordStatus(pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus.valueOf(entity.getRecordStatus()))
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .documentType(DocumentType.valueOf(entity.getDocumentType()))
            .documentNumber(entity.getDocumentNumber())
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .address(entity.getAddress())
            .zoneId(entity.getZoneId())
            .streetId(entity.getStreetId())
            .role(Role.valueOf(entity.getRole()))
            .build();
    }
}
```

---

## 4️⃣ EVENTS - DTOs de Eventos

#### 📄 UserCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsusers.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se crea un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    @Builder.Default
    private String eventType = "USER_CREATED";

    private String eventId;
    private LocalDateTime timestamp;

    // Datos del usuario
    private String userId;
    private String organizationId;
    private String email;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String role;

    // Metadata
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 UserUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsusers.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento publicado cuando se actualiza un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    @Builder.Default
    private String eventType = "USER_UPDATED";

    private String eventId;
    private LocalDateTime timestamp;

    // Identificador
    private String userId;
    private String organizationId;

    // Campos modificados
    private Map<String, Object> changedFields;

    // Metadata
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 UserDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsusers.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se elimina un usuario (soft delete).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {

    @Builder.Default
    private String eventType = "USER_DELETED";

    private String eventId;
    private LocalDateTime timestamp;

    // Identificador
    private String userId;
    private String organizationId;

    // Información del cambio
    private String previousStatus;  // ACTIVE
    private String reason;

    // Metadata
    private String deletedBy;
    private String correlationId;
}
```

---

#### 📄 UserRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsusers.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se restaura un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestoredEvent {

    @Builder.Default
    private String eventType = "USER_RESTORED";

    private String eventId;
    private LocalDateTime timestamp;

    // Identificador
    private String userId;
    private String organizationId;

    // Información del cambio
    private String previousStatus;  // INACTIVE

    // Metadata
    private String restoredBy;
    private String correlationId;
}
```

---

#### 📄 UserPurgedEvent.java

```java
package pe.edu.vallegrande.vgmsusers.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se elimina un usuario permanentemente (hard delete).
 *
 * <p>Incluye snapshot de datos para auditoría.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPurgedEvent {

    @Builder.Default
    private String eventType = "USER_PURGED";

    private String eventId;
    private LocalDateTime timestamp;

    // Identificador
    private String userId;
    private String organizationId;

    // Snapshot para auditoría
    private String email;
    private String documentNumber;
    private String firstName;
    private String lastName;

    // Razón (requerida para auditoría)
    private String reason;

    // Metadata
    private String purgedBy;
    private String correlationId;
}
```

---

## ✅ Resumen de la Capa de Aplicación

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| Use Cases | 6 clases | CRUD + Restore + Purge |
| DTOs Common | 3 clases | ApiResponse, PageResponse, ErrorMessage |
| DTOs Request | 2 clases | Create, Update |
| DTOs Response | 1 clase | UserResponse |
| Mappers | 1 clase | UserMapper |
| Events | 5 clases | Created, Updated, Deleted, Restored, Purged |

---

> **Siguiente paso**: Lee [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) para ver la implementación de la capa de infraestructura.

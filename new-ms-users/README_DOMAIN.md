# 💎 DOMAIN LAYER - Capa de Dominio

> **El corazón del negocio. Sin dependencias externas.**

## 📋 Principios

1. **Independencia Total**: No importa nada de `application` ni `infrastructure`
2. **Reglas de Negocio Puras**: Lógica que no cambia por tecnología
3. **Entidades Ricas**: Modelos con comportamiento, no solo datos (Anemic Domain evitado)

---

## 📂 Estructura

```
domain/
├── models/
│   ├── User.java                    → Entidad principal
│   └── valueobjects/
│       ├── Role.java                → Enum de roles
│       ├── DocumentType.java        → Enum tipos de documento
│       └── RecordStatus.java        → Enum estado del registro
├── ports/
│   ├── in/                          → Interfaces de casos de uso
│   │   ├── ICreateUserUseCase.java
│   │   ├── IGetUserUseCase.java
│   │   ├── IUpdateUserUseCase.java
│   │   ├── IDeleteUserUseCase.java
│   │   ├── IRestoreUserUseCase.java
│   │   └── IPurgeUserUseCase.java
│   └── out/                         → Interfaces de repositorios/clientes
│       ├── IUserRepository.java
│       ├── IAuthenticationClient.java
│       ├── IOrganizationClient.java
│       ├── INotificationClient.java
│       └── IUserEventPublisher.java
└── exceptions/                      → Excepciones de dominio
    ├── DomainException.java
    ├── NotFoundException.java
    ├── BusinessRuleException.java
    ├── ValidationException.java
    ├── ConflictException.java
    ├── ExternalServiceException.java
    ├── UserNotFoundException.java
    ├── DuplicateDocumentException.java
    └── InvalidContactException.java
```

---

## 1️⃣ MODELS - Entidades de Dominio

### 📄 User.java

```java
package pe.edu.vallegrande.vgmsusers.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.InvalidContactException;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;

import java.time.LocalDateTime;

/**
 * Entidad de dominio User.
 *
 * <p>Representa un usuario del sistema JASS Digital. Contiene lógica de negocio
 * para validaciones y comportamiento del usuario.</p>
 *
 * <p><b>Regla de negocio importante:</b> Al menos email O phone debe estar presente.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String organizationId;
    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // Datos personales
    private String firstName;
    private String lastName;
    private DocumentType documentType;
    private String documentNumber;
    private String email;      // OPCIONAL pero al menos uno debe existir
    private String phone;      // OPCIONAL pero al menos uno debe existir
    private String address;

    // Referencias geográficas
    private String zoneId;
    private String streetId;

    // Rol del usuario
    private Role role;

    // ═══════════════════════════════════════════════════════════════
    // COMPORTAMIENTO DE DOMINIO (Rich Domain Model)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Valida que el usuario tenga al menos un medio de contacto.
     *
     * @throws InvalidContactException si no tiene email ni teléfono
     */
    public void validateContact() {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();

        if (!hasEmail && !hasPhone) {
            throw new InvalidContactException(
                "El usuario debe tener al menos un medio de contacto (email o teléfono)"
            );
        }
    }

    /**
     * Obtiene el nombre completo del usuario.
     *
     * @return nombre completo (firstName + lastName)
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    /**
     * Verifica si el usuario está activo.
     *
     * @return true si el estado es ACTIVE
     */
    public boolean isActive() {
        return RecordStatus.ACTIVE.equals(recordStatus);
    }

    /**
     * Verifica si el usuario está inactivo (eliminado lógicamente).
     *
     * @return true si el estado es INACTIVE
     */
    public boolean isInactive() {
        return RecordStatus.INACTIVE.equals(recordStatus);
    }

    /**
     * Verifica si el usuario es administrador (SUPER_ADMIN o ADMIN).
     *
     * @return true si tiene rol de administrador
     */
    public boolean isAdmin() {
        return Role.SUPER_ADMIN.equals(role) || Role.ADMIN.equals(role);
    }

    /**
     * Verifica si el usuario es super administrador.
     *
     * @return true si tiene rol SUPER_ADMIN
     */
    public boolean isSuperAdmin() {
        return Role.SUPER_ADMIN.equals(role);
    }

    /**
     * Marca el usuario como eliminado (soft delete).
     *
     * @param deletedBy identificador de quien elimina
     * @return nueva instancia con estado INACTIVE
     */
    public User markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    /**
     * Restaura el usuario eliminado.
     *
     * @param restoredBy identificador de quien restaura
     * @return nueva instancia con estado ACTIVE
     */
    public User restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    /**
     * Crea una nueva instancia con datos actualizados.
     * Mantiene inmutabilidad del objeto.
     *
     * @param firstName nuevo nombre (null para mantener actual)
     * @param lastName nuevo apellido (null para mantener actual)
     * @param email nuevo email (null para mantener actual)
     * @param phone nuevo teléfono (null para mantener actual)
     * @param address nueva dirección (null para mantener actual)
     * @param updatedBy quien actualiza
     * @return nueva instancia actualizada
     */
    public User updateWith(
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String updatedBy
    ) {
        User updated = this.toBuilder()
            .firstName(firstName != null ? firstName : this.firstName)
            .lastName(lastName != null ? lastName : this.lastName)
            .email(email != null ? email : this.email)
            .phone(phone != null ? phone : this.phone)
            .address(address != null ? address : this.address)
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy)
            .build();

        // Validar que siga teniendo contacto
        updated.validateContact();

        return updated;
    }
}
```

---

### 📄 valueobjects/Role.java

```java
package pe.edu.vallegrande.vgmsusers.domain.models.valueobjects;

/**
 * Enum que define los roles de usuario en el sistema.
 *
 * <p><b>Jerarquía de permisos:</b></p>
 * <ul>
 *   <li>SUPER_ADMIN: Acceso total al sistema</li>
 *   <li>ADMIN: Administrador de una organización específica</li>
 *   <li>CLIENT: Usuario final / Cliente del servicio de agua</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public enum Role {

    /**
     * Super Administrador - Acceso total al sistema.
     * Puede gestionar todas las organizaciones.
     */
    SUPER_ADMIN("Super Administrador"),

    /**
     * Administrador - Gestiona una organización específica.
     * Puede crear usuarios, ver reportes, gestionar pagos.
     */
    ADMIN("Administrador"),

    /**
     * Cliente - Usuario final del servicio.
     * Puede ver sus recibos, pagar, reportar incidencias.
     */
    CLIENT("Cliente");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica si el rol tiene permisos de administración.
     *
     * @return true si es SUPER_ADMIN o ADMIN
     */
    public boolean hasAdminPrivileges() {
        return this == SUPER_ADMIN || this == ADMIN;
    }

    /**
     * Verifica si el rol puede gestionar otros usuarios.
     *
     * @return true si es SUPER_ADMIN o ADMIN
     */
    public boolean canManageUsers() {
        return this == SUPER_ADMIN || this == ADMIN;
    }
}
```

---

### 📄 valueobjects/DocumentType.java

```java
package pe.edu.vallegrande.vgmsusers.domain.models.valueobjects;

/**
 * Enum que define los tipos de documento de identidad.
 *
 * <p>Tipos soportados en Perú:</p>
 * <ul>
 *   <li>DNI: Documento Nacional de Identidad (8 dígitos)</li>
 *   <li>RUC: Registro Único de Contribuyentes (11 dígitos)</li>
 *   <li>CE: Carné de Extranjería (9 caracteres alfanuméricos)</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public enum DocumentType {

    /**
     * Documento Nacional de Identidad.
     * Formato: 8 dígitos numéricos.
     */
    DNI("DNI", "Documento Nacional de Identidad", "^[0-9]{8}$"),

    /**
     * Registro Único de Contribuyentes.
     * Formato: 11 dígitos numéricos.
     */
    RUC("RUC", "Registro Único de Contribuyentes", "^[0-9]{11}$"),

    /**
     * Carné de Extranjería.
     * Formato: 9 caracteres alfanuméricos.
     */
    CE("CE", "Carné de Extranjería", "^[A-Za-z0-9]{9}$");

    private final String code;
    private final String description;
    private final String pattern;

    DocumentType(String code, String description, String pattern) {
        this.code = code;
        this.description = description;
        this.pattern = pattern;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * Valida si un número de documento cumple el formato esperado.
     *
     * @param documentNumber número a validar
     * @return true si el formato es válido
     */
    public boolean isValidFormat(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            return false;
        }
        return documentNumber.matches(pattern);
    }
}
```

---

### 📄 valueobjects/RecordStatus.java

```java
package pe.edu.vallegrande.vgmsusers.domain.models.valueobjects;

/**
 * Enum que define el estado del registro para soft delete.
 *
 * <p><b>Estados:</b></p>
 * <ul>
 *   <li>ACTIVE: Registro activo y visible</li>
 *   <li>INACTIVE: Registro eliminado lógicamente</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public enum RecordStatus {

    /**
     * Registro activo - Visible en consultas normales.
     */
    ACTIVE("Activo"),

    /**
     * Registro inactivo - Eliminado lógicamente.
     * Solo visible en consultas administrativas.
     */
    INACTIVE("Inactivo");

    private final String displayName;

    RecordStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica si el estado representa un registro activo.
     *
     * @return true si es ACTIVE
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}
```

---

## 2️⃣ PORTS - Interfaces (Contratos)

### 📁 ports/in/ - Casos de Uso (Input)

#### 📄 ICreateUserUseCase.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para crear usuarios.
 *
 * <p>Define el contrato para la creación de nuevos usuarios en el sistema.
 * La implementación debe:</p>
 * <ul>
 *   <li>Validar datos de entrada</li>
 *   <li>Verificar que no exista duplicado por documento</li>
 *   <li>Validar organización, zona y calle</li>
 *   <li>Crear usuario en Keycloak</li>
 *   <li>Publicar evento de creación</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface ICreateUserUseCase {

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param user datos del usuario a crear (sin ID)
     * @param createdBy identificador de quien crea
     * @return Mono con el usuario creado (con ID generado)
     */
    Mono<User> execute(User user, String createdBy);
}
```

---

#### 📄 IGetUserUseCase.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para consultar usuarios.
 *
 * <p>Define el contrato para todas las operaciones de lectura de usuarios.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IGetUserUseCase {

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id identificador único
     * @return Mono con el usuario encontrado
     * @throws UserNotFoundException si no existe
     */
    Mono<User> findById(String id);

    /**
     * Obtiene un usuario por número de documento.
     *
     * @param documentNumber número de documento
     * @return Mono con el usuario encontrado
     * @throws UserNotFoundException si no existe
     */
    Mono<User> findByDocumentNumber(String documentNumber);

    /**
     * Lista todos los usuarios activos.
     *
     * @return Flux con usuarios activos
     */
    Flux<User> findAllActive();

    /**
     * Lista todos los usuarios (incluye inactivos).
     * Para uso administrativo.
     *
     * @return Flux con todos los usuarios
     */
    Flux<User> findAll();

    /**
     * Lista usuarios por organización.
     *
     * @param organizationId ID de la organización
     * @return Flux con usuarios de la organización
     */
    Flux<User> findByOrganizationId(String organizationId);

    /**
     * Lista usuarios activos por organización.
     *
     * @param organizationId ID de la organización
     * @return Flux con usuarios activos de la organización
     */
    Flux<User> findActiveByOrganizationId(String organizationId);

    /**
     * Verifica si existe un usuario con el documento dado.
     *
     * @param documentNumber número de documento
     * @return Mono con true si existe
     */
    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
```

---

#### 📄 IUpdateUserUseCase.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para actualizar usuarios.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IUpdateUserUseCase {

    /**
     * Actualiza un usuario existente.
     *
     * @param id identificador del usuario
     * @param user datos a actualizar
     * @param updatedBy identificador de quien actualiza
     * @return Mono con el usuario actualizado
     * @throws UserNotFoundException si no existe
     */
    Mono<User> execute(String id, User user, String updatedBy);
}
```

---

#### 📄 IDeleteUserUseCase.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para eliminación lógica de usuarios (soft delete).
 *
 * <p>La eliminación lógica marca el usuario como INACTIVE pero no lo elimina
 * físicamente de la base de datos. Esto permite:</p>
 * <ul>
 *   <li>Mantener historial y auditoría</li>
 *   <li>Posibilidad de restauración</li>
 *   <li>Integridad referencial</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IDeleteUserUseCase {

    /**
     * Elimina lógicamente un usuario (soft delete).
     *
     * @param id identificador del usuario
     * @param deletedBy identificador de quien elimina
     * @param reason razón de la eliminación (opcional)
     * @return Mono con el usuario marcado como inactivo
     * @throws UserNotFoundException si no existe
     * @throws BusinessRuleException si el usuario ya está inactivo
     */
    Mono<User> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreUserUseCase.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para restaurar usuarios eliminados.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IRestoreUserUseCase {

    /**
     * Restaura un usuario previamente eliminado (soft delete).
     *
     * @param id identificador del usuario
     * @param restoredBy identificador de quien restaura
     * @return Mono con el usuario restaurado
     * @throws UserNotFoundException si no existe
     * @throws BusinessRuleException si el usuario ya está activo
     */
    Mono<User> execute(String id, String restoredBy);
}
```

---

#### 📄 IPurgeUserUseCase.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para eliminación física de usuarios (hard delete).
 *
 * <p><b>⚠️ ADVERTENCIA:</b> Esta operación es irreversible y debe usarse
 * con precaución. Solo debe ejecutarse en casos específicos:</p>
 * <ul>
 *   <li>Cumplimiento normativo (GDPR, derecho al olvido)</li>
 *   <li>Datos de prueba</li>
 *   <li>Errores de creación</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IPurgeUserUseCase {

    /**
     * Elimina físicamente un usuario de la base de datos.
     *
     * <p>Solo permite eliminar usuarios previamente marcados como INACTIVE.</p>
     *
     * @param id identificador del usuario
     * @param purgedBy identificador de quien elimina
     * @param reason razón de la eliminación (requerida para auditoría)
     * @return Mono vacío cuando se completa
     * @throws UserNotFoundException si no existe
     * @throws BusinessRuleException si el usuario está activo
     */
    Mono<Void> execute(String id, String purgedBy, String reason);
}
```

---

### 📁 ports/out/ - Repositorios y Clientes (Output)

#### 📄 IUserRepository.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de usuarios.
 *
 * <p>Define el contrato que debe implementar cualquier mecanismo de
 * persistencia (R2DBC, MongoDB, etc.).</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IUserRepository {

    /**
     * Guarda un nuevo usuario.
     *
     * @param user usuario a guardar
     * @return Mono con el usuario guardado (con ID generado)
     */
    Mono<User> save(User user);

    /**
     * Actualiza un usuario existente.
     *
     * @param user usuario con datos actualizados
     * @return Mono con el usuario actualizado
     */
    Mono<User> update(User user);

    /**
     * Busca un usuario por ID.
     *
     * @param id identificador único
     * @return Mono con el usuario o vacío si no existe
     */
    Mono<User> findById(String id);

    /**
     * Busca un usuario por número de documento.
     *
     * @param documentNumber número de documento
     * @return Mono con el usuario o vacío si no existe
     */
    Mono<User> findByDocumentNumber(String documentNumber);

    /**
     * Lista todos los usuarios.
     *
     * @return Flux con todos los usuarios
     */
    Flux<User> findAll();

    /**
     * Lista usuarios por estado.
     *
     * @param status estado del registro
     * @return Flux con usuarios del estado indicado
     */
    Flux<User> findByRecordStatus(RecordStatus status);

    /**
     * Lista usuarios por organización.
     *
     * @param organizationId ID de la organización
     * @return Flux con usuarios de la organización
     */
    Flux<User> findByOrganizationId(String organizationId);

    /**
     * Lista usuarios activos por organización.
     *
     * @param organizationId ID de la organización
     * @param status estado del registro
     * @return Flux con usuarios filtrados
     */
    Flux<User> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status);

    /**
     * Verifica si existe un usuario con el documento dado.
     *
     * @param documentNumber número de documento
     * @return Mono con true si existe
     */
    Mono<Boolean> existsByDocumentNumber(String documentNumber);

    /**
     * Elimina físicamente un usuario.
     *
     * @param id identificador del usuario
     * @return Mono vacío cuando se completa
     */
    Mono<Void> deleteById(String id);
}
```

---

#### 📄 IAuthenticationClient.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para comunicación con el servicio de autenticación.
 *
 * <p>Permite crear y gestionar usuarios en Keycloak a través del
 * microservicio vg-ms-authentication.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IAuthenticationClient {

    /**
     * Crea un usuario en el sistema de autenticación (Keycloak).
     *
     * @param userId ID del usuario
     * @param email email del usuario
     * @param firstName nombre
     * @param lastName apellido
     * @param role rol asignado
     * @return Mono con el ID del usuario creado en Keycloak
     */
    Mono<String> createUser(
        String userId,
        String email,
        String firstName,
        String lastName,
        String role
    );

    /**
     * Desactiva un usuario en Keycloak.
     *
     * @param userId ID del usuario
     * @return Mono vacío cuando se completa
     */
    Mono<Void> disableUser(String userId);

    /**
     * Reactiva un usuario en Keycloak.
     *
     * @param userId ID del usuario
     * @return Mono vacío cuando se completa
     */
    Mono<Void> enableUser(String userId);

    /**
     * Elimina un usuario de Keycloak permanentemente.
     *
     * @param userId ID del usuario
     * @return Mono vacío cuando se completa
     */
    Mono<Void> deleteUser(String userId);
}
```

---

#### 📄 IOrganizationClient.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para comunicación con el servicio de organizaciones.
 *
 * <p>Permite validar que la organización, zona y calle existen antes
 * de crear un usuario.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IOrganizationClient {

    /**
     * Verifica si existe una organización.
     *
     * @param organizationId ID de la organización
     * @return Mono con true si existe
     */
    Mono<Boolean> existsOrganization(String organizationId);

    /**
     * Verifica si existe una zona dentro de una organización.
     *
     * @param organizationId ID de la organización
     * @param zoneId ID de la zona
     * @return Mono con true si existe
     */
    Mono<Boolean> existsZone(String organizationId, String zoneId);

    /**
     * Verifica si existe una calle dentro de una zona.
     *
     * @param zoneId ID de la zona
     * @param streetId ID de la calle
     * @return Mono con true si existe
     */
    Mono<Boolean> existsStreet(String zoneId, String streetId);

    /**
     * Valida toda la jerarquía: organización -> zona -> calle.
     *
     * @param organizationId ID de la organización
     * @param zoneId ID de la zona
     * @param streetId ID de la calle
     * @return Mono con true si toda la jerarquía es válida
     */
    Mono<Boolean> validateHierarchy(String organizationId, String zoneId, String streetId);
}
```

---

#### 📄 INotificationClient.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para comunicación con el servicio de notificaciones.
 *
 * <p>Permite enviar notificaciones a los usuarios vía WhatsApp, SMS o Email.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface INotificationClient {

    /**
     * Envía mensaje de bienvenida al nuevo usuario.
     *
     * @param phone número de teléfono
     * @param firstName nombre del usuario
     * @param organizationName nombre de la organización
     * @return Mono vacío cuando se envía
     */
    Mono<Void> sendWelcomeMessage(String phone, String firstName, String organizationName);

    /**
     * Envía notificación de actualización de datos.
     *
     * @param phone número de teléfono
     * @param firstName nombre del usuario
     * @return Mono vacío cuando se envía
     */
    Mono<Void> sendProfileUpdatedNotification(String phone, String firstName);
}
```

---

#### 📄 IUserEventPublisher.java

```java
package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Puerto de salida para publicación de eventos de dominio.
 *
 * <p>Define el contrato para publicar eventos en RabbitMQ.
 * Los eventos son consumidos por otros microservicios para mantener
 * consistencia eventual.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
public interface IUserEventPublisher {

    /**
     * Publica evento de usuario creado.
     *
     * @param user usuario creado
     * @param createdBy identificador de quien creó
     * @return Mono vacío cuando se publica
     */
    Mono<Void> publishUserCreated(User user, String createdBy);

    /**
     * Publica evento de usuario actualizado.
     *
     * @param user usuario actualizado
     * @param changedFields campos que cambiaron
     * @param updatedBy identificador de quien actualizó
     * @return Mono vacío cuando se publica
     */
    Mono<Void> publishUserUpdated(User user, Map<String, Object> changedFields, String updatedBy);

    /**
     * Publica evento de usuario eliminado (soft delete).
     *
     * @param userId ID del usuario
     * @param organizationId ID de la organización
     * @param reason razón de eliminación
     * @param deletedBy identificador de quien eliminó
     * @return Mono vacío cuando se publica
     */
    Mono<Void> publishUserDeleted(String userId, String organizationId, String reason, String deletedBy);

    /**
     * Publica evento de usuario restaurado.
     *
     * @param userId ID del usuario
     * @param organizationId ID de la organización
     * @param restoredBy identificador de quien restauró
     * @return Mono vacío cuando se publica
     */
    Mono<Void> publishUserRestored(String userId, String organizationId, String restoredBy);

    /**
     * Publica evento de usuario purgado (hard delete).
     *
     * @param user snapshot del usuario eliminado
     * @param reason razón de eliminación
     * @param purgedBy identificador de quien eliminó
     * @return Mono vacío cuando se publica
     */
    Mono<Void> publishUserPurged(User user, String reason, String purgedBy);
}
```

---

## 3️⃣ EXCEPTIONS - Excepciones de Dominio

#### 📄 DomainException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Clase base abstracta para todas las excepciones de dominio.
 *
 * <p>Proporciona:</p>
 * <ul>
 *   <li>Código de error único</li>
 *   <li>Código HTTP asociado</li>
 *   <li>Mensaje descriptivo</li>
 * </ul>
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

#### 📄 NotFoundException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción para recursos no encontrados.
 * HTTP 404.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String resource, String id) {
        super(
            String.format("%s con ID '%s' no encontrado", resource, id),
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

#### 📄 BusinessRuleException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción para violaciones de reglas de negocio.
 * HTTP 400.
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

#### 📄 ValidationException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción para errores de validación de datos.
 * HTTP 400.
 */
public class ValidationException extends DomainException {

    private final String field;

    public ValidationException(String field, String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
```

---

#### 📄 ConflictException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción para conflictos de recursos (duplicados).
 * HTTP 409.
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message, "RESOURCE_CONFLICT", 409);
    }
}
```

---

#### 📄 ExternalServiceException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción para errores de servicios externos.
 * HTTP 503.
 */
public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String serviceName) {
        super(
            String.format("Servicio '%s' no disponible temporalmente", serviceName),
            "EXTERNAL_SERVICE_UNAVAILABLE",
            503
        );
    }

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(
            String.format("Error comunicándose con servicio '%s'", serviceName),
            "EXTERNAL_SERVICE_ERROR",
            503,
            cause
        );
    }
}
```

---

#### 📄 UserNotFoundException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción específica cuando un usuario no existe.
 */
public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String id) {
        super("Usuario", id);
    }

    public static UserNotFoundException byDocument(String documentNumber) {
        return new UserNotFoundException(
            String.format("Usuario con documento '%s' no encontrado", documentNumber)
        );
    }

    private UserNotFoundException(String message) {
        super(message);
    }
}
```

---

#### 📄 DuplicateDocumentException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción cuando ya existe un usuario con el mismo documento.
 */
public class DuplicateDocumentException extends ConflictException {

    public DuplicateDocumentException(String documentNumber) {
        super(String.format(
            "Ya existe un usuario con el número de documento '%s'",
            documentNumber
        ));
    }
}
```

---

#### 📄 InvalidContactException.java

```java
package pe.edu.vallegrande.vgmsusers.domain.exceptions;

/**
 * Excepción cuando el usuario no tiene medio de contacto válido.
 * Regla de negocio: al menos email O teléfono debe existir.
 */
public class InvalidContactException extends BusinessRuleException {

    public InvalidContactException(String message) {
        super(message, "INVALID_CONTACT");
    }
}
```

---

## ✅ Resumen de la Capa de Dominio

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| Models | 1 entidad + 3 enums | User + Role, DocumentType, RecordStatus |
| Ports In | 6 interfaces | CRUD completo + Restore + Purge |
| Ports Out | 5 interfaces | Repository + 3 Clients + EventPublisher |
| Exceptions | 9 clases | Base + genéricas + específicas |

---

> **Siguiente paso**: Lee [README_APPLICATION.md](README_APPLICATION.md) para ver la implementación de la capa de aplicación.

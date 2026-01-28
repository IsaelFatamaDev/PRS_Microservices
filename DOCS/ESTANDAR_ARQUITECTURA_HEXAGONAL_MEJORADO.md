# 🏗️ ESTÁNDAR MEJORADO DE ARQUITECTURA HEXAGONAL

> **Sistema:** JASS Digital - Microservicios
> **Versión:** 2.0 - Mejorado y Corregido
> **Fecha:** Enero 2026
> **Bases de Datos:** PostgreSQL + MongoDB
> **Seguridad:** Keycloak + JWT + JWE
> **Patrones:** Arquitectura Hexagonal + Circuit Breaker + Event-Driven

---

## 📑 ÍNDICE

1. [Principios de Arquitectura Hexagonal](#principios-de-arquitectura-hexagonal)
2. [Estructura Estándar MongoDB](#estructura-estándar-mongodb)
3. [Estructura Estándar PostgreSQL](#estructura-estándar-postgresql)
4. [Capas y Responsabilidades](#capas-y-responsabilidades)
5. [Patrones de Código](#patrones-de-código)
6. [Seguridad y Comunicación](#seguridad-y-comunicación)
7. [Ejemplos Prácticos](#ejemplos-prácticos)
8. [Checklist de Validación](#checklist-de-validación)

---

## 🎯 PRINCIPIOS DE ARQUITECTURA HEXAGONAL

### Los 5 Principios Fundamentales

#### **1. Dominio Puro (Domain-Driven)**

El dominio NO debe depender de frameworks, bases de datos o detalles de infraestructura.

```java
// ✅ CORRECTO - Dominio puro
// domain/models/User.java
public class User {  // Solo Java puro, sin anotaciones
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;

    // Constructor, getters, setters
    // Lógica de negocio (métodos de dominio)

    public boolean canAccessResource(String resourceId) {
        return this.status == UserStatus.ACTIVE &&
               this.permissions.contains(resourceId);
    }
}

// ❌ INCORRECTO - Dominio acoplado
// domain/models/User.java
@Document(collection = "users")  // ❌ Anotación de MongoDB
public class User {
    @Id  // ❌ Anotación de persistencia
    private String id;
    // ...
}
```

#### **2. Separación de Capas (Layered Architecture)**

Tres capas bien definidas:

```
┌─────────────────────────────────────────┐
│         CAPA DE APLICACIÓN              │
│  (Casos de uso, orquestación)           │
│  - Services                             │
│  - Use Cases                            │
└─────────────────────────────────────────┘
              ↓ usa ↑
┌─────────────────────────────────────────┐
│         CAPA DE DOMINIO (CORE)          │
│  (Lógica de negocio pura)               │
│  - Models                               │
│  - Value Objects                        │
│  - Domain Services                      │
│  - Enums                                │
└─────────────────────────────────────────┘
              ↑ usa ↓
┌─────────────────────────────────────────┐
│      CAPA DE INFRAESTRUCTURA            │
│  (Detalles técnicos, adaptadores)       │
│  - REST Controllers                     │
│  - Repositories                         │
│  - Documents/Entities                   │
│  - Mappers                              │
│  - External Clients                     │
└─────────────────────────────────────────┘
```

#### **3. Dependency Inversion (Inversión de Dependencias)**

Las capas externas dependen de las internas, nunca al revés.

```java
// CAPA DE APLICACIÓN (service)
public interface UserService {  // Puerto (interface en aplicación)
    Mono<User> createUser(User user);
}

// CAPA DE INFRAESTRUCTURA (repository)
public interface UserRepository {  // Adaptador (implementación en infra)
    Mono<UserDocument> save(UserDocument document);
}

// IMPLEMENTACIÓN DEL SERVICIO
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;  // Inyección de dependencia
    private final UserMapper mapper;

    @Override
    public Mono<User> createUser(User user) {
        return Mono.just(user)
            .map(mapper::toDocument)
            .flatMap(repository::save)
            .map(mapper::toDomain);
    }
}
```

#### **4. Mapeo Entre Capas (Data Transfer)**

SIEMPRE usar mappers para convertir entre representaciones:

```
Domain Model  ←→  Mapper  ←→  Document/Entity  ←→  Database
(Lógica)                     (Persistencia)

Domain Model  ←→  Mapper  ←→  DTO  ←→  REST API
(Lógica)                     (Transferencia)
```

#### **5. Testabilidad (Testable Architecture)**

Cada capa debe ser testeable independientemente:

```java
// Test de DOMINIO (sin BD, sin HTTP)
@Test
void shouldCalculateUserAge() {
    User user = User.builder()
        .birthDate(LocalDate.of(1990, 1, 1))
        .build();

    int age = user.calculateAge();

    assertEquals(34, age);
}

// Test de SERVICIO (con mocks)
@Test
void shouldCreateUser() {
    // Given
    User user = createTestUser();
    UserDocument document = createTestDocument();
    when(mapper.toDocument(user)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.just(document));

    // When
    StepVerifier.create(userService.createUser(user))
        .expectNextMatches(u -> u.getId() != null)
        .verifyComplete();
}
```

---

## 📁 ESTRUCTURA ESTÁNDAR MONGODB

### Estructura Completa

```
vg-ms-{service}/
├── 📄 pom.xml
├── 📄 Dockerfile
├── 📄 docker-compose.yml
├── 📄 README.md
├── 📄 .gitignore
├── 📄 .env.example
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/pe/edu/vallegrande/{package}/
    │   │   ├── 📄 {Service}Application.java
    │   │   │
    │   │   ├── 📁 application/                    # ⚙️ CAPA DE APLICACIÓN
    │   │   │   └── 📁 service/
    │   │   │       ├── 📄 {Entity}Service.java     # Interface (Puerto)
    │   │   │       └── 📁 impl/
    │   │   │           └── 📄 {Entity}ServiceImpl.java  # Implementación
    │   │   │
    │   │   ├── 📁 domain/                         # 🎯 CAPA DE DOMINIO (CORE)
    │   │   │   ├── 📁 models/                      # Modelos de dominio PUROS
    │   │   │   │   ├── 📄 {Entity}.java            # ✅ SIN anotaciones de BD
    │   │   │   │   ├── 📄 {ValueObject}.java       # Value Objects inmutables
    │   │   │   │   └── 📄 {AggregateRoot}.java     # Entidades principales
    │   │   │   │
    │   │   │   └── 📁 enums/                       # Enumeraciones del dominio
    │   │   │       ├── 📄 {Status}.java
    │   │   │       ├── 📄 {Type}.java
    │   │   │       └── 📄 Constants.java
    │   │   │
    │   │   └── 📁 infrastructure/                 # 🔧 CAPA DE INFRAESTRUCTURA
    │   │       │
    │   │       ├── 📁 document/                    # ✅ Documentos MongoDB (Persistencia)
    │   │       │   ├── 📄 {Entity}Document.java    # @Document aquí
    │   │       │   ├── 📁 embedded/                # Documentos embebidos
    │   │       │   │   └── 📄 {Embedded}Document.java
    │   │       │   └── 📄 BaseDocument.java        # Clase base con auditoría
    │   │       │
    │   │       ├── 📁 repository/                  # Repositorios MongoDB
    │   │       │   └── 📄 {Entity}Repository.java  # ReactiveMongoRepository
    │   │       │
    │   │       ├── 📁 mapper/                      # Mappers entre capas
    │   │       │   ├── 📄 {Entity}Mapper.java      # Document ↔ Domain
    │   │       │   ├── 📄 {Entity}DtoMapper.java   # Domain ↔ DTO
    │   │       │   └── 📄 BaseMapper.java
    │   │       │
    │   │       ├── 📁 dto/                         # Data Transfer Objects
    │   │       │   ├── 📁 request/
    │   │       │   │   ├── 📄 Create{Entity}Request.java
    │   │       │   │   ├── 📄 Update{Entity}Request.java
    │   │       │   │   └── 📄 Filter{Entity}Request.java
    │   │       │   ├── 📁 response/
    │   │       │   │   ├── 📄 {Entity}Response.java
    │   │       │   │   ├── 📄 {Entity}DetailResponse.java
    │   │       │   │   └── 📄 {Entity}ListResponse.java
    │   │       │   └── 📁 common/
    │   │       │       ├── 📄 ResponseDto.java
    │   │       │       ├── 📄 ErrorMessage.java
    │   │       │       └── 📄 ValidationError.java
    │   │       │
    │   │       ├── 📁 rest/                        # Controladores REST
    │   │       │   ├── 📁 admin/
    │   │       │   │   └── 📄 Admin{Entity}Rest.java
    │   │       │   ├── 📁 client/
    │   │       │   │   └── 📄 {Entity}Rest.java
    │   │       │   ├── 📁 internal/                # Endpoints MS-to-MS
    │   │       │   │   └── 📄 Internal{Entity}Rest.java
    │   │       │   └── 📁 common/
    │   │       │       └── 📄 Common{Entity}Rest.java
    │   │       │
    │   │       ├── 📁 client/                      # Clientes HTTP (Opcional)
    │   │       │   ├── 📁 external/                # APIs externas
    │   │       │   │   ├── 📄 {External}Client.java
    │   │       │   │   └── 📁 dto/
    │   │       │   │       ├── 📄 {External}Request.java
    │   │       │   │       └── 📄 {External}Response.java
    │   │       │   ├── 📁 internal/                # Otros microservicios
    │   │       │   │   └── 📄 {Service}InternalClient.java
    │   │       │   └── 📁 validator/
    │   │       │       ├── 📄 ExternalClientValidator.java
    │   │       │       └── 📄 InternalClientValidator.java
    │   │       │
    │   │       ├── 📁 config/                      # Configuraciones
    │   │       │   ├── 📄 MongoConfig.java
    │   │       │   ├── 📄 WebClientConfig.java
    │   │       │   ├── 📄 SecurityConfig.java
    │   │       │   ├── 📄 JacksonConfig.java
    │   │       │   └── 📄 Resilience4jConfig.java
    │   │       │
    │   │       ├── 📁 security/                    # Seguridad
    │   │       │   ├── 📄 JwtRoleConverter.java
    │   │       │   ├── 📄 JweService.java
    │   │       │   ├── 📄 JweEncryptionService.java
    │   │       │   ├── 📄 JweDecryptionService.java
    │   │       │   ├── 📄 JwtPropagationFilter.java
    │   │       │   └── 📄 CustomAuthenticationEntryPoint.java
    │   │       │
    │   │       ├── 📁 exception/                   # Manejo de excepciones
    │   │       │   ├── 📄 GlobalExceptionHandler.java
    │   │       │   ├── 📄 RestExceptionHandler.java
    │   │       │   └── 📁 custom/
    │   │       │       ├── 📄 ResourceNotFoundException.java
    │   │       │       ├── 📄 InvalidTokenException.java
    │   │       │       ├── 📄 ExternalServiceException.java
    │   │       │       └── 📄 {Custom}Exception.java
    │   │       │
    │   │       └── 📁 validation/                  # Validaciones (Opcional)
    │   │           ├── 📄 RequestValidator.java
    │   │           └── 📄 {Entity}ValidationService.java
    │   │
    │   └── 📁 resources/
    │       ├── 📄 application.yml
    │       ├── 📄 application-dev.yml
    │       ├── 📄 application-prod.yml
    │       ├── 📄 logback-spring.xml              # Configuración de logs
    │       └── 📁 doc/
    │           ├── 📄 API_DOCUMENTATION.md
    │           ├── 📄 ARCHITECTURE.md
    │           └── 📄 {service}-collection.json   # Postman collection
    │
    └── 📁 test/
        └── 📁 java/pe/edu/vallegrande/{package}/
            ├── 📁 domain/
            │   └── 📁 models/
            │       └── 📄 {Entity}Test.java         # Tests unitarios de dominio
            ├── 📁 application/
            │   └── 📁 service/
            │       └── 📄 {Entity}ServiceTest.java  # Tests de servicio
            └── 📁 infrastructure/
                └── 📁 rest/
                    └── 📄 {Entity}RestIntegrationTest.java  # Tests de integración
```

### Dependencias Maven (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
    </parent>

    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-{service}</artifactId>
    <version>1.0.0</version>
    <name>vg-ms-{service}</name>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2024.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Boot WebFlux (Reactivo) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- MongoDB Reactive -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>

        <!-- Security OAuth2 Resource Server (JWT) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Resilience4j (Circuit Breaker, Retry, etc.) -->
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

        <!-- JWE Support (Nimbus JOSE+JWT) -->
        <dependency>
            <groupId>com.nimbusds</groupId>
            <artifactId>nimbus-jose-jwt</artifactId>
            <version>9.37</version>
        </dependency>

        <!-- Actuator (Monitoring) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Micrometer (Métricas) -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Distributed Tracing -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>

        <!-- Lombok (Opcional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- DevTools (Development) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>de.flapdoodle.embed</groupId>
            <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
            <version>4.11.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 📁 ESTRUCTURA ESTÁNDAR POSTGRESQL

### Diferencias con MongoDB

```
vg-ms-{service}/
└── 📁 src/main/java/pe/edu/vallegrande/{package}/
    ├── 📁 domain/
    │   └── 📁 models/                              # ✅ MISMO - Modelos puros
    │       └── 📄 {Entity}.java
    │
    └── 📁 infrastructure/
        ├── 📁 entity/                              # ⚠️ DIFERENTE - Entidades JPA/R2DBC
        │   ├── 📄 {Entity}Entity.java              # @Entity o @Table (R2DBC)
        │   └── 📄 BaseEntity.java                  # Clase base con auditoría
        │
        ├── 📁 repository/                          # ⚠️ DIFERENTE - Repositorios
        │   └── 📄 {Entity}Repository.java          # ReactiveCrudRepository (R2DBC)
        │                                           # o JpaRepository (JPA)
        │
        └── 📁 persistence/                         # ⚠️ OPCIONAL - Schema
            ├── 📄 schema.sql                       # DDL scripts
            └── 📁 mapper/
                └── 📄 {Entity}PersistenceMapper.java
```

### Dependencias Maven (PostgreSQL con R2DBC)

```xml
<dependencies>
    <!-- Spring Boot WebFlux (Reactivo) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- R2DBC PostgreSQL (Reactivo) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
    </dependency>

    <!-- PostgreSQL JDBC Driver (para migraciones con Flyway) -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Flyway (Migraciones de BD) -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Las demás dependencias son iguales a MongoDB -->
</dependencies>
```

---

## 📦 CAPAS Y RESPONSABILIDADES

### 1. Capa de Dominio (domain/)

**Responsabilidad:** Lógica de negocio pura, reglas del dominio

#### domain/models/

**QUÉ DEBE CONTENER:**

- ✅ Modelos de negocio (Entities, Value Objects, Aggregates)
- ✅ Métodos con lógica de dominio
- ✅ Validaciones de negocio
- ✅ Cálculos y transformaciones de dominio

**QUÉ NO DEBE CONTENER:**

- ❌ Anotaciones de persistencia (@Document, @Entity, @Table, @Id, @Column)
- ❌ Anotaciones de frameworks (@Service, @Component, @Autowired)
- ❌ Dependencias de Spring, MongoDB, PostgreSQL
- ❌ Llamadas a bases de datos o servicios externos

**Ejemplo:**

```java
// ✅ CORRECTO - Modelo de dominio puro
package pe.edu.vallegrande.vgmsusers.domain.models;

public class User {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime birthDate;
    private UserStatus status;
    private Set<String> roles;
    private OrganizationId organizationId;  // Value Object

    // Constructor
    private User(String id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = validateEmail(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = UserStatus.PENDING;
        this.roles = new HashSet<>();
    }

    // Factory method
    public static User create(String email, String firstName, String lastName) {
        return new User(null, email, firstName, lastName);
    }

    // Lógica de dominio
    public int calculateAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public boolean isAdult() {
        return calculateAge() >= 18;
    }

    public boolean canAccessResource(String resourceId) {
        return this.status == UserStatus.ACTIVE &&
               this.roles.stream().anyMatch(role -> role.hasPermission(resourceId));
    }

    public void activate() {
        if (this.status != UserStatus.PENDING) {
            throw new DomainException("User can only be activated from PENDING status");
        }
        this.status = UserStatus.ACTIVE;
    }

    public void assignRole(String roleName) {
        if (this.status != UserStatus.ACTIVE) {
            throw new DomainException("Cannot assign role to inactive user");
        }
        this.roles.add(roleName);
    }

    // Validaciones de dominio
    private String validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidEmailException("Invalid email format");
        }
        return email;
    }

    // Getters (no setters públicos - inmutabilidad cuando sea posible)
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return firstName + " " + lastName; }
    public UserStatus getStatus() { return status; }
}
```

#### domain/enums/

```java
// Enumeraciones con lógica de negocio
public enum UserStatus {
    PENDING("Pendiente", "Usuario registrado pero no activado"),
    ACTIVE("Activo", "Usuario con acceso completo"),
    SUSPENDED("Suspendido", "Usuario temporalmente deshabilitado"),
    INACTIVE("Inactivo", "Usuario dado de baja");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean canBeActivated() {
        return this == PENDING || this == SUSPENDED;
    }

    // Getters
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
```

---

### 2. Capa de Aplicación (application/)

**Responsabilidad:** Orquestar casos de uso, coordinar entre dominio e infraestructura

#### application/service/

**QUÉ DEBE CONTENER:**

- ✅ Interfaces de servicios (puertos)
- ✅ Implementaciones de casos de uso
- ✅ Orquestación de flujos de negocio
- ✅ Transacciones
- ✅ Coordinación entre múltiples entidades

**QUÉ NO DEBE CONTENER:**

- ❌ Lógica de negocio (va en dominio)
- ❌ Detalles de BD (va en infraestructura)
- ❌ Detalles de HTTP/REST (va en infraestructura)

**Ejemplo:**

```java
// Interface (Puerto)
package pe.edu.vallegrande.vgmsusers.application.service;

public interface UserService {
    Mono<User> createUser(User user);
    Mono<User> findById(String id);
    Mono<User> updateUser(String id, User user);
    Mono<Void> deleteUser(String id);
    Flux<User> findByOrganization(String organizationId);
    Mono<User> activateUser(String id);
}

// Implementación
package pe.edu.vallegrande.vgmsusers.application.service.impl;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final OrganizationClient organizationClient;
    private final NotificationClient notificationClient;

    // Constructor injection
    public UserServiceImpl(
            UserRepository repository,
            UserMapper mapper,
            OrganizationClient organizationClient,
            NotificationClient notificationClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.organizationClient = organizationClient;
        this.notificationClient = notificationClient;
    }

    @Override
    public Mono<User> createUser(User user) {
        log.info("Creating user with email: {}", user.getEmail());

        return validateOrganization(user.getOrganizationId())
            .then(Mono.just(user))
            .flatMap(this::checkEmailUniqueness)
            .map(mapper::toDocument)
            .flatMap(repository::save)
            .map(mapper::toDomain)
            .flatMap(this::sendWelcomeNotification)
            .doOnSuccess(created -> log.info("User created successfully: {}", created.getId()))
            .doOnError(error -> log.error("Error creating user", error));
    }

    @Override
    public Mono<User> activateUser(String id) {
        return repository.findById(id)
            .map(mapper::toDomain)
            .doOnNext(User::activate)  // Lógica de dominio
            .map(mapper::toDocument)
            .flatMap(repository::save)
            .map(mapper::toDomain)
            .flatMap(this::sendActivationNotification);
    }

    // Métodos privados de orquestación
    private Mono<Void> validateOrganization(String organizationId) {
        return organizationClient.exists(organizationId)
            .filter(exists -> exists)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(organizationId)))
            .then();
    }

    private Mono<User> checkEmailUniqueness(User user) {
        return repository.existsByEmail(user.getEmail())
            .flatMap(exists -> exists
                ? Mono.error(new EmailAlreadyExistsException(user.getEmail()))
                : Mono.just(user));
    }

    private Mono<User> sendWelcomeNotification(User user) {
        return notificationClient.sendWelcomeEmail(user.getEmail())
            .onErrorResume(error -> {
                log.error("Failed to send welcome email to {}", user.getEmail(), error);
                return Mono.empty();  // No bloqueamos la creación si falla la notificación
            })
            .thenReturn(user);
    }

    private Mono<User> sendActivationNotification(User user) {
        return notificationClient.sendActivationEmail(user.getEmail())
            .onErrorResume(error -> {
                log.error("Failed to send activation email", error);
                return Mono.empty();
            })
            .thenReturn(user);
    }
}
```

---

### 3. Capa de Infraestructura (infrastructure/)

**Responsabilidad:** Detalles técnicos, adaptadores para BD, REST, clientes HTTP, etc.

#### infrastructure/document/ (MongoDB)

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.document;

@Document(collection = "users")  // ✅ AQUÍ SÍ van las anotaciones
@CompoundIndex(def = "{'organizationId': 1, 'email': 1}", unique = true)
public class UserDocument extends BaseDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("email")
    private String email;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("birth_date")
    private LocalDateTime birthDate;

    @Field("status")
    private String status;

    @Field("roles")
    private Set<String> roles;

    @Field("organization_id")
    @Indexed
    private String organizationId;

    // Constructors, getters, setters
}

// Clase base con auditoría
@Data
public abstract class BaseDocument {

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;

    @Field("active")
    private Boolean active = true;
}
```

#### infrastructure/entity/ (PostgreSQL)

```java
package pe.edu.vallegrande.vgmspayments.infrastructure.entity;

@Table("payments")  // R2DBC
public class PaymentEntity extends BaseEntity {

    @Id
    private Integer id;

    @Column("payment_code")
    private String paymentCode;

    @Column("user_id")
    private String userId;

    @Column("organization_id")
    private String organizationId;

    @Column("amount")
    private BigDecimal amount;

    @Column("payment_date")
    private LocalDateTime paymentDate;

    @Column("status")
    private String status;

    // Constructors, getters, setters
}

// Para JPA sería:
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_organization_id", columnList = "organization_id")
})
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // ...
}
```

#### infrastructure/mapper/

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.mapper;

@Component
public class UserMapper {

    // Document → Domain
    public User toDomain(UserDocument document) {
        if (document == null) {
            return null;
        }

        return User.builder()
            .id(document.getId())
            .email(document.getEmail())
            .firstName(document.getFirstName())
            .lastName(document.getLastName())
            .birthDate(document.getBirthDate())
            .status(UserStatus.valueOf(document.getStatus()))
            .roles(document.getRoles())
            .organizationId(document.getOrganizationId())
            .build();
    }

    // Domain → Document
    public UserDocument toDocument(User domain) {
        if (domain == null) {
            return null;
        }

        UserDocument document = new UserDocument();
        document.setId(domain.getId());
        document.setEmail(domain.getEmail());
        document.setFirstName(domain.getFirstName());
        document.setLastName(domain.getLastName());
        document.setBirthDate(domain.getBirthDate());
        document.setStatus(domain.getStatus().name());
        document.setRoles(domain.getRoles());
        document.setOrganizationId(domain.getOrganizationId());
        return document;
    }

    // Método helper para actualizar documento existente
    public void updateDocument(User domain, UserDocument document) {
        document.setEmail(domain.getEmail());
        document.setFirstName(domain.getFirstName());
        document.setLastName(domain.getLastName());
        document.setStatus(domain.getStatus().name());
        // No actualizar id, createdAt, etc.
    }
}

@Component
public class UserDtoMapper {

    // Domain → Response DTO
    public UserResponse toResponse(User domain) {
        return UserResponse.builder()
            .id(domain.getId())
            .email(domain.getEmail())
            .fullName(domain.getFullName())
            .status(domain.getStatus().getDisplayName())
            .age(domain.calculateAge())
            .build();
    }

    // Request DTO → Domain
    public User toDomain(CreateUserRequest request) {
        return User.create(
            request.getEmail(),
            request.getFirstName(),
            request.getLastName()
        );
    }
}
```

#### infrastructure/rest/

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.rest.admin;

@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
@Tag(name = "User Admin API", description = "CRUD operations for users")
public class AdminUserRest {

    private final UserService userService;
    private final UserDtoMapper dtoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new user")
    public Mono<ResponseEntity<ResponseDto<UserResponse>>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return Mono.just(request)
            .map(dtoMapper::toDomain)
            .flatMap(userService::createUser)
            .map(dtoMapper::toResponse)
            .map(response -> ResponseDto.success("User created successfully", response))
            .map(ResponseEntity::ok)
            .onErrorResume(this::handleError);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public Mono<ResponseEntity<ResponseDto<UserResponse>>> getUserById(
            @PathVariable String id) {

        return userService.findById(id)
            .map(dtoMapper::toResponse)
            .map(response -> ResponseDto.success("User found", response))
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public Mono<ResponseEntity<ResponseDto<UserResponse>>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {

        return Mono.just(request)
            .map(dtoMapper::toDomain)
            .flatMap(user -> userService.updateUser(id, user))
            .map(dtoMapper::toResponse)
            .map(response -> ResponseDto.success("User updated successfully", response))
            .map(ResponseEntity::ok)
            .onErrorResume(this::handleError);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate user")
    public Mono<ResponseEntity<ResponseDto<UserResponse>>> activateUser(
            @PathVariable String id) {

        return userService.activateUser(id)
            .map(dtoMapper::toResponse)
            .map(response -> ResponseDto.success("User activated", response))
            .map(ResponseEntity::ok)
            .onErrorResume(this::handleError);
    }

    // Manejo de errores centralizado
    private Mono<ResponseEntity<ResponseDto<UserResponse>>> handleError(Throwable error) {
        if (error instanceof EmailAlreadyExistsException) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseDto.error("Email already exists", error.getMessage())));
        }

        if (error instanceof OrganizationNotFoundException) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.error("Invalid organization", error.getMessage())));
        }

        log.error("Unexpected error", error);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseDto.error("Internal server error", "An unexpected error occurred")));
    }
}
```

---

## 🔐 SEGURIDAD Y COMUNICACIÓN

### SecurityConfig Estándar

```java
package pe.edu.vallegrande.{package}.infrastructure.config;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())  // Manejado por Gateway
            .authorizeExchange(exchanges -> exchanges
                // Endpoints públicos
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/actuator/info").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/swagger-ui.html").permitAll()

                // Endpoints internos MS-to-MS
                .pathMatchers("/internal/**").hasRole("INTERNAL_SERVICE")

                // Endpoints por roles
                .pathMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .pathMatchers("/api/v1/management/**").hasRole("SUPER_ADMIN")
                .pathMatchers("/api/v1/client/**").hasRole("CLIENT")
                .pathMatchers("/api/v1/common/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "CLIENT")

                // Resto requiere autenticación
                .anyExchange().authenticated())

            // OAuth2 Resource Server (JWT)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))

            .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter =
            new ReactiveJwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            jwt -> {
                // Extraer roles de Keycloak
                Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                return Flux.fromIterable(authorities);
            }
        );

        return jwtAuthenticationConverter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Roles desde "roles" claim
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .forEach(authorities::add);
        }

        // Roles desde "realm_access" (Keycloak)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            realmRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .forEach(authorities::add);
        }

        return authorities;
    }
}
```

### WebClient con Resilience4j

```java
package pe.edu.vallegrande.{package}.infrastructure.config;

@Configuration
public class WebClientConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final RetryRegistry retryRegistry;

    @Bean
    public WebClient resilientWebClient() {
        return WebClient.builder()
            .filter(jwtPropagationFilter())
            .filter(resilienceFilter())
            .filter(loggingFilter())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    // Propagar JWT automáticamente
    private ExchangeFilterFunction jwtPropagationFilter() {
        return (request, next) ->
            ReactiveSecurityContextHolder.getContext()
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

    // Aplicar Resilience4j (Circuit Breaker, Retry, Time Limiter)
    private ExchangeFilterFunction resilienceFilter() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("default");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("default");
        Retry retry = retryRegistry.retry("default");

        return (request, next) ->
            Mono.fromCallable(() -> next.exchange(request))
                .flatMap(mono -> mono)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .transform(TimeLimiterOperator.of(timeLimiter))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .onErrorResume(CallNotPermittedException.class, ex ->
                    Mono.error(new CircuitBreakerOpenException("Circuit breaker is OPEN", ex))
                );
    }

    // Logging de requests/responses
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}
```

### application.yml Completo

```yaml
# ============================================================================
# CONFIGURACIÓN ESTÁNDAR DE MICROSERVICIO
# ============================================================================

spring:
  application:
    name: vg-ms-{service}

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  # ============== MONGODB ==============
  data:
    mongodb:
      uri: ${MONGODB_URI}
      # Configuración de pool
      auto-index-creation: true

  # ============== SEGURIDAD OAuth2 ==============
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
          jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}

# ============== JWE (Comunicación MS-to-MS) ==============
jwe:
  internal:
    secret: ${JWE_INTERNAL_SECRET}
    expiration: ${JWE_INTERNAL_EXPIRATION:86400}
    issuer: ${JWE_INTERNAL_ISSUER:ms-{service}}
    audience: ${JWE_INTERNAL_AUDIENCE:jass-microservices}

# ============== RESILIENCE4J ==============
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
        retryExceptions:
          - org.springframework.web.reactive.function.client.WebClientRequestException
          - java.io.IOException

  timelimiter:
    instances:
      default:
        timeoutDuration: 10s

# ============== ACTUATOR / MONITORING ==============
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
  tracing:
    sampling:
      probability: 1.0

# ============== LOGGING ==============
logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
    org.springframework.security: INFO
    org.springframework.web: DEBUG
    org.springframework.data.mongodb: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"

# ============== SERVER ==============
server:
  port: ${SERVER_PORT:8080}
  error:
    include-message: always
    include-binding-errors: always
```

### .env.example

```env
# ============== SPRING ==============
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# ============== MONGODB ==============
MONGODB_URI=mongodb://localhost:27017/ms_{service}_dev

# ============== KEYCLOAK ==============
KEYCLOAK_ISSUER_URI=https://keycloak.domain.com/realms/sistema-jass
KEYCLOAK_JWK_SET_URI=https://keycloak.domain.com/realms/sistema-jass/protocol/openid-connect/certs

# ============== JWE (INTERNO) ==============
JWE_INTERNAL_SECRET=CHANGE-THIS-IN-PRODUCTION-256-BIT-KEY
JWE_INTERNAL_EXPIRATION=86400
JWE_INTERNAL_ISSUER=ms-{service}
JWE_INTERNAL_AUDIENCE=jass-microservices

# ============== SERVICIOS EXTERNOS ==============
USERS_SERVICE_URL=http://localhost:8085
ORGANIZATIONS_SERVICE_URL=http://localhost:8086
NOTIFICATIONS_SERVICE_URL=http://localhost:8087
```

---

## ✅ CHECKLIST DE VALIDACIÓN

Use este checklist para validar que su microservicio cumple con el estándar:

### Arquitectura Hexagonal

- [ ] **Dominio Puro**
  - [ ] Modelos en `domain/models/` sin anotaciones de BD
  - [ ] Sin dependencias de Spring en modelos de dominio
  - [ ] Lógica de negocio en modelos de dominio
  - [ ] Value Objects inmutables cuando sea apropiado

- [ ] **Separación de Capas**
  - [ ] `infrastructure/document/` o `infrastructure/entity/` con anotaciones de BD
  - [ ] `infrastructure/mapper/` para convertir entre capas
  - [ ] `application/service/` con interfaces y casos de uso
  - [ ] `infrastructure/rest/` para controladores REST

- [ ] **Mappers**
  - [ ] Mapper de Domain ↔ Document/Entity
  - [ ] Mapper de Domain ↔ DTO
  - [ ] Métodos helper para actualizar documentos existentes

### Seguridad

- [ ] **SecurityConfig implementado**
  - [ ] OAuth2 Resource Server configurado
  - [ ] Endpoints públicos bien definidos
  - [ ] Roles y permisos configurados
  - [ ] Extracción de roles desde JWT/Keycloak

- [ ] **Sin credenciales hardcodeadas**
  - [ ] Todas las credenciales en variables de entorno
  - [ ] `.env.example` documentado
  - [ ] Passwords no commitados en Git

- [ ] **Comunicación MS-to-MS segura**
  - [ ] Propagación de JWT implementada
  - [ ] JWE configurado (opcional pero recomendado)
  - [ ] Endpoints `/internal/**` protegidos

### Resiliencia

- [ ] **Resilience4j configurado**
  - [ ] Circuit Breaker en WebClient
  - [ ] Retry con backoff exponencial
  - [ ] Time Limiter configurado

- [ ] **Timeouts configurados**
  - [ ] Connect timeout: 2-5s
  - [ ] Read timeout: 5-10s
  - [ ] Response timeout: según caso de uso

### Observabilidad

- [ ] **Actuator configurado**
  - [ ] `/actuator/health` público
  - [ ] `/actuator/prometheus` expuesto
  - [ ] Health indicators configurados

- [ ] **Logging estructurado**
  - [ ] TraceId y SpanId en logs
  - [ ] Logs con contexto relevante
  - [ ] Niveles de log apropiados

### Testing

- [ ] **Tests unitarios**
  - [ ] Tests de modelos de dominio
  - [ ] Tests de servicios (con mocks)
  - [ ] Cobertura > 70%

- [ ] **Tests de integración**
  - [ ] Tests de endpoints REST
  - [ ] Tests con MongoDB embebido o Testcontainers
  - [ ] Tests de repositorios

### Documentación

- [ ] **README.md completo**
  - [ ] Descripción del microservicio
  - [ ] Cómo ejecutar localmente
  - [ ] Variables de entorno necesarias
  - [ ] Endpoints principales

- [ ] **OpenAPI/Swagger**
  - [ ] Anotaciones @Operation en endpoints
  - [ ] Modelos documentados
  - [ ] Ejemplos de request/response

---

## 📚 CONCLUSIÓN

Este estándar mejorado proporciona una base sólida para construir microservicios con arquitectura hexagonal correcta. Los puntos clave son:

1. **Dominio SIEMPRE puro** - Sin anotaciones de BD ni frameworks
2. **Separación clara de capas** - Domain, Application, Infrastructure
3. **Mappers entre capas** - Nunca exponer Document/Entity directamente
4. **Seguridad robusta** - OAuth2, JWT, JWE para MS-to-MS
5. **Resiliencia implementada** - Circuit Breaker, Retry, Timeouts
6. **Observabilidad completa** - Logs, métricas, tracing

**Beneficios de seguir este estándar:**

- ✅ Fácil cambio de tecnología de BD
- ✅ Lógica de negocio testeable sin infraestructura
- ✅ Código mantenible y escalable
- ✅ Seguridad robusta
- ✅ Sistema resiliente ante fallos

---

**Documento:** Estándar de Arquitectura Hexagonal v2.0
**Fecha:** Enero 2026
**Aplicable a:** Todos los microservicios JASS Digital
**Estado:** APROBADO PARA IMPLEMENTACIÓN

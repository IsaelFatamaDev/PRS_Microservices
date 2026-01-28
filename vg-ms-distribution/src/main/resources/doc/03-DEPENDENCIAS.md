# Dependencias del Proyecto

## Estructura del pom.xml

El archivo `pom.xml` define todas las dependencias y configuración de Maven para el proyecto.

---

## Información del Proyecto

```xml
<groupId>pe.edu.vallegrande</groupId>
<artifactId>vg-ms-distribution</artifactId>
<version>2.0.0</version>
<name>vg-ms-distribution</name>
<description>Microservicio para gestión de distribución</description>
```

### Propiedades
```xml
<java.version>17</java.version>
<maven.version>3.9.6</maven.version>
<spring-cloud.version>2023.0.1</spring-cloud.version>
<keycloak.version>25.0.6</keycloak.version>
```

---

## Dependencias Principales

### 1. Spring Boot Starter Parent

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.5</version>
</parent>
```

**Propósito:**
- Proporciona gestión de dependencias
- Define versiones compatibles de librerías
- Configura plugins de Maven
- Establece configuración por defecto

---

### 2. Spring Boot Starter WebFlux

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

**Propósito:**
- Framework web reactivo
- Servidor Netty embebido
- Programación no bloqueante
- Soporte para Mono/Flux

**Incluye:**
- Spring WebFlux
- Netty (servidor reactivo)
- Project Reactor
- Jackson (JSON)

---

### 3. Spring WebFlux (Adicional)

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webflux</artifactId>
</dependency>
```

**Propósito:**
- WebClient para consumir servicios externos
- Cliente HTTP reactivo

---

### 4. Spring Boot Starter Data MongoDB Reactive

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

**Propósito:**
- Integración con MongoDB de forma reactiva
- Repositorios reactivos
- Driver MongoDB Reactive Streams

**Incluye:**
- MongoDB Reactive Streams Driver
- Spring Data MongoDB Reactive
- Soporte para ReactiveMongoRepository

---

### 5. Spring Boot Starter Validation

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Propósito:**
- Validación de beans con anotaciones
- Hibernate Validator
- Jakarta Validation API

**Anotaciones disponibles:**
- `@NotNull`, `@NotBlank`, `@NotEmpty`
- `@Size`, `@Min`, `@Max`
- `@Pattern`, `@Email`
- `@Valid` (validación en cascada)

---

### 6. Spring Boot Starter Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Propósito:**
- Endpoints de monitoreo y gestión
- Health checks
- Métricas de la aplicación

**Endpoints expuestos:**
- `/actuator/health` - Estado de salud
- `/actuator/prometheus` - Métricas para Prometheus

---

### 7. Lombok

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

**Propósito:**
- Reducir código boilerplate
- Generar getters/setters automáticamente
- Constructores, builders, etc.

**Scope:** `provided` (solo en compilación, no en runtime)

**Anotaciones:**
- `@Data`, `@Getter`, `@Setter`
- `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Slf4j` (logger)

---

### 8. Micrometer Registry Prometheus

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Propósito:**
- Exportar métricas en formato Prometheus
- Monitoreo de aplicación
- Integración con sistemas de observabilidad

---

### 9. Spring Boot Starter Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Propósito:**
- Autenticación y autorización
- Protección de endpoints
- Integración con OAuth2

**Incluye:**
- Spring Security Core
- Spring Security Web
- Spring Security Config

---

### 10. Spring Boot Starter OAuth2 Resource Server

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Propósito:**
- Validar tokens JWT
- Actuar como Resource Server OAuth2
- Integración con Keycloak

**Funcionalidad:**
- Validación de JWT
- Extracción de claims
- Verificación de firma con JWK Set

---

### 11. JJWT (JSON Web Token)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**Propósito:**
- Crear y validar tokens JWT
- Firmar y verificar tokens
- Parsear claims

**Uso en el proyecto:**
- Validación de tokens internos (JWE)
- Extracción de información de usuario

---

### 12. Keycloak Admin Client

```xml
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>25.0.6</version>
</dependency>
```

**Propósito:**
- Cliente para administrar Keycloak
- Gestión de usuarios y roles
- Integración con servidor Keycloak

---

### 13. Spring Cloud Starter Gateway

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

**Propósito:**
- API Gateway
- Routing de requests
- Filtros y transformaciones

**Nota:** Usado en conjunto con el gateway del sistema JASS

---

## Dependencias de Testing

### 14. Spring Boot Starter Test

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Propósito:**
- Testing de aplicaciones Spring Boot
- JUnit 5, Mockito, AssertJ

**Incluye:**
- JUnit Jupiter
- Mockito
- AssertJ
- Spring Test

---

### 15. Mockito

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
```

**Propósito:**
- Crear mocks de dependencias
- Testing unitario
- Verificación de comportamiento

---

### 16. Reactor Test

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Propósito:**
- Testing de código reactivo
- StepVerifier para Mono/Flux
- Verificación de flujos reactivos

---

### 17. JUnit Jupiter

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

**Propósito:**
- Framework de testing moderno
- Anotaciones `@Test`, `@BeforeEach`, etc.
- Assertions y assumptions

---

## Dependency Management

### Spring Cloud Dependencies

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Propósito:**
- Gestionar versiones de Spring Cloud
- Asegurar compatibilidad entre componentes
- Evitar conflictos de versiones

---

## Plugins de Maven

### 1. Maven Enforcer Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <configuration>
        <rules>
            <requireMavenVersion>
                <version>[3.9.6,)</version>
            </requireMavenVersion>
        </rules>
    </configuration>
</plugin>
```

**Propósito:**
- Forzar versión mínima de Maven
- Validar requisitos del proyecto

---

### 2. Spring Boot Maven Plugin

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
        <layers>
            <enabled>true</enabled>
        </layers>
    </configuration>
</plugin>
```

**Propósito:**
- Crear JAR ejecutable
- Excluir Lombok del JAR final
- Habilitar Spring Boot Layered JARs (para Docker)

---

## Árbol de Dependencias Simplificado

```
vg-ms-distribution
├── Spring Boot 3.4.5
│   ├── WebFlux (Reactive Web)
│   │   ├── Netty (Servidor)
│   │   └── Project Reactor
│   ├── Data MongoDB Reactive
│   │   └── MongoDB Reactive Streams Driver
│   ├── Security
│   │   ├── OAuth2 Resource Server
│   │   └── JWT Validation
│   ├── Validation
│   │   └── Hibernate Validator
│   └── Actuator
│       └── Micrometer + Prometheus
├── Spring Cloud 2023.0.1
│   └── Gateway
├── Keycloak 25.0.6
│   └── Admin Client
├── Lombok
│   └── Code Generation
└── Testing
    ├── JUnit 5
    ├── Mockito
    └── Reactor Test
```

---

## Compatibilidad de Versiones

| Componente | Versión | Compatible con |
|------------|---------|----------------|
| Spring Boot | 3.4.5 | Java 17+ |
| Spring Cloud | 2023.0.1 | Boot 3.x |
| MongoDB Driver | Reactive | Spring Data 3.x |
| Keycloak | 25.0.6 | OAuth2/OIDC |
| JUnit | 5.10.2 | Java 17+ |
| Mockito | 5.11.0 | JUnit 5 |

---

## Conclusión

Las dependencias están cuidadosamente seleccionadas para:
- ✅ **Programación Reactiva** con WebFlux y MongoDB Reactive
- ✅ **Seguridad** con OAuth2, JWT y Keycloak
- ✅ **Validación** con Bean Validation
- ✅ **Monitoreo** con Actuator y Prometheus
- ✅ **Testing** con JUnit 5, Mockito y Reactor Test
- ✅ **Productividad** con Lombok
- ✅ **Escalabilidad** con arquitectura de microservicios

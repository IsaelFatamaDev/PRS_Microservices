# 04 - DEPENDENCIAS POM.XML COMPLETAS

Este documento contiene los `pom.xml` completos para cada tipo de microservicio.

---

## 📋 VERSIONES ESTÁNDAR

| Tecnología | Versión | Notas |
|-----------|---------|-------|
| **Java** | 21 | LTS (Long Term Support) - Virtual Threads |
| **Spring Boot** | 3.5.10 | Versión estable 2026 |
| **Spring Cloud** | 2025.0.1 | Para Circuit Breaker (Resilience4j) |
| **SpringDoc OpenAPI** | 2.3.0 | Swagger UI compatible con Spring Boot 3.x |
| **R2DBC PostgreSQL** | 1.0.2.RELEASE | Driver reactivo |
| **MongoDB Reactive** | 4.11.1 | Incluido en Spring Data |
| **Flyway** | 10.4.1 | Migraciones DB |

---

## 1️⃣ POM PARA SERVICIOS POSTGRESQL (Users, Commercial, Inventory, Infrastructure)

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>
    
    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-users</artifactId>
    <version>1.0.0</version>
    <name>vg-ms-users</name>
    <description>Microservicio de Usuarios - JASS Digital</description>
    
    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2025.0.1</spring-cloud.version>
        <springdoc.version>2.3.0</springdoc.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Webflux (Reactive) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        
        <!-- Spring Data R2DBC (PostgreSQL Reactivo) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-r2dbc</artifactId>
        </dependency>
        
        <!-- PostgreSQL R2DBC Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>r2dbc-postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- PostgreSQL JDBC Driver (para Flyway) -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Flyway Core (Migraciones) -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- Flyway PostgreSQL -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Spring JDBC (necesario para Flyway) -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
        </dependency>
        
        <!-- Spring AMQP (RabbitMQ) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Resilience4j Circuit Breaker (Spring Cloud Starter) -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        
        <!-- SpringDoc OpenAPI (Swagger UI) -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Actuator (Health, Metrics) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot DevTools (Desarrollo) -->
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
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
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
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 2️⃣ POM PARA SERVICIOS MONGODB (Organizations, Water-Quality, Distribution, Claims)

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>
    
    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-organizations</artifactId>
    <version>1.0.0</version>
    <name>vg-ms-organizations</name>
    <description>Microservicio de Organizaciones - JASS Digital</description>
    
    <properties>
        <java.version>21</java.version>
        <springdoc.version>2.3.0</springdoc.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Webflux (Reactive) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        
        <!-- Spring Data MongoDB Reactive -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>
        
        <!-- Spring Cloud Stream RabbitMQ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- SpringDoc OpenAPI (Swagger UI) -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Actuator (Health, Metrics) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot DevTools -->
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
    </dependencies>
    
    <build>
        <plugins>
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
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3️⃣ POM PARA AUTHENTICATION (Keycloak)

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>
    
    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-authentication</artifactId>
    <version>1.0.0</version>
    <name>vg-ms-authentication</name>
    <description>Microservicio de Autenticación - JASS Digital</description>
    
    <properties>
        <java.version>21</java.version>
        <springdoc.version>2.3.0</springdoc.version>
        <keycloak.version>23.0.3</keycloak.version>
        <resilience4j.version>2.1.0</resilience4j.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Webflux -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        
        <!-- Keycloak Admin Client -->
        <dependency>
            <groupId>org.keycloak</groupId>
            <artifactId>keycloak-admin-client</artifactId>
            <version>${keycloak.version}</version>
        </dependency>
        
        <!-- Spring Security OAuth2 Resource Server -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Resilience4j Circuit Breaker -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-reactor</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        
        <!-- SpringDoc OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- DevTools -->
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

## 4️⃣ POM PARA GATEWAY

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>
    
    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-gateway</artifactId>
    <version>1.0.0</version>
    <name>vg-ms-gateway</name>
    <description>API Gateway - JASS Digital</description>
    
    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <keycloak.version>23.0.3</keycloak.version>
    </properties>
    
    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
        <!-- Spring Security OAuth2 Resource Server -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        
        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- DevTools -->
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
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
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

## 📊 RESUMEN DE DEPENDENCIAS POR SERVICIO

### Matriz de Dependencias por Tipo de Microservicio

| Dependencia | PostgreSQL (4) | MongoDB (5) | Auth | Gateway |
|-------------|----------------|-------------|------|---------|
| **Core Framework** |
| Spring Boot Webflux | ✅ | ✅ | ✅ | ❌ |
| Spring Cloud Gateway | ❌ | ❌ | ❌ | ✅ |
| **Base de Datos** |
| Spring Data R2DBC | ✅ | ❌ | ❌ | ❌ |
| R2DBC PostgreSQL Driver | ✅ | ❌ | ❌ | ❌ |
| PostgreSQL JDBC Driver | ✅ | ❌ | ❌ | ❌ |
| Flyway Core | ✅ | ❌ | ❌ | ❌ |
| Flyway PostgreSQL | ✅ | ❌ | ❌ | ❌ |
| Spring Data MongoDB Reactive | ❌ | ✅ | ❌ | ❌ |
| **Mensajería** |
| Spring AMQP (RabbitMQ) | ✅ | ✅ | ❌ | ❌ |
| **Comunicación entre Servicios** |
| Spring WebFlux (WebClient) | ✅ | ❌ | ✅ | ❌ |
| Resilience4j Spring Boot 3 | ✅ | ❌ | ✅ | ❌ |
| Resilience4j Reactor | ✅ | ❌ | ✅ | ❌ |
| **Seguridad** |
| Keycloak Admin Client | ❌ | ❌ | ✅ | ❌ |
| OAuth2 Resource Server | ❌ | ❌ | ✅ | ✅ |
| **Documentación y Validación** |
| SpringDoc OpenAPI WebFlux | ✅ | ✅ | ✅ | ❌ |
| Validation | ✅ | ✅ | ✅ | ❌ |
| **Observabilidad** |
| Spring Boot Actuator | ✅ | ✅ | ✅ | ✅ |
| **Desarrollo** |
| Lombok | ✅ | ✅ | ✅ | ✅ |
| Spring Boot DevTools | ✅ | ✅ | ✅ | ✅ |
| **Testing** |
| Spring Boot Starter Test | ✅ | ✅ | ✅ | ✅ |
| Reactor Test | ✅ | ✅ | ✅ | ✅ |

---

### Servicios por Categoría

#### 🗄️ Microservicios PostgreSQL (4 servicios)
- **vg-ms-users** (Puerto 8081)
- **vg-ms-commercial-operations** (Puerto 8084)
- **vg-ms-inventory-purchases** (Puerto 8087)
- **vg-ms-infrastructure** (Puerto 8089)

**Dependencias clave:**
- R2DBC PostgreSQL (reactivo)
- Flyway (migraciones)
- RabbitMQ (eventos)
- Resilience4j (circuit breaker)
- SpringDoc OpenAPI (Swagger)

---

#### 📦 Microservicios MongoDB (5 servicios)
- **vg-ms-organizations** (Puerto 8083)
- **vg-ms-water-quality** (Puerto 8085)
- **vg-ms-distribution** (Puerto 8086)
- **vg-ms-claims-incidents** (Puerto 8088)

**Dependencias clave:**
- MongoDB Reactive
- RabbitMQ (eventos)
- SpringDoc OpenAPI (Swagger)

---

#### 🔐 Microservicio de Autenticación
- **vg-ms-authentication** (Puerto 8082)

**Dependencias especiales:**
- Keycloak Admin Client (23.0.3)
- OAuth2 Resource Server
- Resilience4j (para llamar a users)
- **NO usa** base de datos propia
- **NO usa** RabbitMQ

---

#### 🌐 API Gateway
- **vg-ms-gateway** (Puerto 8080)

**Dependencias especiales:**
- Spring Cloud Gateway
- OAuth2 Resource Server (autenticación)
- **NO usa** base de datos
- **NO usa** RabbitMQ
- **NO usa** Swagger (los servicios backend lo tienen)

---

## 🎯 COMANDOS MAVEN ÚTILES

```bash
# Compilar
mvn clean compile

# Empaquetar (JAR)
mvn clean package

# Ejecutar tests
mvn test

# Ejecutar aplicación
mvn spring-boot:run

# Saltar tests al compilar
mvn clean package -DskipTests

# Instalar en repositorio local
mvn clean install

# Verificar dependencias
mvn dependency:tree
```

---

## ✅ NOTAS IMPORTANTES

1. **Java 21** es LTS y trae Virtual Threads (Project Loom) para mejor rendimiento en aplicaciones reactivas
2. **Spring Boot 3.5.10** es compatible con Java 21 y trae mejoras de rendimiento
3. **Spring Cloud 2025.0.1** se usa para el Circuit Breaker con Resilience4j (más moderno que dependencias directas)
4. **SpringDoc OpenAPI 2.3.0** es la versión correcta para Spring Boot 3.x (NO uses Springfox)
5. **Resilience4j** se incluye via `spring-cloud-starter-circuitbreaker-reactor-resilience4j` en servicios que hacen llamadas a otros (users, authentication, infrastructure)
6. **Flyway** solo para PostgreSQL (MongoDB no necesita migraciones automáticas con schemas)
7. **R2DBC** para programación reactiva con PostgreSQL
8. **Spring JDBC** es necesario para que Flyway funcione (aunque uses R2DBC para consultas)
9. Todas las dependencias están **alineadas** con Spring Boot 3.5.10 y Java 21
10. **Eureka NO es necesario** en esta fase inicial - ver documento `07_EUREKA_FAQ.md`

---

## 📦 RESUMEN FINAL

| Aspecto | Valor |
|---------|-------|
| **Java Version** | 21 (LTS) |
| **Spring Boot Version** | 3.5.10 |
| **Total Microservicios** | 11 |
| **Servicios PostgreSQL** | 4 |
| **Servicios MongoDB** | 5 |
| **Servicios Especiales** | 2 (Auth + Gateway) |
| **Arquitectura** | Reactiva (WebFlux) |
| **Mensajería** | RabbitMQ |
| **Circuit Breaker** | Resilience4j |
| **Documentación** | SpringDoc OpenAPI 2.3.0 |

---

## ✅ NOTAS IMPORTANTES

1. **Java 17** es LTS y compatible con Spring Boot 3.x

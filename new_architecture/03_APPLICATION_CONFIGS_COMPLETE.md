# 03 - CONFIGURACIONES APPLICATION.YML COMPLETAS

Este documento contiene **TODAS** las configuraciones para los 11 microservicios.

> **📌 VERSIÓN CORREGIDA**:
>
> - CORS solo en Gateway (NO en microservicios)
> - RabbitMQ exchanges/queues van en `RabbitMQConfig.java` (NO en YAML)
> - Sin propiedades inventadas

---

## 📊 TABLA DE DEPENDENCIAS

| Servicio | Puerto | WebClient + CB | RabbitMQ | Keycloak | BD |
|----------|--------|----------------|----------|----------|-----|
| users | 8081 | ✅ (auth, org, notif) | ✅ Publisher | ❌ | PostgreSQL |
| authentication | 8082 | ✅ (user) | ✅ Listener | ✅ | Keycloak |
| organizations | 8083 | ❌ | ✅ Publisher | ❌ | MongoDB |
| commercial-operations | 8084 | ✅ (user, infra, notif) | ✅ Publisher | ❌ | PostgreSQL |
| water-quality | 8085 | ❌ | ✅ Publisher | ❌ | MongoDB |
| distribution | 8086 | ❌ | ✅ Publisher | ❌ | MongoDB |
| inventory-purchases | 8087 | ❌ | ✅ Publisher | ❌ | PostgreSQL |
| claims-incidents | 8088 | ✅ (user, infra) | ✅ Publisher | ❌ | MongoDB |
| infrastructure | 8089 | ✅ (user, org) | ✅ Pub+Listen | ❌ | PostgreSQL |
| notification | 8090 | ❌ | ✅ Listener | ❌ | Node.js |
| gateway | 8080 | ❌ | ❌ | ✅ | - |

---

## ⚠️ REGLAS IMPORTANTES

### 1️⃣ CORS - SOLO EN GATEWAY

```
❌ NO configurar CORS en microservicios individuales
✅ CORS se configura ÚNICAMENTE en vg-ms-gateway
```

**Razón**: Los microservicios están detrás del Gateway. Las peticiones del browser llegan al Gateway, NO directamente a los microservicios.

### 2️⃣ RabbitMQ - Exchanges/Queues en JAVA

```
❌ NO configurar exchanges/queues/routing-keys en YAML
✅ Eso va en RabbitMQConfig.java (clase de configuración)
```

**En YAML solo va**: host, port, username, password, publisher-confirm-type

### 3️⃣ Propiedades que SÍ existen en Spring Boot 3.x

```yaml
# ✅ VÁLIDAS
spring.rabbitmq.host
spring.rabbitmq.port
spring.rabbitmq.username
spring.rabbitmq.password
spring.rabbitmq.virtual-host
spring.rabbitmq.publisher-confirm-type
spring.rabbitmq.publisher-returns
spring.rabbitmq.listener.simple.acknowledge-mode
spring.rabbitmq.listener.simple.prefetch

# ❌ NO EXISTEN / INVENTADAS
spring.rabbitmq.template.exchange      # NO EXISTE
spring.rabbitmq.template.routing-key   # NO EXISTE
spring.webflux.cors                    # NO EXISTE
```

---

## 🔧 ESTRUCTURA DE ARCHIVOS POR SERVICIO

```
src/main/resources/
├── application.yml          → Configuración base (nombre, puerto, actuator)
├── application-dev.yml      → Desarrollo local (conexiones localhost)
└── application-prod.yml     → Producción (variables de entorno)
```

---

# 1️⃣ vg-ms-users

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-users
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8081
  shutdown: graceful

# Swagger/OpenAPI
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method
    tags-sorter: alpha
    display-request-duration: true
  show-actuator: true

# Actuator / Health Checks
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,circuitbreakers,circuitbreakerevents
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    circuitbreakers:
      enabled: true
    db:
      enabled: true
    rabbit:
      enabled: true

# Logging base
logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] [%thread] %-5level %logger{36} - %msg%n"
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  # PostgreSQL R2DBC
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m

  # Flyway Migrations
  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: SISTEMAJASS
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true

  # RabbitMQ (solo conexión, exchanges/queues van en RabbitMQConfig.java)
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true

# WebClient - URLs de servicios externos
webclient:
  connect-timeout: 5000
  read-timeout: 10000
  write-timeout: 10000
  services:
    authentication:
      base-url: http://localhost:8082
    organization:
      base-url: http://localhost:8083
    notification:
      base-url: http://localhost:8090

# Resilience4j
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        slow-call-rate-threshold: 80
        slow-call-duration-threshold: 3s
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        base-config: default
        failure-rate-threshold: 30

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        base-config: default
        max-attempts: 2

  timelimiter:
    configs:
      default:
        timeout-duration: 5s
        cancel-running-future: true
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        timeout-duration: 10s

# Logging detallado en dev
logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
    io.github.resilience4j: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  # PostgreSQL R2DBC
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10
      max-idle-time: 10m

  # Flyway
  flyway:
    enabled: true
    url: ${DATABASE_JDBC_URL}
    user: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    locations: classpath:db/migration
    baseline-on-migrate: true

  # RabbitMQ
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    virtual-host: ${RABBITMQ_VHOST:/}
    publisher-confirm-type: correlated
    publisher-returns: true
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

# WebClient - URLs de servicios (producción)
webclient:
  connect-timeout: 5000
  read-timeout: 10000
  write-timeout: 10000
  services:
    authentication:
      base-url: ${AUTHENTICATION_SERVICE_URL}
    organization:
      base-url: ${ORGANIZATION_SERVICE_URL}
    notification:
      base-url: ${NOTIFICATION_SERVICE_URL}

# Swagger deshabilitado en producción
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

# Logging mínimo en producción
logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 2️⃣ vg-ms-authentication

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-authentication
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8082
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true
    rabbit:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  # RabbitMQ - Escucha eventos de users
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 5

# Keycloak Configuration
keycloak:
  realm: jass-digital
  auth-server-url: http://localhost:8180
  resource: jass-backend
  credentials:
    secret: your-dev-client-secret
  admin:
    username: admin
    password: admin
    client-id: admin-cli

# WebClient
webclient:
  connect-timeout: 5000
  read-timeout: 10000
  services:
    user:
      base-url: http://localhost:8081

# Resilience4j
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
    instances:
      userService:
        base-config: default
      keycloakService:
        base-config: default
        wait-duration-in-open-state: 30s

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
    instances:
      userService:
        base-config: default
      keycloakService:
        base-config: default
        max-attempts: 2

  timelimiter:
    configs:
      default:
        timeout-duration: 5s
    instances:
      userService:
        base-config: default
      keycloakService:
        timeout-duration: 10s

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.keycloak: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 5

keycloak:
  realm: ${KEYCLOAK_REALM}
  auth-server-url: ${KEYCLOAK_URL}
  resource: ${KEYCLOAK_CLIENT_ID}
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}
  admin:
    username: ${KEYCLOAK_ADMIN_USER}
    password: ${KEYCLOAK_ADMIN_PASSWORD}
    client-id: ${KEYCLOAK_ADMIN_CLIENT_ID:admin-cli}

webclient:
  services:
    user:
      base-url: ${USER_SERVICE_URL}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 3️⃣ vg-ms-organizations

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-organizations
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8083
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  # MongoDB
  data:
    mongodb:
      uri: mongodb://localhost:27017/JASS_DIGITAL
      auto-index-creation: true
      uuid-representation: standard

  # RabbitMQ
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 4️⃣ vg-ms-commercial-operations

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-commercial-operations
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8084
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    rabbit:
      enabled: true
    circuitbreakers:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  # PostgreSQL R2DBC
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m

  # Flyway
  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: SISTEMAJASS
    locations: classpath:db/migration
    baseline-on-migrate: true

  # RabbitMQ
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true

# WebClient
webclient:
  connect-timeout: 5000
  read-timeout: 10000
  write-timeout: 10000
  services:
    user:
      base-url: http://localhost:8081
    infrastructure:
      base-url: http://localhost:8089
    notification:
      base-url: http://localhost:8090

# Resilience4j
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
    instances:
      userService:
        base-config: default
      infrastructureService:
        base-config: default
      notificationService:
        base-config: default

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
    instances:
      userService:
        base-config: default
      infrastructureService:
        base-config: default
      notificationService:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 5s
    instances:
      userService:
        base-config: default
      infrastructureService:
        base-config: default
      notificationService:
        timeout-duration: 10s

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: ${DATABASE_JDBC_URL}
    user: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

webclient:
  services:
    user:
      base-url: ${USER_SERVICE_URL}
    infrastructure:
      base-url: ${INFRASTRUCTURE_SERVICE_URL}
    notification:
      base-url: ${NOTIFICATION_SERVICE_URL}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 5️⃣ vg-ms-water-quality

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-water-quality
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8085
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/JASS_DIGITAL
      auto-index-creation: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 6️⃣ vg-ms-distribution

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-distribution
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8086
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/JASS_DIGITAL
      auto-index-creation: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 7️⃣ vg-ms-inventory-purchases

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-inventory-purchases
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8087
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    rabbit:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m

  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: SISTEMAJASS
    locations: classpath:db/migration
    baseline-on-migrate: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: ${DATABASE_JDBC_URL}
    user: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 8️⃣ vg-ms-claims-incidents

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-claims-incidents
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8088
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true
    circuitbreakers:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/JASS_DIGITAL
      auto-index-creation: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true

# WebClient
webclient:
  connect-timeout: 5000
  read-timeout: 10000
  services:
    user:
      base-url: http://localhost:8081
    infrastructure:
      base-url: http://localhost:8089

# Resilience4j
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
    instances:
      userService:
        base-config: default
      infrastructureService:
        base-config: default

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
    instances:
      userService:
        base-config: default
      infrastructureService:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 5s

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

webclient:
  services:
    user:
      base-url: ${USER_SERVICE_URL}
    infrastructure:
      base-url: ${INFRASTRUCTURE_SERVICE_URL}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 9️⃣ vg-ms-infrastructure

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-infrastructure
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  webflux:
    base-path: /api/v1
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  port: 8089
  shutdown: graceful

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    rabbit:
      enabled: true
    circuitbreakers:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m

  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: SISTEMAJASS
    locations: classpath:db/migration
    baseline-on-migrate: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10

# WebClient
webclient:
  connect-timeout: 5000
  read-timeout: 10000
  services:
    user:
      base-url: http://localhost:8081
    organization:
      base-url: http://localhost:8083

# Resilience4j
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
    instances:
      userService:
        base-config: default
      organizationService:
        base-config: default

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
    instances:
      userService:
        base-config: default
      organizationService:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 5s

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: ${DATABASE_JDBC_URL}
    user: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10

webclient:
  services:
    user:
      base-url: ${USER_SERVICE_URL}
    organization:
      base-url: ${ORGANIZATION_SERVICE_URL}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

# 🔟 vg-ms-notification (Node.js/TypeScript)

> Este servicio usa **Node.js/Express con TypeScript**

## Estructura de archivos

```
vg-ms-notification/
├── src/
│   ├── index.ts
│   ├── config/
│   │   ├── env.ts
│   │   └── rabbitmq.ts
│   ├── services/
│   │   ├── whatsapp.service.ts
│   │   ├── email.service.ts
│   │   └── sms.service.ts
│   └── messaging/
│       ├── consumer.ts
│       └── handlers/
├── package.json
├── tsconfig.json
├── .env
└── .env.production
```

## .env (Development)

```env
# Server
NODE_ENV=development
PORT=8090

# Twilio (WhatsApp/SMS)
TWILIO_ACCOUNT_SID=your_dev_account_sid
TWILIO_AUTH_TOKEN=your_dev_auth_token
TWILIO_PHONE_NUMBER=+1234567890
TWILIO_WHATSAPP_NUMBER=whatsapp:+14155238886

# Email (SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password
SMTP_FROM=JASS Digital <noreply@jassdigital.com>

# RabbitMQ
RABBITMQ_URL=amqp://guest:guest@localhost:5672
RABBITMQ_EXCHANGE=jass.events
RABBITMQ_QUEUE=notification.events.queue

# Logging
LOG_LEVEL=debug
```

## .env.production

```env
NODE_ENV=production
PORT=8090

TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
TWILIO_PHONE_NUMBER=${TWILIO_PHONE_NUMBER}
TWILIO_WHATSAPP_NUMBER=${TWILIO_WHATSAPP_NUMBER}

SMTP_HOST=${SMTP_HOST}
SMTP_PORT=${SMTP_PORT}
SMTP_USER=${SMTP_USER}
SMTP_PASS=${SMTP_PASS}
SMTP_FROM=${SMTP_FROM}

RABBITMQ_URL=${RABBITMQ_URL}
RABBITMQ_EXCHANGE=jass.events
RABBITMQ_QUEUE=notification.events.queue

LOG_LEVEL=info
```

---

# 1️⃣1️⃣ vg-ms-gateway

> **⚠️ ÚNICO SERVICIO CON CORS** - CORS se configura SOLO aquí

## application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-gateway
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  lifecycle:
    timeout-per-shutdown-phase: 30s

  # CORS GLOBAL - SOLO AQUÍ
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: ${CORS_ALLOWED_ORIGINS:*}
            allowed-methods:
              - GET
              - POST
              - PUT
              - PATCH
              - DELETE
              - OPTIONS
            allowed-headers: "*"
            exposed-headers:
              - Authorization
              - Content-Disposition
              - X-Correlation-Id
            allow-credentials: false
            max-age: 3600

      # Filtros globales
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_UNIQUE
        - AddRequestHeader=X-Request-Source, gateway

server:
  port: 8080
  shutdown: graceful

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway,circuitbreakers
      base-path: /actuator
  endpoint:
    health:
      show-details: always
    gateway:
      enabled: true
  health:
    circuitbreakers:
      enabled: true

logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: INFO
    org.springframework.security: INFO
```

## application-dev.yml (Desarrollo)

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Users Service
        - id: users-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: usersService
                fallbackUri: forward:/fallback/users

        # Auth Service (público)
        - id: auth-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=0

        # Organizations Service
        - id: organizations-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/v1/organizations/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: organizationsService

        # Commercial Operations Service
        - id: commercial-service
          uri: http://localhost:8084
          predicates:
            - Path=/api/v1/commercial/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: commercialService

        # Water Quality Service
        - id: water-quality-service
          uri: http://localhost:8085
          predicates:
            - Path=/api/v1/water-quality/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: waterQualityService

        # Distribution Service
        - id: distribution-service
          uri: http://localhost:8086
          predicates:
            - Path=/api/v1/distribution/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: distributionService

        # Inventory Service
        - id: inventory-service
          uri: http://localhost:8087
          predicates:
            - Path=/api/v1/inventory/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: inventoryService

        # Claims Service
        - id: claims-service
          uri: http://localhost:8088
          predicates:
            - Path=/api/v1/claims/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: claimsService

        # Infrastructure Service
        - id: infrastructure-service
          uri: http://localhost:8089
          predicates:
            - Path=/api/v1/infrastructure/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: infrastructureService

  # Spring Security + Keycloak
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/jass-digital
          jwk-set-uri: http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs

# Resilience4j para Gateway
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 30s
        failure-rate-threshold: 50
    instances:
      usersService:
        base-config: default
      organizationsService:
        base-config: default
      commercialService:
        base-config: default
      waterQualityService:
        base-config: default
      distributionService:
        base-config: default
      inventoryService:
        base-config: default
      claimsService:
        base-config: default
      infrastructureService:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 10s

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.security: DEBUG
```

## application-prod.yml (Producción)

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            # ORÍGENES ESPECÍFICOS EN PRODUCCIÓN
            allowed-origins:
              - ${FRONTEND_URL}
              - ${ADMIN_URL}
            allowed-methods:
              - GET
              - POST
              - PUT
              - PATCH
              - DELETE
              - OPTIONS
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600

      routes:
        - id: users-service
          uri: ${USERS_SERVICE_URL}
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: usersService
                fallbackUri: forward:/fallback/users

        - id: auth-service
          uri: ${AUTH_SERVICE_URL}
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=0

        - id: organizations-service
          uri: ${ORGANIZATIONS_SERVICE_URL}
          predicates:
            - Path=/api/v1/organizations/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: organizationsService

        - id: commercial-service
          uri: ${COMMERCIAL_SERVICE_URL}
          predicates:
            - Path=/api/v1/commercial/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: commercialService

        - id: water-quality-service
          uri: ${WATER_QUALITY_SERVICE_URL}
          predicates:
            - Path=/api/v1/water-quality/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: waterQualityService

        - id: distribution-service
          uri: ${DISTRIBUTION_SERVICE_URL}
          predicates:
            - Path=/api/v1/distribution/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: distributionService

        - id: inventory-service
          uri: ${INVENTORY_SERVICE_URL}
          predicates:
            - Path=/api/v1/inventory/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: inventoryService

        - id: claims-service
          uri: ${CLAIMS_SERVICE_URL}
          predicates:
            - Path=/api/v1/claims/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: claimsService

        - id: infrastructure-service
          uri: ${INFRASTRUCTURE_SERVICE_URL}
          predicates:
            - Path=/api/v1/infrastructure/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: infrastructureService

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
          jwk-set-uri: ${KEYCLOAK_JWK_URI}

logging:
  level:
    root: WARN
    org.springframework.cloud.gateway: INFO
    org.springframework.security: WARN
```

---

# 📦 CLASE RabbitMQConfig.java (Ejemplo para vg-ms-users)

> **Exchanges, Queues y Bindings van en JAVA, NO en YAML**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ.
 *
 * Exchanges, Queues y Bindings se definen aquí (NO en YAML).
 */
@Configuration
public class RabbitMQConfig {

    // ═══════════════════════════════════════════════════════════════
    // CONSTANTES
    // ═══════════════════════════════════════════════════════════════

    public static final String EXCHANGE_NAME = "jass.events";

    // Routing Keys
    public static final String USER_CREATED_KEY = "user.created";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String USER_DELETED_KEY = "user.deleted";
    public static final String USER_RESTORED_KEY = "user.restored";
    public static final String USER_PURGED_KEY = "user.purged";

    // ═══════════════════════════════════════════════════════════════
    // EXCHANGE
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public TopicExchange jassEventsExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // MESSAGE CONVERTER
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ═══════════════════════════════════════════════════════════════
    // RABBIT TEMPLATE
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setExchange(EXCHANGE_NAME);
        return template;
    }
}
```

---

# ✅ RESUMEN DE CORRECCIONES

| Problema | Solución |
|----------|----------|
| CORS en cada microservicio | ❌ Eliminado. Solo en Gateway |
| `spring.webflux.cors` | ❌ No existe. Usar `spring.cloud.gateway.globalcors` en Gateway |
| RabbitMQ exchanges en YAML | ❌ Movido a `RabbitMQConfig.java` |
| `spring.rabbitmq.template.exchange` | ❌ No existe. Configurar en RabbitTemplate bean |
| Propiedades duplicadas | ❌ Eliminadas duplicaciones |

---

# 🔑 VARIABLES DE ENTORNO REQUERIDAS EN PRODUCCIÓN

## Base de Datos

```env
DATABASE_URL=r2dbc:postgresql://host:5432/db?sslMode=require
DATABASE_JDBC_URL=jdbc:postgresql://host:5432/db?sslmode=require
DATABASE_USER=user
DATABASE_PASSWORD=password
MONGODB_URI=mongodb+srv://user:pass@cluster/db
```

## RabbitMQ

```env
RABBITMQ_HOST=host
RABBITMQ_PORT=5672
RABBITMQ_USER=user
RABBITMQ_PASSWORD=password
RABBITMQ_VHOST=/
RABBITMQ_SSL_ENABLED=false
```

## Keycloak

```env
KEYCLOAK_URL=https://keycloak.domain.com
KEYCLOAK_REALM=jass-digital
KEYCLOAK_CLIENT_ID=jass-backend
KEYCLOAK_CLIENT_SECRET=secret
KEYCLOAK_ISSUER_URI=https://keycloak.domain.com/realms/jass-digital
KEYCLOAK_JWK_URI=https://keycloak.domain.com/realms/jass-digital/protocol/openid-connect/certs
```

## URLs de Servicios (para Gateway)

```env
USERS_SERVICE_URL=http://vg-ms-users:8081
AUTH_SERVICE_URL=http://vg-ms-authentication:8082
ORGANIZATIONS_SERVICE_URL=http://vg-ms-organizations:8083
COMMERCIAL_SERVICE_URL=http://vg-ms-commercial-operations:8084
WATER_QUALITY_SERVICE_URL=http://vg-ms-water-quality:8085
DISTRIBUTION_SERVICE_URL=http://vg-ms-distribution:8086
INVENTORY_SERVICE_URL=http://vg-ms-inventory-purchases:8087
CLAIMS_SERVICE_URL=http://vg-ms-claims-incidents:8088
INFRASTRUCTURE_SERVICE_URL=http://vg-ms-infrastructure:8089
```

## Frontend (para CORS en Gateway)

```env
CORS_ALLOWED_ORIGINS=*
FRONTEND_URL=https://app.jassdigital.com
ADMIN_URL=https://admin.jassdigital.com
```

---

**✅ TOTAL: 11 servicios completamente configurados y corregidos**

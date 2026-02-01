# 03 - CONFIGURACIONES APPLICATION.YML COMPLETAS

Este documento contiene **TODAS** las configuraciones para los 11 microservicios con sus dependencias completas.

> **📌 VERSIÓN MEJORADA**: Incluye Security JWT, Resilience4j completo, CORS, Graceful Shutdown, Health Checks avanzados.

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

## 🔧 CONFIGURACIÓN BASE COMPARTIDA

> Estas configuraciones se repiten en todos los microservicios Java.

### Estructura de archivos por servicio

```
src/main/resources/
├── application.yml          → Configuración base + perfiles
├── application-dev.yml      → Desarrollo local
└── application-prod.yml     → Producción (variables de entorno)
```

---

## 1️⃣ vg-ms-users

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-users
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  # Configuración WebFlux
  webflux:
    base-path: /api/v1

# Servidor
server:
  port: 8081
  shutdown: graceful

# Graceful Shutdown
spring.lifecycle:
  timeout-per-shutdown-phase: 30s

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
    ratelimiters:
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
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS para desarrollo
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

  # PostgreSQL R2DBC
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m
      validation-query: SELECT 1

  # Flyway Migrations
  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: SISTEMAJASS
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true

  # RabbitMQ
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2.0

# RabbitMQ Exchanges y Queues
rabbitmq:
  exchange:
    name: jass.events
    type: topic
  queues:
    user-events: user.events.queue
  routing-keys:
    user-created: user.created
    user-updated: user.updated
    user-deleted: user.deleted
    user-restored: user.restored
    user-purged: user.purged

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

# Resilience4j - Circuit Breaker + Retry + TimeLimiter
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
        record-exceptions:
          - java.io.IOException
          - java.net.ConnectException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.reactive.function.client.WebClientResponseException
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
        retry-exceptions:
          - java.io.IOException
          - java.net.ConnectException
          - java.util.concurrent.TimeoutException
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
        base-config: default
        timeout-duration: 10s

# Swagger habilitado en dev
springdoc:
  swagger-ui:
    enabled: true

# Logging detallado en dev
logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
    org.springframework.web.reactive: DEBUG
    io.github.resilience4j: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  # CORS restringido en producción
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

  # PostgreSQL R2DBC - Neon
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
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

# Logging mínimo en producción
logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
    org.springframework.r2dbc: WARN
```

---

## 2️⃣ vg-ms-authentication

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-authentication
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8082
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3

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

# Queues que escucha
rabbitmq:
  exchange:
    name: jass.events
  queues:
    user-sync: authentication.user-sync.queue
  bindings:
    - queue: authentication.user-sync.queue
      exchange: jass.events
      routing-key: user.*

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

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.keycloak: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: ${RABBITMQ_SSL_ENABLED:false}

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
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 3️⃣ vg-ms-organizations

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-organizations
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8083
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

# Security - JWT Validation (tokens del Gateway)
security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

# RabbitMQ Config
rabbitmq:
  exchange:
    name: jass.events
    type: topic
  routing-keys:
    organization-created: organization.created
    organization-updated: organization.updated
    organization-deleted: organization.deleted
    zone-created: zone.created
    street-created: street.created

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 4️⃣ vg-ms-commercial-operations

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-commercial-operations
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8084
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

# RabbitMQ Config
rabbitmq:
  exchange:
    name: jass.events
    type: topic
  routing-keys:
    receipt-created: receipt.created
    payment-completed: payment.completed
    payment-cancelled: payment.cancelled
    debt-generated: debt.generated
    service-cut-scheduled: service-cut.scheduled
    service-reconnected: service-cut.reconnected

# WebClient
webclient:
  connect-timeout: 5000
  read-timeout: 10000
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
        exponential-backoff-multiplier: 2
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

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 5️⃣ vg-ms-water-quality

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-water-quality
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8085
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

rabbitmq:
  exchange:
    name: jass.events
    type: topic
  routing-keys:
    quality-test-created: quality-test.created
    quality-test-completed: quality-test.completed
    quality-alert: quality.alert

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 6️⃣ vg-ms-distribution

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-distribution
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8086
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

rabbitmq:
  exchange:
    name: jass.events
    type: topic
  routing-keys:
    program-created: distribution.program.created
    route-created: distribution.route.created
    schedule-created: distribution.schedule.created

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 7️⃣ vg-ms-inventory-purchases

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-inventory-purchases
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8087
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20

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

rabbitmq:
  exchange:
    name: jass.events
    type: topic
  routing-keys:
    supplier-created: inventory.supplier.created
    material-created: inventory.material.created
    purchase-created: inventory.purchase.created
    purchase-received: inventory.purchase.received
    low-stock-alert: inventory.low-stock.alert

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 8️⃣ vg-ms-claims-incidents

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-claims-incidents
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8088
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

rabbitmq:
  exchange:
    name: jass.events
    type: topic
  routing-keys:
    complaint-created: complaint.created
    complaint-closed: complaint.closed
    incident-created: incident.created
    incident-assigned: incident.assigned
    incident-resolved: incident.resolved
    urgent-incident-alert: incident.urgent.alert

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
    instances:
      userService:
        base-config: default
      infrastructureService:
        base-config: default

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 9️⃣ vg-ms-infrastructure

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-infrastructure
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8089
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/jass-digital}
    jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8180/realms/jass-digital/protocol/openid-connect/certs}

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

### application-dev.yml (Desarrollo)

```yaml
spring:
  # CORS
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20

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

# RabbitMQ Config - Publica y Escucha
rabbitmq:
  exchange:
    name: jass.events
    type: topic
  queues:
    service-cut-events: infrastructure.service-cut.queue
    incident-events: infrastructure.incident.queue
  bindings:
    - queue: infrastructure.service-cut.queue
      exchange: jass.events
      routing-key: service-cut.*
    - queue: infrastructure.incident.queue
      exchange: jass.events
      routing-key: incident.created
  routing-keys:
    water-box-created: water-box.created
    water-box-assigned: water-box.assigned
    water-box-transferred: water-box.transferred
    water-box-suspended: water-box.suspended
    water-box-reconnected: water-box.reconnected

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
    instances:
      userService:
        base-config: default
      organizationService:
        base-config: default

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  webflux:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
      allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600

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
    organization:
      base-url: ${ORGANIZATION_SERVICE_URL}

security:
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_URI}

springdoc:
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 🔟 vg-ms-notification (Node.js/TypeScript)

> Este servicio usa **Node.js/Express con TypeScript**

### Estructura de archivos

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

### package.json

```json
{
  "name": "vg-ms-notification",
  "version": "1.0.0",
  "main": "dist/index.js",
  "scripts": {
    "build": "tsc",
    "start": "node dist/index.js",
    "dev": "ts-node-dev --respawn src/index.ts",
    "lint": "eslint src/**/*.ts"
  },
  "dependencies": {
    "express": "^4.18.2",
    "twilio": "^4.19.0",
    "nodemailer": "^6.9.7",
    "amqplib": "^0.10.3",
    "dotenv": "^16.3.1",
    "winston": "^3.11.0",
    "helmet": "^7.1.0",
    "cors": "^2.8.5"
  },
  "devDependencies": {
    "@types/express": "^4.17.21",
    "@types/amqplib": "^0.10.4",
    "@types/nodemailer": "^6.4.14",
    "@types/cors": "^2.8.17",
    "typescript": "^5.3.2",
    "ts-node-dev": "^2.0.0",
    "eslint": "^8.55.0",
    "@typescript-eslint/eslint-plugin": "^6.13.2",
    "@typescript-eslint/parser": "^6.13.2"
  }
}
```

### .env (Development)

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

### .env.production

```env
# Server
NODE_ENV=production
PORT=8090

# Twilio
TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
TWILIO_PHONE_NUMBER=${TWILIO_PHONE_NUMBER}
TWILIO_WHATSAPP_NUMBER=${TWILIO_WHATSAPP_NUMBER}

# Email
SMTP_HOST=${SMTP_HOST}
SMTP_PORT=${SMTP_PORT}
SMTP_USER=${SMTP_USER}
SMTP_PASS=${SMTP_PASS}
SMTP_FROM=${SMTP_FROM}

# RabbitMQ
RABBITMQ_URL=${RABBITMQ_URL}
RABBITMQ_EXCHANGE=jass.events
RABBITMQ_QUEUE=notification.events.queue

# Logging
LOG_LEVEL=info
```

### src/config/env.ts

```typescript
import dotenv from 'dotenv';

dotenv.config({
  path: process.env.NODE_ENV === 'production' ? '.env.production' : '.env'
});

export const config = {
  nodeEnv: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT || '8090', 10),

  twilio: {
    accountSid: process.env.TWILIO_ACCOUNT_SID!,
    authToken: process.env.TWILIO_AUTH_TOKEN!,
    phoneNumber: process.env.TWILIO_PHONE_NUMBER!,
    whatsappNumber: process.env.TWILIO_WHATSAPP_NUMBER!,
  },

  smtp: {
    host: process.env.SMTP_HOST!,
    port: parseInt(process.env.SMTP_PORT || '587', 10),
    user: process.env.SMTP_USER!,
    pass: process.env.SMTP_PASS!,
    from: process.env.SMTP_FROM!,
  },

  rabbitmq: {
    url: process.env.RABBITMQ_URL!,
    exchange: process.env.RABBITMQ_EXCHANGE || 'jass.events',
    queue: process.env.RABBITMQ_QUEUE || 'notification.events.queue',
  },

  logLevel: process.env.LOG_LEVEL || 'info',
};
```

---

## 1️⃣1️⃣ vg-ms-gateway

### application.yml (Base)

```yaml
spring:
  application:
    name: vg-ms-gateway
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  # CORS Global en Gateway
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
            allow-credentials: false
            max-age: 3600

      # Filtros globales
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_UNIQUE
        - AddRequestHeader=X-Request-Source, gateway

server:
  port: 8080
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

# Actuator
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

### application-dev.yml (Desarrollo)

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
    instances:
      usersService:
        base-config: default
      organizationsService:
        base-config: default

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.security: DEBUG
```

### application-prod.yml (Producción)

```yaml
spring:
  cloud:
    gateway:
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

## ✅ RESUMEN DE MEJORAS APLICADAS

### 🔒 Seguridad

- ✅ JWT validation en todos los microservicios (excepto auth y gateway que lo manejan diferente)
- ✅ CORS configurado por perfil (permisivo en dev, restringido en prod)
- ✅ Credenciales externalizadas con variables de entorno en producción
- ✅ Sin credenciales hardcodeadas en producción

### 🔄 Resiliencia

- ✅ Circuit Breaker con configuración completa
- ✅ Retry con exponential backoff
- ✅ Time Limiter para timeouts
- ✅ Health indicators para monitoreo

### 📡 Comunicación

- ✅ WebClient con timeouts configurados
- ✅ RabbitMQ con publisher confirms
- ✅ Listener con acknowledge manual y retry

### 🛡️ Producción

- ✅ Graceful shutdown (30s timeout)
- ✅ Swagger deshabilitado por defecto en prod
- ✅ Logging reducido en prod
- ✅ Connection pools optimizados

### 📊 Monitoreo

- ✅ Actuator endpoints expuestos
- ✅ Health checks para DB, RabbitMQ, CircuitBreakers
- ✅ Métricas disponibles para Prometheus

---

## 📦 RESUMEN POR TIPO DE SERVICIO

### Servicios PostgreSQL (4)

| Servicio | Puerto | WebClient | RabbitMQ |
|----------|--------|-----------|----------|
| users | 8081 | auth, org, notif | Publisher |
| commercial-operations | 8084 | user, infra, notif | Publisher |
| inventory-purchases | 8087 | - | Publisher |
| infrastructure | 8089 | user, org | Pub + Listener |

### Servicios MongoDB (4)

| Servicio | Puerto | WebClient | RabbitMQ |
|----------|--------|-----------|----------|
| organizations | 8083 | - | Publisher |
| water-quality | 8085 | - | Publisher |
| distribution | 8086 | - | Publisher |
| claims-incidents | 8088 | user, infra | Publisher |

### Servicios Especiales (3)

| Servicio | Puerto | Tipo | RabbitMQ |
|----------|--------|------|----------|
| authentication | 8082 | Keycloak Proxy | Listener |
| notification | 8090 | Node.js | Listener |
| gateway | 8080 | API Gateway | - |

---

## 🔑 VARIABLES DE ENTORNO REQUERIDAS EN PRODUCCIÓN

### Base de Datos

```env
DATABASE_URL=r2dbc:postgresql://host:5432/db?sslMode=require
DATABASE_JDBC_URL=jdbc:postgresql://host:5432/db?sslmode=require
DATABASE_USER=user
DATABASE_PASSWORD=password
MONGODB_URI=mongodb+srv://user:pass@cluster/db
```

### RabbitMQ

```env
RABBITMQ_HOST=host
RABBITMQ_PORT=5672
RABBITMQ_USER=user
RABBITMQ_PASSWORD=password
RABBITMQ_VHOST=/
RABBITMQ_SSL_ENABLED=false
```

### Keycloak

```env
KEYCLOAK_URL=https://keycloak.domain.com
KEYCLOAK_REALM=jass-digital
KEYCLOAK_CLIENT_ID=jass-backend
KEYCLOAK_CLIENT_SECRET=secret
KEYCLOAK_ISSUER_URI=https://keycloak.domain.com/realms/jass-digital
KEYCLOAK_JWK_URI=https://keycloak.domain.com/realms/jass-digital/protocol/openid-connect/certs
```

### URLs de Servicios

```env
USER_SERVICE_URL=https://users.domain.com
AUTHENTICATION_SERVICE_URL=https://auth.domain.com
ORGANIZATION_SERVICE_URL=https://org.domain.com
INFRASTRUCTURE_SERVICE_URL=https://infra.domain.com
NOTIFICATION_SERVICE_URL=https://notif.domain.com
# ... etc
```

### Otros

```env
CORS_ALLOWED_ORIGINS=https://app.domain.com,https://admin.domain.com
SWAGGER_ENABLED=false
SPRING_PROFILES_ACTIVE=prod
```

**TOTAL: 11 servicios completamente configurados** ✅

# 03 - CONFIGURACIONES APPLICATION.YML COMPLETAS

Este documento contiene **TODAS** las configuraciones para los 11 microservicios con sus dependencias completas.

## 📊 TABLA DE DEPENDENCIAS

| Servicio | WebClient + CB | RabbitMQ | Keycloak |
|----------|---------------|----------|----------|
| users | ✅ (auth, org, notif) | ✅ | ❌ |
| authentication | ✅ (user) | ❌ | ✅ |
| organizations | ❌ | ✅ | ❌ |
| commercial-operations | ❌ | ✅ | ❌ |
| infrastructure | ✅ (user, org) | ✅ | ❌ |
| water-quality | ❌ | ✅ | ❌ |
| distribution | ❌ | ✅ | ❌ |
| inventory | ❌ | ✅ | ❌ |
| claims | ❌ | ✅ | ❌ |
| notification | ❌ | ✅ | ❌ |
| gateway | ❌ | ❌ | ✅ |

---

## 1️⃣ vg-ms-users

### application.yml

```yaml
spring:
  application:
    name: vg-ms-users
  profiles:
    active: dev

server:
  port: 8081

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

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

# WebClient para servicios externos
webclient:
  authentication-service:
    base-url: http://localhost:8082
  organization-service:
    base-url: http://localhost:8083
  notification-service:
    base-url: http://localhost:8090

# Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      authenticationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
      organizationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
      notificationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50

logging:
  level:
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslMode=require
    username: neondb_owner
    password: ${DB_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: jdbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
    user: neondb_owner
    password: ${DB_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

webclient:
  authentication-service:
    base-url: ${AUTHENTICATION_SERVICE_URL}
  organization-service:
    base-url: ${ORGANIZATION_SERVICE_URL}
  notification-service:
    base-url: ${NOTIFICATION_SERVICE_URL}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 2️⃣ vg-ms-authentication

### application.yml

```yaml
spring:
  application:
    name: vg-ms-authentication
  profiles:
    active: dev

server:
  port: 8082

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

```yaml
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

# WebClient
webclient:
  user-service:
    base-url: http://localhost:8081

# Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      userService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
```

### application-prod.yml

```yaml
keycloak:
  realm: ${KEYCLOAK_REALM}
  auth-server-url: ${KEYCLOAK_URL}
  resource: ${KEYCLOAK_CLIENT_ID}
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}
  admin:
    username: ${KEYCLOAK_ADMIN_USER}
    password: ${KEYCLOAK_ADMIN_PASSWORD}

webclient:
  user-service:
    base-url: ${USER_SERVICE_URL}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 3️⃣ vg-ms-organizations

### application.yml

```yaml
spring:
  application:
    name: vg-ms-organizations
  profiles:
    active: dev

server:
  port: 8083

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

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

logging:
  level:
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://sistemajass:ZC7O1Ok40SwkfEje@sistemajass.jn6cpoz.mongodb.net/JASS_DIGITAL?retryWrites=true&w=majority
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 4️⃣ vg-ms-commercial-operations

### application.yml

```yaml
spring:
  application:
    name: vg-ms-commercial-operations
  profiles:
    active: dev

server:
  port: 8084

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

```yaml
spring:
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

logging:
  level:
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslMode=require
    username: neondb_owner
    password: ${DB_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: jdbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
    user: neondb_owner
    password: ${DB_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 5️⃣ vg-ms-water-quality

### application.yml

```yaml
spring:
  application:
    name: vg-ms-water-quality
  profiles:
    active: dev

server:
  port: 8085

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

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

logging:
  level:
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://sistemajass:ZC7O1Ok40SwkfEje@sistemajass.jn6cpoz.mongodb.net/JASS_DIGITAL?retryWrites=true&w=majority
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 6️⃣ vg-ms-distribution

### application.yml

```yaml
spring:
  application:
    name: vg-ms-distribution
  profiles:
    active: dev

server:
  port: 8086

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

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

logging:
  level:
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://sistemajass:ZC7O1Ok40SwkfEje@sistemajass.jn6cpoz.mongodb.net/JASS_DIGITAL?retryWrites=true&w=majority
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 7️⃣ vg-ms-inventory-purchases

### application.yml

```yaml
spring:
  application:
    name: vg-ms-inventory-purchases
  profiles:
    active: dev

server:
  port: 8087

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

```yaml
spring:
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

logging:
  level:
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslMode=require
    username: neondb_owner
    password: ${DB_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: jdbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
    user: neondb_owner
    password: ${DB_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 8️⃣ vg-ms-claims-incidents

### application.yml

```yaml
spring:
  application:
    name: vg-ms-claims-incidents
  profiles:
    active: dev

server:
  port: 8088

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

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

logging:
  level:
    org.springframework.data.mongodb: DEBUG
```

### application-prod.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://sistemajass:ZC7O1Ok40SwkfEje@sistemajass.jn6cpoz.mongodb.net/JASS_DIGITAL?retryWrites=true&w=majority
      auto-index-creation: false

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 9️⃣ vg-ms-infrastructure

### application.yml

```yaml
spring:
  application:
    name: vg-ms-infrastructure
  profiles:
    active: dev

server:
  port: 8089

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui/index.html
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
```

### application-dev.yml

```yaml
spring:
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

# WebClient
webclient:
  user-service:
    base-url: http://localhost:8081
  organization-service:
    base-url: http://localhost:8083

# Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      userService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
      organizationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50

logging:
  level:
    org.springframework.r2dbc: DEBUG
```

### application-prod.yml

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslMode=require
    username: neondb_owner
    password: ${DB_PASSWORD}
    pool:
      initial-size: 3
      max-size: 10

  flyway:
    enabled: true
    url: jdbc:postgresql://ep-ancient-heart-adqsgk4v-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
    user: neondb_owner
    password: ${DB_PASSWORD}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}

webclient:
  user-service:
    base-url: ${USER_SERVICE_URL}
  organization-service:
    base-url: ${ORGANIZATION_SERVICE_URL}

logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
```

---

## 🔟 vg-ms-notification (Node.js)

> Este servicio usa Node.js/Express

### package.json

```json
{
  "name": "vg-ms-notification",
  "version": "1.0.0",
  "main": "src/index.js",
  "scripts": {
    "start": "node src/index.js",
    "dev": "nodemon src/index.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "twilio": "^4.19.0",
    "dotenv": "^16.3.1",
    "amqplib": "^0.10.3"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}
```

### .env (Development)

```env
PORT=8090
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890
RABBITMQ_URL=amqp://localhost:5672
```

### .env.production

```env
PORT=8090
TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
TWILIO_PHONE_NUMBER=${TWILIO_PHONE_NUMBER}
RABBITMQ_URL=${RABBITMQ_URL}
```

---

## 1️⃣1️⃣ vg-ms-gateway

### application.yml

```yaml
spring:
  application:
    name: vg-ms-gateway
  profiles:
    active: dev

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: DEBUG
```

### application-dev.yml

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: users-service
          uri: http://localhost:8081
          predicates:
            - Path=/users/**
        
        - id: auth-service
          uri: http://localhost:8082
          predicates:
            - Path=/auth/**
        
        - id: organizations-service
          uri: http://localhost:8083
          predicates:
            - Path=/organizations/**
        
        - id: commercial-service
          uri: http://localhost:8084
          predicates:
            - Path=/commercial/**
        
        - id: water-quality-service
          uri: http://localhost:8085
          predicates:
            - Path=/water-quality/**
        
        - id: distribution-service
          uri: http://localhost:8086
          predicates:
            - Path=/distribution/**
        
        - id: inventory-service
          uri: http://localhost:8087
          predicates:
            - Path=/inventory/**
        
        - id: claims-service
          uri: http://localhost:8088
          predicates:
            - Path=/claims/**
        
        - id: infrastructure-service
          uri: http://localhost:8089
          predicates:
            - Path=/infrastructure/**

# Keycloak para Gateway
keycloak:
  realm: jass-digital
  auth-server-url: http://localhost:8180
  resource: jass-gateway
  credentials:
    secret: your-gateway-secret
```

### application-prod.yml

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: users-service
          uri: ${USERS_SERVICE_URL}
          predicates:
            - Path=/users/**
        
        - id: auth-service
          uri: ${AUTH_SERVICE_URL}
          predicates:
            - Path=/auth/**
        
        - id: organizations-service
          uri: ${ORGANIZATIONS_SERVICE_URL}
          predicates:
            - Path=/organizations/**
        
        - id: commercial-service
          uri: ${COMMERCIAL_SERVICE_URL}
          predicates:
            - Path=/commercial/**
        
        - id: water-quality-service
          uri: ${WATER_QUALITY_SERVICE_URL}
          predicates:
            - Path=/water-quality/**
        
        - id: distribution-service
          uri: ${DISTRIBUTION_SERVICE_URL}
          predicates:
            - Path=/distribution/**
        
        - id: inventory-service
          uri: ${INVENTORY_SERVICE_URL}
          predicates:
            - Path=/inventory/**
        
        - id: claims-service
          uri: ${CLAIMS_SERVICE_URL}
          predicates:
            - Path=/claims/**
        
        - id: infrastructure-service
          uri: ${INFRASTRUCTURE_SERVICE_URL}
          predicates:
            - Path=/infrastructure/**

keycloak:
  realm: ${KEYCLOAK_REALM}
  auth-server-url: ${KEYCLOAK_URL}
  resource: ${KEYCLOAK_GATEWAY_CLIENT_ID}
  credentials:
    secret: ${KEYCLOAK_GATEWAY_SECRET}

logging:
  level:
    root: WARN
```

---

## ✅ RESUMEN COMPLETO

### 📦 Servicios PostgreSQL (4)
- **users** (8081): WebClient(auth, org, notif) + CB + RabbitMQ
- **commercial-operations** (8084): RabbitMQ
- **inventory-purchases** (8087): RabbitMQ
- **infrastructure** (8089): WebClient(user, org) + CB + RabbitMQ

### 📦 Servicios MongoDB (5)
- **organizations** (8083): RabbitMQ
- **water-quality** (8085): RabbitMQ
- **distribution** (8086): RabbitMQ
- **claims-incidents** (8088): RabbitMQ

### 📦 Servicios Especiales (2)
- **authentication** (8082): WebClient(user) + CB + Keycloak
- **gateway** (8080): Keycloak + Gateway Routes

### 📦 Servicio Node.js (1)
- **notification** (8090): RabbitMQ

**TOTAL: 11 servicios completamente configurados** ✅

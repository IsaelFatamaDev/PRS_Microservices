# 09 - GUÍA DE EXCEPCIONES Y EVENTOS

## 🔥 ESTRUCTURA DE EXCEPCIONES POR MICROSERVICIO

### ✅ **Todos los microservicios Spring Boot DEBEN tener:**

```
domain/exceptions/
├── DomainException.java              → [BASE] Clase abstracta base
├── NotFoundException.java            → [404] Recurso no encontrado
├── BusinessRuleException.java        → [400] Regla de negocio violada
└── [Específicas del dominio]         → Ej: InvalidContactException
```

```
infrastructure/adapters/in/rest/
└── GlobalExceptionHandler.java       → [HANDLER] @RestControllerAdvice
```

---

## 📊 ESTADO POR MICROSERVICIO:

### 1. ✅ vg-ms-users
**Excepciones:**
```
domain/exceptions/
├── UserNotFoundException.java
├── OrganizationNotFoundException.java
└── InvalidContactException.java        → Email O phone requerido
```
**Handler:** ✅ GlobalExceptionHandler en infrastructure
**Status:** ✅ COMPLETO

---

### 2. ✅ vg-ms-authentication
**Excepciones:**
```
domain/exceptions/
├── InvalidCredentialsException.java     → Login fallido
└── KeycloakException.java               → Error comunicación Keycloak
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 3. ✅ vg-ms-organizations
**Excepciones:**
```
domain/exceptions/
├── OrganizationNotFoundException.java
├── ZoneNotFoundException.java
├── StreetNotFoundException.java
├── FareNotFoundException.java
└── DuplicateFareException.java          → Tarifa ya existe
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 4. ✅ vg-ms-commercial-operations
**Excepciones:**
```
domain/exceptions/
├── ReceiptNotFoundException.java
├── PaymentNotFoundException.java
├── DebtNotFoundException.java
├── ServiceCutNotFoundException.java
├── DuplicatePaymentException.java       → Pago duplicado
└── InsufficientBalanceException.java    → Saldo insuficiente
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 5. ✅ vg-ms-water-quality
**Excepciones:**
```
domain/exceptions/
├── TestingPointNotFoundException.java
└── QualityTestNotFoundException.java
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 6. ✅ vg-ms-distribution
**Excepciones:**
```
domain/exceptions/
├── ProgramNotFoundException.java
├── RouteNotFoundException.java
└── ScheduleConflictException.java       → Horario en conflicto
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 7. ✅ vg-ms-inventory-purchases
**Excepciones:**
```
domain/exceptions/
├── SupplierNotFoundException.java
├── MaterialNotFoundException.java
├── PurchaseNotFoundException.java
└── InsufficientStockException.java      → Stock insuficiente
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 8. ✅ vg-ms-claims-incidents
**Excepciones:**
```
domain/exceptions/
├── ComplaintNotFoundException.java
├── IncidentNotFoundException.java
├── InvalidTransitionException.java      → Estado inválido
└── UnauthorizedAssignmentException.java → Sin permisos para asignar
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

### 9. ✅ vg-ms-infrastructure
**Excepciones:**
```
domain/exceptions/
├── WaterBoxNotFoundException.java
├── AssignmentNotFoundException.java
├── WaterBoxAlreadyAssignedException.java
└── InvalidTransferException.java        → Transferencia inválida
```
**Handler:** ✅ GlobalExceptionHandler
**Status:** ✅ COMPLETO

---

## 🎯 RESUMEN DE EXCEPCIONES:

| Microservicio | Excepciones Propias | GlobalExceptionHandler | Status |
|---------------|---------------------|------------------------|--------|
| users | 3 | ✅ | ✅ |
| authentication | 2 | ✅ | ✅ |
| organizations | 5 | ✅ | ✅ |
| commercial | 6 | ✅ | ✅ |
| water-quality | 2 | ✅ | ✅ |
| distribution | 3 | ✅ | ✅ |
| inventory | 4 | ✅ | ✅ |
| claims | 4 | ✅ | ✅ |
| infrastructure | 4 | ✅ | ✅ |

**TODOS los microservicios tienen manejo completo de excepciones** ✅

---

## 📬 EVENTOS EN MICROSERVICIOS

### 🎯 TIPOS DE EVENTOS:

1. **Eventos Propios (Publisher)** - Eventos que PUBLICA el microservicio
2. **Eventos Externos (Listener)** - Eventos que CONSUME de otros servicios

---

## 📤 EVENTOS PROPIOS (Publishers)

### 1. vg-ms-users
**Publica:**
```
application/events/
├── UserCreatedEvent.java                → Cuando se crea usuario
├── UserUpdatedEvent.java                → Cuando se actualiza
└── publishers/
    └── UserEventPublisherImpl.java      → @Component
```

**RabbitMQ Exchange:** `user.events`
**Routing Keys:**
- `user.created` → Enviado a: authentication, notification
- `user.updated` → Enviado a: notification

**Implementación:**
```java
@Component
@RequiredArgsConstructor
public class UserEventPublisherImpl implements IUserEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void publishUserCreated(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .organizationId(user.getOrganizationId())
            .timestamp(LocalDateTime.now())
            .build();
        
        rabbitTemplate.convertAndSend(
            "user.events",      // exchange
            "user.created",     // routing key
            event
        );
    }
}
```

---

### 2. vg-ms-commercial-operations
**Publica:**
```
application/events/
├── PaymentCreatedEvent.java             → Pago registrado
├── ReceiptGeneratedEvent.java           → Recibos generados
├── ServiceCutScheduledEvent.java        → Corte programado
└── publishers/
    └── CommercialEventPublisher.java
```

**RabbitMQ Exchange:** `commercial.events`
**Routing Keys:**
- `payment.created` → Enviado a: notification
- `receipt.generated` → Enviado a: notification
- `service-cut.scheduled` → Enviado a: notification, infrastructure

---

### 3. vg-ms-claims-incidents
**Publica:**
```
application/events/
├── ComplaintCreatedEvent.java
├── IncidentCreatedEvent.java
└── publishers/
    └── ClaimEventPublisher.java
```

**RabbitMQ Exchange:** `claims.events`
**Routing Keys:**
- `complaint.created` → Enviado a: notification
- `incident.created` → Enviado a: notification, infrastructure

---

## 📥 EVENTOS EXTERNOS (Listeners)

### 1. vg-ms-authentication
**Escucha:**
```
infrastructure/messaging/
└── listeners/
    └── UserEventListener.java           → @Component
```

**Consume de:** `user.events`
**Routing Key:** `user.created`

**Acción:** Crear usuario en Keycloak cuando se crea en vg-ms-users

**Implementación:**
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {
    
    private final IKeycloakClient keycloakClient;
    
    @RabbitListener(queues = "authentication.user.created")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user.created event: {}", event.getUserId());
        
        keycloakClient.createUser(
            event.getEmail(),
            event.getFirstName(),
            event.getLastName()
        ).subscribe(
            success -> log.info("User created in Keycloak: {}", event.getUserId()),
            error -> log.error("Error creating user in Keycloak", error)
        );
    }
}
```

---

### 2. vg-ms-notification
**Escucha:**
```
infrastructure/messaging/
└── listeners/
    ├── UserEventListener.js
    ├── PaymentEventListener.js
    ├── ComplaintEventListener.js
    └── IncidentEventListener.js
```

**Consume múltiples eventos:**
- `user.created` → Enviar WhatsApp de bienvenida
- `payment.created` → Enviar comprobante
- `receipt.generated` → Notificar recibos disponibles
- `complaint.created` → Confirmar recepción
- `incident.created` → Notificar incidente

---

### 3. vg-ms-infrastructure
**Escucha:**
```
infrastructure/messaging/
└── listeners/
    ├── ServiceCutEventListener.java
    └── IncidentEventListener.java
```

**Consume:**
- `service-cut.scheduled` → Actualizar estado de water-box
- `incident.created` → Crear tarea de mantenimiento si aplica

---

## 📋 CONFIGURACIÓN DE RABBITMQ

### Spring Boot (application.yml):

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: 5672
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    
# Configuración de exchanges y queues
rabbitmq:
  exchanges:
    user-events: user.events
    commercial-events: commercial.events
    claims-events: claims.events
```

### Configuración de RabbitMQ:

```java
@Configuration
public class RabbitMQConfig {
    
    // Exchange para eventos de usuarios
    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange("user.events");
    }
    
    // Queue para authentication service
    @Bean
    public Queue authenticationUserQueue() {
        return new Queue("authentication.user.created");
    }
    
    // Binding
    @Bean
    public Binding authenticationUserBinding() {
        return BindingBuilder
            .bind(authenticationUserQueue())
            .to(userEventsExchange())
            .with("user.created");
    }
}
```

---

## 🎯 PATRÓN DE EVENTOS:

### ✅ Eventos Propios (Publisher):
```
application/events/
├── [Nombre]Event.java                   → Clase del evento
└── publishers/
    └── [Nombre]EventPublisher.java      → Implementa interface de ports/out
```

### ✅ Eventos Externos (Listener):
```
infrastructure/messaging/
└── listeners/
    └── [Nombre]EventListener.java       → @RabbitListener
```

---

## 📊 MATRIZ DE EVENTOS:

| Servicio | Publica | Escucha |
|----------|---------|---------|
| **users** | user.created, user.updated | - |
| **authentication** | - | user.created |
| **organizations** | - | - |
| **commercial** | payment.created, receipt.generated, service-cut.scheduled | - |
| **water-quality** | - | - |
| **distribution** | - | - |
| **inventory** | - | - |
| **claims** | complaint.created, incident.created | - |
| **infrastructure** | - | service-cut.scheduled, incident.created |
| **notification** | - | ALL (user.*, payment.*, complaint.*, incident.*) |

---

## ✅ RESUMEN:

### Excepciones:
- ✅ **Todos los servicios** tienen excepciones de dominio propias
- ✅ **Todos los servicios** tienen GlobalExceptionHandler
- ✅ Mapeo correcto a HTTP status codes

### Eventos:
- ✅ **Eventos propios** en `application/events/publishers/`
- ✅ **Eventos externos** en `infrastructure/messaging/listeners/`
- ✅ Clara diferenciación entre publicar y consumir
- ✅ RabbitMQ como broker de mensajes

**La arquitectura de excepciones y eventos está completa y bien estructurada** 🎯

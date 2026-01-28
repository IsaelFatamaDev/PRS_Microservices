# Documentación Técnica - VG-MS-NOTIFICATION

## Arquitectura Hexagonal Implementada

### Capa de Dominio (Core Business Logic)

#### Value Objects

- **NotificationChannel**: SMS, WHATSAPP, EMAIL, IN_APP
- **NotificationStatus**: PENDING, PROCESSING, SENT, DELIVERED, READ, FAILED, CANCELLED
- **NotificationType**: 17 tipos (USER_CREDENTIALS, PAYMENT_RECEIVED, INCIDENT_CREATED, etc.)
- **NotificationPriority**: URGENT (5 reintentos), HIGH (3), NORMAL (2), LOW (1)
- **TemplateStatus**: ACTIVE, INACTIVE, DRAFT

#### Aggregates

- **Notification**: Entidad principal con lógica de envío y reintentos
- **NotificationTemplate**: Plantillas con renderizado de variables
- **NotificationPreference**: Preferencias de usuario por tipo de notificación

#### Domain Events

- NotificationCreatedEvent
- NotificationSentEvent
- NotificationDeliveredEvent
- NotificationReadEvent
- NotificationFailedEvent
- TemplateCreatedEvent
- TemplateUpdatedEvent

#### Domain Exceptions

- NotificationNotFoundException
- TemplateNotFoundException
- SendNotificationException
- InvalidTemplateException

### Capa de Aplicación (Use Cases)

#### Use Cases Implementados

1. **SendNotificationUseCaseImpl**: Orquesta el envío con renderizado de templates
2. **GetNotificationUseCaseImpl**: Consultas de notificaciones
3. **MarkAsReadUseCaseImpl**: Marca notificaciones como leídas
4. **CreateTemplateUseCaseImpl**: Crea templates de notificación
5. **GetTemplateUseCaseImpl**: Consultas de templates
6. **GetPreferenceUseCaseImpl**: Consulta preferencias de usuario
7. **UpdatePreferenceUseCaseImpl**: Actualiza preferencias
8. **RetryFailedNotificationUseCaseImpl**: Reintenta notificaciones fallidas

### Capa de Infraestructura (Technical Details)

#### Persistence (MongoDB)

- **NotificationDocument**: TTL de 180 días, índices en userId/status/channel/createdAt
- **TemplateDocument**: Índice único en code, índices en channel/status
- **PreferenceDocument**: Índice único en userId

#### Adapters Out

- **WhatsAppServiceImpl**: Integración con API wrapper de whatsapp-web.js
- **SmsServiceImpl**: Integración con gateway SMS local
- **EmailServiceImpl**: SMTP con JavaMailSender
- **DomainEventPublisherImpl**: Publicación a RabbitMQ

#### Adapters In (REST)

- **NotificationRest**: CRUD de notificaciones + envío
- **TemplateRest**: CRUD de templates
- **PreferenceRest**: Gestión de preferencias
- **GlobalExceptionHandler**: Manejo centralizado de errores

#### Adapters In (Messaging)

- **UserCreatedEventListener**: Envía credenciales a nuevos usuarios
- **PaymentEventListener**: Envía recibos y recordatorios de pago

## Flujo de Envío de Notificación

### 1. Recepción de Solicitud

```
POST /api/v1/notifications/send
  ↓
NotificationRest.sendNotification()
  ↓
NotificationMapper.toDomain()
  ↓
SendNotificationUseCaseImpl.execute()
```

### 2. Preparación

```
prepareNotification()
  ↓
¿Tiene templateCode?
  Sí → getTemplateUseCase.findByCode()
       → template.renderTemplate(templateParams)
       → actualizar subject y message
  No → usar subject/message directo
```

### 3. Envío por Canal

```
sendNotification()
  ↓
switch (channel)
  SMS → smsService.sendSms()
  WHATSAPP → whatsAppService.sendMessage()
  EMAIL → emailService.sendEmail()
  IN_APP → (guardado directo)
  ↓
¿Éxito?
  Sí → notification.markAsSent()
       → eventPublisher.publish(NotificationSentEvent)
  No → notification.markAsFailed()
       → eventPublisher.publish(NotificationFailedEvent)
```

### 4. Persistencia

```
notificationRepository.save()
  ↓
NotificationDomainMapper.toDocument()
  ↓
MongoDB.insert()
```

## Sistema de Reintentos

### Lógica de Reintentos

```java
if (notification.canRetry()) {
    notification.incrementRetry();
    int delay = notification.getPriority().getRetryDelayMinutes();
    // Esperar delay minutos
    sendNotificationUseCase.execute(notification);
}
```

### Tabla de Reintentos

| Prioridad | Max Reintentos | Delay | Total Time |
|-----------|----------------|-------|------------|
| URGENT    | 5              | 1 min | 5 minutos  |
| HIGH      | 3              | 5 min | 15 minutos |
| NORMAL    | 2              | 15 min| 30 minutos |
| LOW       | 1              | 60 min| 60 minutos |

## Sistema de Templates

### Ejemplo de Template

```java
Code: "WELCOME_SMS"
Template: "Bienvenido {username}! Tu contraseña es: {password}"
Variables: ["username", "password"]

// Renderizado
Map<String, String> params = Map.of(
    "username", "jdoe",
    "password", "Temp123!"
);
String result = template.renderTemplate(params);
// Output: "Bienvenido jdoe! Tu contraseña es: Temp123!"
```

### Sintaxis de Variables

- `{variableName}`: Variable simple
- Validación: Todas las variables en template deben estar en templateParams
- Excepción: InvalidTemplateException si falta alguna variable

## Integración con Otros Microservicios

### vg-ms-users

```
Evento: user.created
  ↓
UserCreatedEventListener
  ↓
Enviar notificación EMAIL + SMS con credenciales
```

### vg-ms-payments-billing

```
Evento: payment.completed
  ↓
PaymentEventListener
  ↓
Enviar recibo por EMAIL + confirmación por SMS

Evento: payment.overdue
  ↓
PaymentEventListener
  ↓
Enviar alerta URGENTE por SMS
```

### vg-ms-claims-incidents

```
Evento: incident.created
  ↓
IncidentEventListener (TODO)
  ↓
Notificar a usuario por canal preferido
```

## Configuración de Canales

### SMS (Prioridad para Zonas Rurales)

#### Requisitos

1. Gateway SMS local (modem GSM, operador local, Android device)
2. API REST endpoint en `SMS_GATEWAY_URL`
3. Configurar `SMS_ENABLED=true`

#### Formato de Request

```json
POST {SMS_GATEWAY_URL}/send
{
  "to": "+51987654321",
  "message": "Tu mensaje aquí",
  "from": "SISTEMA_JASS"
}
```

### WhatsApp (Número Propio)

#### Requisitos

1. Instalar whatsapp-web.js en servidor Node.js
2. Escanear QR code con tu número WhatsApp
3. Exponer API REST en `WHATSAPP_API_URL`
4. Configurar `WHATSAPP_ENABLED=true`

#### Implementación Node.js

```javascript
const { Client } = require('whatsapp-web.js');
const express = require('express');
const app = express();

const client = new Client();
client.initialize();

client.on('ready', () => console.log('WhatsApp ready!'));

app.post('/send', async (req, res) => {
  const { to, message } = req.body;
  await client.sendMessage(to + '@c.us', message);
  res.json({ success: true, messageId: Date.now() });
});

app.get('/status', (req, res) => {
  res.json({ connected: client.info != null });
});

app.listen(3001);
```

### Email (SMTP)

#### Gmail App Password

1. Ir a <https://myaccount.google.com/security>
2. Activar verificación en 2 pasos
3. Crear contraseña de aplicación
4. Usar en `MAIL_PASSWORD`

#### Configuración Alternativa

- Outlook: smtp-mail.outlook.com:587
- Servidor propio: smtp.yourdomain.com:25

## MongoDB Schema

### notifications Collection

```javascript
{
  _id: ObjectId("..."),
  userId: "user123",
  channel: "SMS",
  recipient: "+51987654321",
  type: "PAYMENT_RECEIVED",
  subject: null,
  message: "Pago recibido",
  status: "SENT",
  priority: "HIGH",
  templateId: null,
  templateParams: {},
  providerName: "LOCAL_SMS_GATEWAY",
  providerId: "SMS_1705329600000",
  errorMessage: null,
  retryCount: 0,
  scheduledAt: null,
  sentAt: ISODate("2024-01-15T10:00:00Z"),
  deliveredAt: null,
  readAt: null,
  createdAt: ISODate("2024-01-15T09:59:50Z"),
  expiresAt: ISODate("2024-07-13T09:59:50Z")  // TTL 180 días
}
```

### Índices

```javascript
db.notifications.createIndex({ userId: 1, createdAt: -1 })
db.notifications.createIndex({ status: 1 })
db.notifications.createIndex({ channel: 1 })
db.notifications.createIndex({ type: 1 })
db.notifications.createIndex({ createdAt: -1 })
db.notifications.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 })
```

## RabbitMQ Topology

### Exchanges

- `notifications.exchange` (topic): Eventos publicados por este microservicio
- `users.exchange` (topic): Eventos de vg-ms-users
- `payments.exchange` (topic): Eventos de vg-ms-payments-billing

### Queues

- `user.created.queue` → Binding: `users.exchange` / `user.created`
- `payment.completed.queue` → Binding: `payments.exchange` / `payment.completed`
- `payment.overdue.queue` → Binding: `payments.exchange` / `payment.overdue`

### Routing Keys Publicados

- `notification.created`
- `notification.sent`
- `notification.delivered`
- `notification.read`
- `notification.failed`

## Principios SOLID Aplicados

### Single Responsibility Principle (SRP)

- Cada Use Case tiene una única responsabilidad
- Mappers separados para cada entidad (NotificationMapper, TemplateMapper, PreferenceMapper)
- Servicios dedicados por canal (WhatsAppService, SmsService, EmailService)

### Open/Closed Principle (OCP)

- Nuevos canales de notificación pueden agregarse sin modificar código existente
- Sistema de eventos extensible

### Liskov Substitution Principle (LSP)

- Implementaciones de INotificationRepository son intercambiables
- Servicios de canal implementan interfaces consistentes

### Interface Segregation Principle (ISP)

- Interfaces pequeñas y específicas (ISendNotificationUseCase, IMarkAsReadUseCase)
- Ports separados por responsabilidad

### Dependency Inversion Principle (DIP)

- Domain depende de interfaces (ports), no de implementaciones
- Inyección de dependencias en todos los componentes

## Patterns Implementados

1. **Hexagonal Architecture**: Separación clara entre domain, application e infrastructure
2. **Domain-Driven Design**: Aggregates, Value Objects, Domain Events
3. **Repository Pattern**: Abstracción de persistencia
4. **Factory Pattern**: Métodos createNew() en aggregates
5. **Strategy Pattern**: Selección de canal de notificación
6. **Template Method**: Renderizado de templates
7. **Observer Pattern**: Domain events y listeners

## Performance y Escalabilidad

### Reactive Stack

- Spring WebFlux para operaciones no bloqueantes
- MongoDB Reactive Driver
- Reactor RabbitMQ

### Optimizaciones

- Índices MongoDB en campos frecuentemente consultados
- TTL para limpieza automática de datos antiguos
- Connection pooling en MongoDB y RabbitMQ
- Schedulers apropiados (boundedElastic para I/O)

### Métricas

- Actuator + Prometheus para monitoreo
- Healthchecks para MongoDB, RabbitMQ y servicios externos

## Seguridad

### Validación

- Jakarta Validation en DTOs
- Validación de formato de teléfonos/emails
- Sanitización de parámetros de templates

### TODO - Mejoras de Seguridad

- [ ] Encriptación de datos sensibles en MongoDB
- [ ] JWT authentication
- [ ] Rate limiting por usuario
- [ ] Audit logging
- [ ] Secrets management (Vault)

## Testing Strategy

### Unit Tests

- Domain models: Lógica de negocio sin dependencias
- Use Cases: Mock de repositories y servicios
- Mappers: Conversiones bidireccionales

### Integration Tests

- Repositories: Embedded MongoDB
- REST Controllers: WebTestClient
- RabbitMQ Listeners: TestRabbitTemplate

### E2E Tests (TODO)

- Flujo completo de envío de notificación
- Integración con servicios reales en ambiente de staging

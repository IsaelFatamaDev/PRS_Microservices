# VG-MS-NOTIFICATION - Microservicio de Notificaciones

Microservicio de notificaciones para Sistema JASS con arquitectura hexagonal, DDD y Domain Events.

## 🎯 Características

- **Multi-canal**: SMS, WhatsApp, Email, In-App
- **Sistema de prioridades**: Urgent (5 reintentos) → High (3) → Normal (2) → Low (1)
- **Templates dinámicos**: Renderizado con variables
- **Sin proveedores cloud**: WhatsApp y SMS usando número/gateway propio
- **Eventos de dominio**: Publicación automática a RabbitMQ
- **TTL automático**: Limpieza de notificaciones antiguas (180 días)
- **Arquitectura reactiva**: Spring WebFlux + MongoDB Reactive

## 📋 Requisitos

- Java 21+
- Maven 3.9+
- MongoDB 7.0+
- RabbitMQ 3.12+
- Docker y Docker Compose (opcional)

## 🏗️ Arquitectura

```
vg-ms-notification/
├── domain/                          # Capa de Dominio (Lógica de negocio)
│   ├── events/                      # Eventos de dominio
│   ├── exceptions/                  # Excepciones de dominio
│   ├── models/                      # Agregados (Notification, NotificationTemplate, NotificationPreference)
│   ├── ports/
│   │   ├── in/                      # Puertos de entrada (Use Cases)
│   │   └── out/                     # Puertos de salida (Repositories, Services)
│   └── valueobjects/                # Value Objects (Channel, Status, Type, Priority)
├── application/                     # Capa de Aplicación
│   ├── dtos/                        # DTOs (Request/Response)
│   ├── mappers/                     # Mappers DTO ↔ Domain
│   └── usecases/                    # Implementaciones de Use Cases
└── infrastructure/                  # Capa de Infraestructura
    ├── adapters/
    │   ├── in/
    │   │   ├── rest/                # Controladores REST
    │   │   └── messaging/           # Listeners RabbitMQ
    │   ├── out/
    │   │   ├── persistence/         # Implementaciones de Repositories
    │   │   ├── whatsapp/            # Servicio WhatsApp (número propio)
    │   │   ├── sms/                 # Servicio SMS (gateway local)
    │   │   ├── email/               # Servicio Email (SMTP)
    │   │   └── messaging/           # Publicador de eventos
    │   └── config/                  # Configuraciones Spring
    └── persistence/
        ├── entities/                # MongoDB Documents
        ├── mappers/                 # Mappers Document ↔ Domain
        └── repositories/            # MongoDB Repositories
```

## 🚀 Inicio Rápido

### Opción 1: Docker Compose (Recomendado)

```bash
# Clonar repositorio
cd vg-ms-notification

# Configurar variables de entorno (crear .env)
cp .env.example .env
# Editar .env con tus credenciales

# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f notification-service
```

### Opción 2: Local

```bash
# 1. Iniciar MongoDB
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# 2. Iniciar RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  --name rabbitmq rabbitmq:3.12-management-alpine

# 3. Configurar variables de entorno
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# 4. Compilar y ejecutar
./mvnw clean package -DskipTests
java -jar target/vg-ms-notification-1.0.0.jar
```

## 📡 Endpoints REST

### Notificaciones

#### Enviar Notificación

```http
POST /api/v1/notifications/send
Content-Type: application/json

{
  "userId": "user123",
  "channel": "SMS",
  "recipient": "+51987654321",
  "type": "PAYMENT_RECEIVED",
  "subject": null,
  "message": "Pago recibido exitosamente",
  "templateCode": null,
  "templateParams": null,
  "priority": "HIGH",
  "createdBy": "SYSTEM"
}
```

#### Obtener Notificación

```http
GET /api/v1/notifications/{id}
```

#### Obtener Notificaciones de Usuario

```http
GET /api/v1/notifications/user/{userId}
```

#### Obtener Notificaciones No Leídas

```http
GET /api/v1/notifications/user/{userId}/unread
```

#### Marcar como Leída

```http
PATCH /api/v1/notifications/{id}/read
```

#### Reintentar Notificación Fallida

```http
POST /api/v1/notifications/{id}/retry
```

### Templates

#### Crear Template

```http
POST /api/v1/templates
Content-Type: application/json

{
  "code": "WELCOME_SMS",
  "name": "SMS de Bienvenida",
  "channel": "SMS",
  "subject": null,
  "template": "Bienvenido {username}! Tu contraseña temporal es: {password}",
  "variables": ["username", "password"],
  "createdBy": "ADMIN"
}
```

#### Obtener Template por Código

```http
GET /api/v1/templates/code/{code}
```

#### Listar Templates Activos

```http
GET /api/v1/templates/active
```

### Preferencias

#### Obtener Preferencias de Usuario

```http
GET /api/v1/preferences/user/{userId}
```

#### Actualizar Preferencias

```http
PUT /api/v1/preferences/user/{userId}
Content-Type: application/json

{
  "preferences": {
    "PAYMENT_RECEIVED": {
      "enabledChannels": ["SMS", "EMAIL"],
      "primaryChannel": "SMS"
    }
  },
  "phoneNumber": "+51987654321",
  "whatsappNumber": "+51987654321",
  "email": "user@example.com",
  "enableSms": true,
  "enableWhatsapp": true,
  "enableEmail": true,
  "enableInApp": true,
  "quietHoursStart": "22:00",
  "quietHoursEnd": "08:00",
  "updatedBy": "USER"
}
```

## 📬 Eventos RabbitMQ

### Eventos Consumidos

#### user.created

```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+51987654321",
  "username": "jdoe",
  "temporaryPassword": "Temp123!"
}
```

#### payment.completed

```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+51987654321",
  "receiptNumber": "REC-2024-001",
  "amount": 35.50,
  "paymentDate": "2024-01-15T10:30:00"
}
```

#### payment.overdue

```json
{
  "userId": "user123",
  "phoneNumber": "+51987654321",
  "amount": 50.00,
  "dueDate": "2024-01-10"
}
```

### Eventos Publicados

- `notification.created`
- `notification.sent`
- `notification.delivered`
- `notification.read`
- `notification.failed`

## 🔧 Configuración

### WhatsApp (Número Propio)

**IMPORTANTE**: Este microservicio NO usa Twilio. Debes configurar tu propio número WhatsApp.

#### Opción 1: whatsapp-web.js (NodeJS)

```bash
# Instalar whatsapp-web.js
npm install whatsapp-web.js qrcode-terminal

# Crear API wrapper (server.js)
const { Client } = require('whatsapp-web.js');
const express = require('express');
const app = express();

const client = new Client();
client.initialize();

client.on('qr', (qr) => {
  // Escanear QR con tu WhatsApp
  console.log('QR Code:', qr);
});

client.on('ready', () => {
  console.log('WhatsApp ready!');
});

app.post('/send', async (req, res) => {
  const { to, message } = req.body;
  await client.sendMessage(to + '@c.us', message);
  res.json({ success: true });
});

app.listen(3001);
```

#### Opción 2: WhatsApp Business API

Requiere aprobación de Facebook. Seguir guía oficial: <https://developers.facebook.com/docs/whatsapp>

### SMS Gateway Local

#### Opción 1: Modem GSM USB

```bash
# Instalar Gammu
sudo apt-get install gammu gammu-smsd

# Configurar /etc/gammu-smsdrc
[gammu]
device = /dev/ttyUSB0
connection = at

# Crear API wrapper en Python/Node.js
```

#### Opción 2: Operador Local (Perú)

Contactar a Claro, Movistar o Entel para API corporativa de envío de SMS.

#### Opción 3: Android SMS Gateway

Instalar "SMS Gateway API" app en dispositivo Android y configurar URL.

### Email (SMTP)

#### Gmail

```yaml
MAIL_HOST: smtp.gmail.com
MAIL_PORT: 587
MAIL_USERNAME: your-email@gmail.com
MAIL_PASSWORD: your-app-password  # Crear en https://myaccount.google.com/apppasswords
```

#### Outlook

```yaml
MAIL_HOST: smtp-mail.outlook.com
MAIL_PORT: 587
```

#### Servidor Propio

```yaml
MAIL_HOST: smtp.yourdomain.com
MAIL_PORT: 25
```

## 📊 Canales y Prioridades

### Canales Disponibles

1. **SMS**: Prioridad #1 para zonas rurales sin internet
2. **WhatsApp**: Prioridad #2 cuando hay conexión
3. **Email**: Prioridad #3 para notificaciones detalladas
4. **In-App**: Prioridad #4 para notificaciones dentro de la aplicación

### Niveles de Prioridad

| Prioridad | Reintentos | Delay entre Reintentos | Uso |
|-----------|------------|------------------------|-----|
| URGENT    | 5          | 1 minuto               | Pagos vencidos, alertas críticas |
| HIGH      | 3          | 5 minutos              | Credenciales, recibos |
| NORMAL    | 2          | 15 minutos             | Notificaciones generales |
| LOW       | 1          | 60 minutos             | Recordatorios informativos |

## 🧪 Testing

```bash
# Tests unitarios
./mvnw test

# Tests de integración
./mvnw verify

# Cobertura
./mvnw jacoco:report
```

## 📈 Monitoreo

### Actuator Endpoints

- Health: <http://localhost:8089/actuator/health>
- Metrics: <http://localhost:8089/actuator/metrics>
- Prometheus: <http://localhost:8089/actuator/prometheus>

### Logs

```bash
# Ver logs en tiempo real
tail -f logs/vg-ms-notification.log

# Docker
docker-compose logs -f notification-service
```

## 🔐 Seguridad

- Validación de entrada en todos los endpoints
- Rate limiting (TODO: implementar con Redis)
- Autenticación JWT (TODO: integrar con vg-ms-authentication)
- Encriptación de datos sensibles en MongoDB (TODO)

## 🚧 TODO

- [ ] Integrar autenticación JWT
- [ ] Implementar rate limiting
- [ ] Agregar soporte para archivos adjuntos en emails
- [ ] Implementar notificaciones push móviles
- [ ] Crear dashboard de administración
- [ ] Agregar métricas avanzadas con Grafana
- [ ] Implementar circuit breaker para servicios externos

## 📄 Licencia

Propiedad de Valle Grande - Sistema JASS

## 👥 Contacto

Equipo de Desarrollo - Sistema JASS

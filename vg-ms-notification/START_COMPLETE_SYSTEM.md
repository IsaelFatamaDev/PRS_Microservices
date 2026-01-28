# 🚀 Guía de Inicio Completo - Sistema de Notificaciones

## 📋 Índice

1. [Requisitos](#requisitos)
2. [Opción 1: Desarrollo Local (Sin WhatsApp Real)](#opción-1-desarrollo-local-sin-whatsapp-real)
3. [Opción 2: Desarrollo con WhatsApp Real](#opción-2-desarrollo-con-whatsapp-real)
4. [Opción 3: Docker Completo](#opción-3-docker-completo)
5. [Verificación del Sistema](#verificación-del-sistema)

---

## 📦 Requisitos

### Para Microservicio Java (vg-ms-notification)

- ✅ Java 21+
- ✅ Maven 3.9+
- ✅ MongoDB 7.0+
- ✅ RabbitMQ 3.12+

### Para WhatsApp Gateway (Opcional)

- ✅ Node.js 16+
- ✅ npm o yarn

---

## Opción 1: Desarrollo Local (Sin WhatsApp Real)

### 🟢 Ideal para: Desarrollo, pruebas de lógica de negocio

```bash
# 1. Iniciar dependencias
docker run -d -p 27017:27017 --name mongodb mongo:7.0
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq rabbitmq:3.12-management-alpine

# 2. Compilar microservicio Java
cd vg-ms-notification
.\build.bat

# 3. Ejecutar con perfil dev (servicios externos deshabilitados)
java -jar target\vg-ms-notification-1.0.0.jar --spring.profiles.active=dev
```

✅ **Qué funciona:**

- ✅ API REST completa
- ✅ MongoDB
- ✅ RabbitMQ
- ✅ Templates
- ✅ Preferencias
- ✅ IDs simulados para WhatsApp/SMS/Email

❌ **Qué NO funciona:**

- ❌ Envíos reales de WhatsApp
- ❌ Envíos reales de SMS
- ❌ Envíos reales de Email

---

## Opción 2: Desarrollo con WhatsApp Real

### 🟢 Ideal para: Pruebas end-to-end, demo con cliente

### Paso 1: Iniciar Dependencias

```bash
# Terminal 1: MongoDB
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# Terminal 2: RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq rabbitmq:3.12-management-alpine
```

### Paso 2: Iniciar WhatsApp Gateway

```bash
# Terminal 3: WhatsApp Gateway
cd whatsapp-gateway
npm install
npm start
```

**IMPORTANTE:** Verás un QR code en la terminal. Escanéalo:

```
========================================
📱 ESCANEA ESTE QR CON TU WHATSAPP:
========================================

█▀▀▀▀▀█ ▀▄  █ ▀ █ █▀▀▀▀▀█
█ ███ █ ▀█▄▄ ▀██  █ ███ █
...

Instrucciones:
1. Abre WhatsApp en tu teléfono
2. Ve a Ajustes > Dispositivos vinculados
3. Toca "Vincular un dispositivo"
4. Escanea el QR de arriba
```

Una vez conectado verás:

```
========================================
🟢 WHATSAPP GATEWAY LISTO
========================================
📱 Número conectado: 51987654321
👤 Nombre: Tu Nombre
🌐 API disponible en: http://localhost:3001
========================================
```

### Paso 3: Actualizar Configuración Java

Edita `application-dev.yml`:

```yaml
whatsapp:
  api:
    url: http://localhost:3001
    enabled: true  # ⬅️ CAMBIAR A true
```

### Paso 4: Iniciar Microservicio Java

```bash
# Terminal 4: Microservicio Java
cd vg-ms-notification
java -jar target\vg-ms-notification-1.0.0.jar --spring.profiles.active=dev
```

### Paso 5: ¡Probar

```bash
# Terminal 5: Enviar WhatsApp REAL
curl -X POST http://localhost:8089/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "channel": "WHATSAPP",
    "recipient": "51999888777",
    "type": "PAYMENT_RECEIVED",
    "message": "Tu pago de S/ 35.50 fue recibido correctamente. Gracias!",
    "priority": "HIGH",
    "createdBy": "SYSTEM"
  }'
```

✅ **El mensaje se envía REALMENTE por WhatsApp** ✅

---

## Opción 3: Docker Completo

### 🟢 Ideal para: Producción, deploy en servidor

### Paso 1: Construir Todo

```bash
# Construir microservicio Java
cd vg-ms-notification
.\build.bat
docker build -t vg-ms-notification:latest .

# Construir WhatsApp Gateway
cd whatsapp-gateway
docker build -t whatsapp-gateway:latest .
```

### Paso 2: Levantar Sistema Completo

Edita `docker-compose.yml` principal:

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:7.0
    container_name: notification-mongodb
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - notification-network

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: notification-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - notification-network

  whatsapp-gateway:
    image: whatsapp-gateway:latest
    container_name: whatsapp-gateway
    ports:
      - "3001:3001"
    volumes:
      - whatsapp_session:/app/.wwebjs_auth
    networks:
      - notification-network

  notification-service:
    image: vg-ms-notification:latest
    container_name: vg-ms-notification
    ports:
      - "8089:8089"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MONGODB_URI: mongodb://mongodb:27017/notification_db
      RABBITMQ_HOST: rabbitmq
      WHATSAPP_API_URL: http://whatsapp-gateway:3001
      WHATSAPP_ENABLED: "true"
    depends_on:
      - mongodb
      - rabbitmq
      - whatsapp-gateway
    networks:
      - notification-network

volumes:
  mongodb_data:
  rabbitmq_data:
  whatsapp_session:

networks:
  notification-network:
    driver: bridge
```

### Paso 3: Iniciar

```bash
docker-compose up -d
```

### Paso 4: Escanear QR

```bash
# Ver logs del WhatsApp Gateway
docker logs whatsapp-gateway
```

Verás el QR, escanéalo con tu WhatsApp.

---

## 🔍 Verificación del Sistema

### 1. Verificar MongoDB

```bash
# Local
mongosh
use notification_db_dev
db.notifications.find().pretty()

# Docker
docker exec -it notification-mongodb mongosh
```

### 2. Verificar RabbitMQ

Abre: <http://localhost:15672>

- Usuario: `guest` / `guest` (local)
- Usuario: `admin` / `admin123` (docker)

### 3. Verificar WhatsApp Gateway

```bash
curl http://localhost:3001/status
```

Respuesta esperada:

```json
{
  "connected": true,
  "info": {
    "wid": { "user": "51987654321" }
  }
}
```

### 4. Verificar Microservicio Java

```bash
curl http://localhost:8089/actuator/health
```

Respuesta esperada:

```json
{
  "status": "UP"
}
```

### 5. Prueba End-to-End

```bash
# Crear template
curl -X POST http://localhost:8089/api/v1/templates \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PAYMENT_RECEIVED",
    "name": "Confirmación de Pago",
    "channel": "WHATSAPP",
    "template": "Hola {name}, recibimos tu pago de S/ {amount}. Gracias!",
    "variables": ["name", "amount"],
    "createdBy": "ADMIN"
  }'

# Enviar notificación usando template
curl -X POST http://localhost:8089/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "channel": "WHATSAPP",
    "recipient": "TU_NUMERO_AQUI",
    "type": "PAYMENT_RECEIVED",
    "templateCode": "PAYMENT_RECEIVED",
    "templateParams": {
      "name": "Juan",
      "amount": "35.50"
    },
    "priority": "HIGH",
    "createdBy": "SYSTEM"
  }'
```

**Resultado:** Deberías recibir un WhatsApp: _"Hola Juan, recibimos tu pago de S/ 35.50. Gracias!"_

---

## 🎯 Resumen de Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| Microservicio Java | 8089 | <http://localhost:8089> |
| WhatsApp Gateway | 3001 | <http://localhost:3001> |
| MongoDB | 27017 | mongodb://localhost:27017 |
| RabbitMQ | 5672 | amqp://localhost:5672 |
| RabbitMQ Management | 15672 | <http://localhost:15672> |

---

## ❓ FAQ

### ¿Necesito WhatsApp Gateway para probar?

**No.** El microservicio Java funciona solo, pero los mensajes serán simulados.

### ¿El QR se genera cada vez?

**No.** Una vez conectado, la sesión se guarda en `.wwebjs_auth/` o en volumen Docker.

### ¿Puedo usar mi número personal de WhatsApp?

**Sí**, pero recomendamos un número separado para el sistema.

### ¿Qué pasa si WhatsApp se desconecta?

El gateway detecta la desconexión y genera un nuevo QR automáticamente.

### ¿Cómo agrego SMS y Email?

- **SMS**: Configura gateway en `application.yml`, cambia `SMS_ENABLED=true`
- **Email**: Agrega credenciales SMTP en `application.yml`, cambia `spring.mail.enabled=true`

---

## 🆘 Troubleshooting

### Error: "MongoDB connection refused"

```bash
docker ps  # Verifica que MongoDB esté corriendo
docker logs mongodb
```

### Error: "WhatsApp not connected"

```bash
cd whatsapp-gateway
npm start
# Escanea el QR nuevamente
```

### Error: "Port 8089 already in use"

```bash
# Windows
netstat -ano | findstr :8089
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8089 | xargs kill -9
```

---

**¡Sistema Completo Listo para Usar!** 🎉

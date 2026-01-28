# 📱 WhatsApp Gateway - Servicio Node.js

Gateway API REST para enviar mensajes de WhatsApp usando tu **número propio** (sin Twilio).

## 🚀 Instalación y Uso

### 1. Instalar Dependencias

```bash
cd whatsapp-gateway
npm install
```

### 2. Iniciar el Gateway

```bash
npm start
```

### 3. Escanear QR Code

Cuando ejecutes el servidor, verás un QR code en la terminal:

```
========================================
📱 ESCANEA ESTE QR CON TU WHATSAPP:
========================================

█▀▀▀▀▀█ ▀▄  █ ▀ █ █▀▀▀▀▀█
█ ███ █ ▀█▄▄ ▀██  █ ███ █
█ ▀▀▀ █  █▀█▀ ▀▄█ █ ▀▀▀ █
▀▀▀▀▀▀▀ █ █ ▀ ▀ █ ▀▀▀▀▀▀▀
...

Instrucciones:
1. Abre WhatsApp en tu teléfono
2. Ve a Ajustes > Dispositivos vinculados
3. Toca "Vincular un dispositivo"
4. Escanea el QR de arriba
```

### 4. ¡Listo

Una vez escaneado, verás:

```
========================================
🟢 WHATSAPP GATEWAY LISTO
========================================
📱 Número conectado: 51987654321
👤 Nombre: Tu Nombre
🌐 API disponible en: http://localhost:3001
========================================
```

**¡Tu sesión queda guardada!** No necesitas escanear el QR cada vez.

## 📡 Endpoints API

### GET /status

Verificar estado de conexión

```bash
curl http://localhost:3001/status
```

**Respuesta:**

```json
{
  "connected": true,
  "info": {
    "wid": { "user": "51987654321" },
    "pushname": "Tu Nombre"
  },
  "qrAvailable": false,
  "timestamp": "2026-01-25T10:30:00.000Z"
}
```

### POST /send

Enviar mensaje individual

```bash
curl -X POST http://localhost:3001/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "51999888777",
    "message": "Hola! Este es un mensaje de prueba"
  }'
```

**Respuesta:**

```json
{
  "success": true,
  "messageId": "3EB0ABCDEF123456",
  "timestamp": 1706177400,
  "to": "51999888777@c.us",
  "message": "Mensaje enviado correctamente"
}
```

### POST /send-bulk

Enviar mensajes masivos

```bash
curl -X POST http://localhost:3001/send-bulk \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      { "to": "51999111222", "message": "Mensaje 1" },
      { "to": "51999333444", "message": "Mensaje 2" }
    ]
  }'
```

### POST /logout

Cerrar sesión

```bash
curl -X POST http://localhost:3001/logout
```

## 🔧 Configuración

### Variables de Entorno

```bash
# Puerto (opcional, default: 3001)
PORT=3001
```

### Archivos Generados

Al conectar, se crea una carpeta `.wwebjs_auth/` que guarda tu sesión:

```
whatsapp-gateway/
├── .wwebjs_auth/              # ⬅️ Sesión guardada (no subir a git)
│   └── session-whatsapp-gateway-sistema-jass/
├── node_modules/
├── server.js
├── package.json
└── README.md
```

**IMPORTANTE**: Agrega `.wwebjs_auth/` al `.gitignore`

## 🔄 Integración con vg-ms-notification

El microservicio Java ya está configurado para llamar a este gateway:

**En Java (WhatsAppServiceImpl.java):**

```java
// Llamada HTTP al gateway
POST http://localhost:3001/send
Body: { "to": "+51999888777", "message": "Tu mensaje" }
```

**Variables de entorno en Java:**

```yaml
whatsapp:
  api:
    url: http://localhost:3001
    enabled: true  # ⬅️ Activar cuando el gateway esté corriendo
```

## 🐳 Docker (Opcional)

### Dockerfile

```dockerfile
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install --production

# Instalar Chromium para Puppeteer
RUN apk add --no-cache \
    chromium \
    nss \
    freetype \
    harfbuzz \
    ca-certificates \
    ttf-freefont

ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser

COPY . .

EXPOSE 3001

CMD ["npm", "start"]
```

### Ejecutar con Docker

```bash
docker build -t whatsapp-gateway .
docker run -d -p 3001:3001 -v $(pwd)/.wwebjs_auth:/app/.wwebjs_auth whatsapp-gateway
```

## 🧪 Pruebas

### 1. Verificar que está corriendo

```bash
curl http://localhost:3001
```

### 2. Verificar conexión

```bash
curl http://localhost:3001/status
```

### 3. Enviar mensaje de prueba

```bash
curl -X POST http://localhost:3001/send \
  -H "Content-Type: application/json" \
  -d '{"to":"TU_NUMERO","message":"Prueba desde API"}'
```

## ❓ FAQ

### ¿Por qué usar Node.js y no Java?

WhatsApp Web usa Puppeteer (controla navegador Chrome) que solo está bien soportado en Node.js. Es más fácil y confiable.

### ¿Necesito escanear el QR cada vez?

No. La sesión se guarda en `.wwebjs_auth/`. Solo escaneas una vez.

### ¿Puedo usar múltiples números?

Sí, ejecuta varias instancias con diferentes `clientId` y puertos.

### ¿Qué pasa si WhatsApp desconecta?

El gateway detecta la desconexión y genera un nuevo QR automáticamente.

### ¿Es seguro?

La sesión se guarda localmente encriptada. WhatsApp detecta el dispositivo vinculado.

### ¿Tiene límites de mensajes?

WhatsApp tiene límites anti-spam (~20-30 msg/min). El gateway incluye delay de 1 segundo entre mensajes.

## 🔒 Seguridad

- [ ] No expongas el gateway a internet directamente
- [ ] Usa firewall para permitir solo tráfico del microservicio Java
- [ ] Considera agregar autenticación (API Key, JWT)
- [ ] Backupea `.wwebjs_auth/` regularmente

## 🛠️ Troubleshooting

### Error: "Cannot find module 'whatsapp-web.js'"

```bash
npm install
```

### Error: "Puppeteer no puede iniciar Chrome"

```bash
# Windows: Instala Chrome
# Linux:
sudo apt-get install -y chromium-browser
```

### Error: "QR Code expira"

El QR dura 60 segundos. Simplemente espera, se genera uno nuevo automáticamente.

### Error: "WhatsApp disconnected"

Revisa si WhatsApp está cerrado en el teléfono o si eliminaste el dispositivo vinculado.

## 📞 Soporte

Si tienes problemas, verifica:

1. Node.js >= 16 instalado
2. WhatsApp funcionando en el teléfono
3. Puerto 3001 disponible
4. Firewall no bloquea el puerto

## 📄 Licencia

Uso interno - Sistema JASS

const express = require('express');
const { Client, LocalAuth } = require('whatsapp-web.js');
const qrcode = require('qrcode-terminal');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Estado de la conexión
let isReady = false;
let qrCodeString = null;
let clientInfo = null;

// Crear cliente WhatsApp con autenticación local (guarda sesión)
const client = new Client({
     authStrategy: new LocalAuth({
          clientId: "whatsapp-gateway-sistema-jass"
     }),
     puppeteer: {
          headless: true,
          args: [
               '--no-sandbox',
               '--disable-setuid-sandbox',
               '--disable-dev-shm-usage',
               '--disable-accelerated-2d-canvas',
               '--no-first-run',
               '--no-zygote',
               '--disable-gpu'
          ]
     }
});

// Evento: Generar QR Code
client.on('qr', (qr) => {
     console.log('\n========================================');
     console.log('📱 ESCANEA ESTE QR CON TU WHATSAPP:');
     console.log('========================================\n');
     qrcode.generate(qr, { small: true });
     console.log('\nInstrucciones:');
     console.log('1. Abre WhatsApp en tu teléfono');
     console.log('2. Ve a Ajustes > Dispositivos vinculados');
     console.log('3. Toca "Vincular un dispositivo"');
     console.log('4. Escanea el QR de arriba\n');

     qrCodeString = qr;
     isReady = false;
});

// Evento: Cliente autenticado
client.on('authenticated', () => {
     console.log('✅ WhatsApp autenticado correctamente');
     console.log('💾 Sesión guardada localmente');
});

// Evento: Cliente listo
client.on('ready', () => {
     isReady = true;
     clientInfo = client.info;
     console.log('\n========================================');
     console.log('🟢 WHATSAPP GATEWAY LISTO');
     console.log('========================================');
     console.log(`📱 Número conectado: ${clientInfo.wid.user}`);
     console.log(`👤 Nombre: ${clientInfo.pushname}`);
     console.log(`🌐 API disponible en: http://localhost:${PORT}`);
     console.log('========================================\n');
});

// Evento: Desconexión
client.on('disconnected', (reason) => {
     console.log('❌ WhatsApp desconectado:', reason);
     isReady = false;
     qrCodeString = null;
     clientInfo = null;
});

// Evento: Error de autenticación
client.on('auth_failure', (msg) => {
     console.error('❌ Error de autenticación:', msg);
     isReady = false;
});

// Inicializar cliente
console.log('🚀 Iniciando WhatsApp Gateway...');
console.log('⏳ Espera el QR Code para escanear...\n');
client.initialize();

// ==================== ENDPOINTS API ====================

// GET /status - Estado de la conexión
app.get('/status', (req, res) => {
     res.json({
          connected: isReady,
          info: clientInfo,
          qrAvailable: qrCodeString !== null,
          timestamp: new Date().toISOString()
     });
});

// GET /qr - Obtener QR Code (si aún no está conectado)
app.get('/qr', (req, res) => {
     if (isReady) {
          return res.status(400).json({
               error: 'Ya estás conectado. No se necesita QR.',
               info: clientInfo
          });
     }

     if (!qrCodeString) {
          return res.status(404).json({
               error: 'QR Code aún no generado. Espera unos segundos.'
          });
     }

     res.json({
          qr: qrCodeString,
          message: 'Escanea este QR con WhatsApp'
     });
});

// POST /send - Enviar mensaje
app.post('/send', async (req, res) => {
     if (!isReady) {
          return res.status(503).json({
               error: 'WhatsApp no está conectado',
               message: 'Escanea el QR code primero'
          });
     }

     const { to, message } = req.body;

     if (!to || !message) {
          return res.status(400).json({
               error: 'Faltan parámetros requeridos: to, message'
          });
     }

     try {
          // Formatear número (agregar @c.us si no lo tiene)
          const chatId = to.includes('@') ? to : `${to.replace(/[^\d]/g, '')}@c.us`;

          // Enviar mensaje
          const result = await client.sendMessage(chatId, message);

          console.log(`✅ Mensaje enviado a ${to}`);

          res.json({
               success: true,
               messageId: result.id.id,
               timestamp: result.timestamp,
               to: chatId,
               message: 'Mensaje enviado correctamente'
          });
     } catch (error) {
          console.error('❌ Error al enviar mensaje:', error);
          res.status(500).json({
               error: 'Error al enviar mensaje',
               details: error.message
          });
     }
});

// POST /send-bulk - Enviar mensajes masivos
app.post('/send-bulk', async (req, res) => {
     if (!isReady) {
          return res.status(503).json({
               error: 'WhatsApp no está conectado'
          });
     }

     const { messages } = req.body;

     if (!Array.isArray(messages)) {
          return res.status(400).json({
               error: 'Se requiere un array de mensajes: [{ to, message }]'
          });
     }

     const results = [];

     for (const msg of messages) {
          try {
               const chatId = msg.to.includes('@') ? msg.to : `${msg.to.replace(/[^\d]/g, '')}@c.us`;
               const result = await client.sendMessage(chatId, msg.message);

               results.push({
                    to: msg.to,
                    success: true,
                    messageId: result.id.id
               });

               // Delay entre mensajes para evitar spam
               await new Promise(resolve => setTimeout(resolve, 1000));
          } catch (error) {
               results.push({
                    to: msg.to,
                    success: false,
                    error: error.message
               });
          }
     }

     res.json({
          success: true,
          total: messages.length,
          results: results
     });
});

// POST /logout - Cerrar sesión
app.post('/logout', async (req, res) => {
     try {
          await client.logout();
          isReady = false;
          qrCodeString = null;
          clientInfo = null;

          res.json({
               success: true,
               message: 'Sesión cerrada correctamente'
          });
     } catch (error) {
          res.status(500).json({
               error: 'Error al cerrar sesión',
               details: error.message
          });
     }
});

// GET / - Info de la API
app.get('/', (req, res) => {
     res.json({
          name: 'WhatsApp Gateway API',
          version: '1.0.0',
          connected: isReady,
          endpoints: {
               'GET /status': 'Estado de la conexión',
               'GET /qr': 'Obtener QR Code',
               'POST /send': 'Enviar mensaje (body: { to, message })',
               'POST /send-bulk': 'Enviar mensajes masivos (body: { messages: [{to, message}] })',
               'POST /logout': 'Cerrar sesión'
          }
     });
});

// Iniciar servidor
app.listen(PORT, () => {
     console.log(`\n🌐 API REST escuchando en http://localhost:${PORT}`);
     console.log(`📖 Documentación: http://localhost:${PORT}\n`);
});

// Manejo de señales de terminación
process.on('SIGINT', async () => {
     console.log('\n⏹️  Cerrando WhatsApp Gateway...');
     await client.destroy();
     process.exit(0);
});

# 🏗️ ARQUITECTURA HEXAGONAL REACTIVA - SISTEMA JASS

## 📅 Fecha: 21 Enero 2026

## 🎯 Sistema: JASS (Juntas Administradoras de Servicios de Saneamiento)

---

## 🚀 STACK TECNOLÓGICO

```
┌─────────────────────────────────────────────────────────────────┐
│                    STACK REACTIVO COMPLETO                       │
├─────────────────────────────────────────────────────────────────┤
│ 🌐 Framework Web       → Spring WebFlux (Mono/Flux)            │
│ 🗄️  PostgreSQL         → R2DBC (Reactive Relational)          │
│ 🍃 MongoDB             → Spring Data MongoDB Reactive           │
│ 🐰 Message Broker      → RabbitMQ + Reactor RabbitMQ           │
│ 🔗 REST Client         → WebClient (Reactive HTTP)              │
│ 🛡️  Resilience         → Resilience4j (Circuit Breaker)        │
│ 🔐 Seguridad           → Gateway JWT + Microservices Auth      │
│ 🐳 Deployment          → Docker Compose + Red Privada          │
│ 📦 Paquete Base        → pe.edu.vallegrande.vgms{servicio}     │
│ 🏛️  Arquitectura       → Hexagonal + DDD + Clean Code          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📚 TABLA DE CONTENIDOS

1. [Microservicios del Sistema](#microservicios)
2. [Arquitectura de Seguridad](#seguridad)
3. [Convenciones de Nomenclatura](#convenciones)
4. [Arquitectura Hexagonal](#arquitectura-hexagonal)
5. [Comunicación entre Servicios](#comunicacion)
6. [Estructura vg-ms-users](#estructura-users)
7. [Estructura vg-ms-authentication](#estructura-authentication)
8. [Estructura vg-ms-organizations](#estructura-organizations)
9. [Configuración Application.yml](#configuracion)
10. [Código Completo para Copiar](#codigo-completo)
11. [Docker Compose](#docker-compose)
12. [Scripts de Migración](#migraciones)

---

## 🎯 MICROSERVICIOS DEL SISTEMA {#microservicios}

```
┌────────────────────────────────┬────────┬─────────────────────────────────┬──────────────────────────────────┐
│ MICROSERVICIO                  │ PUERTO │ BASE DE DATOS                   │ RESPONSABILIDAD                  │
├────────────────────────────────┼────────┼─────────────────────────────────┼──────────────────────────────────┤
│ vg-ms-gateway                  │  8080  │ -                               │ API Gateway + JWT Validation     │
│ vg-ms-authentication           │  8090  │ Keycloak                        │ Login, JWT tokens, passwords     │
│ vg-ms-users                    │  8081  │ PostgreSQL (vg_users)           │ Usuarios, perfiles, datos        │
│ vg-ms-organizations            │  8082  │ MongoDB (JASS_DIGITAL)          │ Organizaciones, Zonas, Calles    │
│ vg-ms-payments-billing         │  8083  │ PostgreSQL (vg_payments)        │ Pagos, recibos, facturación      │
│ vg-ms-water-quality            │  8084  │ MongoDB (JASS_DIGITAL)          │ Análisis de calidad del agua     │
│ vg-ms-inventory-purchases      │  8085  │ PostgreSQL (vg_inventory)       │ Inventario, compras, materiales  │
│ vg-ms-claims-incidents         │  8086  │ MongoDB (JASS_DIGITAL)          │ Reclamos e incidentes            │
│ vg-ms-distribution             │  8087  │ MongoDB (JASS_DIGITAL)          │ Programas de distribución agua   │
│ vg-ms-infrastructure           │  8088  │ PostgreSQL (vg_infrastructure)  │ Cajas de agua (WaterBox)         │
│ vg-ms-notification             │  8089  │ -                               │ WhatsApp notifications (Twilio)  │
└────────────────────────────────┴────────┴─────────────────────────────────┴──────────────────────────────────┘
```

**Paquetería estándar:**

- `pe.edu.vallegrande.vgmsauthentication`
- `pe.edu.vallegrande.vgmsusers`
- `pe.edu.vallegrande.vgmsorganizations`
- `pe.edu.vallegrande.vgmspayments`
- ... etc.

---

## � RELACIONES ENTRE ENTIDADES - MODELO DE DATOS COMPLETO {#relaciones}

### 📊 Diagrama de Relaciones

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    RELACIONES ENTRE MICROSERVICIOS                                      │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  ┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐                   │
│  │   USUARIO    │────▶│   ORGANIZATION   │────▶│   ZONE / STREET  │                   │
│  │  (vg-ms-     │     │  (vg-ms-         │     │  (vg-ms-         │                   │
│  │   users)     │     │   organizations) │     │   organizations) │                   │
│  └──────────────┘     └──────────────────┘     └──────────────────┘                   │
│         │                                                │                              │
│         │ userId                                         │ zoneId                       │
│         │                                                │                              │
│         ▼                                                ▼                              │
│  ┌──────────────────┐                          ┌──────────────────┐                   │
│  │  WATERBOX        │◀────────────────────────│  DISTRIBUTION    │                   │
│  │  ASSIGNMENT      │  waterBoxId              │  PROGRAM         │                   │
│  │  (vg-ms-         │                          │  (vg-ms-         │                   │
│  │   infrastructure)│                          │   distribution)  │                   │
│  └──────────────────┘                          └──────────────────┘                   │
│         │                                                                              │
│         │ waterBoxId                                                                   │
│         │                                                                              │
│         ▼                                                                              │
│  ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐             │
│  │   WATER BOX      │◀───│    PAYMENT       │     │  QUALITY TEST    │             │
│  │  (vg-ms-         │    │  (vg-ms-         │     │  (vg-ms-         │             │
│  │   infrastructure)│    │   payments)      │     │   water-quality) │             │
│  └──────────────────┘    └──────────────────┘     └──────────────────┘             │
│         │                         │                         │                         │
│         │                         │ userId                  │ testedByUserId          │
│         │                         │ waterBoxId              │ (OPERARIO)              │
│         │                         ▼                         ▼                         │
│         │                  ┌──────────────────┐     ┌──────────────────┐             │
│         │                  │  PAYMENT DETAIL  │     │  COMPLAINT       │             │
│         │                  │  (Desglose)      │     │  (vg-ms-claims)  │             │
│         │                  └──────────────────┘     └──────────────────┘             │
│         │                                                    │                         │
│         │                                                    │ reportedByUserId        │
│         │                                                    │ assignedToUserId        │
│         │                                                    │ (TÉCNICO)               │
│         │                                                    ▼                         │
│         └───────────────────────────────────────────────  USUARIO                     │
│                                                            (CLIENT/ADMIN)              │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### 🔍 Pregunta 1: ¿Cómo sé qué USUARIO (CLIENT) tiene qué CAJA DE AGUA?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ RELACIÓN: Usuario → WaterBox (A través de Assignment)                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ CONSULTA EN vg-ms-infrastructure:                                      │
│                                                                             │
│     GET /api/water-boxes/assignments/by-user/{userId}                      │
│                                                                             │
│     Query SQL:                                                              │
│     SELECT wa.*, wb.box_code, wb.box_type                                  │
│     FROM water_box_assignments wa                                           │
│     JOIN water_boxes wb ON wa.water_box_id = wb.id                         │
│     WHERE wa.user_id = '7f3e4d2a-...'                                       │
│       AND wa.status = 'ACTIVE'                                              │
│       AND wa.end_date IS NULL;                                              │
│                                                                             │
│  2️⃣ ESTRUCTURA DE DATOS:                                                    │
│                                                                             │
│     WaterBoxAssignment {                                                    │
│         id: 1,                                                              │
│         waterBoxId: 5,              ← ID de la caja                        │
│         userId: "7f3e4d2a-...",     ← Usuario CLIENT                       │
│         startDate: "2024-01-15",                                            │
│         endDate: null,              ← null = asignación ACTIVA             │
│         monthlyFee: 15.00,                                                  │
│         status: "ACTIVE",                                                   │
│         transferId: null            ← null = asignación original           │
│     }                                                                       │
│                                                                             │
│     WaterBox {                                                              │
│         id: 5,                                                              │
│         organizationId: "org-123",                                          │
│         boxCode: "BOX-001",         ← Código visible al usuario            │
│         boxType: "RESIDENTIAL",                                             │
│         installationDate: "2024-01-10",                                     │
│         currentAssignmentId: 1,     ← Apunta al Assignment actual          │
│         status: "ACTIVE"                                                    │
│     }                                                                       │
│                                                                             │
│  3️⃣ RESPUESTA API:                                                          │
│                                                                             │
│     {                                                                       │
│       "success": true,                                                      │
│       "data": [                                                             │
│         {                                                                   │
│           "assignmentId": 1,                                                │
│           "waterBoxCode": "BOX-001",                                        │
│           "boxType": "RESIDENTIAL",                                         │
│           "monthlyFee": 15.00,                                              │
│           "assignedSince": "2024-01-15T10:30:00",                           │
│           "isActive": true                                                  │
│         }                                                                   │
│       ]                                                                     │
│     }                                                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 💰 Pregunta 2: ¿Qué PAGOS hizo ese usuario? ¿Cómo se relacionan con la CAJA?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ RELACIÓN: Usuario → Pagos → WaterBox                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ CONSULTA EN vg-ms-payments:                                            │
│                                                                             │
│     GET /api/payments/by-user/{userId}                                      │
│                                                                             │
│     Query SQL:                                                              │
│     SELECT p.*, pd.*                                                        │
│     FROM payments p                                                         │
│     LEFT JOIN payment_details pd ON p.payment_id = pd.payment_id           │
│     WHERE p.user_id = '7f3e4d2a-...'                                        │
│       AND p.payment_status = 'COMPLETED'                                    │
│     ORDER BY p.payment_date DESC;                                           │
│                                                                             │
│  2️⃣ ESTRUCTURA DE DATOS:                                                    │
│                                                                             │
│     PaymentEntity {                                                         │
│         paymentId: "PAY-2024-001",                                          │
│         organizationId: "org-123",                                          │
│         paymentCode: "PAY-001",                                             │
│         userId: "7f3e4d2a-...",     ← Usuario que pagó (CLIENT)            │
│         waterBoxId: "5",            ← Caja por la que pagó                 │
│         paymentType: "MONTHLY_FEE", ← Tipo: cuota mensual                  │
│         paymentMethod: "CASH",                                              │
│         totalAmount: 15.00,                                                 │
│         paymentDate: "2024-02-01",                                          │
│         paymentStatus: "COMPLETED",                                         │
│         externalReference: "REC-001"                                        │
│     }                                                                       │
│                                                                             │
│  3️⃣ CONSULTA ESPECÍFICA POR CAJA:                                          │
│                                                                             │
│     GET /api/payments/by-water-box/{waterBoxId}                             │
│                                                                             │
│     Query SQL:                                                              │
│     SELECT p.*, u.first_name, u.last_name                                  │
│     FROM payments p                                                         │
│     WHERE p.water_box_id = '5'                                              │
│       AND p.organization_id = 'org-123'                                     │
│     ORDER BY p.payment_date DESC;                                           │
│                                                                             │
│     → Muestra TODOS los pagos asociados a esa caja                         │
│     → Incluye pagos de usuarios anteriores si hubo transferencia           │
│                                                                             │
│  4️⃣ HISTORIAL COMPLETO:                                                     │
│                                                                             │
│     Usuario "Juan Pérez" (userId: 7f3e4d2a-...)                            │
│     ├── WaterBox: BOX-001                                                   │
│     │   ├── Pago: PAY-2024-001 | 15.00 | 2024-02-01 | COMPLETED           │
│     │   ├── Pago: PAY-2024-002 | 15.00 | 2024-03-01 | COMPLETED           │
│     │   └── Pago: PAY-2024-003 | 15.00 | 2024-04-01 | PENDING             │
│     └── Total pagado: 30.00                                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 👷 Pregunta 3: ¿Cómo saber qué OPERARIO hizo tal cosa?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AUDITORÍA: Seguimiento de Acciones por Usuario                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣ CAMPOS DE AUDITORÍA EN TODAS LAS ENTIDADES:                            │
│                                                                             │
│     BaseEntity {                                                            │
│         createdAt: LocalDateTime,   ← Cuándo se creó                       │
│         updatedAt: LocalDateTime,   ← Cuándo se modificó                   │
│         createdBy: UUID,            ← Quién lo creó (userId)               │
│         updatedBy: UUID             ← Quién lo modificó (userId)           │
│     }                                                                       │
│                                                                             │
│  2️⃣ SEGUIMIENTO EN vg-ms-water-quality:                                    │
│                                                                             │
│     QualityTest {                                                           │
│         id: "test-001",                                                     │
│         testCode: "TEST-2024-001",                                          │
│         testedByUserId: "op-456",   ← OPERARIO que hizo la prueba         │
│         testDate: "2024-01-15",                                             │
│         testType: "CHLORINE",                                               │
│         results: [...],                                                     │
│         createdBy: "op-456",        ← Mismo operario                       │
│         createdAt: "2024-01-15T09:30:00"                                    │
│     }                                                                       │
│                                                                             │
│     → Query: ¿Qué pruebas hizo el operario "op-456"?                       │
│     GET /api/quality-tests/by-operator/{operatorId}                         │
│                                                                             │
│  3️⃣ SEGUIMIENTO EN vg-ms-claims-incidents:                                 │
│                                                                             │
│     Complaint {                                                             │
│         id: "claim-001",                                                    │
│         complaintCode: "CLAIM-2024-001",                                    │
│         reportedByUserId: "client-789",  ← CLIENT que reportó              │
│         assignedToUserId: "tech-123",    ← TÉCNICO asignado                │
│         resolvedByUserId: "tech-123",    ← TÉCNICO que resolvió            │
│         status: "RESOLVED",                                                 │
│         createdBy: "client-789",         ← CLIENT creador                  │
│         updatedBy: "tech-123",           ← TÉCNICO último en modificar     │
│         resolvedAt: "2024-01-20T14:00:00"                                   │
│     }                                                                       │
│                                                                             │
│     → Query: ¿Qué reclamos resolvió el técnico "tech-123"?                │
│     GET /api/complaints/resolved-by/{technicianId}                          │
│                                                                             │
│  4️⃣ SEGUIMIENTO EN vg-ms-infrastructure (Transferencias):                  │
│                                                                             │
│     WaterBoxTransfer {                                                      │
│         id: 10,                                                             │
│         waterBoxId: 5,                                                      │
│         oldAssignmentId: 1,                                                 │
│         newAssignmentId: 2,                                                 │
│         transferReason: "Cambio de titular",                                │
│         documents: ["DNI.pdf", "Contrato.pdf"],                             │
│         createdBy: "admin-999",     ← ADMIN que hizo la transferencia      │
│         createdAt: "2024-05-10T11:00:00"                                    │
│     }                                                                       │
│                                                                             │
│     → Query: ¿Qué transferencias hizo el admin "admin-999"?                │
│     GET /api/water-boxes/transfers/by-admin/{adminId}                       │
│                                                                             │
│  5️⃣ SEGUIMIENTO EN vg-ms-payments:                                         │
│                                                                             │
│     PaymentEntity {                                                         │
│         paymentId: "PAY-2024-001",                                          │
│         userId: "client-789",       ← CLIENT que pagó                      │
│         createdBy: "cashier-555",   ← CAJERO que registró el pago          │
│         paymentMethod: "CASH",                                              │
│         createdAt: "2024-02-01T15:30:00"                                    │
│     }                                                                       │
│                                                                             │
│     → Query: ¿Qué pagos registró el cajero "cashier-555"?                  │
│     GET /api/payments/registered-by/{cashierId}                             │
│                                                                             │
│  6️⃣ REPORTE DE ACTIVIDAD COMPLETO:                                         │
│                                                                             │
│     Operario "Juan Técnico" (userId: tech-123)                             │
│     ├── Rol: ADMIN (técnico de campo)                                      │
│     ├── Pruebas de calidad realizadas:                                     │
│     │   ├── TEST-2024-001 | Cloro | 2024-01-15 09:30                       │
│     │   ├── TEST-2024-005 | pH     | 2024-01-16 10:00                      │
│     │   └── TEST-2024-010 | Cloro | 2024-01-18 11:45                       │
│     ├── Reclamos resueltos:                                                │
│     │   ├── CLAIM-2024-001 | Fuga de agua | 2024-01-20 14:00              │
│     │   └── CLAIM-2024-003 | Presión baja | 2024-01-22 16:30              │
│     ├── Modificaciones a registros:                                        │
│     │   ├── Usuario USR-005 actualizado | 2024-01-19 09:00                │
│     │   └── Distribución DIS-012 modificada | 2024-01-21 13:15            │
│     └── Total actividades: 8                                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 🎯 Consultas Clave por Microservicio

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ENDPOINTS PARA CONSULTAS DE RELACIONES                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📍 vg-ms-users                                                             │
│     GET /api/users/{userId}                                                 │
│     → Obtener datos completos del usuario                                   │
│     → Incluye: organizationId, zoneId, streetId, roles                     │
│                                                                             │
│  📍 vg-ms-infrastructure                                                    │
│     GET /api/water-boxes/assignments/by-user/{userId}                       │
│     → Ver todas las cajas asignadas a un usuario                           │
│                                                                             │
│     GET /api/water-boxes/assignments/active/{userId}                        │
│     → Ver solo la caja ACTUAL del usuario (endDate = null)                 │
│                                                                             │
│     GET /api/water-boxes/assignments/history/{waterBoxId}                   │
│     → Ver TODOS los usuarios que tuvieron esa caja (histórico)             │
│                                                                             │
│     GET /api/water-boxes/transfers/by-water-box/{waterBoxId}                │
│     → Ver todas las transferencias de una caja                             │
│                                                                             │
│  📍 vg-ms-payments                                                          │
│     GET /api/payments/by-user/{userId}                                      │
│     → Ver todos los pagos de un usuario                                     │
│                                                                             │
│     GET /api/payments/by-water-box/{waterBoxId}                             │
│     → Ver todos los pagos asociados a una caja                             │
│     → Incluye pagos de usuarios anteriores                                 │
│                                                                             │
│     GET /api/payments/pending/{userId}                                      │
│     → Ver pagos pendientes de un usuario                                    │
│                                                                             │
│  📍 vg-ms-water-quality                                                     │
│     GET /api/quality-tests/by-operator/{operatorId}                         │
│     → Ver pruebas realizadas por un operario                               │
│                                                                             │
│     GET /api/quality-tests/by-zone/{zoneId}                                 │
│     → Ver pruebas en una zona geográfica                                   │
│                                                                             │
│  📍 vg-ms-claims-incidents                                                  │
│     GET /api/complaints/reported-by/{userId}                                │
│     → Reclamos creados por un usuario                                      │
│                                                                             │
│     GET /api/complaints/assigned-to/{technicianId}                          │
│     → Reclamos asignados a un técnico                                      │
│                                                                             │
│     GET /api/complaints/resolved-by/{technicianId}                          │
│     → Reclamos resueltos por un técnico                                    │
│                                                                             │
│  📍 vg-ms-distribution                                                      │
│     GET /api/distribution-programs/by-zone/{zoneId}                         │
│     → Programas de distribución de una zona                                │
│     → Saber cuándo llega el agua a cada zona                               │
│                                                                             │
│  📍 vg-ms-organizations                                                     │
│     GET /api/zones/by-organization/{organizationId}                         │
│     → Ver todas las zonas de una organización                              │
│                                                                             │
│     GET /api/streets/by-zone/{zoneId}                                       │
│     → Ver todas las calles de una zona                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 🔄 Flujo Completo: Desde Usuario hasta Pago

```
1. USUARIO SE REGISTRA (vg-ms-users)
   ├── userId: 7f3e4d2a-...
   ├── username: "juan.perez"
   ├── organizationId: "org-123"
   ├── zoneId: "zone-456"
   ├── streetId: "street-789"
   └── roles: "CLIENT"

2. ADMIN ASIGNA CAJA DE AGUA (vg-ms-infrastructure)
   ├── WaterBox: BOX-001 (id: 5)
   ├── WaterBoxAssignment creado:
   │   ├── userId: "7f3e4d2a-..."
   │   ├── waterBoxId: 5
   │   ├── startDate: "2024-01-15"
   │   ├── endDate: null (ACTIVO)
   │   └── monthlyFee: 15.00
   └── WaterBox.currentAssignmentId = 1

3. USUARIO REALIZA PAGO (vg-ms-payments)
   ├── PaymentEntity creado:
   │   ├── userId: "7f3e4d2a-..."
   │   ├── waterBoxId: "5"
   │   ├── paymentType: "MONTHLY_FEE"
   │   ├── totalAmount: 15.00
   │   ├── paymentDate: "2024-02-01"
   │   └── paymentStatus: "COMPLETED"
   └── PaymentDetail:
       ├── Cuota mensual: 15.00
       └── Total: 15.00

4. OPERARIO VERIFICA CALIDAD (vg-ms-water-quality)
   ├── QualityTest creado:
   │   ├── testedByUserId: "op-456" (OPERARIO)
   │   ├── zoneId: "zone-456"
   │   ├── testType: "CHLORINE"
   │   └── status: "APPROVED"
   └── Zona del usuario verificada

5. CONSULTAS DISPONIBLES:
   ├── ¿Qué caja tiene Juan? → GET /water-boxes/assignments/active/{userId}
   ├── ¿Cuánto ha pagado?     → GET /payments/by-user/{userId}
   ├── ¿Historial de caja?    → GET /water-boxes/assignments/history/{waterBoxId}
   ├── ¿Quién hizo la prueba? → GET /quality-tests/by-operator/{operatorId}
   └── ¿Reclamos activos?     → GET /complaints/reported-by/{userId}
```

---

## 🌐 FLUJO COMPLETO DEL SISTEMA - TODOS LOS PROCESOS {#flujo-completo}

### 📋 DIAGRAMA MAESTRO: Desde Creación hasta Operación

```
╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
║                     SISTEMA JASS - FLUJO COMPLETO DE PROCESOS                                    ║
╚═══════════════════════════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 1: CONFIGURACIÓN INICIAL (SUPER_ADMIN)                                                    │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  👑 SUPER_ADMIN                                                                                 │
│      │                                                                                          │
│      ├─► 1.1 CREAR ORGANIZACIÓN (vg-ms-organizations)                                          │
│      │    POST /api/organizations                                                               │
│      │    {                                                                                     │
│      │      "organizationCode": "JASS-001",                                                     │
│      │      "name": "JASS Comunidad San Pedro",                                                 │
│      │      "ruc": "20123456789",                                                               │
│      │      "address": "Jr. Los Andes 123",                                                     │
│      │      "district": "San Pedro",                                                            │
│      │      "status": "ACTIVE"                                                                  │
│      │    }                                                                                     │
│      │    ✅ Resultado: organizationId = "org-123"                                              │
│      │                                                                                          │
│      └─► 1.2 CREAR ADMIN DE ORGANIZACIÓN (vg-ms-users)                                         │
│           POST /api/users                                                                        │
│           {                                                                                      │
│             "username": "admin.jass001",                                                         │
│             "firstName": "Carlos",                                                               │
│             "lastName": "Administrador",                                                         │
│             "password": "Admin123!",                                                             │
│             "organizationId": "org-123",      ← Vincula con organización                       │
│             "roles": "ADMIN",                 ← Rol de administrador                            │
│             "documentType": "DNI",                                                               │
│             "documentNumber": "12345678",                                                        │
│             "email": "admin@jass001.com",                                                        │
│             "phone": "987654321"                                                                 │
│           }                                                                                      │
│           ✅ Resultado: adminUserId = "admin-001"                                                │
│           ✅ vg-ms-authentication crea credentials automáticamente                              │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 2: CONFIGURACIÓN DE INFRAESTRUCTURA (ADMIN)                                               │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  👨‍💼 ADMIN (admin-001)                                                                           │
│      │                                                                                          │
│      ├─► 2.1 CREAR ZONAS (vg-ms-organizations)                                                 │
│      │    POST /api/zones                                                                       │
│      │    {                                                                                     │
│      │      "organizationId": "org-123",                                                        │
│      │      "zoneCode": "ZONA-A",                                                               │
│      │      "zoneName": "Zona Alta",                                                            │
│      │      "description": "Sector alto de la comunidad",                                       │
│      │      "status": "ACTIVE"                                                                  │
│      │    }                                                                                     │
│      │    ✅ Resultado: zoneId = "zone-456"                                                     │
│      │                                                                                          │
│      ├─► 2.2 CREAR CALLES POR ZONA (vg-ms-organizations)                                       │
│      │    POST /api/streets                                                                     │
│      │    {                                                                                     │
│      │      "zoneId": "zone-456",              ← Vincula con zona                              │
│      │      "streetCode": "CALLE-01",                                                           │
│      │      "streetName": "Jr. Los Andes",                                                      │
│      │      "streetType": "JR",                                                                 │
│      │      "status": "ACTIVE"                                                                  │
│      │    }                                                                                     │
│      │    ✅ Resultado: streetId = "street-789"                                                 │
│      │    → Repetir para todas las calles de la zona                                           │
│      │                                                                                          │
│      ├─► 2.3 CONFIGURAR TARIFAS (vg-ms-organizations)                                          │
│      │    POST /api/fares                                                                       │
│      │    {                                                                                     │
│      │      "organizationId": "org-123",                                                        │
│      │      "fareType": "MONTHLY_FEE",         ← Cuota mensual                                 │
│      │      "amount": 15.00,                                                                    │
│      │      "description": "Cuota mensual de agua",                                             │
│      │      "validFrom": "2024-01-01",                                                          │
│      │      "status": "ACTIVE"                                                                  │
│      │    }                                                                                     │
│      │    → Crear tarifas para:                                                                │
│      │       - MONTHLY_FEE (cuota mensual)                                                     │
│      │       - INSTALLATION_FEE (instalación nueva)                                            │
│      │       - RECONNECTION_FEE (reconexión por corte)                                         │
│      │       - LATE_FEE (mora)                                                                 │
│      │       - TRANSFER_FEE (transferencia de caja)                                            │
│      │                                                                                          │
│      └─► 2.4 CONFIGURAR HORARIOS DE DISTRIBUCIÓN (vg-ms-distribution)                          │
│           POST /api/distribution-schedules                                                       │
│           {                                                                                      │
│             "organizationId": "org-123",                                                         │
│             "scheduleCode": "SCH-ZONA-A",                                                        │
│             "scheduleName": "Horario Zona Alta",                                                 │
│             "startTime": "06:00",                                                                │
│             "endTime": "12:00",                                                                  │
│             "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],                                    │
│             "status": "ACTIVE"                                                                   │
│           }                                                                                      │
│           ✅ Cada zona tiene su horario de distribución de agua                                 │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 3: REGISTRO DE CLIENTES Y ASIGNACIÓN DE SUMINISTROS (ADMIN)                               │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  👨‍💼 ADMIN                                                                                       │
│      │                                                                                          │
│      ├─► 3.1 CREAR CLIENTE (vg-ms-users)                                                       │
│      │    POST /api/users                                                                       │
│      │    {                                                                                     │
│      │      "username": "juan.perez",                                                           │
│      │      "firstName": "Juan",                                                                │
│      │      "lastName": "Pérez García",                                                         │
│      │      "password": "Temp123!",           ← Password temporal                              │
│      │      "organizationId": "org-123",                                                        │
│      │      "zoneId": "zone-456",             ← Zona del cliente                               │
│      │      "streetId": "street-789",         ← Calle del cliente                              │
│      │      "roles": "CLIENT",                ← Rol de cliente                                 │
│      │      "documentType": "DNI",                                                              │
│      │      "documentNumber": "87654321",                                                       │
│      │      "address": "Jr. Los Andes 456",                                                     │
│      │      "phone": "912345678",             ← Puede ser null (rural)                         │
│      │      "email": null                     ← Puede ser null (rural)                         │
│      │    }                                                                                     │
│      │    ✅ Resultado: clientUserId = "client-789"                                             │
│      │    ✅ vg-ms-users genera: userCode = "USR-001"                                           │
│      │    ✅ vg-ms-authentication crea credentials                                              │
│      │    ✅ vg-ms-notification envía WhatsApp con credenciales                                │
│      │                                                                                          │
│      └─► 3.2 CREAR Y ASIGNAR CAJA DE AGUA AUTOMÁTICAMENTE (vg-ms-infrastructure)               │
│           Backend hace 2 operaciones en TRANSACCIÓN:                                            │
│           │                                                                                     │
│           ├─► 3.2.1 Crear WaterBox                                                             │
│           │    POST /api/water-boxes                                                            │
│           │    {                                                                                │
│           │      "organizationId": "org-123",                                                   │
│           │      "boxCode": "BOX-001",        ← NÚMERO DE SUMINISTRO generado                  │
│           │      "boxType": "RESIDENTIAL",                                                      │
│           │      "installationDate": "2024-01-15",                                              │
│           │      "status": "ACTIVE"                                                             │
│           │    }                                                                                │
│           │    ✅ waterBoxId = 1                                                                │
│           │                                                                                     │
│           └─► 3.2.2 Asignar al Cliente                                                         │
│                POST /api/water-boxes/assignments                                                │
│                {                                                                                │
│                  "waterBoxId": 1,                                                               │
│                  "userId": "client-789",                                                        │
│                  "startDate": "2024-01-15",                                                     │
│                  "endDate": null,              ← null = asignación activa                      │
│                  "monthlyFee": 15.00,          ← Tarifa configurada                            │
│                  "status": "ACTIVE"                                                             │
│                }                                                                                │
│                ✅ assignmentId = 1                                                              │
│                ✅ WaterBox.currentAssignmentId = 1                                              │
│                                                                                                 │
│           🔔 Notificación:                                                                      │
│              vg-ms-notification envía WhatsApp:                                                 │
│              "Estimado Juan, su número de suministro es BOX-001"                                │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 4: GESTIÓN DE PAGOS (MÚLTIPLES TIPOS)                                                     │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  💰 TIPOS DE PAGOS EN EL SISTEMA                                                                │
│                                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────────────────┐     │
│  │ 4.1 PAGO POR INSTALACIÓN (Primera vez)                                               │     │
│  ├──────────────────────────────────────────────────────────────────────────────────────┤     │
│  │                                                                                       │     │
│  │  👤 CLIENTE recién registrado                                                         │     │
│  │     │                                                                                 │     │
│  │     └─► POST /api/payments (vg-ms-payments)                                          │     │
│  │         {                                                                             │     │
│  │           "userId": "client-789",                                                     │     │
│  │           "waterBoxId": "1",                                                          │     │
│  │           "organizationId": "org-123",                                                │     │
│  │           "paymentType": "INSTALLATION_FEE",    ← Tipo: Instalación                  │     │
│  │           "paymentMethod": "CASH",                                                    │     │
│  │           "totalAmount": 50.00,                 ← Tarifa de instalación              │     │
│  │           "paymentDate": "2024-01-15",                                                │     │
│  │           "paymentStatus": "COMPLETED"                                                │     │
│  │         }                                                                             │     │
│  │         ✅ paymentCode generado: "PAY-2024-001"                                       │     │
│  │         ✅ PaymentDetail creado con desglose:                                         │     │
│  │            - Costo de instalación: 50.00                                             │     │
│  │            - Total: 50.00                                                             │     │
│  │                                                                                       │     │
│  └──────────────────────────────────────────────────────────────────────────────────────┘     │
│                                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────────────────┐     │
│  │ 4.2 PAGO MENSUAL (Cuota recurrente)                                                  │     │
│  ├──────────────────────────────────────────────────────────────────────────────────────┤     │
│  │                                                                                       │     │
│  │  👤 CLIENTE paga mensualidad                                                          │     │
│  │     │                                                                                 │     │
│  │     └─► POST /api/payments                                                           │     │
│  │         {                                                                             │     │
│  │           "userId": "client-789",                                                     │     │
│  │           "waterBoxId": "1",                                                          │     │
│  │           "organizationId": "org-123",                                                │     │
│  │           "paymentType": "MONTHLY_FEE",         ← Tipo: Cuota mensual                │     │
│  │           "paymentMethod": "CASH",                                                    │     │
│  │           "totalAmount": 15.00,                 ← Tarifa mensual                     │     │
│  │           "paymentDate": "2024-02-01",          ← Mes febrero                        │     │
│  │           "period": "2024-02",                  ← Periodo que cubre                  │     │
│  │           "paymentStatus": "COMPLETED"                                                │     │
│  │         }                                                                             │     │
│  │         ✅ paymentCode: "PAY-2024-002"                                                │     │
│  │         ✅ Sistema valida que no haya pago duplicado para ese periodo                │     │
│  │                                                                                       │     │
│  └──────────────────────────────────────────────────────────────────────────────────────┘     │
│                                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────────────────┐     │
│  │ 4.3 CORTE POR NO PAGO + RECONEXIÓN                                                   │     │
│  ├──────────────────────────────────────────────────────────────────────────────────────┤     │
│  │                                                                                       │     │
│  │  ⚠️ SISTEMA detecta mora (3 meses sin pagar)                                         │     │
│  │     │                                                                                 │     │
│  │     ├─► Sistema genera deuda automática:                                             │     │
│  │     │   - 3 cuotas: 3 × 15.00 = 45.00                                                │     │
│  │     │   - Mora (10%): 4.50                                                            │     │
│  │     │   - Total deuda: 49.50                                                          │     │
│  │     │                                                                                 │     │
│  │     ├─► ADMIN realiza CORTE (vg-ms-infrastructure)                                   │     │
│  │     │   PATCH /api/water-boxes/{waterBoxId}/status                                   │     │
│  │     │   {                                                                             │     │
│  │     │     "status": "SUSPENDED",              ← Suministro cortado                   │     │
│  │     │     "suspensionReason": "NO_PAYMENT",                                           │     │
│  │     │     "suspendedBy": "admin-001",                                                 │     │
│  │     │     "suspendedAt": "2024-05-01"                                                 │     │
│  │     │   }                                                                             │     │
│  │     │                                                                                 │     │
│  │     └─► CLIENTE paga para reconectar:                                                │     │
│  │         POST /api/payments                                                            │     │
│  │         {                                                                             │     │
│  │           "paymentType": "RECONNECTION_FEE",   ← Tipo: Reconexión                   │     │
│  │           "totalAmount": 79.50,                ← Deuda + tarifa reconexión           │     │
│  │           "details": [                                                                │     │
│  │             { "description": "Deuda acumulada", "amount": 49.50 },                   │     │
│  │             { "description": "Tarifa reconexión", "amount": 30.00 }                  │     │
│  │           ]                                                                           │     │
│  │         }                                                                             │     │
│  │         ✅ Pago completado → WaterBox.status = "ACTIVE"                               │     │
│  │                                                                                       │     │
│  └──────────────────────────────────────────────────────────────────────────────────────┘     │
│                                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────────────────┐     │
│  │ 4.4 PAGO ESPECÍFICO POR JASS (Mejoras / Mantenimiento)                               │     │
│  ├──────────────────────────────────────────────────────────────────────────────────────┤     │
│  │                                                                                       │     │
│  │  🏗️ ADMIN solicita pago extraordinario para mejoras                                  │     │
│  │     │                                                                                 │     │
│  │     ├─► Crear parámetro de pago especial (vg-ms-organizations)                       │     │
│  │     │   POST /api/parameters                                                          │     │
│  │     │   {                                                                             │     │
│  │     │     "organizationId": "org-123",                                                │     │
│  │     │     "parameterKey": "SPECIAL_FEE_2024_TANK",                                    │     │
│  │     │     "parameterValue": "100.00",                                                 │     │
│  │     │     "description": "Aporte para construcción de tanque elevado",                │     │
│  │     │     "status": "ACTIVE"                                                          │     │
│  │     │   }                                                                             │     │
│  │     │                                                                                 │     │
│  │     └─► Clientes realizan pago:                                                      │     │
│  │         POST /api/payments                                                            │     │
│  │         {                                                                             │     │
│  │           "paymentType": "SPECIAL_FEE",         ← Tipo: Pago especial                │     │
│  │           "totalAmount": 100.00,                                                      │     │
│  │           "description": "Aporte tanque elevado",                                     │     │
│  │           "parameterKey": "SPECIAL_FEE_2024_TANK"                                     │     │
│  │         }                                                                             │     │
│  │         ✅ Sistema registra quién pagó y quién no                                     │     │
│  │                                                                                       │     │
│  └──────────────────────────────────────────────────────────────────────────────────────┘     │
│                                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────────────────┐     │
│  │ 4.5 PAGO POR ZONA (Tarifa diferenciada)                                              │     │
│  ├──────────────────────────────────────────────────────────────────────────────────────┤     │
│  │                                                                                       │     │
│  │  🗺️ Algunas zonas tienen tarifa diferente (Zona Alta = más cara)                    │     │
│  │     │                                                                                 │     │
│  │     ├─► Sistema consulta tarifa por zona:                                            │     │
│  │     │   GET /api/fares/by-zone/{zoneId}                                              │     │
│  │     │   → Zona Alta: 20.00/mes                                                       │     │
│  │     │   → Zona Media: 15.00/mes                                                      │     │
│  │     │   → Zona Baja: 10.00/mes                                                       │     │
│  │     │                                                                                 │     │
│  │     └─► Al crear pago, toma tarifa según zona del usuario:                          │     │
│  │         POST /api/payments                                                            │     │
│  │         {                                                                             │     │
│  │           "userId": "client-789",            ← Usuario en Zona Alta                  │     │
│  │           "paymentType": "MONTHLY_FEE",                                               │     │
│  │           "totalAmount": 20.00,              ← Tarifa de Zona Alta                   │     │
│  │           "zoneId": "zone-456"               ← Referencia a zona                     │     │
│  │         }                                                                             │     │
│  │                                                                                       │     │
│  └──────────────────────────────────────────────────────────────────────────────────────┘     │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 5: GESTIÓN DE INCIDENCIAS Y RECLAMOS                                                      │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  🚨 FLUJO COMPLETO DE INCIDENCIAS                                                               │
│                                                                                                 │
│  5.1 CLIENTE REPORTA INCIDENCIA (vg-ms-claims-incidents)                                       │
│      │                                                                                          │
│      POST /api/complaints                                                                       │
│      {                                                                                          │
│        "reportedByUserId": "client-789",      ← Cliente que reporta                            │
│        "complaintType": "WATER_LEAK",         ← Tipo: Fuga de agua                             │
│        "description": "Fuga en tubería principal de mi calle",                                 │
│        "priority": "HIGH",                                                                      │
│        "location": "Jr. Los Andes 456",                                                         │
│        "zoneId": "zone-456",                  ← Zona afectada                                  │
│        "streetId": "street-789",              ← Calle afectada                                 │
│        "status": "OPEN",                      ← Estado inicial                                 │
│        "createdBy": "client-789"                                                                │
│      }                                                                                          │
│      ✅ complaintCode generado: "CLAIM-2024-001"                                                │
│      ✅ Status: OPEN                                                                            │
│      🔔 Notificación a ADMIN via WhatsApp                                                      │
│                                                                                                 │
│  5.2 ADMIN ASIGNA A OPERARIO/TÉCNICO                                                           │
│      │                                                                                          │
│      PATCH /api/complaints/{complaintId}/assign                                                 │
│      {                                                                                          │
│        "assignedToUserId": "tech-123",        ← Técnico asignado                               │
│        "assignedBy": "admin-001",                                                               │
│        "assignedAt": "2024-01-20T09:00:00",                                                     │
│        "estimatedResolutionDate": "2024-01-20"                                                  │
│      }                                                                                          │
│      ✅ Status: ASSIGNED                                                                        │
│      🔔 Notificación a TÉCNICO y CLIENTE                                                       │
│                                                                                                 │
│  5.3 TÉCNICO EVALÚA Y REGISTRA MATERIALES NECESARIOS                                           │
│      │                                                                                          │
│      ├─► Técnico visita y evalúa:                                                             │
│      │   PATCH /api/complaints/{complaintId}                                                   │
│      │   {                                                                                     │
│      │     "status": "IN_PROGRESS",                                                            │
│      │     "diagnosis": "Tubería de 1/2 pulgada con fisura",                                   │
│      │     "requiredMaterials": [                                                              │
│      │       {                                                                                 │
│      │         "materialId": "MAT-001",       ← ID del material en inventario                 │
│      │         "materialName": "Tubería PVC 1/2\"",                                            │
│      │         "quantity": 2,                 ← 2 metros                                       │
│      │         "unitCost": 5.00                                                                │
│      │       },                                                                                │
│      │       {                                                                                 │
│      │         "materialId": "MAT-015",                                                        │
│      │         "materialName": "Pegamento PVC",                                                │
│      │         "quantity": 1,                 ← 1 unidad                                       │
│      │         "unitCost": 8.00                                                                │
│      │       }                                                                                 │
│      │     ],                                                                                  │
│      │     "estimatedCost": 18.00,            ← Costo total materiales                        │
│      │     "estimatedTime": "2 horas"                                                          │
│      │   }                                                                                     │
│      │                                                                                          │
│      └─► Sistema consulta inventario (vg-ms-inventory-purchases):                             │
│          GET /api/materials/availability                                                        │
│          → Verifica stock disponible                                                           │
│          → Si no hay stock: alerta al ADMIN                                                    │
│                                                                                                 │
│  5.4 TÉCNICO SOLICITA MATERIALES DEL INVENTARIO                                                │
│      │                                                                                          │
│      POST /api/inventory-movements (vg-ms-inventory-purchases)                                 │
│      {                                                                                          │
│        "movementType": "OUTPUT",              ← Salida de inventario                           │
│        "movementCode": "OUT-2024-001",                                                          │
│        "reason": "MAINTENANCE",                                                                 │
│        "complaintId": "CLAIM-2024-001",       ← Vincula con reclamo                            │
│        "requestedBy": "tech-123",             ← Técnico que solicita                           │
│        "approvedBy": "admin-001",             ← Admin que aprueba                              │
│        "items": [                                                                              │
│          {                                                                                     │
│            "materialId": "MAT-001",                                                            │
│            "quantity": 2,                                                                      │
│            "unitCost": 5.00,                                                                   │
│            "totalCost": 10.00                                                                  │
│          },                                                                                    │
│          {                                                                                     │
│            "materialId": "MAT-015",                                                            │
│            "quantity": 1,                                                                      │
│            "unitCost": 8.00,                                                                   │
│            "totalCost": 8.00                                                                   │
│          }                                                                                     │
│        ],                                                                                      │
│        "totalAmount": 18.00,                                                                   │
│        "movementDate": "2024-01-20T10:00:00"                                                   │
│      }                                                                                          │
│      ✅ Stock actualizado automáticamente:                                                     │
│         - MAT-001: 50 → 48 unidades                                                            │
│         - MAT-015: 20 → 19 unidades                                                            │
│      ✅ Kardex registra salida para balance                                                    │
│                                                                                                 │
│  5.5 TÉCNICO RESUELVE Y CIERRA INCIDENCIA                                                      │
│      │                                                                                          │
│      PATCH /api/complaints/{complaintId}/resolve                                                │
│      {                                                                                          │
│        "status": "RESOLVED",                                                                    │
│        "resolvedByUserId": "tech-123",        ← Técnico que resolvió                           │
│        "resolvedAt": "2024-01-20T14:00:00",                                                     │
│        "resolution": "Tubería reemplazada, fuga solucionada",                                  │
│        "materialsUsed": [                     ← Materiales realmente usados                    │
│          { "materialId": "MAT-001", "quantityUsed": 2 },                                       │
│          { "materialId": "MAT-015", "quantityUsed": 1 }                                        │
│        ],                                                                                      │
│        "timeSpent": "2 horas",                                                                  │
│        "photos": ["before.jpg", "after.jpg"]  ← Evidencia                                     │
│      }                                                                                          │
│      ✅ Status: RESOLVED                                                                        │
│      🔔 Notificación a CLIENTE y ADMIN                                                         │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 6: GESTIÓN DE INVENTARIO Y COMPRAS                                                        │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  📦 FLUJO COMPLETO DE INVENTARIO (vg-ms-inventory-purchases)                                   │
│                                                                                                 │
│  6.1 REGISTRO DE PROVEEDORES                                                                   │
│      │                                                                                          │
│      POST /api/suppliers                                                                        │
│      {                                                                                          │
│        "supplierCode": "SUP-001",                                                               │
│        "supplierName": "Distribuidora PVC Norte",                                              │
│        "ruc": "20987654321",                                                                    │
│        "contactName": "Roberto Comercial",                                                      │
│        "phone": "999888777",                                                                    │
│        "email": "ventas@pvcnorte.com",                                                          │
│        "address": "Av. Industrial 123",                                                         │
│        "supplierType": "MATERIALS",           ← Tipo: Materiales                               │
│        "status": "ACTIVE"                                                                       │
│      }                                                                                          │
│      ✅ supplierId = "supplier-001"                                                             │
│                                                                                                 │
│  6.2 REGISTRO DE MATERIALES EN CATÁLOGO                                                        │
│      │                                                                                          │
│      POST /api/materials                                                                        │
│      {                                                                                          │
│        "materialCode": "MAT-001",                                                               │
│        "materialName": "Tubería PVC 1/2\"",                                                     │
│        "category": "PLUMBING",                                                                  │
│        "unit": "METROS",                                                                        │
│        "minStock": 20,                        ← Stock mínimo (alerta)                          │
│        "maxStock": 100,                       ← Stock máximo                                   │
│        "currentStock": 0,                     ← Inicial en 0                                   │
│        "unitCost": 5.00,                                                                        │
│        "status": "ACTIVE"                                                                       │
│      }                                                                                          │
│      ✅ Material registrado en catálogo                                                         │
│                                                                                                 │
│  6.3 COMPRA DE MATERIALES (ENTRADA AL INVENTARIO)                                              │
│      │                                                                                          │
│      ├─► Crear Orden de Compra:                                                               │
│      │   POST /api/purchases                                                                   │
│      │   {                                                                                     │
│      │     "purchaseCode": "PUR-2024-001",                                                     │
│      │     "supplierId": "supplier-001",       ← Proveedor                                     │
│      │     "purchaseDate": "2024-01-10",                                                       │
│      │     "purchaseType": "MATERIALS",                                                        │
│      │     "items": [                                                                          │
│      │       {                                                                                 │
│      │         "materialId": "MAT-001",                                                        │
│      │         "quantity": 50,                ← 50 metros                                      │
│      │         "unitCost": 5.00,                                                               │
│      │         "subtotal": 250.00                                                              │
│      │       },                                                                                │
│      │       {                                                                                 │
│      │         "materialId": "MAT-015",                                                        │
│      │         "quantity": 20,                ← 20 unidades                                    │
│      │         "unitCost": 8.00,                                                               │
│      │         "subtotal": 160.00                                                              │
│      │       }                                                                                 │
│      │     ],                                                                                  │
│      │     "subtotal": 410.00,                                                                 │
│      │     "tax": 73.80,                      ← IGV 18%                                        │
│      │     "total": 483.80,                                                                    │
│      │     "paymentMethod": "TRANSFER",                                                        │
│      │     "status": "COMPLETED",                                                              │
│      │     "createdBy": "admin-001"           ← Admin que compró                              │
│      │   }                                                                                     │
│      │   ✅ purchaseId = "purchase-001"                                                        │
│      │                                                                                          │
│      ├─► Sistema registra ENTRADA en Kardex automáticamente:                                  │
│      │   POST /api/inventory-movements                                                         │
│      │   {                                                                                     │
│      │     "movementType": "INPUT",           ← Entrada                                        │
│      │     "movementCode": "IN-2024-001",                                                      │
│      │     "reason": "PURCHASE",                                                               │
│      │     "purchaseId": "purchase-001",      ← Vincula con compra                            │
│      │     "items": [...],                    ← Mismos items                                  │
│      │     "totalAmount": 483.80                                                               │
│      │   }                                                                                     │
│      │                                                                                          │
│      └─► Stock actualizado automáticamente:                                                   │
│          - MAT-001: 0 → 50 metros                                                              │
│          - MAT-015: 0 → 20 unidades                                                            │
│                                                                                                 │
│  6.4 KARDEX - CONTROL DE ENTRADAS Y SALIDAS                                                    │
│      │                                                                                          │
│      GET /api/inventory-movements/kardex/{materialId}                                           │
│      ↓                                                                                          │
│      ┌───────────┬──────────┬──────────┬─────────┬─────────┬──────────┬─────────┐             │
│      │ Fecha     │ Tipo     │ Concepto │ Entrada │ Salida  │ Saldo    │ Ref     │             │
│      ├───────────┼──────────┼──────────┼─────────┼─────────┼──────────┼─────────┤             │
│      │ 2024-01-10│ ENTRADA  │ Compra   │ 50 m    │         │ 50 m     │ PUR-001 │             │
│      │ 2024-01-20│ SALIDA   │ Mantenim.│         │ 2 m     │ 48 m     │ OUT-001 │             │
│      │ 2024-01-25│ SALIDA   │ Reparac. │         │ 5 m     │ 43 m     │ OUT-002 │             │
│      │ 2024-02-01│ ENTRADA  │ Compra   │ 30 m    │         │ 73 m     │ PUR-002 │             │
│      └───────────┴──────────┴──────────┴─────────┴─────────┴──────────┴─────────┘             │
│                                                                                                 │
│  6.5 ALERTA DE STOCK MÍNIMO                                                                    │
│      │                                                                                          │
│      ⚠️ Sistema detecta automáticamente:                                                       │
│      GET /api/materials/low-stock                                                              │
│      → MAT-001: Stock actual = 18 metros (< minStock 20)                                       │
│      🔔 Notificación al ADMIN: "Material MAT-001 por debajo del stock mínimo"                  │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 7: BALANCE FINANCIERO (ENTRADA Y SALIDA DE DINERO)                                        │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  💵 FLUJO FINANCIERO COMPLETO                                                                   │
│                                                                                                 │
│  7.1 ENTRADAS DE DINERO (INGRESOS)                                                             │
│      │                                                                                          │
│      ├─► vg-ms-payments registra TODOS los pagos de clientes:                                 │
│      │   - Cuotas mensuales                                                                    │
│      │   - Instalaciones                                                                       │
│      │   - Reconexiones                                                                        │
│      │   - Pagos especiales                                                                    │
│      │                                                                                          │
│      └─► Consulta de ingresos:                                                                │
│          GET /api/payments/income-report                                                        │
│          Query params: startDate, endDate, organizationId                                       │
│          ↓                                                                                      │
│          {                                                                                      │
│            "period": "2024-01",                                                                 │
│            "totalIncome": 1250.00,                                                              │
│            "breakdown": {                                                                       │
│              "MONTHLY_FEE": 900.00,          ← Cuotas mensuales                                │
│              "INSTALLATION_FEE": 200.00,     ← Instalaciones                                   │
│              "RECONNECTION_FEE": 80.00,      ← Reconexiones                                    │
│              "SPECIAL_FEE": 70.00            ← Pagos especiales                                │
│            },                                                                                   │
│            "totalTransactions": 67                                                              │
│          }                                                                                      │
│                                                                                                 │
│  7.2 SALIDAS DE DINERO (EGRESOS)                                                               │
│      │                                                                                          │
│      ├─► vg-ms-inventory-purchases registra TODAS las compras:                                │
│      │   - Materiales de construcción                                                          │
│      │   - Herramientas                                                                        │
│      │   - Insumos químicos                                                                    │
│      │   - Servicios                                                                           │
│      │                                                                                          │
│      └─► Consulta de egresos:                                                                 │
│          GET /api/purchases/expense-report                                                      │
│          Query params: startDate, endDate, organizationId                                       │
│          ↓                                                                                      │
│          {                                                                                      │
│            "period": "2024-01",                                                                 │
│            "totalExpense": 850.00,                                                              │
│            "breakdown": {                                                                       │
│              "MATERIALS": 483.80,            ← Materiales                                      │
│              "TOOLS": 200.00,                ← Herramientas                                    │
│              "CHEMICALS": 100.00,            ← Cloro, etc.                                     │
│              "SERVICES": 66.20               ← Servicios                                       │
│            },                                                                                   │
│            "totalPurchases": 8                                                                  │
│          }                                                                                      │
│                                                                                                 │
│  7.3 BALANCE CONSOLIDADO (INGRESOS - EGRESOS)                                                  │
│      │                                                                                          │
│      GET /api/financial/balance                                                                 │
│      {                                                                                          │
│        "period": "2024-01",                                                                     │
│        "totalIncome": 1250.00,               ← De vg-ms-payments                               │
│        "totalExpense": 850.00,               ← De vg-ms-inventory                              │
│        "balance": 400.00,                    ← Utilidad del mes                                │
│        "balanceStatus": "POSITIVE",                                                             │
│        "previousBalance": 500.00,            ← Saldo anterior                                  │
│        "currentBalance": 900.00,             ← Saldo acumulado                                 │
│        "details": {                                                                             │
│          "incomeSources": {...},                                                                │
│          "expenseCategories": {...}                                                             │
│        }                                                                                        │
│      }                                                                                          │
│                                                                                                 │
│  7.4 REPORTES MENSUALES AUTOMÁTICOS                                                            │
│      │                                                                                          │
│      Sistema genera automáticamente al cierre de mes:                                          │
│      ├─► Estado de resultados (ingresos vs egresos)                                           │
│      ├─► Reporte de morosidad (usuarios con deudas)                                           │
│      ├─► Proyección de ingresos próximo mes                                                   │
│      ├─► Alerta si balance es negativo                                                        │
│      └─► Recomendaciones (ajustar tarifas, reducir gastos, etc.)                             │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 8: TRANSFERENCIA DE CAJAS DE AGUA                                                         │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  🔄 FLUJO COMPLETO DE TRANSFERENCIA (vg-ms-infrastructure)                                     │
│                                                                                                 │
│  Escenario: Juan Pérez vende su casa a María López                                             │
│                                                                                                 │
│  8.1 ADMIN INICIA TRANSFERENCIA                                                                │
│      │                                                                                          │
│      ├─► Validaciones previas:                                                                │
│      │   - Usuario actual (Juan) no debe tener deudas pendientes                              │
│      │   - Nuevo usuario (María) debe estar registrado                                        │
│      │   - Documentos legales subidos (escritura, DNI, etc.)                                  │
│      │                                                                                          │
│      └─► POST /api/water-boxes/transfers                                                      │
│          {                                                                                      │
│            "waterBoxId": 1,                                                                     │
│            "currentUserId": "client-789",     ← Juan Pérez (actual)                           │
│            "newUserId": "client-999",         ← María López (nueva)                            │
│            "transferReason": "SALE",          ← Motivo: Venta de propiedad                     │
│            "transferDate": "2024-06-01",                                                        │
│            "documents": [                     ← Documentos legales                             │
│              "escritura_venta.pdf",                                                             │
│              "dni_vendedor.pdf",                                                                │
│              "dni_comprador.pdf",                                                               │
│              "boleta_pago_transferencia.pdf"                                                    │
│            ],                                                                                   │
│            "transferFee": 50.00,              ← Tarifa de transferencia                        │
│            "paidBy": "client-999",            ← María paga la transferencia                    │
│            "approvedBy": "admin-001",         ← Admin que aprueba                              │
│            "notes": "Transferencia por venta de propiedad"                                      │
│          }                                                                                      │
│                                                                                                 │
│  8.2 PROCESO AUTOMÁTICO DE TRANSFERENCIA                                                       │
│      │                                                                                          │
│      Sistema ejecuta en TRANSACCIÓN:                                                           │
│      │                                                                                          │
│      ├─► 1. Cerrar asignación actual (Juan):                                                  │
│      │    UPDATE water_box_assignments                                                         │
│      │    SET endDate = '2024-06-01',                                                          │
│      │        status = 'INACTIVE'                                                              │
│      │    WHERE id = 1;                                                                        │
│      │                                                                                          │
│      ├─► 2. Crear nueva asignación (María):                                                   │
│      │    INSERT INTO water_box_assignments                                                    │
│      │    (water_box_id, user_id, start_date, end_date, monthly_fee, status, transfer_id)     │
│      │    VALUES                                                                               │
│      │    (1, 'client-999', '2024-06-01', NULL, 15.00, 'ACTIVE', 10);                         │
│      │    → assignmentId = 2                                                                   │
│      │                                                                                          │
│      ├─► 3. Actualizar WaterBox:                                                              │
│      │    UPDATE water_boxes                                                                   │
│      │    SET current_assignment_id = 2                                                        │
│      │    WHERE id = 1;                                                                        │
│      │                                                                                          │
│      ├─► 4. Registrar pago de transferencia:                                                  │
│      │    POST /api/payments (vg-ms-payments)                                                  │
│      │    {                                                                                    │
│      │      "userId": "client-999",           ← María                                          │
│      │      "waterBoxId": "1",                                                                 │
│      │      "paymentType": "TRANSFER_FEE",    ← Tarifa de transferencia                       │
│      │      "totalAmount": 50.00                                                               │
│      │    }                                                                                    │
│      │                                                                                          │
│      └─► 5. Notificaciones:                                                                   │
│           🔔 Juan: "Su caja BOX-001 fue transferida a María López"                            │
│           🔔 María: "Le fue asignada la caja BOX-001. Su cuota mensual es S/ 15.00"           │
│           🔔 Admin: "Transferencia completada exitosamente"                                    │
│                                                                                                 │
│  8.3 CONSULTA DE HISTORIAL DE TRANSFERENCIAS                                                   │
│      │                                                                                          │
│      GET /api/water-boxes/transfers/history/{waterBoxId}                                        │
│      ↓                                                                                          │
│      [                                                                                          │
│        {                                                                                        │
│          "transferId": 10,                                                                      │
│          "transferDate": "2024-06-01",                                                          │
│          "previousOwner": "Juan Pérez",                                                         │
│          "newOwner": "María López",                                                             │
│          "transferReason": "SALE",                                                              │
│          "transferFee": 50.00,                                                                  │
│          "approvedBy": "admin-001"                                                              │
│        },                                                                                       │
│        {                                                                                        │
│          "transferId": 5,                                                                       │
│          "transferDate": "2023-01-15",                                                          │
│          "previousOwner": "Pedro Gómez",                                                        │
│          "newOwner": "Juan Pérez",                                                              │
│          "transferReason": "SALE",                                                              │
│          "transferFee": 50.00                                                                   │
│        }                                                                                        │
│      ]                                                                                          │
│      → Historial completo de todos los titulares de esa caja                                   │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 9: HORARIOS DE DISTRIBUCIÓN DE AGUA                                                       │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  ⏰ GESTIÓN DE HORARIOS (vg-ms-distribution)                                                   │
│                                                                                                 │
│  9.1 CREAR PROGRAMA DE DISTRIBUCIÓN POR ZONA                                                   │
│      │                                                                                          │
│      POST /api/distribution-programs                                                            │
│      {                                                                                          │
│        "organizationId": "org-123",                                                             │
│        "programCode": "DIST-ZONA-A-2024",                                                       │
│        "zoneId": "zone-456",                  ← Zona Alta                                      │
│        "scheduleId": "schedule-001",          ← Horario definido                               │
│        "routeId": "route-001",                ← Ruta de distribución                           │
│        "startDate": "2024-01-01",                                                               │
│        "endDate": "2024-12-31",               ← Vigencia anual                                 │
│        "daysOfWeek": [                        ← Días de distribución                           │
│          "MONDAY", "WEDNESDAY", "FRIDAY"                                                        │
│        ],                                                                                       │
│        "startTime": "06:00",                  ← Inicio                                          │
│        "endTime": "12:00",                    ← Fin                                             │
│        "waterFlowRate": "2.5 L/s",           ← Caudal                                          │
│        "estimatedPressure": "15 PSI",                                                           │
│        "status": "ACTIVE"                                                                       │
│      }                                                                                          │
│      ✅ Programa creado: DIST-ZONA-A-2024                                                       │
│                                                                                                 │
│  9.2 CONFIGURAR HORARIO POR CALLE (Más específico)                                             │
│      │                                                                                          │
│      POST /api/distribution-programs                                                            │
│      {                                                                                          │
│        "organizationId": "org-123",                                                             │
│        "programCode": "DIST-CALLE-ANDES",                                                       │
│        "zoneId": "zone-456",                                                                    │
│        "streetIds": ["street-789"],           ← Jr. Los Andes                                  │
│        "scheduleId": "schedule-002",                                                            │
│        "daysOfWeek": ["MONDAY", "WEDNESDAY"],                                                   │
│        "startTime": "08:00",                  ← Horario específico de calle                    │
│        "endTime": "10:00",                                                                      │
│        "status": "ACTIVE"                                                                       │
│      }                                                                                          │
│      ✅ Horario más específico para una calle dentro de la zona                                │
│                                                                                                 │
│  9.3 CONSULTA DE HORARIOS PARA USUARIOS                                                        │
│      │                                                                                          │
│      👤 Cliente consulta su horario:                                                            │
│      GET /api/distribution-programs/my-schedule                                                 │
│      Header: Authorization: Bearer {token de client-789}                                        │
│      ↓                                                                                          │
│      Sistema detecta automáticamente:                                                          │
│      - userId del token                                                                         │
│      - Consulta vg-ms-users: obtiene zoneId y streetId                                         │
│      - Busca programas activos para esa zona/calle                                             │
│      ↓                                                                                          │
│      {                                                                                          │
│        "zoneName": "Zona Alta",                                                                 │
│        "streetName": "Jr. Los Andes",                                                           │
│        "distributionDays": ["Lunes", "Miércoles", "Viernes"],                                  │
│        "schedule": "06:00 - 12:00",                                                             │
│        "nextDistribution": "2024-01-22 06:00",                                                  │
│        "waterFlowRate": "2.5 L/s",                                                              │
│        "estimatedPressure": "15 PSI",                                                           │
│        "message": "El próximo abastecimiento es Lunes 22/01 a las 6:00 AM"                     │
│      }                                                                                          │
│                                                                                                 │
│  9.4 REPORTE DE COBERTURA                                                                      │
│      │                                                                                          │
│      GET /api/distribution-programs/coverage-report                                             │
│      {                                                                                          │
│        "organizationId": "org-123",                                                             │
│        "totalZones": 3,                                                                         │
│        "zonesWithProgram": 3,                 ← 100% cobertura                                 │
│        "totalStreets": 15,                                                                      │
│        "streetsWithProgram": 12,              ← 80% cobertura                                  │
│        "totalUsers": 150,                                                                       │
│        "usersWithService": 150,               ← 100% usuarios cubiertos                        │
│        "averageDistributionDays": 3,          ← Promedio 3 días/semana                         │
│        "alerts": [                                                                              │
│          "Calles sin programa: Jr. Las Flores, Jr. Los Jazmines, Jr. El Pino"                  │
│        ]                                                                                        │
│      }                                                                                          │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ FASE 10: CONTROL DE CALIDAD DEL AGUA                                                           │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                 │
│  🧪 GESTIÓN DE CALIDAD (vg-ms-water-quality)                                                   │
│                                                                                                 │
│  10.1 REGISTRO DE PUNTOS DE PRUEBA                                                             │
│       │                                                                                         │
│       POST /api/testing-points                                                                  │
│       {                                                                                         │
│         "organizationId": "org-123",                                                            │
│         "pointCode": "TP-001",                                                                  │
│         "pointName": "Reservorio Principal",                                                    │
│         "pointType": "RESERVOIR",              ← Tipo: Reservorio, Tanque, Red                 │
│         "zoneId": "zone-456",                  ← Zona donde está                               │
│         "location": "Parte alta del cerro",                                                     │
│         "coordinates": {                                                                        │
│           "latitude": -12.0464,                                                                 │
│           "longitude": -77.0428                                                                 │
│         },                                                                                      │
│         "capacity": "10000 L",                                                                  │
│         "status": "ACTIVE"                                                                      │
│       }                                                                                         │
│       ✅ Punto de prueba registrado                                                             │
│                                                                                                 │
│  10.2 OPERARIO REALIZA PRUEBA DE CALIDAD                                                       │
│       │                                                                                         │
│       POST /api/quality-tests                                                                   │
│       {                                                                                         │
│         "organizationId": "org-123",                                                            │
│         "testCode": "TEST-2024-001",                                                            │
│         "testingPointIds": [                   ← Puede ser múltiples puntos                    │
│           "TP-001",                            ← Reservorio                                     │
│           "TP-005"                             ← Red de distribución                            │
│         ],                                                                                      │
│         "testDate": "2024-01-15T09:00:00",                                                      │
│         "testType": "COMPLETE",                ← Tipo: Completo, Básico, Especial              │
│         "testedByUserId": "operator-456",      ← OPERARIO que hace la prueba                   │
│         "weatherConditions": "Soleado",                                                         │
│         "waterTemperature": 18.5,              ← °C                                             │
│         "results": [                           ← Resultados de pruebas                          │
│           {                                                                                     │
│             "parameter": "CHLORINE",           ← Cloro residual                                 │
│             "value": 0.8,                      ← mg/L                                           │
│             "unit": "mg/L",                                                                     │
│             "minLimit": 0.5,                   ← Límite mínimo permitido                        │
│             "maxLimit": 1.5,                   ← Límite máximo permitido                        │
│             "status": "WITHIN_LIMITS",         ← Estado: Dentro de límites                     │
│             "observation": "Normal"                                                             │
│           },                                                                                    │
│           {                                                                                     │
│             "parameter": "PH",                 ← pH                                              │
│             "value": 7.2,                                                                       │
│             "unit": "pH",                                                                       │
│             "minLimit": 6.5,                                                                    │
│             "maxLimit": 8.5,                                                                    │
│             "status": "WITHIN_LIMITS",                                                          │
│             "observation": "Normal"                                                             │
│           },                                                                                    │
│           {                                                                                     │
│             "parameter": "TURBIDITY",          ← Turbiedad                                      │
│             "value": 3.5,                      ← NTU                                            │
│             "unit": "NTU",                                                                      │
│             "minLimit": 0,                                                                      │
│             "maxLimit": 5.0,                                                                    │
│             "status": "WITHIN_LIMITS",                                                          │
│             "observation": "Aceptable"                                                          │
│           },                                                                                    │
│           {                                                                                     │
│             "parameter": "BACTERIA",           ← Bacterias coliformes                           │
│             "value": 2,                        ← UFC/100ml                                      │
│             "unit": "UFC/100ml",                                                                │
│             "minLimit": 0,                                                                      │
│             "maxLimit": 0,                     ← DEBE ser 0                                     │
│             "status": "OUT_OF_LIMITS",         ← ⚠️ FUERA DE LÍMITES                           │
│             "observation": "Requiere tratamiento"                                               │
│           }                                                                                     │
│         ],                                                                                      │
│         "generalObservations": "Se detectó presencia de bacterias. Se recomienda cloración",   │
│         "photos": ["test_chlorine.jpg", "test_ph.jpg"],                                        │
│         "status": "REQUIRES_ACTION"            ← Estado: Requiere acción                        │
│       }                                                                                         │
│       ✅ Prueba registrada: TEST-2024-001                                                       │
│                                                                                                 │
│  10.3 SISTEMA GENERA ALERTA AUTOMÁTICA                                                         │
│       │                                                                                         │
│       ⚠️ Detecta resultado FUERA DE LÍMITES:                                                   │
│       → Parámetro: BACTERIA = 2 (límite: 0)                                                    │
│       → Severidad: HIGH                                                                         │
│       │                                                                                         │
│       Sistema automático:                                                                       │
│       ├─► Crea incidencia automática (vg-ms-claims-incidents):                                │
│       │   POST /api/complaints                                                                 │
│       │   {                                                                                    │
│       │     "complaintType": "WATER_QUALITY",                                                  │
│       │     "description": "Bacterias detectadas en TEST-2024-001",                            │
│       │     "priority": "URGENT",                                                              │
│       │     "zoneId": "zone-456",                                                              │
│       │     "testId": "TEST-2024-001",          ← Vincula con prueba                           │
│       │     "status": "OPEN"                                                                   │
│       │   }                                                                                    │
│       │                                                                                         │
│       └─► Envía notificaciones:                                                               │
│           🔔 ADMIN: "Alerta: Bacterias detectadas en Zona Alta"                                │
│           🔔 Técnicos: "Acción requerida en punto TP-001"                                      │
│                                                                                                 │
│  10.4 REGISTRO DE TRATAMIENTO CORRECTIVO                                                       │
│       │                                                                                         │
│       PATCH /api/quality-tests/{testId}/treatment                                               │
│       {                                                                                         │
│         "treatmentDate": "2024-01-15T14:00:00",                                                 │
│         "treatmentType": "CHLORINATION",       ← Cloración                                      │
│         "chlorineDose": "2.0 mg/L",            ← Dosis aplicada                                │
│         "treatedByUserId": "operator-456",     ← Operario que trató                            │
│         "materialsUsed": [                                                                      │
│           {                                                                                     │
│             "materialId": "MAT-CHLORINE",                                                       │
│             "quantity": 5,                     ← 5 kg de cloro                                 │
│             "unit": "kg"                                                                        │
│           }                                                                                     │
│         ],                                                                                      │
│         "observations": "Se aplicó tratamiento de choque con cloro",                            │
│         "nextTestDate": "2024-01-16"           ← Próxima prueba de verificación                │
│       }                                                                                         │
│       ✅ Tratamiento registrado                                                                 │
│       ✅ Salida de inventario automática (cloro)                                               │
│                                                                                                 │
│  10.5 PRUEBA DE VERIFICACIÓN (24 HORAS DESPUÉS)                                                │
│       │                                                                                         │
│       POST /api/quality-tests (nueva prueba)                                                    │
│       {                                                                                         │
│         "testCode": "TEST-2024-002",                                                            │
│         "testType": "VERIFICATION",            ← Tipo: Verificación                             │
│         "previousTestId": "TEST-2024-001",     ← Relaciona con prueba anterior                 │
│         "testDate": "2024-01-16T09:00:00",                                                      │
│         "testedByUserId": "operator-456",                                                       │
│         "results": [                                                                            │
│           {                                                                                     │
│             "parameter": "BACTERIA",                                                            │
│             "value": 0,                        ← ✅ CORREGIDO                                   │
│             "status": "WITHIN_LIMITS"                                                           │
│           },                                                                                    │
│           {                                                                                     │
│             "parameter": "CHLORINE",                                                            │
│             "value": 1.0,                                                                       │
│             "status": "WITHIN_LIMITS"                                                           │
│           }                                                                                     │
│         ],                                                                                      │
│         "status": "APPROVED"                   ← ✅ APROBADA                                    │
│       }                                                                                         │
│       ✅ Cierra incidencia automáticamente                                                      │
│       🔔 Notificación: "Calidad de agua normalizada en Zona Alta"                              │
│                                                                                                 │
│  10.6 REPORTE MENSUAL DE CALIDAD                                                               │
│       │                                                                                         │
│       GET /api/quality-tests/monthly-report                                                     │
│       {                                                                                         │
│         "period": "2024-01",                                                                    │
│         "totalTests": 28,                      ← Pruebas realizadas                            │
│         "approvedTests": 25,                   ← 89% aprobadas                                 │
│         "testsWithIssues": 3,                  ← 11% con problemas                             │
│         "parametersOutOfLimits": [                                                              │
│           { "parameter": "BACTERIA", "occurrences": 2 },                                        │
│           { "parameter": "TURBIDITY", "occurrences": 1 }                                        │
│         ],                                                                                      │
│         "averageChlorine": 0.85,               ← Promedio de cloro                             │
│         "averagePH": 7.3,                                                                       │
│         "zonesWithIssues": ["zone-456"],       ← Zonas problemáticas                           │
│         "recommendation": "Incrementar frecuencia de cloración en Zona Alta"                    │
│       }                                                                                         │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
║                             RESUMEN DE INTEGRACIONES                                              ║
╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                                   ║
║  ✅ vg-ms-organizations → Gestiona organizaciones, zonas, calles, tarifas, parámetros            ║
║  ✅ vg-ms-users → Gestiona usuarios (SUPER_ADMIN, ADMIN, CLIENT)                                 ║
║  ✅ vg-ms-authentication → Maneja login, JWT, passwords                                           ║
║  ✅ vg-ms-infrastructure → Cajas de agua, asignaciones, transferencias                            ║
║  ✅ vg-ms-payments → Todos los tipos de pagos (mensual, instalación, reconexión, especiales)     ║
║  ✅ vg-ms-claims-incidents → Incidencias reportadas, asignación a técnicos, materiales usados    ║
║  ✅ vg-ms-inventory-purchases → Inventario, compras, proveedores, kardex, entradas/salidas       ║
║  ✅ vg-ms-distribution → Horarios de distribución por zonas y calles                              ║
║  ✅ vg-ms-water-quality → Pruebas de calidad, parámetros, alertas, tratamientos                  ║
║  ✅ vg-ms-notification → Notificaciones WhatsApp en TODOS los procesos                            ║
║                                                                                                   ║
║  AUDITORÍA COMPLETA:                                                                              ║
║  → Todos los registros tienen: createdBy, updatedBy, createdAt, updatedAt                        ║
║  → Relaciones mediante IDs: userId, waterBoxId, zoneId, streetId, organizationId                 ║
║  → Balance financiero: Ingresos (payments) - Egresos (purchases) = Balance mensual               ║
║  → Trazabilidad total: Quién hizo qué, cuándo, dónde y por qué                                   ║
║                                                                                                   ║
╚═══════════════════════════════════════════════════════════════════════════════════════════════════╝
```

---

## �🔐 ARQUITECTURA DE SEGURIDAD COMPLETA {#seguridad}

### 🎭 Conceptos Clave

```
┌─────────────────────────────────────────────────────────────────────┐
│ AUTENTICACIÓN (Authentication) - "¿Quién eres?"                     │
│ ✅ Login con username/password                                       │
│ ✅ Generar JWT token                                                 │
│ ✅ Validar JWT token (firma, expiración)                            │
│ 📍 RESPONSABLE: vg-ms-authentication + vg-ms-gateway                │
├─────────────────────────────────────────────────────────────────────┤
│ AUTORIZACIÓN (Authorization) - "¿Qué puedes hacer?"                 │
│ ✅ Verificar rol (SUPER_ADMIN, ADMIN, CLIENT)                       │
│ ✅ Verificar permisos (puede acceder a este recurso?)               │
│ ✅ Verificar reglas de negocio (solo sus datos?)                    │
│ 📍 RESPONSABLE: vg-ms-gateway + Microservicios                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 🔄 Flujo Completo

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    FLUJO COMPLETO DE SEGURIDAD                               │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ REGISTRO DE USUARIO                                                     │
│     Cliente → Gateway (8080) → vg-ms-users (8081)                           │
│                                     ↓                                        │
│                           vg-ms-authentication (8090)                        │
│                                                                              │
│     POST /api/users                                                          │
│     {                                                                        │
│       "username": "juan.perez",                                              │
│       "firstName": "Juan",                                                   │
│       "lastName": "Perez",                                                   │
│       "password": "123456",           ← Sin hash (texto plano)               │
│       "organizationId": "uuid-123",                                          │
│       "roles": "CLIENT"                                                      │
│     }                                                                        │
│                                                                              │
│     Flujo:                                                                   │
│     1. vg-ms-users guarda datos del usuario (sin password)                  │
│     2. vg-ms-users llama a vg-ms-authentication:                            │
│        POST /internal/credentials                                            │
│        { "userId": "uuid", "username": "juan.perez", "password": "123456" } │
│     3. vg-ms-authentication hace BCrypt hash y guarda en tabla credentials  │
│                                                                              │
│  ──────────────────────────────────────────────────────────────────────────  │
│                                                                              │
│  2️⃣ LOGIN (AUTENTICACIÓN)                                                   │
│     Cliente → Gateway (8080) → vg-ms-authentication (8090)                  │
│                                                                              │
│     POST /api/auth/login  (RUTA PÚBLICA - sin JWT)                          │
│     {                                                                        │
│       "username": "juan.perez",                                              │
│       "password": "123456"                                                   │
│     }                                                                        │
│                                                                              │
│     vg-ms-authentication:                                                    │
│     1. Busca en tabla credentials                                            │
│     2. Valida password con BCrypt                                            │
│     3. Consulta vg-ms-users para obtener rol y organizationId               │
│     4. Genera JWT con claims:                                                │
│        {                                                                     │
│          "userId": "uuid",                                                   │
│          "username": "juan.perez",                                           │
│          "role": "CLIENT",                                                   │
│          "organizationId": "uuid-org",                                       │
│          "exp": "24h"                                                        │
│        }                                                                     │
│                                                                              │
│     ← Respuesta: { "token": "eyJhbGc...", "user": {...} }                   │
│                                                                              │
│  ──────────────────────────────────────────────────────────────────────────  │
│                                                                              │
│  3️⃣ ACCESO A RECURSO PROTEGIDO (AUTORIZACIÓN)                               │
│     Cliente → Gateway (8080) → vg-ms-payments (8083)                        │
│                                                                              │
│     GET /api/payments                                                        │
│     Header: Authorization: Bearer eyJhbGc...                                 │
│                                                                              │
│     Gateway:                                                                 │
│     1. ✅ Valida JWT (firma, expiración)                                     │
│     2. ✅ Extrae claims (userId, role, organizationId)                       │
│     3. ✅ Verifica rol básico permitido para la ruta                         │
│     4. ✅ Propaga headers a microservicio:                                   │
│        - X-User-Id: uuid                                                     │
│        - X-Role: CLIENT                                                      │
│        - X-Organization-Id: uuid-org                                         │
│                                                                              │
│     vg-ms-payments:                                                          │
│     1. ✅ Lee headers (NO valida JWT, Gateway ya lo hizo)                    │
│     2. ✅ Aplica @PreAuthorize("isAuthenticated()")                          │
│     3. ✅ Valida reglas de negocio:                                          │
│        - CLIENT solo ve sus propios pagos                                    │
│        - ADMIN solo ve pagos de su organización                              │
│        - SUPER_ADMIN ve todos los pagos                                      │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### 🎭 Responsabilidades por Componente

#### **vg-ms-authentication (Puerto 8090)**

```java
// Responsabilidades:
✅ POST /api/auth/login          → Validar credentials, generar JWT
✅ POST /api/auth/register       → Hash password (BCrypt), guardar credentials
✅ POST /api/auth/refresh-token  → Refrescar JWT
✅ POST /api/auth/logout         → Invalidar token (blacklist)
✅ POST /internal/credentials    → Endpoint interno para crear credentials

// Tabla: credentials
CREATE TABLE credentials (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_attempts INT DEFAULT 0,
    last_login TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

// NO hace:
❌ Gestionar datos de usuarios (vg-ms-users lo hace)
❌ Validar JWT en cada request (Gateway lo hace)
❌ Autorización (microservicios lo hacen)
```

#### **vg-ms-gateway (Puerto 8080)**

```java
// Responsabilidades:
✅ Validar JWT en CADA request (excepto /api/auth/*)
✅ Extraer claims (userId, role, organizationId)
✅ Verificar rol básico para la ruta
✅ Propagar headers a microservicios
✅ Routing a microservicios internos

// application.yml
spring:
  cloud:
    gateway:
      routes:
        # RUTA PÚBLICA (sin JWT)
        - id: auth-service
          uri: http://vg-ms-authentication:8090
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1  # Remueve /api

        # RUTAS PROTEGIDAS (con JWT)
        - id: users-service
          uri: http://vg-ms-users:8081
          predicates:
            - Path=/api/users/**
          filters:
            - JwtAuthenticationFilter  # ✅ Valida JWT aquí
            - StripPrefix=1

// NO hace:
❌ Generar JWT (vg-ms-authentication lo hace)
❌ Autorización detallada (microservicios lo hacen)
```

#### **vg-ms-users (Puerto 8081)**

```java
// Responsabilidades:
✅ CRUD de usuarios (datos personales: nombre, email, dirección, etc.)
✅ Relacionar usuario con organización/zona/calle
✅ Autorización con @PreAuthorize
✅ Llamar a vg-ms-authentication para crear credentials

// Flujo de creación:
POST /api/users
1. Valida datos (organization existe, zona/calle válidas)
2. Guarda en tabla users (SIN password)
3. Llama a vg-ms-authentication:
   POST http://vg-ms-authentication:8090/internal/credentials
   { "userId": "uuid", "username": "juan.perez", "password": "123456" }
4. Retorna usuario creado

// Tabla: users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    user_code VARCHAR(50) UNIQUE NOT NULL,
    organization_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(50),
    document_number VARCHAR(50),
    email VARCHAR(255),              -- NULLABLE
    phone VARCHAR(20),               -- NULLABLE
    address TEXT,
    street_id UUID,
    zone_id UUID,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    roles VARCHAR(50) NOT NULL,      -- SUPER_ADMIN, ADMIN, CLIENT
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

// NO hace:
❌ Gestionar passwords (vg-ms-authentication lo hace)
❌ Validar JWT (Gateway lo hace)
```

#### **Otros Microservicios (payments, organizations, etc.)**

```java
// Responsabilidades:
✅ Leer headers del Gateway (X-User-Id, X-Role, X-Organization-Id)
✅ Aplicar @PreAuthorize para autorización
✅ Validar reglas de negocio específicas
✅ Lógica de dominio pura

// Ejemplo Controller:
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<ApiResponse<List<PaymentResponse>>> getPayments(
            Authentication authentication) {

        RequestContextFilter.UserContext context =
            (RequestContextFilter.UserContext) authentication.getDetails();

        String userId = context.getUserId();
        String role = context.getRole();
        String orgId = context.getOrganizationId();

        // Validación de reglas de negocio
        if ("CLIENT".equals(role)) {
            // Solo sus propios pagos
            return getPaymentsByUserId(userId);
        } else if ("ADMIN".equals(role)) {
            // Solo pagos de su organización
            return getPaymentsByOrganizationId(orgId);
        } else {
            // SUPER_ADMIN ve todos
            return getAllPayments();
        }
    }
}

// NO hace:
❌ Validar JWT (Gateway lo hace)
❌ Generar JWT (vg-ms-authentication lo hace)
```

---

## 🎨 CONVENCIONES DE NOMENCLATURA {#convenciones}

```
┌─────────────────┬───────────────────┬────────────────────────────────────┐
│ CAPA/ELEMENTO   │ CONVENCIÓN        │ EJEMPLO                            │
├─────────────────┼───────────────────┼────────────────────────────────────┤
│ Base de Datos   │ snake_case        │ user_id, organization_id           │
│ Paquetes Java   │ lowercase         │ pe.edu.vallegrande.vgmsusers       │
│ Clases          │ PascalCase        │ UserEntity, PaymentService         │
│ Interfaces      │ PascalCase + I    │ IUserRepository, ILoginUseCase     │
│ Campos/Métodos  │ camelCase         │ userId, getUserById()              │
│ Constantes      │ UPPER_SNAKE_CASE  │ USER_EXCHANGE, MAX_RETRY           │
│ API Endpoints   │ kebab-case        │ /api/water-quality                 │
│ JSON Response   │ camelCase         │ {"userId": "...", "firstName": ""} │
│ Reactive Types  │ Mono/Flux         │ Mono<User>, Flux<Payment>          │
│ Docker Services │ kebab-case        │ vg-ms-users, vg-ms-authentication  │
│ Exchange/Queue  │ dot.notation      │ jass.users.exchange                │
└─────────────────┴───────────────────┴────────────────────────────────────┘
```

---

## 🏛️ ARQUITECTURA HEXAGONAL {#arquitectura-hexagonal}

```
┌──────────────────────────────────────────────────────────────┐
│                    HEXAGONAL ARCHITECTURE                     │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│   DOMAIN (Núcleo - Lógica de Negocio Pura)                  │
│   ├── models/          → Entidades de dominio               │
│   ├── ports/           → Interfaces (Contratos)             │
│   │   ├── in/          → Use Cases (entrada)                │
│   │   └── out/         → Repositories, Clients (salida)     │
│   └── exceptions/      → Excepciones de dominio             │
│                                                               │
│   APPLICATION (Casos de Uso - Orquestación)                  │
│   ├── usecases/        → Implementación de casos de uso     │
│   ├── dto/             → Request/Response DTOs              │
│   │   ├── common/      → ApiResponse, ErrorMessage          │
│   │   ├── request/     → CreateUserRequest, etc.            │
│   │   └── response/    → UserResponse, etc.                 │
│   ├── mappers/         → DTOs ↔ Domain Models               │
│   └── events/          → Eventos de dominio                 │
│                                                               │
│   INFRASTRUCTURE (Adaptadores - Frameworks)                   │
│   ├── adapters/                                              │
│   │   ├── in/          → REST Controllers, Event Listeners  │
│   │   └── out/         → Repository Impl, REST Clients      │
│   ├── config/          → Spring Configuration               │
│   │   ├── R2dbcConfig.java                                  │
│   │   ├── WebClientConfig.java                              │
│   │   ├── SecurityConfig.java                               │
│   │   ├── RabbitMQConfig.java                               │
│   │   └── RequestContextFilter.java                         │
│   ├── persistence/                                           │
│   │   ├── entities/    → @Table UserEntity                  │
│   │   └── repositories/→ R2dbcRepository<UserEntity, UUID>  │
│   └── external/        → WebClient a otros microservicios   │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔗 COMUNICACIÓN ENTRE SERVICIOS {#comunicacion}

### Patrón Híbrido: REST + Events

```
┌─────────────────────────────────────────────────────────────┐
│ CUÁNDO USAR REST (WebClient - Síncrono)                    │
├─────────────────────────────────────────────────────────────┤
│ ✅ Validación inmediata (existe organización?)              │
│ ✅ Consultas transaccionales (crear usuario + credentials)  │
│ ✅ Datos críticos para continuar el flujo                   │
│                                                             │
│ Ejemplo:                                                    │
│ vg-ms-users → vg-ms-authentication (crear credentials)     │
│ vg-ms-users → vg-ms-organizations (validar org existe)     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ CUÁNDO USAR EVENTS (RabbitMQ - Asíncrono)                  │
├─────────────────────────────────────────────────────────────┤
│ ✅ Notificaciones (enviar email de bienvenida)              │
│ ✅ Auditoría (registrar acción en bitácora)                 │
│ ✅ Propagación de datos (actualizar caché)                  │
│ ✅ Procesos en segundo plano                                │
│                                                             │
│ Ejemplo:                                                    │
│ vg-ms-users → RabbitMQ → vg-ms-notification (email)        │
│ vg-ms-payments → RabbitMQ → vg-ms-infrastructure (audit)   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 ESTRUCTURA: vg-ms-users {#estructura-users}

```
vg-ms-users/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsusers/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── User.java                           → [CLASS] Modelo de dominio principal
│   │   │   │   └── valueobjects/
│   │   │   │       ├── Role.java                       → [ENUM] SUPER_ADMIN, ADMIN, CLIENT
│   │   │   │       ├── DocumentType.java               → [ENUM] DNI, PASSPORT, RUC
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IGetUserUseCase.java            → [INTERFACE]
│   │   │   │   │   ├── IUpdateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   └── IDeleteUserUseCase.java         → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IUserRepository.java            → [INTERFACE] Reactivo (Mono/Flux)
│   │   │   │       ├── IAuthenticationClient.java      → [INTERFACE] WebClient crear credentials
│   │   │   │       ├── IOrganizationClient.java        → [INTERFACE] WebClient validar org/zona/calle
│   │   │   │       ├── INotificationClient.java        → [INTERFACE] WebClient enviar WhatsApp
│   │   │   │       └── IUserEventPublisher.java        → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/
│   │   │       ├── UserNotFoundException.java          → [CLASS] extends RuntimeException
│   │   │       └── OrganizationNotFoundException.java  → [CLASS] extends RuntimeException
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateUserUseCaseImpl.java          → [CLASS] @Service
│   │   │   │   ├── GetUserUseCaseImpl.java             → [CLASS] @Service
│   │   │   │   └── UpdateUserUseCaseImpl.java          → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUserRequest.java          → [CLASS] @Valid
│   │   │   │   │   └── UpdateUserRequest.java          → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       └── UserResponse.java               → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   └── UserMapper.java                     → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── UserCreatedEvent.java               → [CLASS] Evento
│   │   │       └── publishers/
│   │   │           └── UserEventPublisherImpl.java     → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       └── UserController.java         → [CLASS] @RestController
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   └── UserRepositoryImpl.java     → [CLASS] @Repository
│   │       │       └── external/
│   │       │           ├── AuthenticationClientImpl.java → [CLASS] @Component
│   │       │           ├── OrganizationClientImpl.java → [CLASS] @Component (validar org/zona/calle)
│   │       │           └── NotificationClientImpl.java → [CLASS] @Component
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   └── UserEntity.java                 → [CLASS] @Table("users")
│   │       │   └── repositories/
│   │       │       └── UserR2dbcRepository.java        → [INTERFACE] R2dbcRepository
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── WebClientConfig.java                → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component WebFilter
│   │
│   └── resources/
│       ├── application.yml                             → Base común
│       ├── application-dev.yml                         → Docker local
│       ├── application-prod.yml                        → Docker Compose VPC
│       └── db/migration/
│           └── V1__create_users_table.sql              → SQL Script
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 📦 ESTRUCTURA: vg-ms-authentication {#estructura-authentication}

```
vg-ms-authentication/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsauthentication/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   └── Credentials.java                    → [CLASS] username, passwordHash
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ILoginUseCase.java              → [INTERFACE]
│   │   │   │   │   └── IRegisterCredentialsUseCase.java → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── ICredentialsRepository.java     → [INTERFACE]
│   │   │   │       └── IUserServiceClient.java         → [INTERFACE] WebClient a vg-ms-users
│   │   │   └── exceptions/
│   │   │       ├── InvalidCredentialsException.java    → [CLASS]
│   │   │       └── UserLockedException.java            → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── LoginUseCaseImpl.java               → [CLASS] @Service
│   │   │   │   └── RegisterCredentialsUseCaseImpl.java → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java               → [CLASS]
│   │   │   │   │   └── CreateCredentialsRequest.java   → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── LoginResponse.java              → [CLASS] { token, user }
│   │   │   └── security/
│   │   │       └── JwtService.java                     → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── AuthController.java         → [CLASS] @RestController
│   │       │   │       └── InternalCredentialsController.java → [CLASS] /internal/credentials
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   └── CredentialsRepositoryImpl.java → [CLASS] @Repository
│   │       │       └── external/
│   │       │           └── UserServiceClientImpl.java  → [CLASS] @Component
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   └── CredentialsEntity.java          → [CLASS] @Table("credentials")
│   │       │   └── repositories/
│   │       │       └── CredentialsR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java
│   │           ├── WebClientConfig.java
│   │           └── SecurityConfig.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           └── V1__create_credentials_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 📦 ESTRUCTURA: vg-ms-organizations {#estructura-organizations}

```
vg-ms-organizations/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsorganizations/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Organization.java                   → [CLASS] Organización/JASS
│   │   │   │   ├── Zone.java                           → [CLASS] Zonas geográficas
│   │   │   │   ├── Street.java                         → [CLASS] Calles por zona
│   │   │   │   ├── Fare.java                           → [CLASS] Tarifas (MONTHLY_FEE, INSTALLATION_FEE, etc.)
│   │   │   │   ├── Parameter.java                      → [CLASS] Parámetros de configuración
│   │   │   │   └── valueobjects/
│   │   │   │       ├── OrganizationType.java           → [ENUM] JASS, JAAS, OMSABAR
│   │   │   │       ├── FareType.java                   → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, RECONNECTION_FEE, LATE_FEE, TRANSFER_FEE
│   │   │   │       ├── StreetType.java                 → [ENUM] JR, AV, CALLE, PASAJE
│   │   │   │       ├── ParameterType.java              → [ENUM] BILLING_DAY, GRACE_PERIOD, etc.
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateOrganizationUseCase.java
│   │   │   │   │   ├── ICreateZoneUseCase.java
│   │   │   │   │   └── ICreateStreetUseCase.java
│   │   │   │   └── out/
│   │   │   │       ├── IOrganizationRepository.java    → [INTERFACE] Reactive
│   │   │   │       ├── IZoneRepository.java
│   │   │   │       └── IStreetRepository.java
│   │   │   └── exceptions/
│   │   │       └── OrganizationNotFoundException.java
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateOrganizationUseCaseImpl.java
│   │   │   │   ├── CreateZoneUseCaseImpl.java
│   │   │   │   └── CreateStreetUseCaseImpl.java
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java
│   │   │   │   │   └── ErrorMessage.java
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateOrganizationRequest.java
│   │   │   │   │   ├── CreateZoneRequest.java
│   │   │   │   │   └── CreateStreetRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── OrganizationResponse.java
│   │   │   │       ├── ZoneResponse.java
│   │   │   │       └── StreetResponse.java
│   │   │   └── mappers/
│   │   │       ├── OrganizationMapper.java
│   │   │       ├── ZoneMapper.java
│   │   │       └── StreetMapper.java
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           ├── OrganizationController.java
│   │       │           ├── ZoneController.java
│   │       │           └── StreetController.java
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── OrganizationDocument.java       → [CLASS] @Document("organizations")
│   │       │   │   ├── ZoneDocument.java               → [CLASS] @Document("zones")
│   │       │   │   ├── StreetDocument.java             → [CLASS] @Document("streets")
│   │       │   │   ├── FareDocument.java               → [CLASS] @Document("fares")
│   │       │   │   └── ParameterDocument.java          → [CLASS] @Document("parameters")
│   │       │   └── repositories/
│   │       │       ├── OrganizationMongoRepository.java → [INTERFACE] ReactiveMongoRepository
│   │       │       ├── ZoneMongoRepository.java
│   │       │       ├── StreetMongoRepository.java
│   │       │       ├── FareMongoRepository.java
│   │       │       └── ParameterMongoRepository.java
│   │       └── config/
│   │           ├── MongoConfig.java
│   │           ├── RabbitMQConfig.java
│   │           ├── SecurityConfig.java
│   │           └── RequestContextFilter.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 📦 ESTRUCTURA: vg-ms-payments-billing {#estructura-payments}

```
vg-ms-payments-billing/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmspayments/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Payment.java                        → [CLASS] Pago principal
│   │   │   │   ├── PaymentDetail.java                  → [CLASS] Detalles/desglose del pago
│   │   │   │   ├── Debt.java                           → [CLASS] Deuda pendiente
│   │   │   │   └── valueobjects/
│   │   │   │       ├── PaymentType.java                → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, RECONNECTION_FEE, etc.
│   │   │   │       ├── PaymentMethod.java              → [ENUM] CASH, BANK_TRANSFER, CARD, YAPE, PLIN
│   │   │   │       ├── PaymentStatus.java              → [ENUM] PENDING, COMPLETED, CANCELLED, FAILED
│   │   │   │       └── DebtStatus.java                 → [ENUM] PENDING, PARTIAL, PAID, CANCELLED
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreatePaymentUseCase.java      → [INTERFACE]
│   │   │   │   │   └── IGetPaymentUseCase.java         → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IPaymentRepository.java         → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── PaymentNotFoundException.java       → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   └── CreatePaymentUseCaseImpl.java       → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   └── CreatePaymentRequest.java       → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── PaymentResponse.java            → [CLASS]
│   │   │   └── mappers/
│   │   │       └── PaymentMapper.java                  → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           └── PaymentController.java      → [CLASS] @RestController
│   │       │                                              Lee headers: X-User-Id, X-Role, X-Organization-Id
│   │       │                                              ❌ NO llama REST a users ni organizations
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── PaymentEntity.java              → [CLASS] @Table("payments")
│   │       │   │   ├── PaymentDetailEntity.java        → [CLASS] @Table("payment_details")
│   │       │   │   └── DebtEntity.java                 → [CLASS] @Table("debts")
│   │       │   └── repositories/
│   │       │       ├── PaymentR2dbcRepository.java     → [INTERFACE]
│   │       │       ├── PaymentDetailR2dbcRepository.java → [INTERFACE]
│   │       │       └── DebtR2dbcRepository.java        → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_payments_table.sql
│           ├── V2__create_payment_details_table.sql
│           └── V3__create_debts_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-payments **NO necesita WebClientConfig** porque:

- userId viene en header `X-User-Id` (Gateway ya validó JWT)
- organizationId viene en header `X-Organization-Id`
- Solo lee headers y autoriza según reglas de negocio

---

## 📦 ESTRUCTURA: vg-ms-water-quality {#estructura-water-quality}

```
vg-ms-water-quality/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmswaterquality/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── TestingPoint.java                   → [CLASS] Puntos de muestreo
│   │   │   │   ├── QualityTest.java                    → [CLASS] Pruebas de calidad
│   │   │   │   └── valueobjects/
│   │   │   │       ├── PointType.java                  → [ENUM] RESERVOIR, TAP, WELL, SOURCE
│   │   │   │       ├── TestType.java                   → [ENUM] CHLORINE, PH, TURBIDITY, BACTERIOLOGICAL, CHEMICAL
│   │   │   │       ├── TestResult.java                 → [ENUM] APPROVED, REJECTED, REQUIRES_TREATMENT
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateMeasurementUseCase.java  → [INTERFACE]
│   │   │   │   │   └── IGetMeasurementUseCase.java     → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IWaterQualityRepository.java    → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── MeasurementNotFoundException.java   → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   └── CreateMeasurementUseCaseImpl.java   → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateMeasurementRequest.java   → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── WaterQualityResponse.java       → [CLASS]
│   │   │   └── mappers/
│   │   │       └── WaterQualityMapper.java             → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           └── WaterQualityController.java → [CLASS] @RestController
│   │       │                                              Lee headers: X-User-Id, X-Organization-Id
│   │       │                                              ❌ NO llama REST a users ni organizations
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── TestingPointDocument.java       → [CLASS] @Document("testing_points")
│   │       │   │   └── QualityTestDocument.java        → [CLASS] @Document("quality_tests")
│   │       │   └── repositories/
│   │       │       ├── TestingPointMongoRepository.java → [INTERFACE]
│   │       │       └── QualityTestMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-water-quality **NO necesita WebClientConfig** porque:

- ❌ NO necesita validar usuario por REST (viene en header)
- ❌ NO necesita validar organización por REST (viene en header)
- ❌ NO necesita `organization-service.token` (inseguro)

---

## 📦 ESTRUCTURA: vg-ms-distribution {#estructura-distribution}

```
vg-ms-distribution/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsdistribution/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── DistributionProgram.java            → [CLASS] Programa de distribución
│   │   │   │   ├── DistributionRoute.java              → [CLASS] Rutas de distribución
│   │   │   │   ├── DistributionSchedule.java           → [CLASS] Horarios de distribución
│   │   │   │   └── valueobjects/
│   │   │   │       ├── DayOfWeek.java                  → [ENUM] MONDAY, TUESDAY, WEDNESDAY, etc.
│   │   │   │       ├── DistributionStatus.java         → [ENUM] ACTIVE, INACTIVE, SUSPENDED
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateDistributionUseCase.java → [INTERFACE]
│   │   │   │   │   └── IGetDistributionUseCase.java    → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IDistributionRepository.java    → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── DistributionNotFoundException.java  → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   └── CreateDistributionUseCaseImpl.java  → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateDistributionRequest.java  → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── DistributionResponse.java       → [CLASS]
│   │   │   └── mappers/
│   │   │       └── DistributionMapper.java             → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           └── DistributionController.java → [CLASS] @RestController
│   │       │                                              Lee headers: X-Organization-Id, X-Role
│   │       │                                              ❌ NO llama REST a organizations
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── DistributionProgramDocument.java → [CLASS] @Document("distribution_programs")
│   │       │   │   ├── DistributionRouteDocument.java  → [CLASS] @Document("distribution_routes")
│   │       │   │   └── DistributionScheduleDocument.java → [CLASS] @Document("distribution_schedules")
│   │       │   └── repositories/
│   │       │       ├── DistributionProgramMongoRepository.java → [INTERFACE]
│   │       │       ├── DistributionRouteMongoRepository.java → [INTERFACE]
│   │       │       └── DistributionScheduleMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-distribution **NO necesita WebClientConfig** porque:

- ✅ MongoDB (JASS_DIGITAL) - documents, no entities
- ❌ NO necesita validar organización por REST
- ❌ NO necesita `organization-service.token` (inseguro)
- ✅ organizationId viene en header `X-Organization-Id`
- Gestiona: DistributionProgram, DistributionRoute, DistributionSchedule

---

## 📦 ESTRUCTURA: vg-ms-inventory-purchases {#estructura-inventory}

```
vg-ms-inventory-purchases/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsinventory/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Supplier.java                       → [CLASS] Proveedores
│   │   │   │   ├── Material.java                       → [CLASS] Materiales/Productos
│   │   │   │   ├── ProductCategory.java                → [CLASS] Categorías de productos
│   │   │   │   ├── Purchase.java                       → [CLASS] Orden de compra
│   │   │   │   ├── PurchaseDetail.java                 → [CLASS] Detalle de compra (líneas)
│   │   │   │   ├── InventoryMovement.java              → [CLASS] Kardex/movimientos
│   │   │   │   └── valueobjects/
│   │   │   │       ├── MovementType.java               → [ENUM] IN, OUT, ADJUSTMENT
│   │   │   │       ├── PurchaseStatus.java             → [ENUM] PENDING, RECEIVED, CANCELLED
│   │   │   │       ├── Unit.java                       → [ENUM] UNIT, METERS, KG, LITERS
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateProductUseCase.java      → [INTERFACE]
│   │   │   │   │   └── IRegisterKardexUseCase.java     → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IProductRepository.java         → [INTERFACE]
│   │   │   │       └── IKardexRepository.java          → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── ProductNotFoundException.java       → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateProductUseCaseImpl.java       → [CLASS] @Service
│   │   │   │   └── RegisterKardexUseCaseImpl.java      → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateProductRequest.java       → [CLASS]
│   │   │   │   │   └── KardexConsumptionRequest.java   → [CLASS]
│   │   │   │   └── response/
│   │   │   │       ├── ProductResponse.java            → [CLASS]
│   │   │   │       └── KardexResponse.java             → [CLASS]
│   │   │   └── mappers/
│   │   │       ├── ProductMapper.java                  → [CLASS] @Component
│   │   │       └── KardexMapper.java                   → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           ├── ProductController.java      → [CLASS] @RestController
│   │       │           └── KardexController.java       → [CLASS] @RestController
│   │       │                                              Lee headers: X-User-Id, X-Organization-Id
│   │       │                                              ❌ NO llama REST a users
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── SupplierEntity.java             → [CLASS] @Table("suppliers")
│   │       │   │   ├── MaterialEntity.java             → [CLASS] @Table("materials")
│   │       │   │   ├── ProductCategoryEntity.java      → [CLASS] @Table("product_categories")
│   │       │   │   ├── PurchaseEntity.java             → [CLASS] @Table("purchases")
│   │       │   │   ├── PurchaseDetailEntity.java       → [CLASS] @Table("purchase_details")
│   │       │   │   └── InventoryMovementEntity.java    → [CLASS] @Table("inventory_movements")
│   │       │   └── repositories/
│   │       │       ├── SupplierR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── MaterialR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── ProductCategoryR2dbcRepository.java → [INTERFACE]
│   │       │       ├── PurchaseR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── PurchaseDetailR2dbcRepository.java → [INTERFACE]
│   │       │       └── InventoryMovementR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_suppliers_table.sql
│           ├── V2__create_materials_table.sql
│           ├── V3__create_product_categories_table.sql
│           ├── V4__create_purchases_table.sql
│           ├── V5__create_purchase_details_table.sql
│           └── V6__create_inventory_movements_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-inventory **NO necesita WebClientConfig** porque:

- ❌ NO necesita llamar vg-ms-users por REST
- ✅ userId viene en header `X-User-Id` (Gateway ya validó)
- ❌ NO necesita JWT propagation filter (Gateway ya lo hace)

---

## 📦 ESTRUCTURA: vg-ms-claims-incidents {#estructura-claims}

```
vg-ms-claims-incidents/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsclaims/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Complaint.java                      → [CLASS] Quejas de clientes
│   │   │   │   ├── ComplaintCategory.java              → [CLASS] Categorías de quejas
│   │   │   │   ├── ComplaintResponse.java              → [CLASS] Respuestas a quejas
│   │   │   │   ├── Incident.java                       → [CLASS] Incidentes de infraestructura
│   │   │   │   ├── IncidentType.java                   → [CLASS] Tipos de incidentes
│   │   │   │   ├── IncidentResolution.java             → [CLASS] Resoluciones de incidentes
│   │   │   │   └── valueobjects/
│   │   │   │       ├── ComplaintPriority.java          → [ENUM] LOW, MEDIUM, HIGH, URGENT
│   │   │   │       ├── ComplaintStatus.java            → [ENUM] RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │       ├── ResponseType.java               → [ENUM] INVESTIGACION, SOLUCION, SEGUIMIENTO
│   │   │   │       ├── IncidentSeverity.java           → [ENUM] LOW, MEDIUM, HIGH, CRITICAL
│   │   │   │       ├── IncidentStatus.java             → [ENUM] REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │       ├── ResolutionType.java             → [ENUM] REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
│   │   │   │       ├── MaterialUsed.java               → [VALUE OBJECT] Embedded: productId, quantity, unit, unitCost
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateClaimUseCase.java        → [INTERFACE]
│   │   │   │   │   └── IGetClaimUseCase.java           → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IClaimRepository.java           → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── ClaimNotFoundException.java         → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   └── CreateClaimUseCaseImpl.java         → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateClaimRequest.java         → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── ClaimResponse.java              → [CLASS]
│   │   │   └── mappers/
│   │   │       └── ClaimMapper.java                    → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           └── ClaimController.java        → [CLASS] @RestController
│   │       │                                              Lee headers: X-User-Id, X-Username, X-Organization-Id
│   │       │                                              ❌ NO llama REST a users
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── ComplaintDocument.java          → [CLASS] @Document("complaints")
│   │       │   │   ├── ComplaintCategoryDocument.java  → [CLASS] @Document("complaint_categories")
│   │       │   │   ├── ComplaintResponseDocument.java  → [CLASS] @Document("complaint_responses")
│   │       │   │   ├── IncidentDocument.java           → [CLASS] @Document("incidents")
│   │       │   │   ├── IncidentTypeDocument.java       → [CLASS] @Document("incident_types")
│   │       │   │   └── IncidentResolutionDocument.java → [CLASS] @Document("incident_resolutions")
│   │       │   └── repositories/
│   │       │       ├── ComplaintMongoRepository.java   → [INTERFACE]
│   │       │       ├── ComplaintCategoryMongoRepository.java → [INTERFACE]
│   │       │       ├── ComplaintResponseMongoRepository.java → [INTERFACE]
│   │       │       ├── IncidentMongoRepository.java    → [INTERFACE]
│   │       │       ├── IncidentTypeMongoRepository.java → [INTERFACE]
│   │       │       └── IncidentResolutionMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-claims-incidents **NO necesita WebClientConfig** porque:

- ❌ NO necesita UserServiceClient
- ✅ userId y username vienen en headers `X-User-Id`, `X-Username`
- Crea reclamos con datos del header directamente

---

## 📦 ESTRUCTURA: vg-ms-infrastructure {#estructura-infrastructure}

```
vg-ms-infrastructure/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsinfrastructure/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── WaterBox.java                       → [CLASS] Caja de agua principal
│   │   │   │   ├── WaterBoxAssignment.java             → [CLASS] Asignación de caja a usuario
│   │   │   │   ├── WaterBoxTransfer.java               → [CLASS] Transferencia entre usuarios
│   │   │   │   └── valueobjects/
│   │   │   │       ├── BoxType.java                    → [ENUM] RESIDENTIAL, COMMERCIAL, COMMUNAL, INSTITUTIONAL
│   │   │   │       ├── AssignmentStatus.java           → [ENUM] ACTIVE, INACTIVE, TRANSFERRED
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE, SUSPENDED
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IAssignWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   └── ITransferWaterBoxUseCase.java   → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IWaterBoxRepository.java        → [INTERFACE]
│   │   │   │       ├── IWaterBoxAssignmentRepository.java → [INTERFACE]
│   │   │   │       └── IWaterBoxTransferRepository.java → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       ├── WaterBoxNotFoundException.java      → [CLASS]
│   │   │       └── WaterBoxAlreadyAssignedException.java → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateWaterBoxUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   ├── AssignWaterBoxUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   └── TransferWaterBoxUseCaseImpl.java    → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateWaterBoxRequest.java      → [CLASS]
│   │   │   │   │   ├── AssignWaterBoxRequest.java      → [CLASS]
│   │   │   │   │   └── TransferWaterBoxRequest.java    → [CLASS]
│   │   │   │   └── response/
│   │   │   │       ├── WaterBoxResponse.java           → [CLASS]
│   │   │   │       ├── WaterBoxAssignmentResponse.java → [CLASS]
│   │   │   │       └── WaterBoxTransferResponse.java   → [CLASS]
│   │   │   └── mappers/
│   │   │       ├── WaterBoxMapper.java                 → [CLASS] @Component
│   │   │       ├── WaterBoxAssignmentMapper.java       → [CLASS] @Component
│   │   │       └── WaterBoxTransferMapper.java         → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   └── in/
│   │       │       └── rest/
│   │       │           ├── WaterBoxController.java     → [CLASS] @RestController
│   │       │           ├── WaterBoxAssignmentController.java → [CLASS] @RestController
│   │       │           └── WaterBoxTransferController.java → [CLASS] @RestController
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── WaterBoxEntity.java             → [CLASS] @Table("water_boxes")
│   │       │   │   ├── WaterBoxAssignmentEntity.java   → [CLASS] @Table("water_box_assignments")
│   │       │   │   └── WaterBoxTransferEntity.java     → [CLASS] @Table("water_box_transfers")
│   │       │   └── repositories/
│   │       │       ├── WaterBoxR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── WaterBoxAssignmentR2dbcRepository.java → [INTERFACE]
│   │       │       └── WaterBoxTransferR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_water_boxes_table.sql
│           ├── V2__create_water_box_assignments_table.sql
│           └── V3__create_water_box_transfers_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-infrastructure gestiona **CAJAS DE AGUA (WaterBox)**:

- ✅ Crear cajas de agua (boxCode, boxType, installationDate)
- ✅ Asignar cajas a usuarios (currentAssignmentId)
- ✅ Transferir cajas entre usuarios (WaterBoxTransfer)
- ❌ NO gestiona calles ni zonas (eso es vg-ms-organizations)
- Lee headers para autorización: X-Organization-Id, X-Role

---

## 📦 ESTRUCTURA: vg-ms-notification {#estructura-notification}

```
vg-ms-notification/
├── src/
│   ├── index.ts                                        → [FILE] Express server
│   ├── routes/
│   │   └── whatsapp.routes.ts                          → [FILE] Rutas WhatsApp
│   ├── controllers/
│   │   └── whatsapp.controller.ts                      → [FILE] Lógica de envío
│   ├── services/
│   │   └── whatsapp.service.ts                         → [FILE] Twilio/WhatsApp API
│   ├── middlewares/
│   │   └── auth.middleware.ts                          → [FILE] Validación headers
│   └── config/
│       └── twilio.config.ts                            → [FILE] Configuración Twilio
│
├── package.json
├── tsconfig.json
├── Dockerfile
└── README.md
```

**NOTA IMPORTANTE**: vg-ms-notification **NO tiene conexiones salientes REST**.

- Node.js/TypeScript con Express
- Recibe requests de vg-ms-users para enviar WhatsApp
- Integración con Twilio API

---

## ⚙️ CONFIGURACIÓN APPLICATION.YML {#configuracion}

### 1️⃣ application.yml (BASE - Común)

```yaml
# ═══════════════════════════════════════════════════════════════
# CONFIGURACIÓN BASE - vg-ms-users
# Valores comunes para TODOS los perfiles (dev, prod)
# ═══════════════════════════════════════════════════════════════

spring:
  application:
    name: vg-ms-users

  # ═══════════════════ FLYWAY (Migraciones) ═══════════════════
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public

  # ═══════════════════ JACKSON ═══════════════════
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    time-zone: America/Lima

# ═══════════════════ SERVER ═══════════════════
server:
  port: 8081
  error:
    include-message: always
    include-binding-errors: always

# ═══════════════════ LOGGING ═══════════════════
logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsusers: DEBUG
    org.springframework.r2dbc: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# ═══════════════════ MANAGEMENT (Actuator) ═══════════════════
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# ═══════════════════ RESILIENCE4J ═══════════════════
resilience4j:
  circuitbreaker:
    instances:
      organizationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 50
      authenticationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50

  retry:
    instances:
      organizationService:
        max-attempts: 3
        wait-duration: 500ms
      authenticationService:
        max-attempts: 3
        wait-duration: 500ms
```

### 2️⃣ application-dev.yml (DESARROLLO - Docker Local)

```yaml
# ═══════════════════════════════════════════════════════════════
# PERFIL DE DESARROLLO (dev)
# Docker local en subsistema WSL/Linux
# Activar con: --spring.profiles.active=dev
# ═══════════════════════════════════════════════════════════════

spring:
  # ═══════════════════ R2DBC (PostgreSQL Reactive) ═══════════════════
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/vg_users
    username: jass_user
    password: jass2026
    pool:
      enabled: true
      initial-size: 10
      max-size: 20
      max-idle-time: 30m
      validation-query: SELECT 1

  # ═══════════════════ FLYWAY ═══════════════════
  flyway:
    url: jdbc:postgresql://localhost:5432/vg_users
    user: jass_user
    password: jass2026
    enabled: true

  # ═══════════════════ RABBITMQ ═══════════════════
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

# ═══════════════════ WEBCLIENT (REST Clients) ═══════════════════
services:
  organizations:
    url: http://localhost:8082
    timeout: 2000
  authentication:
    url: http://localhost:8090
    timeout: 2000

# ═══════════════════ LOGGING ═══════════════════
logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsusers: DEBUG
    org.springframework.r2dbc: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG
    org.springframework.amqp: DEBUG
    org.flywaydb: DEBUG
```

### 3️⃣ application-prod.yml (PRODUCCIÓN - Docker Compose)

```yaml
# ═══════════════════════════════════════════════════════════════
# PERFIL DE PRODUCCIÓN (prod/docker)
# Docker Compose con VPC interna
# Variables de entorno desde docker-compose.yml
# ═══════════════════════════════════════════════════════════════

spring:
  # ═══════════════════ R2DBC ═══════════════════
  r2dbc:
    url: ${SPRING_R2DBC_URL:r2dbc:postgresql://postgres:5432/vg_users}
    username: ${SPRING_R2DBC_USERNAME:jass_user}
    password: ${SPRING_R2DBC_PASSWORD:jass2026}
    pool:
      enabled: true
      initial-size: 20
      max-size: 50
      max-idle-time: 30m
      validation-query: SELECT 1

  # ═══════════════════ FLYWAY ═══════════════════
  flyway:
    url: jdbc:postgresql://postgres:5432/vg_users
    user: ${SPRING_R2DBC_USERNAME:jass_user}
    password: ${SPRING_R2DBC_PASSWORD:jass2026}
    enabled: true

  # ═══════════════════ RABBITMQ ═══════════════════
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:jass_user}
    password: ${RABBITMQ_PASSWORD:jass2026}
    virtual-host: ${RABBITMQ_VIRTUAL_HOST:jass}

# ═══════════════════ WEBCLIENT ═══════════════════
services:
  organizations:
    url: ${SERVICES_ORGANIZATIONS_URL:http://vg-ms-organizations:8082}
    timeout: 2000
  authentication:
    url: ${SERVICES_AUTHENTICATION_URL:http://vg-ms-authentication:8090}
    timeout: 2000

# ═══════════════════ LOGGING ═══════════════════
logging:
  level:
    root: WARN
    pe.edu.vallegrande.vgmsusers: INFO
    org.springframework.r2dbc: WARN
    io.r2dbc.postgresql.QUERY: WARN
```

---

## 📦 CÓDIGO COMPLETO PARA COPIAR {#codigo-completo}

### 🔧 UserEntity.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("user_code")
    private String userCode;  // USR-001

    @Column("organization_id")
    private UUID organizationId;

    @Column("username")
    private String username;  // Para login

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("document_type")
    private String documentType;  // DNI, CE, PASAPORTE

    @Column("document_number")
    private String documentNumber;

    @Column("email")
    private String email;  // NULLABLE - Zonas rurales

    @Column("phone")
    private String phone;  // NULLABLE - Zonas rurales

    @Column("address")
    private String address;

    @Column("street_id")
    private UUID streetId;  // Relación con vg-ms-organizations

    @Column("zone_id")
    private UUID zoneId;  // Relación con vg-ms-organizations

    @Column("status")
    private String status;  // ACTIVE, INACTIVE, SUSPENDED

    @Column("roles")
    private String roles;  // SUPER_ADMIN, ADMIN, CLIENT

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("created_by")
    private UUID createdBy;

    @Column("updated_by")
    private UUID updatedBy;
}
```

### 🔧 Role.java (ENUM)

```java
package pe.edu.vallegrande.vgmsusers.domain.models;

public enum Role {
    SUPER_ADMIN,  // Acceso total al sistema
    ADMIN,        // Administrador de organización
    CLIENT        // Usuario final
}
```

### 🔧 RabbitMQConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Exchanges
    public static final String USER_EXCHANGE = "jass.users.exchange";

    // Queues
    public static final String USER_CREATED_QUEUE = "jass.users.created.queue";
    public static final String USER_UPDATED_QUEUE = "jass.users.updated.queue";
    public static final String USER_DELETED_QUEUE = "jass.users.deleted.queue";

    // Routing Keys
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_DELETED_ROUTING_KEY = "user.deleted";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(USER_CREATED_QUEUE)
                .withArgument("x-message-ttl", 86400000)  // 24 horas
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(USER_UPDATED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userCreatedQueue)
                .to(userExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding userUpdatedBinding(Queue userUpdatedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userUpdatedQueue)
                .to(userExchange)
                .with(USER_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding userDeletedBinding(Queue userDeletedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userDeletedQueue)
                .to(userExchange)
                .with(USER_DELETED_ROUTING_KEY);
    }
}
```

### 🔧 RequestContextFilter.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class RequestContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Lee headers propagados desde el Gateway
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String role = exchange.getRequest().getHeaders().getFirst("X-Role");
        String organizationId = exchange.getRequest().getHeaders().getFirst("X-Organization-Id");

        if (userId == null || role == null) {
            log.debug("No user context headers found");
            return chain.filter(exchange);
        }

        // Crea Authentication SIN validar JWT (Gateway ya lo hizo)
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);

        UserContext userContext = new UserContext(userId, organizationId, role);
        authentication.setDetails(userContext);

        log.debug("Request context set for user: {} with role: {}", userId, role);

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    public static class UserContext {
        private final String userId;
        private final String organizationId;
        private final String role;

        public UserContext(String userId, String organizationId, String role) {
            this.userId = userId;
            this.organizationId = organizationId;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getOrganizationId() { return organizationId; }
        public String getRole() { return role; }
    }
}
```

### 🔧 SecurityConfig.java (Microservicio - Simplificado)

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity  // Habilita @PreAuthorize
public class SecurityConfig {

    private final RequestContextFilter requestContextFilter;

    public SecurityConfig(RequestContextFilter requestContextFilter) {
        this.requestContextFilter = requestContextFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**", "/health").permitAll()
                .anyExchange().authenticated()  // Requiere headers del Gateway
            )

            .addFilterAt(requestContextFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 🔧 Resilience4jConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .slowCallRateThreshold(50)
                .recordExceptions(
                        org.springframework.web.reactive.function.client.WebClientRequestException.class,
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class
                )
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig circuitBreakerConfig) {
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean(name = "organizationServiceCircuitBreaker")
    public CircuitBreaker organizationServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("organizationService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.warn("OrganizationService Circuit Breaker: {} -> {}",
                             event.getStateTransition().getFromState(),
                             event.getStateTransition().getToState()))
                .onError(event ->
                    log.error("OrganizationService Circuit Breaker error: {}",
                              event.getThrowable().getMessage()));

        return circuitBreaker;
    }

    @Bean(name = "authenticationServiceCircuitBreaker")
    public CircuitBreaker authenticationServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("authenticationService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.warn("AuthenticationService Circuit Breaker: {} -> {}",
                             event.getStateTransition().getFromState(),
                             event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(
                        org.springframework.web.reactive.function.client.WebClientRequestException.class,
                        java.io.IOException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class,
                        org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest.class
                )
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig retryConfig) {
        return RetryRegistry.of(retryConfig);
    }

    @Bean(name = "organizationServiceRetry")
    public Retry organizationServiceRetry(RetryRegistry registry) {
        Retry retry = registry.retry("organizationService");

        retry.getEventPublisher()
                .onRetry(event ->
                    log.warn("OrganizationService retry #{}: {}",
                             event.getNumberOfRetryAttempts(),
                             event.getLastThrowable().getMessage()));

        return retry;
    }
}
```

### 🔧 WebClientConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${services.organizations.url:http://vg-ms-organizations:8082}")
    private String organizationsServiceUrl;

    @Value("${services.authentication.url:http://vg-ms-authentication:8090}")
    private String authenticationServiceUrl;

    @Value("${services.organizations.timeout:2000}")
    private int timeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse());
    }

    @Bean(name = "organizationWebClient")
    public WebClient organizationWebClient(WebClient.Builder builder) {
        HttpClient httpClient = createHttpClient();
        return builder
                .baseUrl(organizationsServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean(name = "authenticationWebClient")
    public WebClient authenticationWebClient(WebClient.Builder builder) {
        HttpClient httpClient = createHttpClient();
        return builder
                .baseUrl(authenticationServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private HttpClient createHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
```

### 🔧 R2dbcConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableR2dbcRepositories(basePackages = "pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories")
@EnableR2dbcAuditing  // Habilita @CreatedDate y @LastModifiedDate
@EnableTransactionManagement
public class R2dbcConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```

---

## 🐳 DOCKER COMPOSE COMPLETO {#docker-compose}

```yaml
version: '3.8'

networks:
  jass-network:
    driver: bridge

volumes:
  postgres_data:
  mongodb_data:
  rabbitmq_data:

services:
  # ═══════════════════ DATABASES ═══════════════════
  postgres:
    image: postgres:16-alpine
    container_name: jass-postgres
    environment:
      POSTGRES_USER: jass_user
      POSTGRES_PASSWORD: jass2026
      POSTGRES_DB: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-postgres.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - jass-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U jass_user"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  mongodb:
    image: mongo:7-jammy
    container_name: jass-mongodb
    environment:
      MONGO_INITDB_ROOT_USERNAME: jass_user
      MONGO_INITDB_ROOT_PASSWORD: jass2026
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - jass-network
    healthcheck:
      test: echo 'db.runCommand("ping").ok' | mongosh localhost:27017/test --quiet
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: jass-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: jass_user
      RABBITMQ_DEFAULT_PASS: jass2026
      RABBITMQ_DEFAULT_VHOST: jass
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - jass-network
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # ═══════════════════ GATEWAY (Punto de entrada único) ═══════════════════
  vg-ms-gateway:
    build: ./vg-ms-gateway
    container_name: vg-gateway
    ports:
      - "8080:8080"  # ✅ ÚNICO PUERTO EXPUESTO PÚBLICAMENTE
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JWT_SECRET: ${JWT_SECRET}
      # Rutas a microservicios internos
      SERVICES_AUTHENTICATION_URL: http://vg-ms-authentication:8090
      SERVICES_USERS_URL: http://vg-ms-users:8081
      SERVICES_ORGANIZATIONS_URL: http://vg-ms-organizations:8082
      SERVICES_PAYMENTS_URL: http://vg-ms-payments:8083
    networks:
      - jass-network
    depends_on:
      - vg-ms-authentication
      - vg-ms-users
      - vg-ms-organizations
    restart: unless-stopped

  # ═══════════════════ AUTHENTICATION (Genera JWT) ═══════════════════
  vg-ms-authentication:
    build: ./vg-ms-authentication
    container_name: vg-authentication
    # ❌ NO exponer puerto público
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_authentication
      SPRING_R2DBC_USERNAME: jass_user
      SPRING_R2DBC_PASSWORD: jass2026
      JWT_SECRET: ${JWT_SECRET}
      SERVICES_USERS_URL: http://vg-ms-users:8081
    networks:
      - jass-network
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

  # ═══════════════════ USERS ═══════════════════
  vg-ms-users:
    build: ./vg-ms-users
    container_name: vg-users
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_users
      SPRING_R2DBC_USERNAME: jass_user
      SPRING_R2DBC_PASSWORD: jass2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: jass_user
      RABBITMQ_PASSWORD: jass2026
      RABBITMQ_VIRTUAL_HOST: jass
      SERVICES_AUTHENTICATION_URL: http://vg-ms-authentication:8090
      SERVICES_ORGANIZATIONS_URL: http://vg-ms-organizations:8082
    networks:
      - jass-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # ═══════════════════ ORGANIZATIONS (MongoDB) ═══════════════════
  vg-ms-organizations:
    build: ./vg-ms-organizations
    container_name: vg-organizations
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATA_MONGODB_URI: mongodb://jass_user:jass2026@mongodb:27017/JASS_DIGITAL?authSource=admin
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: jass_user
      RABBITMQ_PASSWORD: jass2026
      RABBITMQ_VIRTUAL_HOST: jass
    networks:
      - jass-network
    depends_on:
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # ═══════════════════ PAYMENTS ═══════════════════
  vg-ms-payments:
    build: ./vg-ms-payments-billing
    container_name: vg-payments
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_payments
      SPRING_R2DBC_USERNAME: jass_user
      SPRING_R2DBC_PASSWORD: jass2026
      SERVICES_USERS_URL: http://vg-ms-users:8081
    networks:
      - jass-network
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
```

### Variables de entorno (.env)

```bash
# JWT Secret (compartido entre Gateway y Authentication)
JWT_SECRET=VallegrrandeJASS2026SecretKeyMinimo32CaracteresParaHMACSHA256Seguridad

# PostgreSQL
POSTGRES_USER=jass_user
POSTGRES_PASSWORD=jass2026

# MongoDB
MONGO_USER=jass_user
MONGO_PASSWORD=jass2026

# RabbitMQ
RABBITMQ_USER=jass_user
RABBITMQ_PASSWORD=jass2026
RABBITMQ_VHOST=jass
```

---

## 📊 SCRIPTS DE MIGRACIÓN {#migraciones}

### init-postgres.sql

```sql
-- Crear bases de datos
CREATE DATABASE vg_authentication;
CREATE DATABASE vg_users;
CREATE DATABASE vg_payments;
CREATE DATABASE vg_distribution;
CREATE DATABASE vg_infrastructure;
CREATE DATABASE vg_inventory;

-- Conectar y habilitar extensiones
\c vg_authentication;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_users;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_payments;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_distribution;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_infrastructure;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_inventory;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### V1__create_users_table.sql

```sql
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_code VARCHAR(50) NOT NULL UNIQUE,
    organization_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(50),
    document_number VARCHAR(50),
    email VARCHAR(255),              -- NULLABLE
    phone VARCHAR(20),               -- NULLABLE
    address TEXT,
    street_id UUID,
    zone_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    roles VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Índices
CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_users_document ON users(organization_id, document_number);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_user_code ON users(user_code);
CREATE INDEX idx_users_street_id ON users(street_id);
CREATE INDEX idx_users_zone_id ON users(zone_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
```

### V1__create_credentials_table.sql (vg-ms-authentication)

```sql
CREATE TABLE IF NOT EXISTS credentials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Índices
CREATE INDEX idx_credentials_user_id ON credentials(user_id);
CREATE UNIQUE INDEX idx_credentials_username ON credentials(username);
```

---

## ✅ RESUMEN FINAL

```
┌──────────────────────────────────────────────────────────────────┐
│ ARQUITECTURA: Gateway-First + Distributed Authorization         │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ 1. CLIENTE → Gateway (puerto 8080 - único público)              │
│                                                                  │
│ 2. LOGIN:                                                        │
│    Gateway → vg-ms-authentication → Genera JWT                  │
│                                                                  │
│ 3. REQUESTS:                                                     │
│    Gateway valida JWT → Propaga headers → Microservicio         │
│                                                                  │
│ 4. REGISTRO:                                                     │
│    vg-ms-users guarda datos → llama a vg-ms-authentication      │
│                                                                  │
│ 5. RED INTERNA:                                                  │
│    Microservicios se llaman por nombres Docker                  │
│    Solo Gateway expuesto públicamente                           │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**VENTAJAS:**

- ✅ JWT validado UNA SOLA VEZ
- ✅ Microservicios NO necesitan JWT secret
- ✅ Separación Authentication vs Authorization
- ✅ Red VPC privada
- ✅ Cumple DDD/Hexagonal/Clean Code
- ✅ 100% Reactivo (WebFlux, R2DBC, MongoDB Reactive)
- ✅ Paquetería consistente (pe.edu.vallegrande.vgms*)

---

## 📊 ESQUEMAS DE BASE DE DATOS COMPLETOS {#esquemas-bd}

### 🗄️ Bases de Datos del Sistema

```
┌──────────────────────────────┬─────────────────────┬─────────────────────────┐
│ BASE DE DATOS                │ TIPO                │ MICROSERVICIOS          │
├──────────────────────────────┼─────────────────────┼─────────────────────────┤
│ vg_users                     │ PostgreSQL          │ vg-ms-users             │
│ vg_infrastructure            │ PostgreSQL          │ vg-ms-infrastructure    │
│ vg_payments                  │ PostgreSQL          │ vg-ms-payments          │
│ vg_inventory                 │ PostgreSQL          │ vg-ms-inventory         │
│ JASS_DIGITAL                 │ MongoDB             │ vg-ms-organizations     │
│ JASS_DIGITAL                 │ MongoDB             │ vg-ms-water-quality     │
│ JASS_DIGITAL                 │ MongoDB             │ vg-ms-claims-incidents  │
│ JASS_DIGITAL                 │ MongoDB             │ vg-ms-distribution      │
│ keycloak (externa)           │ PostgreSQL          │ vg-ms-authentication    │
└──────────────────────────────┴─────────────────────┴─────────────────────────┘
```

---

### 1️⃣ vg_users (PostgreSQL) - vg-ms-users

#### Tabla: users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_code VARCHAR(50) NOT NULL UNIQUE,
    organization_id VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(50),
    document_number VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    street_id VARCHAR(255),
    zone_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    roles VARCHAR(50) NOT NULL,
    profile_photo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_user_code UNIQUE (user_code),
    CONSTRAINT uk_users_document UNIQUE (organization_id, document_number),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT ck_users_roles CHECK (roles IN ('SUPER_ADMIN', 'ADMIN', 'CLIENT', 'OPERATOR', 'TECHNICIAN', 'CASHIER'))
);

CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_users_zone_id ON users(zone_id);
CREATE INDEX idx_users_street_id ON users(street_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_roles ON users(roles);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_document ON users(document_number);
```

**Campos Clave:**

- `user_code`: Código único generado automáticamente (USR-001, USR-002...)
- `organization_id`: Referencia a Organizations (MongoDB)
- `zone_id`: Referencia a Zones (MongoDB)
- `street_id`: Referencia a Streets (MongoDB)
- `roles`: SUPER_ADMIN, ADMIN, CLIENT, OPERATOR, TECHNICIAN, CASHIER

---

### 2️⃣ vg_infrastructure (PostgreSQL) - vg-ms-infrastructure

#### Tabla: water_boxes

```sql
CREATE TABLE water_boxes (
    id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(255) NOT NULL,
    box_code VARCHAR(50) NOT NULL UNIQUE,
    box_type VARCHAR(50) NOT NULL,
    installation_date DATE NOT NULL,
    location TEXT,
    meter_number VARCHAR(50),
    meter_brand VARCHAR(100),
    current_assignment_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    suspension_reason TEXT,
    suspended_by UUID,
    suspended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT uk_water_boxes_box_code UNIQUE (box_code),
    CONSTRAINT ck_water_boxes_type CHECK (box_type IN ('RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL', 'PUBLIC')),
    CONSTRAINT ck_water_boxes_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'MAINTENANCE'))
);

CREATE INDEX idx_water_boxes_organization_id ON water_boxes(organization_id);
CREATE INDEX idx_water_boxes_status ON water_boxes(status);
CREATE INDEX idx_water_boxes_box_code ON water_boxes(box_code);
CREATE INDEX idx_water_boxes_current_assignment ON water_boxes(current_assignment_id);
```

#### Tabla: water_box_assignments

```sql
CREATE TABLE water_box_assignments (
    id BIGSERIAL PRIMARY KEY,
    water_box_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    monthly_fee DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    transfer_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_assignments_water_box FOREIGN KEY (water_box_id) REFERENCES water_boxes(id) ON DELETE CASCADE,
    CONSTRAINT ck_assignments_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'TRANSFERRED')),
    CONSTRAINT ck_assignments_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_assignments_water_box_id ON water_box_assignments(water_box_id);
CREATE INDEX idx_assignments_user_id ON water_box_assignments(user_id);
CREATE INDEX idx_assignments_status ON water_box_assignments(status);
CREATE INDEX idx_assignments_active ON water_box_assignments(user_id, status) WHERE status = 'ACTIVE' AND end_date IS NULL;
CREATE INDEX idx_assignments_transfer_id ON water_box_assignments(transfer_id);
```

#### Tabla: water_box_transfers

```sql
CREATE TABLE water_box_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_code VARCHAR(50) NOT NULL UNIQUE,
    water_box_id BIGINT NOT NULL,
    old_assignment_id BIGINT NOT NULL,
    new_assignment_id BIGINT NOT NULL,
    transfer_date DATE NOT NULL,
    transfer_reason VARCHAR(100) NOT NULL,
    transfer_fee DECIMAL(10,2),
    documents JSONB,
    notes TEXT,
    approved_by UUID,
    approved_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,

    CONSTRAINT fk_transfers_water_box FOREIGN KEY (water_box_id) REFERENCES water_boxes(id) ON DELETE CASCADE,
    CONSTRAINT fk_transfers_old_assignment FOREIGN KEY (old_assignment_id) REFERENCES water_box_assignments(id),
    CONSTRAINT fk_transfers_new_assignment FOREIGN KEY (new_assignment_id) REFERENCES water_box_assignments(id),
    CONSTRAINT ck_transfers_reason CHECK (transfer_reason IN ('SALE', 'INHERITANCE', 'DONATION', 'OTHER')),
    CONSTRAINT ck_transfers_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_transfers_water_box_id ON water_box_transfers(water_box_id);
CREATE INDEX idx_transfers_transfer_date ON water_box_transfers(transfer_date);
CREATE INDEX idx_transfers_status ON water_box_transfers(status);
CREATE INDEX idx_transfers_approved_by ON water_box_transfers(approved_by);
```

---

### 3️⃣ vg_payments (PostgreSQL) - vg-ms-payments-billing

#### Tabla: payments

```sql
CREATE TABLE payments (
    payment_id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(255) NOT NULL,
    payment_code VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    water_box_id BIGINT,
    payment_type VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT NOW(),
    period VARCHAR(10),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    external_reference VARCHAR(255),
    receipt_number VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT uk_payments_payment_code UNIQUE (payment_code),
    CONSTRAINT uk_payments_receipt UNIQUE (receipt_number),
    CONSTRAINT ck_payments_type CHECK (payment_type IN ('MONTHLY_FEE', 'INSTALLATION_FEE', 'RECONNECTION_FEE', 'LATE_FEE', 'TRANSFER_FEE', 'SPECIAL_FEE')),
    CONSTRAINT ck_payments_method CHECK (payment_method IN ('CASH', 'TRANSFER', 'CARD', 'YAPE', 'PLIN')),
    CONSTRAINT ck_payments_status CHECK (payment_status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT ck_payments_amount CHECK (total_amount > 0)
);

CREATE INDEX idx_payments_organization_id ON payments(organization_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_water_box_id ON payments(water_box_id);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_period ON payments(period);
CREATE INDEX idx_payments_type ON payments(payment_type);
CREATE INDEX idx_payments_user_period ON payments(user_id, period);
```

#### Tabla: payment_details

```sql
CREATE TABLE payment_details (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_payment_details_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE,
    CONSTRAINT ck_payment_details_amount CHECK (amount >= 0)
);

CREATE INDEX idx_payment_details_payment_id ON payment_details(payment_id);
```

#### Tabla: debts

```sql
CREATE TABLE debts (
    id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    water_box_id BIGINT,
    debt_type VARCHAR(50) NOT NULL,
    original_amount DECIMAL(10,2) NOT NULL,
    remaining_amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,

    CONSTRAINT ck_debts_type CHECK (debt_type IN ('MONTHLY_FEE', 'LATE_FEE', 'DAMAGE_FEE', 'OTHER')),
    CONSTRAINT ck_debts_status CHECK (status IN ('PENDING', 'PARTIAL', 'PAID', 'CANCELLED')),
    CONSTRAINT ck_debts_amounts CHECK (remaining_amount <= original_amount AND remaining_amount >= 0)
);

CREATE INDEX idx_debts_user_id ON debts(user_id);
CREATE INDEX idx_debts_water_box_id ON debts(water_box_id);
CREATE INDEX idx_debts_status ON debts(status);
CREATE INDEX idx_debts_due_date ON debts(due_date);
```

---

### 4️⃣ vg_inventory (PostgreSQL) - vg-ms-inventory-purchases

#### Tabla: suppliers

```sql
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(255) NOT NULL,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    contact_name VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    supplier_type VARCHAR(50) NOT NULL,
    payment_terms VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT ck_suppliers_type CHECK (supplier_type IN ('MATERIALS', 'TOOLS', 'CHEMICALS', 'SERVICES', 'OTHER')),
    CONSTRAINT ck_suppliers_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_suppliers_supplier_code ON suppliers(supplier_code);
CREATE INDEX idx_suppliers_ruc ON suppliers(ruc);
CREATE INDEX idx_suppliers_status ON suppliers(status);
```

#### Tabla: product_categories

```sql
CREATE TABLE product_categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_category_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,

    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_category_id) REFERENCES product_categories(id),
    CONSTRAINT ck_categories_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_categories_category_code ON product_categories(category_code);
CREATE INDEX idx_categories_parent_id ON product_categories(parent_category_id);
```

#### Tabla: materials

```sql
CREATE TABLE materials (
    id BIGSERIAL PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    material_name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    unit VARCHAR(50) NOT NULL,
    min_stock DECIMAL(10,2) NOT NULL DEFAULT 0,
    max_stock DECIMAL(10,2),
    current_stock DECIMAL(10,2) NOT NULL DEFAULT 0,
    unit_cost DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_materials_category FOREIGN KEY (category_id) REFERENCES product_categories(id),
    CONSTRAINT ck_materials_unit CHECK (unit IN ('UNIT', 'METERS', 'KILOGRAMS', 'LITERS', 'BOX', 'PAIR', 'SET')),
    CONSTRAINT ck_materials_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    CONSTRAINT ck_materials_stock CHECK (current_stock >= 0)
);

CREATE INDEX idx_materials_material_code ON materials(material_code);
CREATE INDEX idx_materials_category_id ON materials(category_id);
CREATE INDEX idx_materials_status ON materials(status);
CREATE INDEX idx_materials_low_stock ON materials(current_stock, min_stock) WHERE current_stock < min_stock;
```

#### Tabla: purchases

```sql
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    purchase_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_type VARCHAR(50) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    invoice_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT ck_purchases_type CHECK (purchase_type IN ('MATERIALS', 'TOOLS', 'CHEMICALS', 'SERVICES', 'OTHER')),
    CONSTRAINT ck_purchases_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_purchases_total CHECK (total = subtotal + tax)
);

CREATE INDEX idx_purchases_purchase_code ON purchases(purchase_code);
CREATE INDEX idx_purchases_supplier_id ON purchases(supplier_id);
CREATE INDEX idx_purchases_purchase_date ON purchases(purchase_date);
CREATE INDEX idx_purchases_status ON purchases(status);
```

#### Tabla: purchase_details

```sql
CREATE TABLE purchase_details (
    id BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_purchase_details_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_details_material FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT ck_purchase_details_quantity CHECK (quantity > 0),
    CONSTRAINT ck_purchase_details_subtotal CHECK (subtotal = quantity * unit_cost)
);

CREATE INDEX idx_purchase_details_purchase_id ON purchase_details(purchase_id);
CREATE INDEX idx_purchase_details_material_id ON purchase_details(material_id);
```

#### Tabla: inventory_movements

```sql
CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    movement_code VARCHAR(50) NOT NULL UNIQUE,
    movement_type VARCHAR(20) NOT NULL,
    movement_date TIMESTAMP NOT NULL DEFAULT NOW(),
    reason VARCHAR(100) NOT NULL,
    purchase_id BIGINT,
    complaint_id VARCHAR(100),
    material_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    balance_after DECIMAL(10,2) NOT NULL,
    requested_by UUID,
    approved_by UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,

    CONSTRAINT fk_movements_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id),
    CONSTRAINT fk_movements_material FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT ck_movements_type CHECK (movement_type IN ('INPUT', 'OUTPUT', 'ADJUSTMENT', 'TRANSFER')),
    CONSTRAINT ck_movements_reason CHECK (reason IN ('PURCHASE', 'SALE', 'MAINTENANCE', 'DAMAGE', 'ADJUSTMENT', 'DONATION', 'THEFT', 'OTHER')),
    CONSTRAINT ck_movements_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_movements_movement_code ON inventory_movements(movement_code);
CREATE INDEX idx_movements_material_id ON inventory_movements(material_id);
CREATE INDEX idx_movements_movement_date ON inventory_movements(movement_date);
CREATE INDEX idx_movements_movement_type ON inventory_movements(movement_type);
CREATE INDEX idx_movements_complaint_id ON inventory_movements(complaint_id);
```

---

### 5️⃣ JASS_DIGITAL (MongoDB) - vg-ms-organizations

#### Collection: organizations

```javascript
{
  _id: ObjectId("..."),
  organizationCode: "JASS-001",
  name: "JASS Comunidad San Pedro",
  ruc: "20123456789",
  address: "Jr. Los Andes 123",
  district: "San Pedro",
  province: "Huaylas",
  region: "Ancash",
  phone: "987654321",
  email: "admin@jass001.com",
  legalRepresentative: "Carlos Administrador",
  foundationDate: ISODate("2020-01-15"),
  status: "ACTIVE", // ACTIVE, INACTIVE
  configuration: {
    timezone: "America/Lima",
    currency: "PEN",
    language: "es"
  },
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  updatedAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: "super-admin-001",
  updatedBy: "super-admin-001"
}
```

**Índices:**

```javascript
db.organizations.createIndex({ "organizationCode": 1 }, { unique: true })
db.organizations.createIndex({ "ruc": 1 }, { unique: true })
db.organizations.createIndex({ "status": 1 })
```

#### Collection: zones

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  zoneCode: "ZONA-A",
  zoneName: "Zona Alta",
  description: "Sector alto de la comunidad",
  waterSourceType: "SPRING", // SPRING, WELL, RESERVOIR, RIVER
  estimatedPopulation: 150,
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  updatedAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: UUID("..."),
  updatedBy: UUID("...")
}
```

**Índices:**

```javascript
db.zones.createIndex({ "organizationId": 1, "zoneCode": 1 }, { unique: true })
db.zones.createIndex({ "organizationId": 1 })
db.zones.createIndex({ "status": 1 })
```

#### Collection: streets

```javascript
{
  _id: ObjectId("..."),
  zoneId: ObjectId("..."),
  streetCode: "CALLE-01",
  streetName: "Jr. Los Andes",
  streetType: "JR", // JR, AV, CA, PS, MZ
  totalHouses: 45,
  totalWaterBoxes: 42,
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  updatedAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: UUID("..."),
  updatedBy: UUID("...")
}
```

**Índices:**

```javascript
db.streets.createIndex({ "zoneId": 1, "streetCode": 1 }, { unique: true })
db.streets.createIndex({ "zoneId": 1 })
db.streets.createIndex({ "status": 1 })
```

#### Collection: fares

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  fareType: "MONTHLY_FEE", // MONTHLY_FEE, INSTALLATION_FEE, RECONNECTION_FEE, LATE_FEE, TRANSFER_FEE
  amount: 15.00,
  description: "Cuota mensual de agua",
  validFrom: ISODate("2024-01-01"),
  validUntil: null, // null = vigencia indefinida
  zoneId: null, // null = aplica a todas las zonas
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: UUID("...")
}
```

**Índices:**

```javascript
db.fares.createIndex({ "organizationId": 1, "fareType": 1 })
db.fares.createIndex({ "status": 1, "validFrom": 1 })
```

#### Collection: parameters

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  parameterKey: "SPECIAL_FEE_2024_TANK",
  parameterValue: "100.00",
  dataType: "DECIMAL", // STRING, DECIMAL, INTEGER, BOOLEAN, DATE
  description: "Aporte para construcción de tanque elevado",
  category: "SPECIAL_FEE",
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  updatedAt: ISODate("2024-01-01T00:00:00Z")
}
```

---

### 6️⃣ JASS_DIGITAL (MongoDB) - vg-ms-water-quality

#### Collection: testing_points

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  pointCode: "TP-001",
  pointName: "Reservorio Principal",
  pointType: "RESERVOIR", // RESERVOIR, TANK, NETWORK, SPRING, WELL
  zoneId: ObjectId("..."),
  location: "Parte alta del cerro",
  coordinates: {
    latitude: -12.0464,
    longitude: -77.0428
  },
  capacity: "10000 L",
  installationDate: ISODate("2020-05-01"),
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: UUID("...")
}
```

**Índices:**

```javascript
db.testing_points.createIndex({ "organizationId": 1, "pointCode": 1 }, { unique: true })
db.testing_points.createIndex({ "zoneId": 1 })
db.testing_points.createIndex({ "coordinates": "2dsphere" })
```

#### Collection: quality_tests

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  testCode: "TEST-2024-001",
  testingPointIds: [ObjectId("..."), ObjectId("...")],
  testDate: ISODate("2024-01-15T09:00:00Z"),
  testType: "COMPLETE", // COMPLETE, BASIC, VERIFICATION, SPECIAL
  testedByUserId: UUID("operator-456"),
  weatherConditions: "Soleado",
  waterTemperature: 18.5,
  results: [
    {
      parameter: "CHLORINE",
      value: 0.8,
      unit: "mg/L",
      minLimit: 0.5,
      maxLimit: 1.5,
      status: "WITHIN_LIMITS", // WITHIN_LIMITS, OUT_OF_LIMITS
      observation: "Normal"
    },
    {
      parameter: "PH",
      value: 7.2,
      unit: "pH",
      minLimit: 6.5,
      maxLimit: 8.5,
      status: "WITHIN_LIMITS",
      observation: "Normal"
    },
    {
      parameter: "TURBIDITY",
      value: 3.5,
      unit: "NTU",
      minLimit: 0,
      maxLimit: 5.0,
      status: "WITHIN_LIMITS",
      observation: "Aceptable"
    },
    {
      parameter: "BACTERIA",
      value: 2,
      unit: "UFC/100ml",
      minLimit: 0,
      maxLimit: 0,
      status: "OUT_OF_LIMITS",
      observation: "Requiere tratamiento"
    }
  ],
  generalObservations: "Se detectó presencia de bacterias. Se recomienda cloración",
  photos: ["test_chlorine.jpg", "test_ph.jpg"],
  treatment: {
    treatmentDate: ISODate("2024-01-15T14:00:00Z"),
    treatmentType: "CHLORINATION",
    chlorineDose: "2.0 mg/L",
    treatedByUserId: UUID("operator-456"),
    materialsUsed: [
      {
        materialId: "MAT-CHLORINE",
        quantity: 5,
        unit: "kg"
      }
    ],
    observations: "Se aplicó tratamiento de choque con cloro",
    nextTestDate: ISODate("2024-01-16")
  },
  status: "REQUIRES_ACTION", // APPROVED, REQUIRES_ACTION, IN_TREATMENT
  createdAt: ISODate("2024-01-15T09:00:00Z"),
  updatedAt: ISODate("2024-01-15T14:00:00Z")
}
```

**Índices:**

```javascript
db.quality_tests.createIndex({ "testCode": 1 }, { unique: true })
db.quality_tests.createIndex({ "organizationId": 1, "testDate": -1 })
db.quality_tests.createIndex({ "testedByUserId": 1 })
db.quality_tests.createIndex({ "status": 1 })
db.quality_tests.createIndex({ "testingPointIds": 1 })
```

---

### 7️⃣ JASS_DIGITAL (MongoDB) - vg-ms-claims-incidents

#### Collection: complaints (Quejas/Reclamos)

```javascript
{
  _id: ObjectId("..."),
  organizationId: ObjectId("..."),
  complaintCode: "CLAIM-2024-001",
  userId: ObjectId("..."),              // Cliente que reporta
  categoryId: ObjectId("..."),          // Referencia a complaint_categories
  waterBoxId: ObjectId("..."),          // Caja de agua relacionada
  complaintDate: ISODate("2024-01-20T08:00:00Z"),
  subject: "Fuga de agua en mi calle",
  description: "Fuga en tubería principal de Jr. Los Andes",
  priority: "HIGH",                      // LOW, MEDIUM, HIGH, CRITICAL
  status: "RECEIVED",                    // RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
  assignedToUserId: ObjectId("..."),     // Técnico asignado
  expectedResolutionDate: ISODate("2024-01-21"),
  actualResolutionDate: ISODate("2024-01-20T14:00:00Z"),
  satisfactionRating: 5,                 // 1-5 cuando se resuelve
  createdAt: ISODate("2024-01-20T08:00:00Z")
}
```

**Índices:**

```javascript
db.complaints.createIndex({ "complaintCode": 1 }, { unique: true })
db.complaints.createIndex({ "organizationId": 1, "createdAt": -1 })
db.complaints.createIndex({ "userId": 1 })
db.complaints.createIndex({ "assignedToUserId": 1 })
db.complaints.createIndex({ "status": 1 })
db.complaints.createIndex({ "categoryId": 1 })
db.complaints.createIndex({ "priority": 1, "status": 1 })
```

#### Collection: complaint_categories (Categorías de Quejas)

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  categoryCode: "CAT-WATER-LEAK",
  categoryName: "Fuga de Agua",
  description: "Reclamos relacionados con fugas de agua",
  priorityLevel: "HIGH",                 // LOW, MEDIUM, HIGH, CRITICAL
  maxResponseTime: 24,                   // Horas máximas de respuesta
  status: "ACTIVE",                      // ACTIVE, INACTIVE
  createdAt: ISODate("2024-01-01T00:00:00Z")
}
```

**Índices:**

```javascript
db.complaint_categories.createIndex({ "organizationId": 1, "categoryCode": 1 }, { unique: true })
db.complaint_categories.createIndex({ "status": 1 })
db.complaint_categories.createIndex({ "priorityLevel": 1 })
```

#### Collection: complaint_responses (Respuestas a Quejas)

```javascript
{
  _id: ObjectId("..."),
  complaintId: ObjectId("..."),          // Referencia a complaints
  responseDate: ISODate("2024-01-20T10:00:00Z"),
  responseType: "INVESTIGACION",         // INVESTIGACION, SOLUCION, SEGUIMIENTO
  message: "Técnico enviado al lugar para evaluar la fuga",
  respondedByUserId: "admin-001",        // Usuario que responde
  internalNotes: "Notas internas para el equipo",
  createdAt: ISODate("2024-01-20T10:00:00Z")
}
```

**Índices:**

```javascript
db.complaint_responses.createIndex({ "complaintId": 1 })
db.complaint_responses.createIndex({ "responseDate": -1 })
db.complaint_responses.createIndex({ "respondedByUserId": 1 })
```

#### Collection: incidents (Incidentes de Infraestructura)

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  incidentCode: "INC-2024-001",
  incidentTypeId: ObjectId("..."),       // Referencia a incident_types
  incidentCategory: "PIPE_BURST",
  zoneId: ObjectId("..."),               // Zona afectada
  incidentDate: ISODate("2024-01-20T08:00:00Z"),
  title: "Rotura de tubería principal",
  description: "Tubería principal rota afectando 15 viviendas",
  severity: "CRITICAL",                  // LOW, MEDIUM, HIGH, CRITICAL
  status: "REPORTED",                    // REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED
  affectedBoxesCount: 15,                // Número de cajas afectadas
  reportedByUserId: "operator-456",      // Quien reporta
  assignedToUserId: "tech-123",          // Técnico asignado
  resolvedByUserId: "tech-123",          // Quien resolvió
  resolved: false,
  resolutionNotes: "Tubería reemplazada, servicio restaurado",
  recordStatus: "ACTIVE"                 // ACTIVE, INACTIVE
}
```

**Índices:**

```javascript
db.incidents.createIndex({ "incidentCode": 1 }, { unique: true })
db.incidents.createIndex({ "organizationId": 1, "incidentDate": -1 })
db.incidents.createIndex({ "incidentTypeId": 1 })
db.incidents.createIndex({ "zoneId": 1 })
db.incidents.createIndex({ "status": 1 })
db.incidents.createIndex({ "severity": 1, "status": 1 })
db.incidents.createIndex({ "assignedToUserId": 1 })
db.incidents.createIndex({ "resolved": 1 })
```

#### Collection: incident_types (Tipos de Incidentes)

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  typeCode: "TYPE-PIPE-BURST",
  typeName: "Rotura de Tubería",
  description: "Incidentes relacionados con roturas en tuberías",
  priorityLevel: "HIGH",                 // LOW, MEDIUM, HIGH, CRITICAL
  estimatedResolutionTime: 4,            // Horas estimadas
  requiresExternalService: false,        // ¿Requiere servicio externo?
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z")
}
```

**Índices:**

```javascript
db.incident_types.createIndex({ "organizationId": 1, "typeCode": 1 }, { unique: true })
db.incident_types.createIndex({ "status": 1 })
db.incident_types.createIndex({ "priorityLevel": 1 })
```

#### Collection: incident_resolutions (Resoluciones de Incidentes)

```javascript
{
  _id: ObjectId("..."),
  incidentId: ObjectId("..."),           // Referencia a incidents
  resolutionDate: ISODate("2024-01-20T14:00:00Z"),
  resolutionType: "REPARACION_COMPLETA", // REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
  actionsTaken: "Se reemplazó tubería principal de 2 pulgadas por 10 metros",
  materialsUsed: [                       // Embedded documents (MaterialUsed)
    {
      productId: "MAT-001",
      quantity: 10,
      unit: "METROS",
      unitCost: 15.00
    },
    {
      productId: "MAT-015",
      quantity: 2,
      unit: "UNIT",
      unitCost: 8.00
    }
  ],
  laborHours: 4,                         // Horas de trabajo
  totalCost: 166.00,                     // Costo total (materiales + mano de obra)
  resolvedByUserId: "tech-123",
  qualityCheck: true,                    // ¿Se hizo verificación de calidad?
  followUpRequired: false,               // ¿Requiere seguimiento?
  resolutionNotes: "Trabajo completado satisfactoriamente",
  createdAt: ISODate("2024-01-20T14:00:00Z")
}
```

**Índices:**

```javascript
db.incident_resolutions.createIndex({ "incidentId": 1 })
db.incident_resolutions.createIndex({ "resolutionDate": -1 })
db.incident_resolutions.createIndex({ "resolvedByUserId": 1 })
db.incident_resolutions.createIndex({ "resolutionType": 1 })
db.incident_resolutions.createIndex({ "qualityCheck": 1 })
### 8️⃣ JASS_DIGITAL (MongoDB) - vg-ms-distribution

#### Collection: distribution_schedules

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  scheduleCode: "SCH-ZONA-A",
  scheduleName: "Horario Zona Alta",
  startTime: "06:00",
  endTime: "12:00",
  daysOfWeek: ["MONDAY", "WEDNESDAY", "FRIDAY"],
  description: "Horario de distribución para zona alta",
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: UUID("...")
}
```

**Índices:**

```javascript
db.distribution_schedules.createIndex({ "organizationId": 1, "scheduleCode": 1 }, { unique: true })
db.distribution_schedules.createIndex({ "status": 1 })
```

#### Collection: distribution_routes

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  routeCode: "ROUTE-001",
  routeName: "Ruta Zona Alta",
  description: "Ruta principal para zona alta",
  zones: [ObjectId("zone-456")],
  streets: [ObjectId("street-789"), ObjectId("street-790")],
  estimatedUsers: 150,
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z")
}
```

**Índices:**

```javascript
db.distribution_routes.createIndex({ "organizationId": 1, "routeCode": 1 }, { unique: true })
db.distribution_routes.createIndex({ "zones": 1 })
```

#### Collection: distribution_programs

```javascript
{
  _id: ObjectId("..."),
  organizationId: "org-123",
  programCode: "DIST-ZONA-A-2024",
  zoneId: ObjectId("..."),
  streetIds: [ObjectId("...")],
  scheduleId: ObjectId("..."),
  routeId: ObjectId("..."),
  startDate: ISODate("2024-01-01"),
  endDate: ISODate("2024-12-31"),
  daysOfWeek: ["MONDAY", "WEDNESDAY", "FRIDAY"],
  startTime: "06:00",
  endTime: "12:00",
  waterFlowRate: "2.5 L/s",
  estimatedPressure: "15 PSI",
  status: "ACTIVE",
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  createdBy: UUID("...")
}
```

**Índices:**

```javascript
db.distribution_programs.createIndex({ "organizationId": 1, "programCode": 1 }, { unique: true })
db.distribution_programs.createIndex({ "zoneId": 1 })
db.distribution_programs.createIndex({ "scheduleId": 1 })
db.distribution_programs.createIndex({ "status": 1 })
```

---

## 🌐 ESTÁNDAR DE APIs REST - TODOS LOS MICROSERVICIOS {#estandar-apis}

### 📋 Convenciones Generales

```
┌──────────────────────────────────────────────────────────────────────┐
│ ESTÁNDAR REST API - SISTEMA JASS                                    │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│ Base URL: http://{host}:{port}/api                                  │
│ Content-Type: application/json                                      │
│ Authorization: Bearer {JWT_TOKEN}                                   │
│                                                                      │
│ MÉTODOS HTTP:                                                        │
│ • GET     → Consultar recursos (lista o individual)                 │
│ • POST    → Crear nuevo recurso                                     │
│ • PUT     → Actualizar recurso completo                             │
│ • PATCH   → Actualizar parcialmente                                 │
│ • DELETE  → Eliminar recurso (soft delete)                          │
│                                                                      │
│ CÓDIGOS DE RESPUESTA:                                                │
│ • 200 OK              → Operación exitosa                            │
│ • 201 Created         → Recurso creado                               │
│ • 204 No Content      → Operación exitosa sin respuesta              │
│ • 400 Bad Request     → Datos inválidos                              │
│ • 401 Unauthorized    → Sin autenticación                            │
│ • 403 Forbidden       → Sin permisos                                 │
│ • 404 Not Found       → Recurso no encontrado                        │
│ • 409 Conflict        → Conflicto (duplicado, etc.)                  │
│ • 500 Internal Error  → Error del servidor                           │
│                                                                      │
│ PAGINACIÓN (Query Params):                                           │
│ • page=0              → Número de página (base 0)                    │
│ • size=20             → Tamaño de página (default 20)                │
│ • sort=field,asc|desc → Ordenamiento                                 │
│                                                                      │
│ FILTROS (Query Params):                                              │
│ • status=ACTIVE       → Filtrar por estado                           │
│ • search=keyword      → Búsqueda general                             │
│ • startDate=2024-01-01→ Rango de fechas (inicio)                     │
│ • endDate=2024-12-31  → Rango de fechas (fin)                        │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### Estructura de Respuesta Estándar

```json
// Respuesta Exitosa (200, 201)
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": {
    // Datos del recurso
  },
  "timestamp": "2024-01-21T10:30:00Z"
}

// Respuesta con Lista Paginada
{
  "status": "success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  },
  "timestamp": "2024-01-21T10:30:00Z"
}

// Respuesta de Error
{
  "status": "error",
  "message": "Error description",
  "errors": [
    {
      "field": "email",
      "message": "Email is required",
      "code": "VALIDATION_ERROR"
    }
  ],
  "timestamp": "2024-01-21T10:30:00Z",
  "path": "/api/users"
}
```

---

### 1️⃣ vg-ms-authentication (Puerto 8090)

```
BASE: http://localhost:8090/api/auth
```

#### Endpoints

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | `/register` | Registro de usuario | No |
| POST | `/login` | Login (genera JWT) | No |
| POST | `/refresh-token` | Renovar token | Sí (Refresh Token) |
| POST | `/logout` | Cerrar sesión | Sí |
| POST | `/change-password` | Cambiar contraseña | Sí |
| POST | `/forgot-password` | Recuperar contraseña | No |
| POST | `/reset-password` | Resetear contraseña | No (Token de email) |
| GET | `/validate-token` | Validar JWT | Sí |

#### Ejemplos de Uso

**POST /api/auth/register**

```json
// Request
{
  "userId": "7f3e4d2a-...",
  "username": "juan.perez",
  "password": "SecurePass123!",
  "email": "juan@example.com"
}

// Response 201 Created
{
  "status": "success",
  "message": "User registered successfully",
  "data": {
    "userId": "7f3e4d2a-...",
    "username": "juan.perez",
    "createdAt": "2024-01-21T10:00:00Z"
  }
}
```

**POST /api/auth/login**

```json
// Request
{
  "username": "juan.perez",
  "password": "SecurePass123!"
}

// Response 200 OK
{
  "status": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": "7f3e4d2a-...",
    "username": "juan.perez",
    "roles": ["CLIENT"]
  }
}
```

---

### 2️⃣ vg-ms-users (Puerto 8081)

```
BASE: http://localhost:8081/api/users
```

#### Endpoints

| Método | Endpoint | Descripción | Roles Permitidos |
|--------|----------|-------------|------------------|
| GET | `/` | Listar usuarios (paginado) | ADMIN, SUPER_ADMIN |
| GET | `/{id}` | Obtener usuario por ID | Todos |
| GET | `/me` | Obtener perfil actual | Todos |
| GET | `/username/{username}` | Buscar por username | ADMIN |
| GET | `/document/{docNumber}` | Buscar por documento | ADMIN |
| GET | `/organization/{orgId}` | Usuarios por organización | ADMIN |
| GET | `/zone/{zoneId}` | Usuarios por zona | ADMIN |
| GET | `/street/{streetId}` | Usuarios por calle | ADMIN |
| POST | `/` | Crear usuario | ADMIN, SUPER_ADMIN |
| PUT | `/{id}` | Actualizar usuario completo | ADMIN |
| PATCH | `/{id}` | Actualizar parcialmente | Usuario mismo o ADMIN |
| PATCH | `/{id}/status` | Cambiar estado | ADMIN |
| DELETE | `/{id}` | Eliminar usuario (soft) | SUPER_ADMIN |

#### Ejemplos de Uso

**GET /api/users?page=0&size=20&status=ACTIVE&role=CLIENT**

```json
// Response 200 OK
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": "7f3e4d2a-...",
        "userCode": "USR-001",
        "username": "juan.perez",
        "firstName": "Juan",
        "lastName": "Pérez García",
        "email": "juan@example.com",
        "phone": "912345678",
        "organizationId": "org-123",
        "zoneId": "zone-456",
        "streetId": "street-789",
        "roles": "CLIENT",
        "status": "ACTIVE",
        "createdAt": "2024-01-15T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**POST /api/users**

```json
// Request
{
  "username": "maria.lopez",
  "firstName": "María",
  "lastName": "López",
  "password": "TempPass123!",
  "organizationId": "org-123",
  "zoneId": "zone-456",
  "streetId": "street-789",
  "roles": "CLIENT",
  "documentType": "DNI",
  "documentNumber": "87654321",
  "phone": "987654321",
  "address": "Jr. Los Andes 789"
}

// Response 201 Created
{
  "status": "success",
  "message": "User created successfully",
  "data": {
    "id": "uuid-...",
    "userCode": "USR-002",
    "username": "maria.lopez",
    "createdAt": "2024-01-21T10:00:00Z"
  }
}
```

---

### 3️⃣ vg-ms-organizations (Puerto 8082)

```
BASE: http://localhost:8082/api
```

#### Endpoints - Organizations

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/organizations` | Listar organizaciones | SUPER_ADMIN |
| GET | `/organizations/{id}` | Obtener por ID | ADMIN |
| POST | `/organizations` | Crear organización | SUPER_ADMIN |
| PUT | `/organizations/{id}` | Actualizar | SUPER_ADMIN |
| DELETE | `/organizations/{id}` | Eliminar | SUPER_ADMIN |

#### Endpoints - Zones

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/zones` | Listar zonas | ADMIN |
| GET | `/zones/{id}` | Obtener zona | ADMIN |
| GET | `/zones/organization/{orgId}` | Zonas por org | ADMIN |
| POST | `/zones` | Crear zona | ADMIN |
| PUT | `/zones/{id}` | Actualizar zona | ADMIN |
| DELETE | `/zones/{id}` | Eliminar zona | ADMIN |

#### Endpoints - Streets

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/streets` | Listar calles | ADMIN |
| GET | `/streets/{id}` | Obtener calle | ADMIN |
| GET | `/streets/zone/{zoneId}` | Calles por zona | ADMIN |
| POST | `/streets` | Crear calle | ADMIN |
| PUT | `/streets/{id}` | Actualizar calle | ADMIN |
| DELETE | `/streets/{id}` | Eliminar calle | ADMIN |

#### Endpoints - Fares (Tarifas)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/fares` | Listar tarifas | ADMIN |
| GET | `/fares/{id}` | Obtener tarifa | ADMIN |
| GET | `/fares/organization/{orgId}` | Por organización | ADMIN |
| GET | `/fares/by-type/{type}` | Por tipo | ADMIN |
| GET | `/fares/by-zone/{zoneId}` | Por zona | ADMIN |
| POST | `/fares` | Crear tarifa | ADMIN |
| PUT | `/fares/{id}` | Actualizar tarifa | ADMIN |
| DELETE | `/fares/{id}` | Eliminar tarifa | ADMIN |

#### Ejemplos de Uso

**GET /api/zones/organization/{orgId}**

```json
// Response 200 OK
{
  "status": "success",
  "data": [
    {
      "_id": "zone-456",
      "organizationId": "org-123",
      "zoneCode": "ZONA-A",
      "zoneName": "Zona Alta",
      "description": "Sector alto de la comunidad",
      "waterSourceType": "SPRING",
      "estimatedPopulation": 150,
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

**POST /api/streets**

```json
// Request
{
  "zoneId": "zone-456",
  "streetCode": "CALLE-01",
  "streetName": "Jr. Los Andes",
  "streetType": "JR",
  "totalHouses": 45,
  "status": "ACTIVE"
}

// Response 201 Created
{
  "status": "success",
  "message": "Street created successfully",
  "data": {
    "_id": "street-789",
    "streetCode": "CALLE-01",
    "createdAt": "2024-01-21T10:00:00Z"
  }
}
```

---

### 4️⃣ vg-ms-infrastructure (Puerto 8088)

```
BASE: http://localhost:8088/api
```

#### Endpoints - Water Boxes

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/water-boxes` | Listar cajas | ADMIN |
| GET | `/water-boxes/{id}` | Obtener caja | ADMIN |
| GET | `/water-boxes/organization/{orgId}` | Por organización | ADMIN |
| GET | `/water-boxes/code/{boxCode}` | Por código | ADMIN |
| POST | `/water-boxes` | Crear caja | ADMIN |
| PUT | `/water-boxes/{id}` | Actualizar caja | ADMIN |
| PATCH | `/water-boxes/{id}/status` | Cambiar estado (corte) | ADMIN |
| DELETE | `/water-boxes/{id}` | Eliminar caja | ADMIN |

#### Endpoints - Assignments

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/water-boxes/assignments` | Listar asignaciones | ADMIN |
| GET | `/water-boxes/assignments/{id}` | Obtener por ID | ADMIN |
| GET | `/water-boxes/assignments/by-user/{userId}` | Por usuario | ADMIN, Usuario |
| GET | `/water-boxes/assignments/active/{userId}` | Activa del usuario | ADMIN, Usuario |
| GET | `/water-boxes/assignments/history/{waterBoxId}` | Historial de caja | ADMIN |
| POST | `/water-boxes/assignments` | Asignar caja | ADMIN |
| PATCH | `/water-boxes/assignments/{id}/close` | Cerrar asignación | ADMIN |

#### Endpoints - Transfers

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/water-boxes/transfers` | Listar transferencias | ADMIN |
| GET | `/water-boxes/transfers/{id}` | Obtener por ID | ADMIN |
| GET | `/water-boxes/transfers/history/{waterBoxId}` | Historial | ADMIN |
| POST | `/water-boxes/transfers` | Crear transferencia | ADMIN |
| PATCH | `/water-boxes/transfers/{id}/approve` | Aprobar | ADMIN |
| DELETE | `/water-boxes/transfers/{id}` | Cancelar | ADMIN |

#### Ejemplos de Uso

**GET /api/water-boxes/assignments/active/{userId}**

```json
// Response 200 OK
{
  "status": "success",
  "data": {
    "id": 1,
    "waterBoxId": 5,
    "userId": "7f3e4d2a-...",
    "startDate": "2024-01-15",
    "endDate": null,
    "monthlyFee": 15.00,
    "status": "ACTIVE",
    "waterBox": {
      "id": 5,
      "boxCode": "BOX-001",
      "boxType": "RESIDENTIAL",
      "installationDate": "2024-01-15"
    }
  }
}
```

**POST /api/water-boxes/transfers**

```json
// Request
{
  "waterBoxId": 5,
  "currentUserId": "client-789",
  "newUserId": "client-999",
  "transferReason": "SALE",
  "transferDate": "2024-06-01",
  "transferFee": 50.00,
  "documents": [
    "escritura_venta.pdf",
    "dni_vendedor.pdf",
    "dni_comprador.pdf"
  ],
  "notes": "Transferencia por venta de propiedad"
}

// Response 201 Created
{
  "status": "success",
  "message": "Transfer created successfully",
  "data": {
    "id": 10,
    "transferCode": "TRANS-2024-010",
    "waterBoxId": 5,
    "transferDate": "2024-06-01",
    "status": "COMPLETED"
  }
}
```

---

### 5️⃣ vg-ms-payments-billing (Puerto 8083)

```
BASE: http://localhost:8083/api
```

#### Endpoints - Payments

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/payments` | Listar pagos | ADMIN, CASHIER |
| GET | `/payments/{id}` | Obtener pago | ADMIN |
| GET | `/payments/by-user/{userId}` | Por usuario | ADMIN, Usuario |
| GET | `/payments/by-water-box/{waterBoxId}` | Por caja | ADMIN |
| GET | `/payments/by-period` | Por periodo | ADMIN |
| GET | `/payments/pending/{userId}` | Pendientes de usuario | ADMIN, Usuario |
| POST | `/payments` | Registrar pago | CASHIER, ADMIN |
| PATCH | `/payments/{id}/cancel` | Cancelar pago | ADMIN |
| GET | `/payments/receipt/{paymentId}` | Generar recibo PDF | CASHIER |

#### Endpoints - Debts

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/debts` | Listar deudas | ADMIN |
| GET | `/debts/by-user/{userId}` | Deudas de usuario | ADMIN, Usuario |
| GET | `/debts/overdue` | Deudas vencidas | ADMIN |
| POST | `/debts` | Registrar deuda | ADMIN |
| PATCH | `/debts/{id}/pay` | Pagar deuda | CASHIER |

#### Endpoints - Reports

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/payments/reports/income` | Reporte de ingresos | ADMIN |
| GET | `/payments/reports/monthly` | Reporte mensual | ADMIN |
| GET | `/payments/reports/by-type` | Por tipo de pago | ADMIN |
| GET | `/payments/reports/by-zone/{zoneId}` | Por zona | ADMIN |

#### Ejemplos de Uso

**POST /api/payments**

```json
// Request - Pago Mensual
{
  "userId": "7f3e4d2a-...",
  "waterBoxId": 5,
  "organizationId": "org-123",
  "paymentType": "MONTHLY_FEE",
  "paymentMethod": "CASH",
  "totalAmount": 15.00,
  "period": "2024-02",
  "details": [
    {
      "description": "Cuota mensual",
      "amount": 15.00,
      "quantity": 1
    }
  ]
}

// Response 201 Created
{
  "status": "success",
  "message": "Payment registered successfully",
  "data": {
    "paymentId": 123,
    "paymentCode": "PAY-2024-123",
    "receiptNumber": "REC-2024-123",
    "totalAmount": 15.00,
    "paymentDate": "2024-02-01T10:00:00Z",
    "paymentStatus": "COMPLETED"
  }
}
```

**GET /api/payments/reports/income?startDate=2024-01-01&endDate=2024-01-31**

```json
// Response 200 OK
{
  "status": "success",
  "data": {
    "period": "2024-01",
    "totalIncome": 1250.00,
    "breakdown": {
      "MONTHLY_FEE": 900.00,
      "INSTALLATION_FEE": 200.00,
      "RECONNECTION_FEE": 80.00,
      "SPECIAL_FEE": 70.00
    },
    "totalTransactions": 67,
    "paymentMethods": {
      "CASH": 800.00,
      "TRANSFER": 350.00,
      "YAPE": 100.00
    }
  }
}
```

---

### 6️⃣ vg-ms-inventory-purchases (Puerto 8085)

```
BASE: http://localhost:8085/api
```

#### Endpoints - Materials

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/materials` | Listar materiales | ADMIN, OPERATOR |
| GET | `/materials/{id}` | Obtener material | ADMIN |
| GET | `/materials/low-stock` | Stock bajo | ADMIN |
| GET | `/materials/availability` | Disponibilidad | OPERATOR |
| POST | `/materials` | Crear material | ADMIN |
| PUT | `/materials/{id}` | Actualizar | ADMIN |
| DELETE | `/materials/{id}` | Eliminar | ADMIN |

#### Endpoints - Purchases

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/purchases` | Listar compras | ADMIN |
| GET | `/purchases/{id}` | Obtener compra | ADMIN |
| GET | `/purchases/by-supplier/{supplierId}` | Por proveedor | ADMIN |
| POST | `/purchases` | Registrar compra | ADMIN |
| PATCH | `/purchases/{id}/complete` | Completar compra | ADMIN |
| DELETE | `/purchases/{id}` | Cancelar compra | ADMIN |

#### Endpoints - Inventory Movements

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/inventory-movements` | Listar movimientos | ADMIN |
| GET | `/inventory-movements/kardex/{materialId}` | Kardex | ADMIN |
| POST | `/inventory-movements` | Registrar salida | OPERATOR, ADMIN |
| GET | `/inventory-movements/by-complaint/{complaintId}` | Por incidencia | ADMIN |

#### Endpoints - Suppliers

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/suppliers` | Listar proveedores | ADMIN |
| GET | `/suppliers/{id}` | Obtener proveedor | ADMIN |
| POST | `/suppliers` | Crear proveedor | ADMIN |
| PUT | `/suppliers/{id}` | Actualizar | ADMIN |
| DELETE | `/suppliers/{id}` | Eliminar | ADMIN |

#### Endpoints - Reports

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/purchases/reports/expense` | Reporte egresos | ADMIN |
| GET | `/inventory-movements/reports/monthly` | Movimientos mensuales | ADMIN |

#### Ejemplos de Uso

**POST /api/purchases**

```json
// Request
{
  "supplierId": 1,
  "purchaseDate": "2024-01-10",
  "purchaseType": "MATERIALS",
  "paymentMethod": "TRANSFER",
  "invoiceNumber": "F001-123",
  "items": [
    {
      "materialId": 5,
      "quantity": 50,
      "unitCost": 5.00,
      "subtotal": 250.00
    },
    {
      "materialId": 15,
      "quantity": 20,
      "unitCost": 8.00,
      "subtotal": 160.00
    }
  ],
  "subtotal": 410.00,
  "tax": 73.80,
  "total": 483.80
}

// Response 201 Created
{
  "status": "success",
  "message": "Purchase registered successfully",
  "data": {
    "id": 1,
    "purchaseCode": "PUR-2024-001",
    "total": 483.80,
    "status": "COMPLETED",
    "inventoryMovementCode": "IN-2024-001"
  }
}
```

**GET /api/inventory-movements/kardex/5**

```json
// Response 200 OK
{
  "status": "success",
  "data": {
    "materialId": 5,
    "materialCode": "MAT-001",
    "materialName": "Tubería PVC 1/2\"",
    "movements": [
      {
        "date": "2024-01-10",
        "type": "INPUT",
        "concept": "Compra",
        "input": 50,
        "output": 0,
        "balance": 50,
        "reference": "PUR-001"
      },
      {
        "date": "2024-01-20",
        "type": "OUTPUT",
        "concept": "Mantenimiento",
        "input": 0,
        "output": 2,
        "balance": 48,
        "reference": "OUT-001"
      }
    ],
    "currentStock": 48
  }
}
```

---

### 7️⃣ vg-ms-water-quality (Puerto 8084)

```
BASE: http://localhost:8084/api
```

#### Endpoints - Quality Tests

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/quality-tests` | Listar pruebas | ADMIN, OPERATOR |
| GET | `/quality-tests/{id}` | Obtener prueba | ADMIN |
| GET | `/quality-tests/by-operator/{operatorId}` | Por operario | ADMIN |
| GET | `/quality-tests/by-zone/{zoneId}` | Por zona | ADMIN |
| GET | `/quality-tests/requires-action` | Requieren acción | ADMIN, OPERATOR |
| POST | `/quality-tests` | Registrar prueba | OPERATOR |
| PATCH | `/quality-tests/{id}/treatment` | Registrar tratamiento | OPERATOR |
| GET | `/quality-tests/reports/monthly` | Reporte mensual | ADMIN |

#### Endpoints - Testing Points

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/testing-points` | Listar puntos | ADMIN |
| GET | `/testing-points/{id}` | Obtener punto | ADMIN |
| GET | `/testing-points/by-zone/{zoneId}` | Por zona | ADMIN |
| POST | `/testing-points` | Crear punto | ADMIN |
| PUT | `/testing-points/{id}` | Actualizar | ADMIN |

#### Ejemplos de Uso

**POST /api/quality-tests**

```json
// Request
{
  "organizationId": "org-123",
  "testingPointIds": ["TP-001", "TP-005"],
  "testDate": "2024-01-15T09:00:00Z",
  "testType": "COMPLETE",
  "testedByUserId": "operator-456",
  "waterTemperature": 18.5,
  "results": [
    {
      "parameter": "CHLORINE",
      "value": 0.8,
      "unit": "mg/L",
      "minLimit": 0.5,
      "maxLimit": 1.5,
      "status": "WITHIN_LIMITS"
    },
    {
      "parameter": "BACTERIA",
      "value": 2,
      "unit": "UFC/100ml",
      "minLimit": 0,
      "maxLimit": 0,
      "status": "OUT_OF_LIMITS",
      "observation": "Requiere tratamiento"
    }
  ],
  "generalObservations": "Se detectó presencia de bacterias"
}

// Response 201 Created
{
  "status": "success",
  "message": "Quality test registered successfully",
  "data": {
    "_id": "test-001",
    "testCode": "TEST-2024-001",
    "status": "REQUIRES_ACTION",
    "alertGenerated": true,
    "complaintCreated": "CLAIM-2024-AUTO-001"
  }
}
```

---

### 8️⃣ vg-ms-claims-incidents (Puerto 8086)

```
BASE: http://localhost:8086/api
```

#### Endpoints - Complaints (Quejas/Reclamos)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/complaints` | Listar quejas | ADMIN, TECHNICIAN |
| GET | `/complaints/{id}` | Obtener queja | Todos |
| GET | `/complaints/by-user/{userId}` | Quejas de usuario | ADMIN, Usuario |
| GET | `/complaints/by-category/{categoryId}` | Por categoría | ADMIN |
| GET | `/complaints/assigned-to/{userId}` | Asignadas a técnico | TECHNICIAN |
| GET | `/complaints/status/{status}` | Por estado | ADMIN |
| POST | `/complaints` | Crear queja | CLIENT |
| PATCH | `/complaints/{id}/assign` | Asignar técnico | ADMIN |
| PATCH | `/complaints/{id}/resolve` | Resolver queja | TECHNICIAN |
| DELETE | `/complaints/{id}` | Cancelar queja | ADMIN |

#### Endpoints - Complaint Categories (Categorías)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/complaint-categories` | Listar categorías | ADMIN |
| GET | `/complaint-categories/{id}` | Obtener categoría | ADMIN |
| POST | `/complaint-categories` | Crear categoría | ADMIN |
| PUT | `/complaint-categories/{id}` | Actualizar | ADMIN |
| DELETE | `/complaint-categories/{id}` | Eliminar | ADMIN |

#### Endpoints - Complaint Responses (Respuestas)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/complaint-responses/by-complaint/{complaintId}` | Respuestas de queja | ADMIN, Usuario |
| POST | `/complaint-responses` | Agregar respuesta | ADMIN, TECHNICIAN |

#### Endpoints - Incidents (Incidentes)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/incidents` | Listar incidentes | ADMIN, OPERATOR |
| GET | `/incidents/{id}` | Obtener incidente | ADMIN |
| GET | `/incidents/by-zone/{zoneId}` | Por zona | ADMIN |
| GET | `/incidents/by-type/{typeId}` | Por tipo | ADMIN |
| GET | `/incidents/by-severity/{severity}` | Por severidad | ADMIN |
| GET | `/incidents/assigned-to/{userId}` | Asignados | TECHNICIAN |
| GET | `/incidents/resolved` | Resueltos | ADMIN |
| GET | `/incidents/unresolved` | Sin resolver | ADMIN |
| POST | `/incidents` | Crear incidente | OPERATOR, ADMIN |
| PATCH | `/incidents/{id}/assign` | Asignar técnico | ADMIN |
| PATCH | `/incidents/{id}/resolve` | Resolver | TECHNICIAN |
| DELETE | `/incidents/{id}` | Eliminar | ADMIN |

#### Endpoints - Incident Types (Tipos de Incidentes)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/incident-types` | Listar tipos | ADMIN |
| GET | `/incident-types/{id}` | Obtener tipo | ADMIN |
| POST | `/incident-types` | Crear tipo | ADMIN |
| PUT | `/incident-types/{id}` | Actualizar | ADMIN |
| DELETE | `/incident-types/{id}` | Eliminar | ADMIN |

#### Endpoints - Incident Resolutions (Resoluciones)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/incident-resolutions/by-incident/{incidentId}` | Resolución de incidente | ADMIN |
| GET | `/incident-resolutions/by-technician/{userId}` | Por técnico | ADMIN |
| POST | `/incident-resolutions` | Crear resolución | TECHNICIAN |
| GET | `/incident-resolutions/reports/costs` | Reporte de costos | ADMIN |

#### Ejemplos de Uso

**POST /api/complaints**

```json
// Request
{
  "organizationId": "org-123",
  "userId": "client-789",
  "categoryId": "cat-001",
  "waterBoxId": "box-001",
  "subject": "Fuga de agua en mi calle",
  "description": "Fuga en tubería principal de Jr. Los Andes",
  "priority": "HIGH"
}

// Response 201 Created
{
  "status": "success",
  "message": "Complaint created successfully",
  "data": {
    "_id": "complaint-001",
    "complaintCode": "CLAIM-2024-001",
    "status": "RECEIVED",
    "createdAt": "2024-01-20T08:00:00Z"
  }
}
```

**POST /api/incidents**

```json
// Request
{
  "organizationId": "org-123",
  "incidentTypeId": "type-001",
  "incidentCategory": "PIPE_BURST",
  "zoneId": "zone-456",
  "title": "Rotura de tubería principal",
  "description": "Tubería principal rota afectando 15 viviendas",
  "severity": "CRITICAL",
  "affectedBoxesCount": 15,
  "reportedByUserId": "operator-456"
}

// Response 201 Created
{
  "status": "success",
  "message": "Incident created successfully",
  "data": {
    "_id": "incident-001",
    "incidentCode": "INC-2024-001",
    "status": "REPORTED",
    "severity": "CRITICAL",
    "createdAt": "2024-01-20T08:00:00Z",
    "notificationSent": true
  }
}
```

**POST /api/incident-resolutions**

```json
// Request
{
  "incidentId": "incident-001",
  "resolutionType": "REPARACION_COMPLETA",
  "actionsTaken": "Se reemplazó tubería principal de 2 pulgadas por 10 metros",
  "materialsUsed": [
    {
      "productId": "MAT-001",
      "quantity": 10,
      "unit": "METROS",
      "unitCost": 15.00
    },
    {
      "productId": "MAT-015",
      "quantity": 2,
      "unit": "UNIT",
      "unitCost": 8.00
    }
  ],
  "laborHours": 4,
  "totalCost": 166.00,
  "resolvedByUserId": "tech-123",
  "qualityCheck": true,
  "followUpRequired": false,
  "resolutionNotes": "Trabajo completado satisfactoriamente"
}

// Response 201 Created
{
  "status": "success",
  "message": "Incident resolution created successfully",
  "data": {
    "_id": "resolution-001",
    "incidentId": "incident-001",
    "totalCost": 166.00,
    "inventoryMovementsCreated": ["OUT-2024-001"],
    "createdAt": "2024-01-20T14:00:00Z"
  }
}
```

---

### 9️⃣ vg-ms-distribution (Puerto 8087)

```
BASE: http://localhost:8087/api
```

#### Endpoints - Distribution Programs

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/distribution-programs` | Listar programas | ADMIN |
| GET | `/distribution-programs/{id}` | Obtener programa | ADMIN |
| GET | `/distribution-programs/my-schedule` | Mi horario | CLIENT |
| GET | `/distribution-programs/by-zone/{zoneId}` | Por zona | ADMIN |
| GET | `/distribution-programs/coverage-report` | Reporte cobertura | ADMIN |
| POST | `/distribution-programs` | Crear programa | ADMIN |
| PUT | `/distribution-programs/{id}` | Actualizar | ADMIN |
| DELETE | `/distribution-programs/{id}` | Eliminar | ADMIN |

#### Endpoints - Schedules

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/distribution-schedules` | Listar horarios | ADMIN |
| POST | `/distribution-schedules` | Crear horario | ADMIN |
| PUT | `/distribution-schedules/{id}` | Actualizar | ADMIN |

#### Ejemplos de Uso

**GET /api/distribution-programs/my-schedule**

```json
// Response 200 OK (detecta usuario del JWT)
{
  "status": "success",
  "data": {
    "userId": "7f3e4d2a-...",
    "zoneName": "Zona Alta",
    "streetName": "Jr. Los Andes",
    "distributionDays": ["Lunes", "Miércoles", "Viernes"],
    "schedule": "06:00 - 12:00",
    "nextDistribution": "2024-01-22T06:00:00Z",
    "waterFlowRate": "2.5 L/s",
    "estimatedPressure": "15 PSI",
    "message": "El próximo abastecimiento es Lunes 22/01 a las 6:00 AM"
  }
}
```

---

### 🔟 vg-ms-notification (Puerto 8089)

```
BASE: http://localhost:8089/api
```

#### Endpoints - Notifications

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/notifications/whatsapp` | Enviar WhatsApp | Interno (microservicios) |
| POST | `/notifications/sms` | Enviar SMS | Interno |
| GET | `/notifications/history/{userId}` | Historial usuario | ADMIN |
| GET | `/notifications/status/{notificationId}` | Estado | ADMIN |

#### Ejemplos de Uso

**POST /api/notifications/whatsapp**

```json
// Request (llamado por otro microservicio)
{
  "userId": "7f3e4d2a-...",
  "phone": "+51987654321",
  "messageType": "PAYMENT_CONFIRMATION",
  "templateParams": {
    "userName": "Juan Pérez",
    "paymentCode": "PAY-2024-123",
    "amount": "15.00",
    "period": "Febrero 2024"
  }
}

// Response 200 OK
{
  "status": "success",
  "message": "WhatsApp sent successfully",
  "data": {
    "notificationId": "notif-123",
    "twilioMessageSid": "SM...",
    "status": "SENT",
    "sentAt": "2024-01-21T10:00:00Z"
  }
}
```

---

## ✅ RESUMEN DE ESTÁNDARES

```
┌──────────────────────────────────────────────────────────────────────┐
│ RESUMEN - ESTÁNDARES DE API                                          │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│ ✅ Todas las APIs REST siguen convención /api/{resource}            │
│ ✅ Autenticación JWT via header Authorization: Bearer               │
│ ✅ Respuestas JSON con estructura estándar                           │
│ ✅ Paginación consistente (page, size, sort)                         │
│ ✅ Filtros via query params                                          │
│ ✅ Códigos HTTP estándar (200, 201, 400, 401, 404, 500)             │
│ ✅ Soft delete (status = INACTIVE)                                   │
│ ✅ Auditoría en todas las tablas (createdBy, updatedBy)             │
│ ✅ Validación de permisos por roles                                  │
│ ✅ Documentación OpenAPI/Swagger disponible en /swagger-ui.html     │
│                                                                      │
│ TOTAL ENDPOINTS DOCUMENTADOS: 180+                                   │
│ TOTAL TABLAS POSTGRESQL: 15                                          │
│ TOTAL COLLECTIONS MONGODB: 18                                        │
│   - organizations: 5 collections                                     │
│   - water-quality: 2 collections                                     │
│   - claims-incidents: 6 collections (complaints, categories,         │
│     responses, incidents, types, resolutions)                        │
│   - distribution: 3 collections                                      │
│   - notification: 2 collections                                      │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 📋 RESUMEN: MODELOS/ENTIDADES POR MICROSERVICIO

### 📊 CORRESPONDENCIA COMPLETA: ARQUITECTURA ↔ BASE DE DATOS

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 1️⃣ vg-ms-users (PostgreSQL - vg_users)                                                 │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── User.java                    ↔ TABLE: users                                    │
│     └── valueobjects/                                                                   │
│         ├── Role.java                ← ENUM: SUPER_ADMIN, ADMIN, CLIENT                │
│         ├── DocumentType.java        ← ENUM: DNI, PASSPORT, RUC                        │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE                          │
│                                                                                         │
│  📁 infrastructure/persistence/entities/                                                │
│     └── UserEntity.java              ← @Table("users") | R2DBC                         │
│                                                                                         │
│  🗄️ PostgreSQL: 1 tabla (users)                                                        │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 2️⃣ vg-ms-organizations (MongoDB - JASS_DIGITAL)                                        │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── Organization.java            ↔ COLLECTION: organizations                       │
│     ├── Zone.java                    ↔ COLLECTION: zones                               │
│     ├── Street.java                  ↔ COLLECTION: streets                             │
│     ├── Fare.java                    ↔ COLLECTION: fares                               │
│     ├── Parameter.java               ↔ COLLECTION: parameters                          │
│     └── valueobjects/                                                                   │
│         ├── OrganizationType.java    ← ENUM: JASS, JAAS, OMSABAR                       │
│         ├── FareType.java            ← ENUM: MONTHLY_FEE, INSTALLATION_FEE, etc.       │
│         ├── StreetType.java          ← ENUM: JR, AV, CALLE, PASAJE                     │
│         ├── ParameterType.java       ← ENUM: BILLING_DAY, GRACE_PERIOD                 │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE                          │
│                                                                                         │
│  📁 infrastructure/persistence/documents/                                               │
│     ├── OrganizationDocument.java    ← @Document("organizations") | MongoDB            │
│     ├── ZoneDocument.java            ← @Document("zones")                              │
│     ├── StreetDocument.java          ← @Document("streets")                            │
│     ├── FareDocument.java            ← @Document("fares")                              │
│     └── ParameterDocument.java       ← @Document("parameters")                         │
│                                                                                         │
│  🍃 MongoDB: 5 collections                                                              │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 3️⃣ vg-ms-payments-billing (PostgreSQL - vg_payments)                                   │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── Payment.java                 ↔ TABLE: payments                                 │
│     ├── PaymentDetail.java           ↔ TABLE: payment_details                          │
│     ├── Debt.java                    ↔ TABLE: debts                                    │
│     └── valueobjects/                                                                   │
│         ├── PaymentType.java         ← ENUM: MONTHLY_FEE, INSTALLATION_FEE, etc.       │
│         ├── PaymentMethod.java       ← ENUM: CASH, BANK_TRANSFER, CARD, YAPE, PLIN    │
│         ├── PaymentStatus.java       ← ENUM: PENDING, COMPLETED, CANCELLED, FAILED     │
│         └── DebtStatus.java          ← ENUM: PENDING, PARTIAL, PAID, CANCELLED         │
│                                                                                         │
│  📁 infrastructure/persistence/entities/                                                │
│     ├── PaymentEntity.java           ← @Table("payments") | R2DBC                      │
│     ├── PaymentDetailEntity.java     ← @Table("payment_details")                       │
│     └── DebtEntity.java              ← @Table("debts")                                 │
│                                                                                         │
│  🗄️ PostgreSQL: 3 tablas (payments, payment_details, debts)                            │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 4️⃣ vg-ms-water-quality (MongoDB - JASS_DIGITAL)                                        │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── TestingPoint.java            ↔ COLLECTION: testing_points                      │
│     ├── QualityTest.java             ↔ COLLECTION: quality_tests                       │
│     └── valueobjects/                                                                   │
│         ├── PointType.java           ← ENUM: RESERVOIR, TAP, WELL, SOURCE              │
│         ├── TestType.java            ← ENUM: CHLORINE, PH, TURBIDITY, etc.             │
│         ├── TestResult.java          ← ENUM: APPROVED, REJECTED, REQUIRES_TREATMENT    │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE                          │
│                                                                                         │
│  📁 infrastructure/persistence/documents/                                               │
│     ├── TestingPointDocument.java    ← @Document("testing_points") | MongoDB           │
│     └── QualityTestDocument.java     ← @Document("quality_tests")                      │
│                                                                                         │
│  🍃 MongoDB: 2 collections                                                              │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 5️⃣ vg-ms-inventory-purchases (PostgreSQL - vg_inventory)                               │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── Supplier.java                ↔ TABLE: suppliers                                │
│     ├── Material.java                ↔ TABLE: materials                                │
│     ├── ProductCategory.java         ↔ TABLE: product_categories                       │
│     ├── Purchase.java                ↔ TABLE: purchases                                │
│     ├── PurchaseDetail.java          ↔ TABLE: purchase_details                         │
│     ├── InventoryMovement.java       ↔ TABLE: inventory_movements (Kardex)             │
│     └── valueobjects/                                                                   │
│         ├── MovementType.java        ← ENUM: IN, OUT, ADJUSTMENT                       │
│         ├── PurchaseStatus.java      ← ENUM: PENDING, RECEIVED, CANCELLED              │
│         ├── Unit.java                ← ENUM: UNIT, METERS, KG, LITERS                  │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE                          │
│                                                                                         │
│  📁 infrastructure/persistence/entities/                                                │
│     ├── SupplierEntity.java          ← @Table("suppliers") | R2DBC                     │
│     ├── MaterialEntity.java          ← @Table("materials")                             │
│     ├── ProductCategoryEntity.java   ← @Table("product_categories")                    │
│     ├── PurchaseEntity.java          ← @Table("purchases")                             │
│     ├── PurchaseDetailEntity.java    ← @Table("purchase_details")                      │
│     └── InventoryMovementEntity.java ← @Table("inventory_movements")                   │
│                                                                                         │
│  🗄️ PostgreSQL: 6 tablas                                                                │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 6️⃣ vg-ms-claims-incidents (MongoDB - JASS_DIGITAL)                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── Complaint.java               ↔ COLLECTION: complaints                          │
│     ├── ComplaintCategory.java       ↔ COLLECTION: complaint_categories                │
│     ├── ComplaintResponse.java       ↔ COLLECTION: complaint_responses                 │
│     ├── Incident.java                ↔ COLLECTION: incidents                           │
│     ├── IncidentType.java            ↔ COLLECTION: incident_types                      │
│     ├── IncidentResolution.java      ↔ COLLECTION: incident_resolutions                │
│     └── valueobjects/                                                                   │
│         ├── ComplaintPriority.java   ← ENUM: LOW, MEDIUM, HIGH, URGENT                 │
│         ├── ComplaintStatus.java     ← ENUM: RECEIVED, IN_PROGRESS, RESOLVED, CLOSED   │
│         ├── ResponseType.java        ← ENUM: INVESTIGACION, SOLUCION, SEGUIMIENTO      │
│         ├── IncidentSeverity.java    ← ENUM: LOW, MEDIUM, HIGH, CRITICAL               │
│         ├── IncidentStatus.java      ← ENUM: REPORTED, ASSIGNED, IN_PROGRESS, etc.     │
│         ├── ResolutionType.java      ← ENUM: REPARACION_TEMPORAL, COMPLETA, REEMPLAZO  │
│         ├── MaterialUsed.java        ← VALUE OBJECT (embedded en resolutions)          │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE                          │
│                                                                                         │
│  📁 infrastructure/persistence/documents/                                               │
│     ├── ComplaintDocument.java       ← @Document("complaints") | MongoDB               │
│     ├── ComplaintCategoryDocument    ← @Document("complaint_categories")               │
│     ├── ComplaintResponseDocument    ← @Document("complaint_responses")                │
│     ├── IncidentDocument.java        ← @Document("incidents")                          │
│     ├── IncidentTypeDocument.java    ← @Document("incident_types")                     │
│     └── IncidentResolutionDocument   ← @Document("incident_resolutions")               │
│                                                                                         │
│  🍃 MongoDB: 6 collections                                                              │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 7️⃣ vg-ms-distribution (MongoDB - JASS_DIGITAL)                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── DistributionProgram.java     ↔ COLLECTION: distribution_programs               │
│     ├── DistributionRoute.java       ↔ COLLECTION: distribution_routes                 │
│     ├── DistributionSchedule.java    ↔ COLLECTION: distribution_schedules              │
│     └── valueobjects/                                                                   │
│         ├── DayOfWeek.java           ← ENUM: MONDAY, TUESDAY, ..., SUNDAY              │
│         ├── DistributionStatus.java  ← ENUM: ACTIVE, INACTIVE, SUSPENDED               │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE                          │
│                                                                                         │
│  📁 infrastructure/persistence/documents/                                               │
│     ├── DistributionProgramDocument  ← @Document("distribution_programs") | MongoDB    │
│     ├── DistributionRouteDocument    ← @Document("distribution_routes")                │
│     └── DistributionScheduleDocument ← @Document("distribution_schedules")             │
│                                                                                         │
│  🍃 MongoDB: 3 collections                                                              │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 8️⃣ vg-ms-infrastructure (PostgreSQL - vg_infrastructure)                               │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  📁 domain/models/                                                                      │
│     ├── WaterBox.java                ↔ TABLE: water_boxes                              │
│     ├── WaterBoxAssignment.java      ↔ TABLE: water_box_assignments                    │
│     ├── WaterBoxTransfer.java        ↔ TABLE: water_box_transfers                      │
│     └── valueobjects/                                                                   │
│         ├── BoxType.java             ← ENUM: RESIDENTIAL, COMMERCIAL, etc.             │
│         ├── AssignmentStatus.java    ← ENUM: ACTIVE, INACTIVE, TRANSFERRED             │
│         └── RecordStatus.java        ← ENUM: ACTIVE, INACTIVE, SUSPENDED               │
│                                                                                         │
│  📁 infrastructure/persistence/entities/                                                │
│     ├── WaterBoxEntity.java          ← @Table("water_boxes") | R2DBC                   │
│     ├── WaterBoxAssignmentEntity     ← @Table("water_box_assignments")                 │
│     └── WaterBoxTransferEntity       ← @Table("water_box_transfers")                   │
│                                                                                         │
│  🗄️ PostgreSQL: 3 tablas                                                                │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ TOTALES DEL SISTEMA                                                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  🗄️ PostgreSQL: 4 bases de datos, 15 tablas                                            │
│     • vg_users: 1 tabla (users)                                                         │
│     • vg_payments: 3 tablas (payments, payment_details, debts)                          │
│     • vg_inventory: 6 tablas (suppliers, materials, categories, purchases, etc.)        │
│     • vg_infrastructure: 3 tablas (water_boxes, assignments, transfers)                 │
│                                                                                         │
│  🍃 MongoDB: 1 base de datos (JASS_DIGITAL), 18 collections                             │
│     • vg-ms-organizations: 5 collections                                                │
│     • vg-ms-water-quality: 2 collections                                                │
│     • vg-ms-claims-incidents: 6 collections                                             │
│     • vg-ms-distribution: 3 collections                                                 │
│     • vg-ms-notification: 2 collections (notification_history, templates)               │
│                                                                                         │
│  📦 Total Modelos de Dominio: 30+                                                       │
│  📦 Total Value Objects/Enums: 35+                                                      │
│  📦 Total Entities/Documents: 33                                                        │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

**FIN DEL DOCUMENTO** 🚀

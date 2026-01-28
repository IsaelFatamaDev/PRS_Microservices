# 🚀 ARQUITECTURA JASS REAL - AJUSTADA AL NEGOCIO

> **Sistema JASS Digital - Zonas Rurales del Perú**
> **Fecha:** 20 de Enero de 2026
> **Ajustes:** Según modelo de negocio REAL

---

## 📊 INVENTARIO COMPLETO DE TABLAS/DOCUMENTOS ACTUALES

### ✅ MICROSERVICIO: vg-ms-users (MongoDB)

| Colección | Ubicación | Estado |
|-----------|-----------|--------|
| **users_demo** | `infrastructure/document/UserDocument.java` | ✅ Mantener |
| **user_code_counters** | `infrastructure/document/UserCodeCounterDocument.java` | ✅ Mantener |
| ~~auth_credentials~~ | ~~`domain/models/AuthCredential.java`~~ | ❌ **ELIMINAR** (Keycloak) |

**Campos actuales UserDocument:**

- ✅ id, userCode, username, organizationId
- ✅ personalInfo (embedded), contact (embedded)
- ✅ roles, status
- ✅ registrationDate, lastLogin
- ✅ createdAt, updatedAt, createdBy, updatedBy
- ✅ deletedAt, deletedBy

**Ajustes necesarios:**

1. ❌ Eliminar `AuthCredential` (se maneja en Keycloak)
2. ⚠️ Email/Phone OPCIONALES (zonas rurales)
3. ❌ NO verificar email ni phone
4. ❌ NO guardar fecha de nacimiento (innecesario)
5. ✅ Mantener userCode (identificación interna)

---

### ✅ MICROSERVICIO: vg-ms-infrastructure (PostgreSQL)

| Tabla | Ubicación | Estado |
|-------|-----------|--------|
| **water_boxes** | `infrastructure/persistence/entity/WaterBoxEntity.java` | ✅ Mantener |
| **water_box_assignments** | `infrastructure/persistence/entity/WaterBoxAssignmentEntity.java` | ✅ Mantener |
| **water_box_transfers** | `infrastructure/persistence/entity/WaterBoxTransferEntity.java` | ✅ Mantener |

**Campos water_boxes:**

- id, organizationId, boxCode, boxType
- installationDate, currentAssignmentId
- status, createdAt

**Ajustes necesarios:**

1. ✅ Agregar campo `cutDate` (fecha de corte)
2. ✅ Agregar campo `reconnectionFee` (tarifa de reposición)
3. ✅ Mantener lógica de transferencias entre usuarios

---

### ⚠️ MICROSERVICIO: vg-ms-payments-billing (PostgreSQL R2DBC)

| Tabla | Ubicación | Estado |
|-------|-----------|--------|
| **receipts** | `domain/models/Receipts.java` | ⚠️ **REFACTORIZAR** |
| **payments** | `infrastructure/entity/PaymentEntity.java` | ✅ Mantener |
| **payment_details** | `infrastructure/entity/PaymentDetailEntity.java` | ✅ Mantener |

**PROBLEMA CRÍTICO:** El modelo actual asume medidores y consumo variable

**Modelo REAL de JASS:**

- ❌ NO hay medidores
- ✅ Pago **FIJO MENSUAL** por zona
- ✅ Tarifa por zona (ej: Rinconada S/8, Bellavista S/10, Roma S/20)
- ✅ Pago adicional por **reposición** (si caja fue cortada)
- ✅ Acumulación de meses de deuda

**Campos actuales Receipts:**

- ❌ `receipt_series`, `receipt_number` → Simplificar
- ❌ `payment_id`, `payment_detail_id` → Reestructurar
- ✅ `organizationId`, `amount`, `year`, `month`
- ✅ `concept`, `customerFullName`, `customerDocument`
- ❌ `pdfGenerated`, `pdfPath` → Opcional

**Campos necesarios NUEVOS:**

- ✅ `userId` (referencia al usuario)
- ✅ `waterBoxId` (referencia a la caja)
- ✅ `zoneId` (para determinar tarifa)
- ✅ `monthlyFee` (tarifa fija del mes)
- ✅ `reconnectionFee` (si aplica reposición)
- ✅ `totalAmount` (monthly_fee + reconnection_fee)
- ✅ `monthsOwed` (array de meses adeudados)
- ✅ `dueDate` (fecha de vencimiento)
- ✅ `paymentStatus` (PENDING, PAID, OVERDUE)

---

### ✅ MICROSERVICIO: vg-ms-organizations (MongoDB)

| Colección | Ubicación | Estado |
|-----------|-----------|--------|
| **organizations** | `infrastructure/document/OrganizationDocument.java` | ✅ Mantener |
| **zones** | `infrastructure/document/ZoneDocument.java` | ✅ Mantener |
| **streets** | `infrastructure/document/StreetDocument.java` | ✅ Mantener |
| **fares** | `infrastructure/document/FareDocument.java` | ⚠️ **REFACTORIZAR** |
| **parameters** | `infrastructure/document/ParameterDocument.java` | ✅ Mantener |

**PROBLEMA:** Fares está separado de Zones (debería estar integrado)

**Modelo REAL de JASS:**

**Caso 1: JASS Rinconada-Bellavista**

- Organización: JASS Rinconada-Bellavista
- Zona 1: Rinconada → Tarifa: S/8.00/mes
- Zona 2: Bellavista de Conta → Tarifa: S/10.00/mes

**Caso 2: JASS Roma**

- Organización: JASS Roma
- Zona 1: Roma Centro → Tarifa: S/20.00/mes
- Zona 2: Roma Alta → Tarifa: S/20.00/mes
- Zona 3: Roma Baja → Tarifa: S/20.00/mes

**Ajustes necesarios:**

1. ✅ Integrar `monthlyFee` directamente en `zones`
2. ✅ Agregar `reconnectionFee` en organization o zone
3. ❌ Eliminar colección `fares` separada (redundante)
4. ✅ Mantener calles por zona

---

### ✅ MICROSERVICIO: vg-ms-distribution (MongoDB)

| Colección | Ubicación | Estado |
|-----------|-----------|--------|
| **program** | `infrastructure/document/DistributionProgramDocument.java` | ✅ Mantener |
| ~~route~~ | ~~`domain/models/DistributionRoute.java`~~ | ❌ **Violación** |
| ~~schedule~~ | ~~`domain/models/DistributionSchedule.java`~~ | ❌ **Violación** |

**PROBLEMA:** Route y Schedule tienen @Document en domain (violación hexagonal)

**Ajustes necesarios:**

1. ❌ Eliminar @Document de domain models
2. ✅ Crear `DistributionRouteDocument` en infrastructure
3. ✅ Crear `DistributionScheduleDocument` en infrastructure
4. ✅ Mantener programas de distribución por zonas

---

### ✅ MICROSERVICIO: vg-ms-inventory-purchases (PostgreSQL R2DBC)

| Tabla | Ubicación | Estado |
|-------|-----------|--------|
| **products** | `infrastructure/entities/MaterialEntity.java` | ✅ Mantener |
| **product_categories** | `infrastructure/entities/ProductCategoryEntity.java` | ✅ Mantener |
| **inventory_movements** | `infrastructure/entities/InventoryMovementEntity.java` | ✅ Mantener |
| **purchases** | `infrastructure/entities/PurchaseEntity.java` | ✅ Mantener |
| **purchase_details** | `infrastructure/entities/PurchaseDetailEntity.java` | ✅ Mantener |
| **suppliers** | `infrastructure/entities/SupplierEntity.java` | ⚠️ **Evaluar** |

**Ajustes necesarios:**

1. ⚠️ Evaluar si `suppliers` es relevante (según usuario: "ya no será relevante")
2. ✅ Mantener kardex (FIFO/LIFO)
3. ✅ Mantener entradas/salidas
4. ✅ Mantener categorías de productos

---

### ✅ MICROSERVICIO: vg-ms-water-quality (MongoDB)

| Colección | Ubicación | Estado |
|-----------|-----------|--------|
| **testing_points** | `infrastructure/document/TestingPointDocument.java` | ✅ Mantener |
| **quality_tests** | `infrastructure/document/QualityTestDocument.java` | ✅ Mantener |
| **daily_records** | `infrastructure/document/DailyRecordDocument.java` | ✅ Mantener |
| ~~users~~ | ~~`domain/models/User.java`~~ | ❌ **Violación** |

**PROBLEMA:** User tiene @Document en domain (violación hexagonal)

**Ajustes necesarios:**

1. ❌ Eliminar User del dominio (usar referencia a vg-ms-users)
2. ✅ Mantener puntos de muestreo
3. ✅ Mantener pruebas de calidad
4. ✅ Mantener registros diarios

---

### ✅ MICROSERVICIO: vg-ms-claims-incidents (MongoDB)

| Colección | Ubicación | Estado |
|-----------|-----------|--------|
| **complaints** | `infrastructure/document/ComplaintDocument.java` | ✅ Mantener |
| **complaint_categories** | `infrastructure/document/ComplaintCategoryDocument.java` | ✅ Mantener |
| **complaint_responses** | `infrastructure/document/ComplaintResponseDocument.java` | ✅ Mantener |
| **incidents** | `infrastructure/document/IncidentDocument.java` | ✅ Mantener |
| **incident_resolutions** | `infrastructure/document/IncidentResolutionDocument.java` | ✅ Mantener |
| **incident_types** | `infrastructure/document/IncidentTypeDocument.java` | ✅ Mantener |

**Ajustes necesarios:**

1. ✅ Agregar tipo "FUGA" en incident_types
2. ✅ Agregar tipo "SIN_AGUA" en incident_types
3. ✅ Mantener workflow de resolución
4. ✅ Integrar con RabbitMQ para notificaciones

---

### ⚠️ MICROSERVICIO: vg-ms-authentication (Keycloak)

**Estado actual:** ❌ NO usar base de datos propia

**Ajustes necesarios:**

1. ✅ Keycloak maneja TODA la autenticación
2. ❌ NO guardar tokens en BD
3. ❌ NO guardar passwords
4. ✅ Solo validar JWT/JWE
5. ✅ Integración OAuth2

---

### ⚠️ MICROSERVICIO: vg-ms-notification (Node.js)

**Estado actual:** Solo WhatsApp

**Ajustes necesarios:**

1. ✅ **PRIORIZAR SMS** (zonas rurales sin WhatsApp)
2. ✅ WhatsApp como secundario
3. ⚠️ Email como terciario (muy pocos tienen)
4. ✅ Enviar credenciales de acceso por SMS
5. ✅ Recordatorios de pago por SMS

---

## 🔄 ROLES DEL SISTEMA

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| **SUPER_ADMIN** | Administrador del sistema completo | Acceso total, gestión de organizaciones |
| **ADMIN** | Administrador de una JASS | Gestión completa de su organización |
| **OPERATOR** | Operador/técnico | Infraestructura, distribución, calidad agua |
| **CLIENT** | Usuario final (beneficiario) | Ver su caja, pagos, reclamos |

---

## 🗄️ ESQUEMAS DE BASES DE DATOS CORREGIDOS

### PostgreSQL - Users Service (Nuevo)

```sql
-- ============================================
-- USERS SERVICE (MongoDB -> PostgreSQL)
-- Cambio a PostgreSQL para mejor consistencia
-- ============================================

CREATE SCHEMA IF NOT EXISTS users;

CREATE TYPE users.user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE users.document_type AS ENUM ('DNI', 'CE', 'PASSPORT', 'RUC');
CREATE TYPE users.role_type AS ENUM ('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'CLIENT');

-- Tabla principal de usuarios
CREATE TABLE users.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_id UUID NOT NULL UNIQUE,  -- ID en Keycloak
    user_code VARCHAR(20) UNIQUE NOT NULL,  -- JASS-00001 (interno)
    organization_id UUID NOT NULL,

    -- Información Personal
    document_type users.document_type NOT NULL,
    document_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    -- Contacto (OPCIONAL - zonas rurales)
    phone VARCHAR(20),  -- NULLABLE
    email VARCHAR(255),  -- NULLABLE

    -- Dirección
    address_street VARCHAR(255),
    address_zone_id UUID,  -- Referencia a zones

    -- Rol y Estado
    role users.role_type NOT NULL,
    status users.user_status DEFAULT 'ACTIVE',

    -- Auditoría
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID,
    deleted_at TIMESTAMP,

    INDEX idx_organization_id (organization_id),
    INDEX idx_document (document_type, document_number),
    INDEX idx_user_code (user_code),
    INDEX idx_status (status),
    INDEX idx_zone (address_zone_id),

    CONSTRAINT chk_phone_or_email CHECK (phone IS NOT NULL OR email IS NOT NULL)
);

-- NO hay tabla de credenciales (Keycloak)
-- NO hay email_verified ni phone_verified
-- NO hay fecha de nacimiento
```

---

### PostgreSQL - Infrastructure Service

```sql
-- ============================================
-- INFRASTRUCTURE SERVICE
-- ============================================

CREATE SCHEMA IF NOT EXISTS infrastructure;

CREATE TYPE infrastructure.box_type AS ENUM ('DOMESTIC', 'COMMERCIAL', 'INDUSTRIAL');
CREATE TYPE infrastructure.box_status AS ENUM ('ACTIVE', 'INACTIVE', 'CUT', 'SUSPENDED');

-- Tabla: water_boxes
CREATE TABLE infrastructure.water_boxes (
    id BIGSERIAL PRIMARY KEY,
    organization_id UUID NOT NULL,
    box_code VARCHAR(50) UNIQUE NOT NULL,  -- WB-001
    box_type infrastructure.box_type NOT NULL,

    installation_date DATE NOT NULL,
    current_assignment_id BIGINT,  -- FK a assignments

    -- NUEVO: Gestión de cortes
    is_cut BOOLEAN DEFAULT FALSE,
    cut_date DATE,
    cut_reason TEXT,
    reconnection_fee NUMERIC(10, 2),  -- Tarifa de reposición

    status infrastructure.box_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_organization (organization_id),
    INDEX idx_box_code (box_code),
    INDEX idx_status (status)
);

-- Tabla: water_box_assignments
CREATE TABLE infrastructure.water_box_assignments (
    id BIGSERIAL PRIMARY KEY,
    water_box_id BIGINT NOT NULL,
    user_id UUID NOT NULL,  -- Usuario asignado

    assignment_date DATE NOT NULL,
    assignment_reason VARCHAR(255),

    is_active BOOLEAN DEFAULT TRUE,
    unassignment_date DATE,
    unassignment_reason VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,

    CONSTRAINT fk_water_box FOREIGN KEY (water_box_id)
        REFERENCES infrastructure.water_boxes(id),

    INDEX idx_water_box (water_box_id),
    INDEX idx_user (user_id),
    INDEX idx_active (is_active)
);

-- Tabla: water_box_transfers
CREATE TABLE infrastructure.water_box_transfers (
    id BIGSERIAL PRIMARY KEY,
    water_box_id BIGINT NOT NULL,

    from_user_id UUID NOT NULL,  -- Usuario anterior
    to_user_id UUID NOT NULL,    -- Usuario nuevo

    transfer_date DATE NOT NULL,
    transfer_reason TEXT,
    transfer_document VARCHAR(255),  -- Doc que autoriza

    approved_by UUID,  -- ADMIN que aprobó
    approved_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_water_box FOREIGN KEY (water_box_id)
        REFERENCES infrastructure.water_boxes(id),

    INDEX idx_water_box (water_box_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id)
);
```

---

### PostgreSQL - Payments Service (REFACTORIZADO)

```sql
-- ============================================
-- PAYMENTS SERVICE - MODELO REAL JASS
-- Sin medidores, pago fijo mensual
-- ============================================

CREATE SCHEMA IF NOT EXISTS payments;

CREATE TYPE payments.receipt_status AS ENUM ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED');
CREATE TYPE payments.payment_type AS ENUM ('MONTHLY_SERVICE', 'RECONNECTION', 'OTHER');
CREATE TYPE payments.payment_method AS ENUM ('CASH', 'BANK_TRANSFER', 'MOBILE_PAYMENT');

-- Tabla: receipts (Recibos mensuales)
CREATE TABLE payments.receipts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    receipt_code VARCHAR(50) UNIQUE NOT NULL,  -- REC-2026-001

    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    water_box_id BIGINT NOT NULL,
    zone_id UUID NOT NULL,  -- Para determinar tarifa

    -- Período
    billing_year INTEGER NOT NULL,
    billing_month INTEGER NOT NULL,  -- 1-12

    -- Montos (SIN consumo, SIN medidor, SIN mora, SIN descuento)
    monthly_fee NUMERIC(10, 2) NOT NULL,  -- Tarifa fija de la zona
    reconnection_fee NUMERIC(10, 2) DEFAULT 0,  -- Si aplica reposición
    total_amount NUMERIC(10, 2) NOT NULL,  -- monthly_fee + reconnection_fee

    -- Fechas
    issue_date DATE DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,

    -- Estado
    status payments.receipt_status DEFAULT 'PENDING',

    -- Cliente
    customer_full_name VARCHAR(200) NOT NULL,
    customer_document VARCHAR(20) NOT NULL,

    -- PDF
    pdf_generated BOOLEAN DEFAULT FALSE,
    pdf_path VARCHAR(500),

    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID,

    INDEX idx_organization (organization_id),
    INDEX idx_user (user_id),
    INDEX idx_water_box (water_box_id),
    INDEX idx_zone (zone_id),
    INDEX idx_status (status),
    INDEX idx_period (billing_year, billing_month),
    INDEX idx_due_date (due_date),

    UNIQUE (user_id, billing_year, billing_month)
);

-- Tabla: payments (Pagos realizados)
CREATE TABLE payments.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_code VARCHAR(50) UNIQUE NOT NULL,  -- PAY-2026-001

    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    water_box_id BIGINT NOT NULL,

    -- Tipo y método
    payment_type payments.payment_type NOT NULL,
    payment_method payments.payment_method NOT NULL,

    -- Monto
    amount NUMERIC(10, 2) NOT NULL,

    -- Referencia
    external_reference VARCHAR(100),  -- Nro operación bancaria
    payment_date DATE DEFAULT CURRENT_DATE,

    -- Comprobante
    voucher_path VARCHAR(500),

    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,

    INDEX idx_organization (organization_id),
    INDEX idx_user (user_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_type (payment_type)
);

-- Tabla: receipt_payments (Relación N:M)
-- Un pago puede cubrir múltiples recibos (meses atrasados)
-- Un recibo puede ser pagado con múltiples pagos (pagos parciales)
CREATE TABLE payments.receipt_payments (
    receipt_id UUID NOT NULL,
    payment_id UUID NOT NULL,
    amount_applied NUMERIC(10, 2) NOT NULL,

    PRIMARY KEY (receipt_id, payment_id),

    CONSTRAINT fk_receipt FOREIGN KEY (receipt_id)
        REFERENCES payments.receipts(id),
    CONSTRAINT fk_payment FOREIGN KEY (payment_id)
        REFERENCES payments.payments(id)
);

-- Vista: Deuda por usuario
CREATE VIEW payments.user_debt AS
SELECT
    r.user_id,
    r.organization_id,
    COUNT(r.id) AS pending_receipts,
    ARRAY_AGG(r.billing_month ORDER BY r.billing_year, r.billing_month) AS months_owed,
    SUM(r.total_amount) AS total_debt,
    MIN(r.due_date) AS oldest_due_date
FROM payments.receipts r
WHERE r.status IN ('PENDING', 'OVERDUE')
GROUP BY r.user_id, r.organization_id;
```

---

### MongoDB - Organizations Service (REFACTORIZADO)

```json
// ============================================
// ORGANIZATIONS SERVICE
// Tarifa integrada en zona
// ============================================

// Collection: organizations
{
  "_id": "org_001",
  "organizationCode": "JASS-RINCONADA-BELLAVISTA",
  "organizationName": "JASS Rinconada - Bellavista de Conta",
  "legalRepresentative": "Juan Pérez García",
  "address": "Av. Principal s/n, Rinconada",
  "phone": "+51 987654321",
  "logo": "https://storage.jass.gob.pe/logos/rinconada.png",

  // Tarifas globales de la organización
  "defaultReconnectionFee": 50.00,  // S/50 por reconexión


  "createdAt": ISODate("2024-01-15T00:00:00Z"),
  "createdBy": "admin@jass.gob.pe",
  "updatedAt": ISODate("2026-01-20T00:00:00Z"),
  "updatedBy": "admin@rinconada.jass.gob.pe"
}

// Collection: zones (MEJORADO)
{
  "_id": "zone_001",
  "organizationId": "org_001",
  "zoneCode": "ZONA-RINCONADA",
  "zoneName": "Rinconada",
  "description": "Sector alto de Rinconada",

  // TARIFA INTEGRADA (NO en colección separada)
  "monthlyFee": 8.00,  // S/8.00 fijo mensual

  "populationCount": 150,
  "householdCount": 35,

  "status": "ACTIVE",

  "createdAt": ISODate("2024-01-15T00:00:00Z"),
  "updatedAt": ISODate("2026-01-20T00:00:00Z")
}

{
  "_id": "zone_002",
  "organizationId": "org_001",
  "zoneCode": "ZONA-BELLAVISTA",
  "zoneName": "Bellavista de Conta",
  "description": "Sector bajo de Bellavista",

  // Tarifa diferente por zona
  "monthlyFee": 10.00,  // S/10.00 fijo mensual

  "populationCount": 200,
  "householdCount": 45,

  "status": "ACTIVE",

  "createdAt": ISODate("2024-01-15T00:00:00Z"),
  "updatedAt": ISODate("2026-01-20T00:00:00Z")
}

// JASS Roma (todas las zonas con misma tarifa)
{
  "_id": "zone_003",
  "organizationId": "org_002",
  "zoneCode": "ZONA-ROMA-CENTRO",
  "zoneName": "Roma Centro",

  "monthlyFee": 20.00,  // S/20.00 fijo mensual

  "status": "ACTIVE"
}

// Collection: streets
{
  "_id": "street_001",
  "zoneId": "zone_001",
  "organizationId": "org_001",
  "streetCode": "STR-001",
  "streetName": "Jr. Los Jazmines",
  "description": "Calle principal",

  "status": "ACTIVE",

  "createdAt": ISODate("2024-01-15T00:00:00Z")
}

// ❌ ELIMINAR collection "fares" (redundante)
// La tarifa ahora está en zones.monthlyFee

// Índices
db.organizations.createIndex({ "organizationCode": 1 }, { unique: true });
db.zones.createIndex({ "organizationId": 1, "zoneCode": 1 }, { unique: true });
db.zones.createIndex({ "status": 1 });
db.streets.createIndex({ "zoneId": 1 });
```

---

## 🔄 RABBITMQ - AJUSTES PARA ZONAS RURALES

### Exchanges y Queues Modificados

```
📬 jass.notification.events (Topic)
   Routing Keys:
   • notification.sms.send         → PRIORIDAD 1 (zonas rurales)
   • notification.whatsapp.send    → PRIORIDAD 2 (si tiene)
   • notification.email.send       → PRIORIDAD 3 (pocos lo tienen)

📨 Queues:
   • notification.sms.queue (PRIORIDAD ALTA)
     - Envío de credenciales
     - Recordatorios de pago
     - Alertas de corte
     - Confirmación de pago

   • notification.whatsapp.queue (MEDIA)
     - Solo si usuario tiene WhatsApp registrado

   • notification.email.queue (BAJA)
     - Solo si usuario tiene email registrado
```

### Lógica de Notificación

```java
// infrastructure/adapter/output/messaging/NotificationPublisher.java

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserCredentials(UserCreatedEvent event) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(event.getUserId())
            .username(event.getUsername())
            .temporaryPassword(event.getTemporaryPassword())
            .build();

        // PRIORIDAD: SMS > WhatsApp > Email
        if (event.getPhone() != null) {
            publishSMS(request);  // ✅ PRIORIDAD 1
        }

        if (event.getWhatsappNumber() != null) {
            publishWhatsApp(request);  // ✅ PRIORIDAD 2 (opcional)
        }

        if (event.getEmail() != null) {
            publishEmail(request);  // ✅ PRIORIDAD 3 (raro)
        }
    }

    private void publishSMS(NotificationRequest request) {
        rabbitTemplate.convertAndSend(
            "jass.notification.events",
            "notification.sms.send",
            request
        );
    }
}
```

---

## 📋 RESUMEN DE CAMBIOS NECESARIOS

### 🔴 ELIMINAR (Código Actual)

| Item | Ubicación | Razón |
|------|-----------|-------|
| ❌ `AuthCredential.java` | vg-ms-users/domain/models | Keycloak maneja auth |
| ❌ `@Document` en DistributionRoute | vg-ms-distribution/domain | Violación hexagonal |
| ❌ `@Document` en DistributionSchedule | vg-ms-distribution/domain | Violación hexagonal |
| ❌ `@Document` en User | vg-ms-water-quality/domain | Violación hexagonal |
| ❌ Collection `fares` | vg-ms-organizations | Redundante (va en zones) |
| ❌ `Receipts.java` (modelo viejo) | vg-ms-payments/domain | Asume medidores |

### ✅ AGREGAR (Nuevas Tablas/Campos)

| Item | Ubicación | Razón |
|------|-----------|-------|
| ✅ `monthlyFee` | zones (MongoDB) | Tarifa fija por zona |
| ✅ `reconnectionFee` | organizations/water_boxes | Pago por reposición |
| ✅ `is_cut`, `cut_date` | water_boxes (PostgreSQL) | Gestión de cortes |
| ✅ `months_owed` | receipts (PostgreSQL) | Meses de deuda acumulada |
| ✅ Vista `user_debt` | payments schema | Consulta rápida de deudas |
| ✅ `receipt_payments` | payments (PostgreSQL) | Relación N:M para pagos múltiples |

### ⚠️ MODIFICAR (Campos Existentes)

| Tabla/Documento | Campo | Cambio |
|-----------------|-------|--------|
| users | email | NULLABLE (zonas rurales) |
| users | phone | NULLABLE pero preferible |
| users | date_of_birth | ❌ ELIMINAR (innecesario) |
| users | email_verified | ❌ ELIMINAR (no verificar) |
| users | phone_verified | ❌ ELIMINAR (no verificar) |
| receipts | Estructura completa | Refactorizar (eliminar medidor) |
| zones | Agregar `monthlyFee` | Integrar tarifa |

---

## 🎯 CASOS DE USO REALES

### Caso 1: Pago del Mes Actual

```
Usuario: Juan Pérez
Zona: Rinconada (S/8.00/mes)
Caja: WB-001
Mes: Enero 2026

1. Sistema genera recibo:
   - monthly_fee: S/8.00
   - reconnection_fee: S/0.00
   - total_amount: S/8.00

2. Usuario paga en efectivo

3. Sistema:
   - Marca recibo como PAID
   - Registra pago
   - Envía SMS confirmación
```

### Caso 2: Pago con Deuda Acumulada

```
Usuario: María López
Zona: Bellavista (S/10.00/mes)
Caja: WB-005
Meses adeudados: Noviembre, Diciembre, Enero

1. Sistema muestra deuda:
   - Noviembre: S/10.00
   - Diciembre: S/10.00
   - Enero: S/10.00
   - TOTAL: S/30.00

2. Usuario paga S/30.00

3. Sistema:
   - Marca 3 recibos como PAID
   - Registra un pago
   - Relaciona pago con 3 recibos
   - Envía SMS confirmación
```

### Caso 3: Pago de Reposición

```
Usuario: Pedro Rojas
Zona: Roma (S/20.00/mes)
Caja: WB-010 (cortada por falta de pago)

1. Sistema calcula:
   - monthly_fee: S/20.00
   - reconnection_fee: S/50.00 (definido en org)
   - total_amount: S/70.00

2. Usuario paga S/70.00

3. Sistema:
   - Marca recibo como PAID
   - Actualiza water_box: is_cut = FALSE
   - Registra pago tipo RECONNECTION
   - Envía SMS confirmación
```

### Caso 4: Transferencia de Caja

```
Usuario anterior: Juan Pérez
Usuario nuevo: Carlos Díaz
Caja: WB-001

1. ADMIN aprueba transferencia

2. Sistema:
   - Crea registro en water_box_transfers
   - Desactiva assignment anterior
   - Crea nuevo assignment
   - Actualiza water_box.current_assignment_id
   - Envía SMS a ambos usuarios
```

---

## 🚀 PRÓXIMOS PASOS INMEDIATOS

1. ✅ **Eliminar AuthCredential** (vg-ms-users)
2. ✅ **Refactorizar Payments** (modelo sin medidor)
3. ✅ **Integrar tarifa en zones** (eliminar fares)
4. ✅ **Crear DistributionRouteDocument** (infrastructure)
5. ✅ **Crear DistributionScheduleDocument** (infrastructure)
6. ✅ **Eliminar User de water-quality** (usar referencia)
7. ✅ **Implementar SMS en Notification** (prioridad)
8. ✅ **Agregar campos de corte** (water_boxes)

---

## ✅ VALIDACIÓN DEL MODELO

### ✅ Cumple con el negocio real

- ✅ Pago fijo mensual por zona
- ✅ Sin medidores
- ✅ Tarifa por zona (Rinconada S/8, Bellavista S/10, Roma S/20)
- ✅ Pago de reposición por corte
- ✅ Acumulación de meses de deuda
- ✅ Transferencia de cajas entre usuarios
- ✅ SMS como canal prioritario
- ✅ Email/Phone opcionales (zonas rurales)
- ✅ Keycloak maneja autenticación
- ✅ Roles: SUPER_ADMIN, ADMIN, OPERATOR, CLIENT

---

**Documento creado por:** GitHub Copilot AI
**Fecha:** 20 de Enero de 2026
**Versión:** 2.0 - Ajustado al negocio REAL
**Estado:** Listo para implementación

# 🏗️ ARQUITECTURA COMPLETA - SISTEMA JASS

## Fecha: 20 Enero 2026

---

## 🎨 CONVENCIONES DE NOMENCLATURA (DECISIÓN FINAL)

### ⭐ ESTÁNDAR SELECCIONADO: **snake_case para BD + camelCase para Java**

**Decisión tomada:** Usar la convención mixta que es el estándar de la industria.

### 📋 REGLAS DEFINITIVAS

#### 🗄️ BASE DE DATOS (snake_case)

| Componente | Convención | Ejemplo |
|------------|------------|---------|
| **Tablas PostgreSQL** | **snake_case** (plural) | `users`, `water_boxes`, `maintenance_logs` |
| **Columnas PostgreSQL** | **snake_case** | `user_id`, `water_box_id`, `created_at` |
| **Collections MongoDB** | **snake_case** (plural) | `organizations`, `quality_tests`, `daily_records` |
| **Campos MongoDB** | **snake_case** | `user_id`, `water_box_id`, `created_at` |

**Ejemplo SQL:**

```sql
SELECT user_id, created_at
FROM water_boxes
WHERE zone_id = 'uuid';
```

**Ejemplo MongoDB:**

```json
{
  "_id": "uuid",
  "user_id": "uuid",
  "zone_id": "uuid",
  "created_at": "2026-01-20T10:00:00Z"
}
```

#### ☕ JAVA (camelCase)

| Componente | Convención | Ejemplo |
|------------|------------|---------|
| **Clases (Entities)** | **PascalCase** + Entity | `UserEntity`, `WaterBoxEntity` |
| **Clases (Documents)** | **PascalCase** + Document | `OrganizationDocument`, `NotificationDocument` |
| **Campos Java** | **camelCase** | `userId`, `waterBoxId`, `createdAt` |
| **Métodos Java** | **camelCase** | `getUserById()`, `createPayment()` |
| **Constantes** | **UPPER_SNAKE_CASE** | `MAX_RETRY_ATTEMPTS`, `DEFAULT_PAGE_SIZE` |

**Ejemplo Entity:**

```java
@Entity
@Table(name = "water_boxes")  // snake_case en BD
public class WaterBoxEntity {
    @Column(name = "user_id")  // snake_case en BD
    private UUID userId;        // camelCase en Java

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UUID getUserId() { return userId; }
}
```

**Ejemplo Document:**

```java
@Document(collection = "organizations")  // snake_case en BD
public class OrganizationDocument {
    @Field("user_id")      // snake_case en BD
    private String userId;  // camelCase en Java

    @Field("created_at")
    private LocalDateTime createdAt;
}
```

#### 🌐 APIs REST

| Componente | Convención | Ejemplo |
|------------|------------|---------|
| **Endpoints** | **kebab-case** | `/api/water-boxes`, `/api/maintenance-logs` |
| **Query params** | **camelCase** | `?userId=123`, `?startDate=2026-01` |
| **JSON Response** | **camelCase** | `{"userId": "...", "waterBoxId": "..."}` |

**Ejemplo API:**

```http
GET /api/water-boxes/uuid
Response:
{
  "id": "uuid",
  "userId": "uuid",
  "waterBoxId": "uuid",
  "createdAt": "2026-01-20T10:00:00Z"
}
```

### ✅ VENTAJAS DE ESTA DECISIÓN

- ✅ **SQL limpio:** Sin comillas en queries PostgreSQL
- ✅ **Java idiomático:** Código Java estándar con camelCase
- ✅ **Mapeo claro:** `@Column(name = "user_id")` / `@Field("user_id")` mapea BD → Java
- ✅ **JSON moderno:** APIs REST con camelCase estándar
- ✅ **Herramientas:** Compatibilidad total con pgAdmin, DBeaver, MongoDB Compass
- ✅ **Industria:** Lo que usan Google, Amazon, Netflix, Uber
- ✅ **Consistencia BD:** Ambas bases de datos usan snake_case
- ✅ **Sin confusión:** Regla simple = BD siempre snake_case, Java siempre camelCase

### 📊 RESUMEN VISUAL

```
┌─────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA COMPLETA                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🗄️ PostgreSQL                   🍃 MongoDB                 │
│  ├─ Tablas: snake_case          ├─ Collections: snake_case │
│  ├─ Columnas: snake_case        ├─ Campos: snake_case      │
│  └─ IDs: UUID                   └─ IDs: UUID (String)      │
│                                                             │
│  ☕ Java                          🌐 REST APIs               │
│  ├─ Clases: PascalCase          ├─ URLs: kebab-case        │
│  ├─ Campos: camelCase           ├─ Query: camelCase        │
│  ├─ Métodos: camelCase          └─ JSON: camelCase         │
│  └─ Mapeo: @Column/@Field                                  │
│                                                             │
│  Ejemplo Completo:                                          │
│  BD:   water_boxes.user_id  (snake_case)                   │
│  Java: waterBoxId           (camelCase)                    │
│  API:  /api/water-boxes     (kebab-case)                   │
│        {"waterBoxId": "..."} (camelCase JSON)              │
└─────────────────────────────────────────────────────────────┘
```

### 🔑 Identificadores UUID

**TODOS los IDs serán UUID v4 (no ObjectId de MongoDB):**

- **PostgreSQL:** Tipo `UUID` nativo con `gen_random_uuid()`
- **MongoDB:** Tipo `String` almacenando UUID como string (NO ObjectId automático)
- **Java:** `UUID` (java.util.UUID)

**¿Por qué UUID en MongoDB en lugar de ObjectId automático?**

✅ **Consistencia:** Mismo formato de ID en PostgreSQL y MongoDB
✅ **Referencias:** Fácil referenciar entre microservicios sin conversión
✅ **Migraciones:** Copiar datos entre bases sin conflictos
✅ **APIs:** Mismo formato de ID en todas las respuestas REST
✅ **Seguridad:** UUID más difícil de predecir que ObjectId secuencial

```java
// PostgreSQL - Hibernate genera UUID automáticamente
@Id
@GeneratedValue(generator = "UUID")
@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
@Column(columnDefinition = "uuid", updatable = false, nullable = false)
private UUID id;

// MongoDB - Generamos UUID manualmente en @PrePersist
@Id
private String id;  // UUID como String (36 caracteres)

@PrePersist
public void generateId() {
    if (this.id == null) {
        this.id = UUID.randomUUID().toString();
    }
}
```

**Nota:** MongoDB SÍ genera ObjectId automáticamente, pero lo deshabilitamos para usar UUID por las razones anteriores.

### 📝 Códigos de Negocio Legibles

**Recomendación:** Códigos simples, universales, sin dependencia de organización.

| Entidad | Formato Anterior ❌ | Formato Recomendado ✅ | Ejemplo |
|---------|-------------------|----------------------|---------|
| User | JASS-RINC-00001 | **USER-{YEAR}-{SEQ}** | USER-2026-00001 |
| WaterBox | WB-RINC-001 | **WB-{ZONE}-{SEQ}** | WB-RINC-001 *(mantener)* |
| Receipt | REC-2026-000001 | **REC-{YEAR}-{SEQ}** | REC-2026-000001 *(mantener)* |
| Payment | PAY-2026-000001 | **PAY-{YEAR}-{SEQ}** | PAY-2026-000001 *(mantener)* |
| Maintenance | MAINT-2026-00001 | **MNT-{YEAR}-{SEQ}** | MNT-2026-00001 *(más corto)* |

**Razón del cambio en User:**

- ✅ Más simple y escalable
- ✅ No depende de la organización (usuario puede cambiar de JASS)
- ✅ Sigue patrón consistente con otros códigos (año + secuencia)
- ✅ Más corto y fácil de comunicar por teléfono

---

## 📊 RESUMEN EJECUTIVO

### Total: **11 Microservicios**

| # | Microservicio | Base de Datos | Tablas/Collections | Función |
|---|---------------|---------------|-------------------|---------|
| 1 | vg-ms-authentication | ❌ Ninguna | 0 | Keycloak proxy |
| 2 | vg-ms-users | 🟦 PostgreSQL | 3 | Gestión usuarios |
| 3 | vg-ms-organizations | 🟩 MongoDB | 5 | JASS, zonas, calles |
| 4 | vg-ms-infrastructure | 🟦 PostgreSQL | 5 | Cajas agua, mantenimientos |
| 5 | vg-ms-finance | 🟦 PostgreSQL | 4 | Recibos, pagos, balance |
| 6 | vg-ms-distribution | 🟩 MongoDB | 3 | Programación distribución |
| 7 | vg-ms-inventory-purchases | 🟦 PostgreSQL | 6 | Inventario, compras |
| 8 | vg-ms-water-quality | 🟩 MongoDB | 4 | Calidad agua |
| 9 | vg-ms-claims-incidents | 🟩 MongoDB | 6 | Reclamos, incidentes |
| 10 | vg-ms-notification | 🟩 MongoDB | 3 | Notificaciones SMS/Email |
| 11 | vg-ms-gateway | ❌ Ninguna | 0 | API Gateway |

**PostgreSQL: 4 microservicios (18 tablas)**
**MongoDB: 5 microservicios (21 collections)**
**Sin BD: 2 microservicios**

---

## 1️⃣ VG-MS-AUTHENTICATION

### Base de Datos: ❌ NINGUNA

**Función:** Proxy para Keycloak. Toda autenticación la maneja Keycloak.

**Endpoints:**

- `POST /auth/login` → Redirige a Keycloak
- `POST /auth/logout` → Redirige a Keycloak
- `GET /auth/user-info` → Obtiene info del token JWT

**Entidades:** NINGUNA

---

## 2️⃣ VG-MS-USERS

### Base de Datos: 🟦 PostgreSQL

**Schema:** `users`

### Entidades (3)

#### 1. UserEntity

```java
@Entity
@Table(name = "users", schema = "users")
public class UserEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_code", unique = true, nullable = false, length = 20)
    private String userCode;  // USER-2026-00001

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private UUID keycloakId;  // UUID de Keycloak

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "dni", unique = true, nullable = false, length = 8)
    private String dni;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "organization_id", nullable = false)
    private String organizationId;  // UUID como String (MongoDB)

    @Column(name = "zone_id", nullable = false)
    private String zoneId;  // UUID como String (MongoDB)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### 2. UserCodeCounterEntity

```java
@Entity
@Table(name = "user_code_counters", schema = "users")
public class UserCodeCounterEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "year", unique = true, nullable = false)
    private Integer year;  // 2026

    @Column(name = "last_sequence", nullable = false)
    private Integer lastSequence;  // Último número usado (00001, 00002, ...)

    @Version
    @Column(name = "version")
    private Long version;  // Para concurrencia optimista

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### 3. UserAuditEntity

```java
@Entity
@Table(name = "user_audits", schema = "users")
public class UserAuditEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;  // CREATE, UPDATE, DELETE, STATUS_CHANGE

    @Column(name = "performed_by", nullable = false, columnDefinition = "uuid")
    private UUID performedBy;  // Keycloak UUID

    @Column(name = "changes", columnDefinition = "jsonb")
    private String changes;  // JSON con cambios

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;
}
```

---

## 3️⃣ VG-MS-ORGANIZATIONS

### Base de Datos: 🟩 MongoDB

**Database:** `organizations_db`

### Collections (5)

#### 1. OrganizationDocument

```java
@Document(collection = "organizations")
public class OrganizationDocument {
    @Id
    private String id;  // UUID.randomUUID().toString()

    @Indexed(unique = true)
    @Field("code")
    private String code;  // JASS-RINC, JASS-BELLA, JASS-ROMA

    @Field("name")
    private String name;  // "JASS Rinconada-Bellavista"

    @Field("address")
    private String address;

    // Configuración específica de cada JASS
    @Field("custom_parameters")
    private Map<String, Object> customParameters;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("status")
    private String status;  // ACTIVE, INACTIVE
}
```

#### 2. ZoneDocument

```java
@Document(collection = "zones")
public class ZoneDocument {
    @Id
    private String id;  // UUID.randomUUID().toString()

    @Indexed
    @Field("organization_id")
    private String organizationId;  // UUID de organizations

    @Indexed(unique = true)
    @Field("code")
    private String code;  // RINC, BELLA, ROMA

    @Field("name")
    private String name;  // "Rinconada"

    @Field("total_water_boxes")
    private Integer totalWaterBoxes;

    @Field("active_water_boxes")
    private Integer activeWaterBoxes;

    // Coordenadas geográficas (opcional)
    @Field("latitude")
    private Double latitude;

    @Field("longitude")
    private Double longitude;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("status")
    private String status;  // ACTIVE, INACTIVE
}
```

#### 3. ZoneFareHistoryDocument

```java
@Document(collection = "zone_fare_history")
public class ZoneFareHistoryDocument {
    @Id
    private String id;  // UUID.randomUUID().toString()

    @Indexed
    @Field("zone_id")
    private String zoneId;  // UUID de zones

    @Field("monthly_fee")
    private BigDecimal monthlyFee;  // S/8.00, S/10.00, S/20.00

    @Indexed
    @Field("valid_from")
    private LocalDate validFrom;  // Fecha desde cuando aplica

    @Field("valid_until")
    private LocalDate validUntil;  // Fecha hasta cuando aplicó (null = vigente)

    @Indexed
    @Field("status")
    private String status;  // ACTIVE, INACTIVE

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("created_by")
    private String createdBy;  // UUID de Keycloak (ADMIN)

    @Field("reason")
    private String reason;  // "Ajuste por inflación anual"
}
```

#### 4. StreetDocument

```java
@Document(collection = "streets")
public class StreetDocument {
    @Id
    private String id;  // UUID.randomUUID().toString()

    @Indexed
    @Field("zone_id")
    private String zoneId;  // UUID de zones

    @Field("name")
    private String name;  // "Jr. Los Pinos"

    @Field("reference")
    private String reference;  // "Cerca al parque principal"

    @Field("created_at")
    private LocalDateTime createdAt;
}
```

#### 5. ParameterDocument

```java
@Document(collection = "parameters")
public class ParameterDocument {
    @Id
    private String id;  // UUID.randomUUID().toString()

    @Indexed
    @Field("organization_id")
    private String organizationId;  // UUID de organizations

    @Indexed
    @Field("key")
    private String key;  // RECONNECTION_FEE, SMS_PROVIDER, etc.

    @Field("value")
    private String value;  // "50.00", "TWILIO", etc.

    @Field("type")
    private String type;  // NUMERIC, STRING, BOOLEAN

    @Field("description")
    private String description;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;  // UUID de Keycloak
}
```

---

## 4️⃣ VG-MS-INFRASTRUCTURE

### Base de Datos: 🟦 PostgreSQL

**Schema:** `infrastructure`

### Entidades (5)

#### 1. WaterBoxEntity

```java
@Entity
@Table(name = "water_boxes", schema = "infrastructure")
public class WaterBoxEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "box_code", unique = true, nullable = false, length = 15)
    private String boxCode;  // WB-RINC-001

    @Column(name = "zone_id", nullable = false, length = 36)
    private String zoneId;  // UUID de zones (MongoDB)

    @Column(name = "street_id", length = 36)
    private String streetId;  // UUID de streets (MongoDB)

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "reference", length = 200)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WaterBoxStatus status;  // ACTIVE, INACTIVE, CUT, MAINTENANCE

    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    @Column(name = "last_maintenance_at")
    private LocalDateTime lastMaintenanceAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### 2. WaterBoxAssignmentEntity

```java
@Entity
@Table(name = "water_box_assignments", schema = "infrastructure")
public class WaterBoxAssignmentEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "water_box_id", nullable = false, columnDefinition = "uuid")
    private UUID waterBoxId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;  // UUID de users (PostgreSQL)

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", nullable = false, columnDefinition = "uuid")
    private UUID assignedBy;  // UUID de Keycloak (ADMIN)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssignmentStatus status;  // ACTIVE, TRANSFERRED, CANCELLED

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
```

#### 3. WaterBoxTransferEntity

```java
@Entity
@Table(name = "water_box_transfers", schema = "infrastructure")
public class WaterBoxTransferEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "water_box_id", nullable = false, columnDefinition = "uuid")
    private UUID waterBoxId;

    @Column(name = "from_user_id", nullable = false, columnDefinition = "uuid")
    private UUID fromUserId;

    @Column(name = "to_user_id", nullable = false, columnDefinition = "uuid")
    private UUID toUserId;

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt;

    @Column(name = "authorized_by", nullable = false, columnDefinition = "uuid")
    private UUID authorizedBy;  // UUID de Keycloak (ADMIN)

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;  // PENDING, APPROVED, REJECTED
}
```

#### 4. MaintenanceLogEntity

```java
@Entity
@Table(name = "maintenance_logs", schema = "infrastructure")
public class MaintenanceLogEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "maintenance_code", unique = true, nullable = false, length = 20)
    private String maintenanceCode;  // MNT-2026-00001

    @Column(name = "water_box_id", nullable = false, columnDefinition = "uuid")
    private UUID waterBoxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MaintenanceType type;  // RECONNECTION, REPAIR, PREVENTIVE, EMERGENCY

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MaintenanceStatus status;  // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "performed_by", columnDefinition = "uuid")
    private UUID performedBy;  // UUID de Keycloak (OPERATOR en vg-ms-users)

    @Column(name = "reconnection_payment_id", columnDefinition = "uuid")
    private UUID reconnectionPaymentId;  // Si es reposición (referencia a payments)

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### 5. MaintenanceMaterialEntity

```java
@Entity
@Table(name = "maintenance_materials", schema = "infrastructure")
public class MaintenanceMaterialEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "maintenance_id", nullable = false, columnDefinition = "uuid")
    private UUID maintenanceId;

    @Column(name = "product_id", nullable = false, columnDefinition = "uuid")
    private UUID productId;  // UUID de producto en vg-ms-inventory

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed;

    @Column(nullable = false)
    private BigDecimal unitCost;

    @Column(nullable = false)
    private BigDecimal totalCost;  // quantityUsed * unitCost
}
```

---

## 5️⃣ VG-MS-FINANCE

### Base de Datos: 🟦 PostgreSQL

**Schema:** `finance`

### Entidades (4)

#### 1. ReceiptEntity

```java
@Entity
@Table(name = "receipts", schema = "finance")
public class ReceiptEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "receipt_code", unique = true, nullable = false, length = 20)
    private String receiptCode;  // REC-2026-000001

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;  // UUID de vg-ms-users

    @Column(name = "water_box_id", nullable = false, columnDefinition = "uuid")
    private UUID waterBoxId;  // UUID de vg-ms-infrastructure

    @Column(name = "zone_id", nullable = false, length = 36)
    private String zoneId;  // UUID de vg-ms-organizations (MongoDB)

    // Meses cubiertos (ARRAY PostgreSQL)
    @Column(name = "months_owed", columnDefinition = "text[]", nullable = false)
    private String[] monthsOwed;  // {"2026-01", "2026-02"}

    // Montos
    @Column(name = "monthly_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee;  // Tarifa fija de la zona (S/8, S/10, S/20)

    @Column(name = "months_count", nullable = false)
    private Integer monthsCount;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;  // monthlyFee * monthsCount

    @Column(name = "reconnection_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal reconnectionFee;  // S/50.00 o 0

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;  // subtotal + reconnectionFee

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;  // PENDING, PAID, OVERDUE, CANCELLED

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;  // S3 URL
}
```

#### 2. PaymentEntity (= INGRESOS)

```java
@Entity
@Table(name = "payments", schema = "finance",
       indexes = {@Index(name = "idx_payment_date", columnList = "paid_at")})
public class PaymentEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payment_code", unique = true, nullable = false, length = 20)
    private String paymentCode;  // PAY-2026-000001

    @Column(name = "receipt_id", nullable = false, columnDefinition = "uuid")
    private UUID receiptId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;  // UUID de vg-ms-users

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;  // CASH, YAPE

    @Column(name = "yape_reference", length = 50)
    private String yapeReference;

    @Column(name = "yape_phone", length = 15)
    private String yapePhone;

    @Column(name = "paid_at", nullable = false)  // ⭐ CLAVE para reportes de ingresos
    private LocalDateTime paidAt;

    @Column(name = "received_by", nullable = false, columnDefinition = "uuid")
    private UUID receivedBy;  // UUID de Keycloak (usuario OPERATOR en vg-ms-users)

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;  // S3 URL del comprobante
}
```

#### 3. PaymentDetailEntity

```java
@Entity
@Table(name = "payment_details", schema = "finance")
public class PaymentDetailEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false, columnDefinition = "uuid")
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private PaymentDetailType type;  // MONTHLY_FEE, RECONNECTION_FEE

    @Column(name = "month", length = 7)  // "2026-01" (si aplica)
    private String month;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
}
```

#### 4. ExpenseEntity (= EGRESOS)

```java
@Entity
@Table(name = "expenses", schema = "finance",
       indexes = {
           @Index(name = "idx_expense_date", columnList = "expense_date"),
           @Index(name = "idx_expense_type", columnList = "type")
       })
public class ExpenseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "expense_code", unique = true, nullable = false, length = 20)
    private String expenseCode;  // EXP-2026-000001

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ExpenseType type;  // PURCHASE, SALARY, MAINTENANCE_MATERIALS, UTILITY_BILLS, ADMINISTRATIVE

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)  // ⭐ CLAVE para reportes de egresos
    private LocalDate expenseDate;

    // Trazabilidad
    @Column(name = "source_type", length = 30)
    private String sourceType;  // "PURCHASE", "SALARY", "MAINTENANCE"

    @Column(name = "source_id", length = 100)
    private String sourceId;  // ID genérico como String

    @Column(name = "description", columnDefinition = "text")
    private String description;

    // Referencias a otros microservicios (todos UUID)
    @Column(name = "user_id", columnDefinition = "uuid")  // Para SALARY (vg-ms-users)
    private UUID userId;

    @Column(name = "supplier_id", columnDefinition = "uuid")  // Para PURCHASE (vg-ms-inventory)
    private UUID supplierId;

    @Column(name = "purchase_id", columnDefinition = "uuid")  // Para PURCHASE (vg-ms-inventory)
    private UUID purchaseId;

    @Column(name = "maintenance_id", columnDefinition = "uuid")  // Para MAINTENANCE (vg-ms-infrastructure) - OPCIONAL
    private UUID maintenanceId;

    @Column(name = "authorized_by", nullable = false, columnDefinition = "uuid")
    private UUID authorizedBy;  // UUID de Keycloak (ADMIN)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

// Enum
public enum ExpenseType {
    PURCHASE,
    SALARY,
    MAINTENANCE_MATERIALS,
    UTILITY_BILLS,
    ADMINISTRATIVE
}
```

---

## 6️⃣ VG-MS-DISTRIBUTION

### Base de Datos: 🟩 MongoDB

**Database:** `distribution_db`

### Collections (3)

#### 1. DistributionProgramDocument

```java
@Document(collection = "distribution_programs")
public class DistributionProgramDocument {
    @Id
    private String id;

    @Indexed
    private String organizationId;

    private String name;  // "Programa Semanal Enero 2026"
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Indexed
    private String status;  // DRAFT, ACTIVE, COMPLETED, CANCELLED

    // Horarios por zona (desnormalizado)
    private List<ZoneSchedule> schedules;

    private LocalDateTime createdAt;
    private String createdBy;
}

// Clase embebida
public class ZoneSchedule {
    private String zoneId;
    private String zoneName;
    private String dayOfWeek;  // MONDAY, TUESDAY, etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer estimatedAffectedUsers;
    private String notes;
}
```

#### 2. DistributionRouteDocument

```java
@Document(collection = "distribution_routes")
public class DistributionRouteDocument {
    @Id
    private String id;

    @Indexed
    private String zoneId;

    private String name;  // "Ruta Norte Rinconada"
    private List<String> streetIds;  // Lista de calles en orden

    private Integer estimatedDurationMinutes;

    private LocalDateTime createdAt;
    private String status;  // ACTIVE, INACTIVE
}
```

#### 3. DistributionScheduleDocument

```java
@Document(collection = "distribution_schedules")
public class DistributionScheduleDocument {
    @Id
    private String id;

    @Indexed
    private String programId;

    @Indexed
    private LocalDate scheduleDate;

    private String zoneId;
    private String routeId;

    private LocalTime startTime;
    private LocalTime endTime;

    @Indexed
    private String status;  // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    private Integer affectedUsers;
    private String notes;

    private LocalDateTime completedAt;
    private String completedBy;
}
```

---

## 7️⃣ VG-MS-INVENTORY-PURCHASES

### Base de Datos: 🟦 PostgreSQL

**Schema:** `inventory`

### Entidades (6)

#### 1. ProductEntity

```java
@Entity
@Table(name = "products", schema = "inventory")
public class ProductEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode;  // PROD-00001

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "category_id", nullable = false, columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "unit", nullable = false, length = 20)  // UNIDAD, METRO, KILO, LITRO
    private String unit;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "max_stock")
    private Integer maxStock;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;  // ACTIVE, INACTIVE
}
```

#### 2. ProductCategoryEntity

```java
@Entity
@Table(name = "product_categories", schema = "inventory")
public class ProductCategoryEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;  // TUBERIAS, VALVULAS, HERRAMIENTAS, ACCESORIOS

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### 3. InventoryMovementEntity

```java
@Entity
@Table(name = "inventory_movements", schema = "inventory")
public class InventoryMovementEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, columnDefinition = "uuid")
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MovementType type;  // ENTRY, EXIT, ADJUSTMENT

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "reference_id", length = 100)  // ID de compra o mantenimiento
    private String referenceId;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "performed_by", nullable = false, columnDefinition = "uuid")
    private UUID performedBy;  // UUID de Keycloak
}
```

#### 4. PurchaseEntity

```java
@Entity
@Table(name = "purchases", schema = "inventory")
public class PurchaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "purchase_code", unique = true, nullable = false, length = 20)
    private String purchaseCode;  // PUR-2026-00001

    @Column(name = "supplier_id", nullable = false, columnDefinition = "uuid")
    private UUID supplierId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PurchaseStatus status;  // DRAFT, PENDING, RECEIVED, CANCELLED

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "authorized_by", nullable = false, columnDefinition = "uuid")
    private UUID authorizedBy;  // UUID de Keycloak (ADMIN)

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### 5. PurchaseDetailEntity

```java
@Entity
@Table(name = "purchase_details", schema = "inventory")
public class PurchaseDetailEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "purchase_id", nullable = false, columnDefinition = "uuid")
    private UUID purchaseId;

    @Column(name = "product_id", nullable = false, columnDefinition = "uuid")
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;  // quantity * unitPrice
}
```

#### 6. SupplierEntity

```java
@Entity
@Table(name = "suppliers", schema = "inventory")
public class SupplierEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ruc", unique = true, nullable = false, length = 11)
    private String ruc;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 300)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupplierStatus status;  // ACTIVE, INACTIVE, BLOCKED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## 8️⃣ VG-MS-WATER-QUALITY

### Base de Datos: 🟩 MongoDB

**Database:** `water_quality_db`

### Collections (4)

#### 1. TestingPointDocument

```java
@Document(collection = "testing_points")
public class TestingPointDocument {
    @Id
    private String id;

    @Indexed
    private String organizationId;

    @Indexed(unique = true)
    private String code;  // TP-RINC-001

    private String name;  // "Punto de captación principal"
    private String location;
    private String zoneId;

    // Coordenadas GPS
    private Double latitude;
    private Double longitude;

    @Indexed
    private String status;  // ACTIVE, INACTIVE, MAINTENANCE

    private LocalDateTime createdAt;
}
```

#### 2. QualityTestDocument

```java
@Document(collection = "quality_tests")
public class QualityTestDocument {
    @Id
    private String id;

    @Indexed
    private String testingPointId;

    @Indexed
    private LocalDateTime testedAt;

    private String performedBy;  // Keycloak ID

    // Parámetros medidos
    private Map<String, TestParameter> parameters;

    @Indexed
    private String result;  // COMPLIANT, NON_COMPLIANT, REQUIRES_REVIEW

    private String observations;
    private List<String> attachments;  // URLs S3

    private LocalDateTime createdAt;
}

// Clase embebida
public class TestParameter {
    private String name;  // "pH", "Cloro", "Turbidez"
    private Double value;
    private String unit;
    private Double minThreshold;
    private Double maxThreshold;
    private String status;  // OK, WARNING, CRITICAL
}
```

#### 3. DailyRecordDocument

```java
@Document(collection = "daily_records")
@CompoundIndex(def = "{'recordDate': 1, 'organizationId': 1}")
public class DailyRecordDocument {
    @Id
    private String id;

    @Indexed
    private String organizationId;

    @Indexed
    private LocalDate recordDate;

    private Integer totalTests;
    private Integer compliantTests;
    private Integer nonCompliantTests;

    // Estadísticas
    private Map<String, Double> averageParameters;

    private String recordedBy;
    private LocalDateTime createdAt;

    // TTL: auto-borrar después de 2 años
    @Indexed(expireAfterSeconds = 63072000)
    private LocalDateTime expiresAt;
}
```

#### 4. UserWaterQualityDocument

```java
@Document(collection = "water_quality_users")
public class UserWaterQualityDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String keycloakId;

    private String fullName;

    @Indexed
    private String role;  // TECHNICIAN, SUPERVISOR, ADMIN

    private List<String> authorizedZones;

    @Indexed
    private String status;  // ACTIVE, INACTIVE

    private LocalDateTime createdAt;
}
```

---

## 9️⃣ VG-MS-CLAIMS-INCIDENTS

### Base de Datos: 🟩 MongoDB

**Database:** `claims_incidents_db`

### Collections (6)

#### 1. ComplaintDocument

```java
@Document(collection = "complaints")
public class ComplaintDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String complaintCode;  // COMP-2026-00001

    @Indexed
    private String userId;

    @Indexed
    private String categoryId;

    private String title;

    @TextIndexed
    private String description;

    @Indexed
    private String priority;  // LOW, MEDIUM, HIGH, URGENT

    @Indexed
    private String status;  // NEW, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED

    private List<String> attachments;  // URLs S3

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;

    private String assignedTo;  // Keycloak ID
}
```

#### 2. ComplaintCategoryDocument

```java
@Document(collection = "complaint_categories")
public class ComplaintCategoryDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code;  // WATER_QUALITY, BILLING, SERVICE, INFRASTRUCTURE

    private String name;
    private String description;

    private Integer estimatedResolutionDays;

    @Indexed
    private String status;  // ACTIVE, INACTIVE
}
```

#### 3. ComplaintResponseDocument

```java
@Document(collection = "complaint_responses")
public class ComplaintResponseDocument {
    @Id
    private String id;

    @Indexed
    private String complaintId;

    @TextIndexed
    private String message;

    private String respondedBy;  // Keycloak ID
    private LocalDateTime respondedAt;

    private List<String> attachments;

    @Indexed
    private String responseType;  // COMMENT, STATUS_UPDATE, RESOLUTION
}
```

#### 4. IncidentDocument

```java
@Document(collection = "incidents")
public class IncidentDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String incidentCode;  // INC-2026-00001

    @Indexed
    private String typeId;

    private String title;

    @TextIndexed
    private String description;

    @Indexed
    private String severity;  // MINOR, MODERATE, MAJOR, CRITICAL

    @Indexed
    private String status;  // REPORTED, INVESTIGATING, IN_PROGRESS, RESOLVED, CLOSED

    // Ubicación afectada
    private String zoneId;
    private List<String> affectedWaterBoxIds;
    private Integer affectedUsers;

    private List<String> attachments;

    private LocalDateTime reportedAt;
    private String reportedBy;

    private LocalDateTime resolvedAt;
}
```

#### 5. IncidentResolutionDocument

```java
@Document(collection = "incident_resolutions")
public class IncidentResolutionDocument {
    @Id
    private String id;

    @Indexed
    private String incidentId;

    @TextIndexed
    private String resolution;

    private List<String> actionsTaken;

    private List<String> materialsUsed;  // IDs de productos

    private LocalDateTime resolvedAt;
    private String resolvedBy;

    private List<String> attachments;
}
```

#### 6. IncidentTypeDocument

```java
@Document(collection = "incident_types")
public class IncidentTypeDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code;  // PIPE_BREAK, VALVE_FAILURE, WATER_LEAK, NO_WATER

    private String name;
    private String description;

    private String defaultSeverity;
    private Integer estimatedResolutionHours;

    @Indexed
    private String status;  // ACTIVE, INACTIVE
}
```

---

## 🔟 VG-MS-NOTIFICATION

### Base de Datos: 🟩 MongoDB

**Database:** `notification_db`

### Collections (3)

#### 1. NotificationDocument

```java
@Document(collection = "notifications")
@CompoundIndex(def = "{'createdAt': 1}")
public class NotificationDocument {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String channel;  // SMS, EMAIL, WHATSAPP

    private String recipient;  // Teléfono o email

    @Indexed
    private String type;  // RECEIPT, PAYMENT_CONFIRMATION, INCIDENT_ALERT

    private String subject;  // Solo EMAIL
    private String message;

    @Indexed
    private String status;  // PENDING, SENT, FAILED, DELIVERED

    private String provider;  // TWILIO, AWS_SES, WHATSAPP_API
    private String providerId;
    private String errorMessage;

    private Integer retryCount;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @Indexed
    private LocalDateTime createdAt;

    // TTL: auto-borrar después de 6 meses
    @Indexed(expireAfterSeconds = 15552000)
    private LocalDateTime expiresAt;
}
```

#### 2. NotificationTemplateDocument

```java
@Document(collection = "notification_templates")
public class NotificationTemplateDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code;  // RECEIPT_SMS, PAYMENT_CONFIRMATION_EMAIL

    @Indexed
    private String channel;  // SMS, EMAIL, WHATSAPP

    private String subject;  // Solo EMAIL
    private String template;  // "Hola {userName}, tu recibo {receiptCode}..."

    private List<String> variables;  // ["userName", "receiptCode", "amount"]

    @Indexed
    private String status;  // ACTIVE, INACTIVE

    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

#### 3. NotificationPreferenceDocument

```java
@Document(collection = "notification_preferences")
public class NotificationPreferenceDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    // Preferencias por tipo
    private Map<String, ChannelPreference> preferences;

    private LocalDateTime updatedAt;
}

// Clase embebida
public class ChannelPreference {
    private String notificationType;  // RECEIPT, PAYMENT, INCIDENT
    private List<String> enabledChannels;  // ["SMS", "EMAIL"]
    private String primaryChannel;  // SMS
}
```

---

## 1️⃣1️⃣ VG-MS-GATEWAY

### Base de Datos: ❌ NINGUNA

**Función:** API Gateway con Spring Cloud Gateway. Enruta peticiones a microservicios.

**Configuración:**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: users-service
          uri: lb://vg-ms-users
          predicates:
            - Path=/api/users/**

        - id: finance-service
          uri: lb://vg-ms-finance
          predicates:
            - Path=/api/receipts/**, /api/payments/**, /api/expenses/**

        - id: infrastructure-service
          uri: lb://vg-ms-infrastructure
          predicates:
            - Path=/api/water-boxes/**, /api/maintenance/**
```

**Entidades:** NINGUNA

---

## 📊 TABLA RESUMEN COMPLETA

| Microservicio | BD | Schema/DB | Tablas/Collections | Enums |
|---------------|----|-----------|--------------------|-------|
| vg-ms-authentication | ❌ | - | 0 | - |
| vg-ms-users | 🟦 | users | 3 | UserStatus |
| vg-ms-organizations | 🟩 | organizations_db | 5 | - |
| vg-ms-infrastructure | 🟦 | infrastructure | 5 | WaterBoxStatus, AssignmentStatus, TransferStatus, MaintenanceType, MaintenanceStatus |
| vg-ms-finance | 🟦 | finance | 4 | PaymentStatus, PaymentMethod, PaymentDetailType, ExpenseType |
| vg-ms-distribution | 🟩 | distribution_db | 3 | - |
| vg-ms-inventory-purchases | 🟦 | inventory | 6 | MovementType, PurchaseStatus, SupplierStatus |
| vg-ms-water-quality | 🟩 | water_quality_db | 4 | - |
| vg-ms-claims-incidents | 🟩 | claims_incidents_db | 6 | - |
| vg-ms-notification | 🟩 | notification_db | 3 | - |
| vg-ms-gateway | ❌ | - | 0 | - |

**Total PostgreSQL:** 18 tablas
**Total MongoDB:** 21 collections
**Total Enums:** 13

---

## 🔗 RELACIONES ENTRE MICROSERVICIOS

### PostgreSQL → MongoDB

- `users.organization_id` → `organizations._id`
- `users.zone_id` → `zones._id`
- `water_boxes.zone_id` → `zones._id`
- `water_boxes.street_id` → `streets._id`
- `receipts.zone_id` → `zones._id`

### PostgreSQL → PostgreSQL

- `receipts.user_id` → `users.id`
- `receipts.water_box_id` → `water_boxes.id`
- `payments.user_id` → `users.id`
- `payments.receipt_id` → `receipts.id`
- `expenses.user_id` → `users.id` (para salarios)
- `expenses.supplier_id` → `suppliers.id`
- `expenses.purchase_id` → `purchases.id`
- `expenses.maintenance_id` → `maintenance_logs.id`
- `maintenance_materials.product_id` → `products.id`
- `water_box_assignments.user_id` → `users.id`
- `water_box_transfers.from_user_id/to_user_id` → `users.id`

### MongoDB → PostgreSQL

- Notificaciones usan `userId` para referenciar usuarios
- Reclamos usan `userId` para referenciar usuarios
- Incidentes usan `zoneId` para referenciar zonas

---

## 🎯 CÓDIGOS LEGIBLES POR ENTIDAD (ACTUALIZADOS)

| Entidad | Campo | Formato | Ejemplo | Implementación |
|---------|-------|---------|---------|----------------|
| **User** | userCode | **USER-{YEAR}-{SEQ}** | USER-2026-00001 | PostgreSQL sequence + año |
| WaterBox | boxCode | WB-{ZONE}-{SEQ} | WB-RINC-001 | PostgreSQL sequence por zona |
| Receipt | receiptCode | REC-{YEAR}-{SEQ} | REC-2026-000001 | PostgreSQL sequence + año |
| Payment | paymentCode | PAY-{YEAR}-{SEQ} | PAY-2026-000001 | PostgreSQL sequence + año |
| Expense | expenseCode | EXP-{YEAR}-{SEQ} | EXP-2026-000001 | PostgreSQL sequence + año |
| **Maintenance** | maintenanceCode | **MNT-{YEAR}-{SEQ}** | MNT-2026-00001 | PostgreSQL sequence + año |
| Product | productCode | PROD-{SEQ} | PROD-00001 | PostgreSQL sequence global |
| Purchase | purchaseCode | PUR-{YEAR}-{SEQ} | PUR-2026-00001 | PostgreSQL sequence + año |
| Complaint | complaintCode | COMP-{YEAR}-{SEQ} | COMP-2026-00001 | MongoDB findOneAndUpdate |
| Incident | incidentCode | INC-{YEAR}-{SEQ} | INC-2026-00001 | MongoDB findOneAndUpdate |

**Cambios recomendados:**

- ✅ **USER-2026-00001** (en lugar de JASS-RINC-00001): Más simple, escalable, sin depender de organización
- ✅ **MNT-2026-00001** (en lugar de MAINT-2026-00001): Más corto, fácil de comunicar

---

## 🔧 DETALLES TÉCNICOS DE IMPLEMENTACIÓN

### PostgreSQL UUID Generation

```sql
-- Habilitar extensión UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Usar gen_random_uuid() en defaults
ALTER TABLE users.users
    ALTER COLUMN id SET DEFAULT gen_random_uuid();
```

### Java UUID Configuration

```java
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(
        name = "id",
        columnDefinition = "uuid",
        updatable = false,
        nullable = false
    )
    private UUID id;
}
```

### MongoDB UUID Generation (Manual con snake_case)

**Importante:** MongoDB genera ObjectId automáticamente, pero lo deshabilitamos para usar UUID.

```java
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

// EJEMPLO COMPLETO: Document con snake_case
@Document(collection = "organizations")
public class OrganizationDocument {
    @Id
    private String id;  // UUID como String

    @Field("organization_name")  // snake_case en MongoDB
    private String organizationName;  // camelCase en Java

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}

// OPCIÓN 1: @PrePersist en cada Document
@Document(collection = "organizations")
public class OrganizationDocument {
    @Id
    private String id;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}

// OPCIÓN 2: Listener global (RECOMENDADO - aplica a todos los documents)
@Component
public class UuidGeneratorListener extends AbstractMongoEventListener<Object> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        if (source instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument) source;
            if (doc.getId() == null) {
                doc.setId(UUID.randomUUID().toString());
            }
        }
    }
}

// BaseDocument interface para todos los documents
public interface BaseDocument {
    String getId();
    void setId(String id);
}

// Implementación en cada Document
@Document(collection = "organizations")
public class OrganizationDocument implements BaseDocument {
    @Id
    private String id;

    @Override
    public String getId() { return id; }

    @Override
    public void setId(String id) { this.id = id; }
}
```

**📌 Nota sobre @Field:**

- **Opcional si el nombre es igual:** Si el campo Java es `name` y MongoDB es `name`, no necesitas `@Field`
- **Obligatorio si es diferente:** Como usamos snake_case en BD y camelCase en Java, SÍ necesitas `@Field`
- **Ejemplo:** `@Field("created_at")` para mapear `createdAt` (Java) → `created_at` (MongoDB)

### Sequence para Códigos Legibles

```sql
-- PostgreSQL
CREATE SEQUENCE users.user_code_seq_2026 START WITH 1;

-- Función para generar código
CREATE OR REPLACE FUNCTION users.generate_user_code()
RETURNS TEXT AS $$
DECLARE
    year INT := EXTRACT(YEAR FROM CURRENT_DATE);
    seq INT;
BEGIN
    seq := nextval('users.user_code_seq_' || year::TEXT);
    RETURN 'USER-' || year::TEXT || '-' || LPAD(seq::TEXT, 5, '0');
END;
$$ LANGUAGE plpgsql;
```

---

## 📝 PRÓXIMOS PASOS

1. ✅ Arquitectura de microservicios definida (COMPLETADO)
2. ✅ Modelos de datos completos con UUID (COMPLETADO)
3. ✅ Nomenclatura estandarizada (COMPLETADO)
4. ⏭️ Generar estructura de carpetas hexagonal
5. ⏭️ Crear archivos base (pom.xml, application.yml, docker-compose.yml)
6. ⏭️ Implementar entidades y repositories
7. ⏭️ Crear DTOs y mappers (MapStruct)
8. ⏭️ Implementar services (casos de uso)
9. ⏭️ Crear controllers (REST APIs)
10. ⏭️ Configurar RabbitMQ (eventos entre microservicios)
11. ⏭️ Implementar seguridad con Keycloak
12. ⏭️ Crear migraciones Flyway (PostgreSQL)
13. ⏭️ Crear índices MongoDB
14. ⏭️ Implementar generadores de códigos legibles
15. ⏭️ Configurar Redis para caché

---

## ✨ RESUMEN DE MEJORAS APLICADAS

### 🆔 UUID en Todos los IDs

- **PostgreSQL:** Tipo `UUID` nativo con `gen_random_uuid()`
- **MongoDB:** Tipo `String` con `UUID.randomUUID().toString()`
- **Java:** Tipo `UUID` (java.util.UUID)
- **Ventajas:** Escalabilidad, seguridad, distribución global sin colisiones

### 🏷️ Nomenclatura Snake_Case

- **Tablas PostgreSQL:** `water_boxes`, `maintenance_logs`, `payment_details`
- **Columnas PostgreSQL:** `user_id`, `water_box_id`, `created_at`
- **Collections MongoDB:** `zone_fare_history`, `quality_tests`, `complaint_responses`
- **Campos MongoDB:** `zoneId`, `waterBoxId`, `createdAt` (camelCase)
- **Ventajas:** Estándar SQL, legibilidad, compatibilidad con herramientas

### 📝 Códigos Mejorados

- **Usuarios:** `USER-2026-00001` (sin dependencia de organización)
- **Mantenimiento:** `MNT-2026-00001` (más corto, fácil de comunicar)
- **Ventajas:** Simple, escalable, fácil de leer y comunicar por teléfono

### 🔗 Referencias UUID Consistentes

- Todas las referencias entre microservicios usan UUID
- PostgreSQL: `columnDefinition = "uuid"` para FKs
- MongoDB: Strings con UUID (36 caracteres)
- Trazabilidad completa garantizada

### 📐 Anotaciones JPA Completas

- Nombres de columnas explícitos (`@Column(name = "...")`)
- Precisión en BigDecimal (`precision = 10, scale = 2`)
- Índices definidos en entidades (`@Index`)
- Enums con `@Enumerated(EnumType.STRING)`
- `updatable = false` para IDs y fechas de creación

---

**Generado:** 20 Enero 2026
**Autor:** GitHub Copilot
**Versión:** 2.0 - Master Document con UUID y Nomenclatura Estandarizada

# 🗄️ BASE DE DATOS POR MICROSERVICIO

## Fecha: 20 Enero 2026

---

## 📊 RESUMEN EJECUTIVO

| Microservicio | Base de Datos | Motivo Principal |
|--------------|---------------|------------------|
| **vg-ms-authentication** | ❌ NINGUNA | Keycloak maneja TODO |
| **vg-ms-users** | 🟦 PostgreSQL | Datos estructurados + relaciones |
| **vg-ms-organizations** | 🟩 MongoDB | Estructura flexible + consultas simples |
| **vg-ms-infrastructure** | 🟦 PostgreSQL | Transacciones críticas + mantenimientos |
| **vg-ms-finance** | 🟦 PostgreSQL | ACID + balance + reportes financieros |
| **vg-ms-distribution** | 🟩 MongoDB | Programación flexible + lecturas rápidas |
| **vg-ms-inventory-purchases** | 🟦 PostgreSQL | Transacciones + kardex + reportes |
| **vg-ms-water-quality** | 🟩 MongoDB | Documentos independientes + series temporales |
| **vg-ms-claims-incidents** | 🟩 MongoDB | Estructura variable + búsqueda texto |
| **vg-ms-notification** | 🟩 MongoDB | Logs + alta escritura + TTL |
| **vg-ms-finance** | 🟦 PostgreSQL | Balance, ingresos, egresos, salarios |
| **vg-ms-gateway** | ❌ NINGUNA | Solo enrutamiento |

**PostgreSQL: 5** | **MongoDB: 5** | **Sin BD: 2**

---

## 1️⃣ VG-MS-AUTHENTICATION

### Base de Datos: ❌ NINGUNA

**¿Por qué?**

- Keycloak maneja toda la autenticación
- NO necesita persistencia propia
- Solo valida tokens JWT

**Collections/Entidades Java: NINGUNA**

---

## 2️⃣ VG-MS-USERS

### Base de Datos: 🟦 PostgreSQL

**¿Por qué PostgreSQL?**

- ✅ Datos altamente estructurados (usuarios tienen campos fijos)
- ✅ Relaciones con water_boxes, zones, organizations
- ✅ Consultas JOIN complejas (usuario + caja + zona)
- ✅ Integridad referencial crítica
- ✅ Reportes de usuarios por zona/organización

**Entidades Java (3):**

```java
// 1. UserEntity.java
@Entity
@Table(name = "users_demo", schema = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String userCode;  // JASS-RINC-00001

    private String keycloakId;  // UUID de Keycloak
    private String firstName;
    private String lastName;
    private String dni;
    private String phone;
    private String email;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Long organizationId;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Long zoneId;

    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// 2. UserCodeCounterEntity.java
@Entity
@Table(name = "user_code_counters", schema = "users")
public class UserCodeCounterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String organizationCode;  // RINC, BELLA, ROMA

    private Integer lastSequence;  // Último número usado

    @Version
    private Long version;  // Para concurrencia optimista
}

// 3. UserAuditEntity.java
@Entity
@Table(name = "user_audits", schema = "users")
public class UserAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String action;  // CREATE, UPDATE, DELETE, STATUS_CHANGE
    private String performedBy;
    private String changes;  // JSON con cambios
    private LocalDateTime performedAt;
}
```

---

## 3️⃣ VG-MS-ORGANIZATIONS

### Base de Datos: 🟩 MongoDB

**¿Por qué MongoDB?**

- ✅ Estructura flexible (cada JASS tiene parámetros diferentes)
- ✅ Documentos independientes (pocas relaciones)
- ✅ Consultas simples por ID o código
- ✅ Desnormalización de zonas/calles (evita JOINs)
- ✅ Fácil agregar campos custom por organización

**Collections MongoDB (4):**

```java
// 1. OrganizationDocument.java
@Document(collection = "organizations")
public class OrganizationDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code;  // JASS-RINC

    private String name;  // "JASS Rinconada-Bellavista"
    private String address;

    // Configuración específica de cada JASS (SMS provider, etc.)
    private Map<String, Object> customParameters;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;  // ACTIVE, INACTIVE
}

// 2. ZoneDocument.java
@Document(collection = "zones")
public class ZoneDocument {
    @Id
    private String id;

    @Indexed
    private String organizationId;  // Referencia a organizations

    @Indexed(unique = true)
    private String code;  // RINC, BELLA, ROMA

    private String name;  // "Rinconada"

    private Integer totalWaterBoxes;
    private Integer activeWaterBoxes;

    // Coordenadas geográficas (opcional)
    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt;
    private String status;
}

// 2B. ZoneFareHistoryDocument.java
@Document(collection = "zone_fare_history")
public class ZoneFareHistoryDocument {
    @Id
    private String id;

    @Indexed
    private String zoneId;  // Referencia a zones

    private BigDecimal monthlyFee;  // S/8, S/10, S/20

    @Indexed
    private LocalDate validFrom;  // Fecha desde cuando aplica esta tarifa

    private LocalDate validUntil;  // Fecha hasta cuando aplicó (null = vigente)

    @Indexed
    private String status;  // ACTIVE, INACTIVE

    private LocalDateTime createdAt;
    private String createdBy;  // Keycloak ID del ADMIN que hizo el cambio
    private String reason;  // "Ajuste por inflación", "Acuerdo asamblea"
}

// 3. StreetDocument.java
@Document(collection = "streets")
public class StreetDocument {
    @Id
    private String id;

    @Indexed
    private String zoneId;  // Referencia a zones

    private String name;  // "Jr. Los Pinos"
    private String reference;  // "Cerca al parque principal"

    private LocalDateTime createdAt;
}

// 4. ParameterDocument.java
@Document(collection = "parameters")
public class ParameterDocument {
    @Id
    private String id;

    @Indexed
    private String organizationId;  // Referencia a organizations

    @Indexed
    private String key;  // RECONNECTION_FEE, SMS_PROVIDER, etc.

    private String value;  // "50.00", "TWILIO", etc.
    private String type;  // NUMERIC, STRING, BOOLEAN
    private String description;

    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

## 4️⃣ VG-MS-INFRASTRUCTURE

### Base de Datos: 🟦 PostgreSQL

**¿Por qué PostgreSQL?**

- ✅ Transacciones CRÍTICAS (asignación/transferencia de cajas)
- ✅ Relaciones fuertes (caja → usuario → zona)
- ✅ Historial de asignaciones (trazabilidad total)
- ✅ Integridad referencial obligatoria
- ✅ Consultas complejas para reportes

**Entidades Java (5):**

```java
// 1. WaterBoxEntity.java
@Entity
@Table(name = "water_boxes", schema = "infrastructure")
public class WaterBoxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 15)
    private String boxCode;  // WB-RINC-001

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private Long zoneId;

    @ManyToOne
    @JoinColumn(name = "street_id")
    private Long streetId;

    private String address;
    private String reference;

    @Enumerated(EnumType.STRING)
    private WaterBoxStatus status;  // ACTIVE, INACTIVE, CUT, MAINTENANCE

    private LocalDateTime installedAt;
    private LocalDateTime lastMaintenanceAt;
    private LocalDateTime createdAt;
}

// 2. WaterBoxAssignmentEntity.java
@Entity
@Table(name = "water_box_assignments", schema = "infrastructure")
public class WaterBoxAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "water_box_id", nullable = false)
    private Long waterBoxId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    private LocalDateTime assignedAt;
    private String assignedBy;  // Keycloak ID del ADMIN

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;  // ACTIVE, TRANSFERRED, CANCELLED

    private LocalDateTime endedAt;
    private String notes;
}

// 3. WaterBoxTransferEntity.java
@Entity
@Table(name = "water_box_transfers", schema = "infrastructure")
public class WaterBoxTransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "water_box_id", nullable = false)
    private Long waterBoxId;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private Long toUserId;

    private LocalDateTime transferredAt;
    private String authorizedBy;  // Keycloak ID del ADMIN
    private String reason;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;  // PENDING, APPROVED, REJECTED
}
```

---

## 5️⃣ VG-MS-PAYMENTS-BILLING

### Base de Datos: 🟦 PostgreSQL

**¿Por qué PostgreSQL?**

- ✅ Transacciones ACID obligatorias (dinero de por medio)
- ✅ Reportes financieros complejos (recaudación, morosidad)
- ✅ Relaciones fuertes (recibo → pago → usuario → zona)
- ✅ Consultas JOIN para dashboard financiero
- ✅ Integridad referencial crítica

**Entidades Java (3):**

```java
// 1. ReceiptEntity.java
@Entity
@Table(name = "receipts", schema = "payments")
public class ReceiptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String receiptCode;  // REC-2026-000001

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "water_box_id", nullable = false)
    private Long waterBoxId;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private Long zoneId;

    // Montos (SOLO 2 conceptos)
    @Column(nullable = false)
    private BigDecimal monthlyFee;  // S/8, S/10, S/20

    @Column(nullable = false)
    private BigDecimal reconnectionFee;  // S/50 o 0

    @Column(nullable = false)
    private BigDecimal totalAmount;  // monthly_fee + reconnection_fee

    // Meses adeudados (formato JSON)
    @Column(columnDefinition = "jsonb")
    private String monthsOwed;  // ["2026-01", "2026-02"]

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;  // PENDING, PAID, OVERDUE

    private LocalDate dueDate;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
}

// 2. PaymentEntity.java
@Entity
@Table(name = "payments", schema = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String paymentCode;  // PAY-2026-000001

    @ManyToOne
    @JoinColumn(name = "receipt_id", nullable = false)
    private Long receiptId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;  // CASH, YAPE (solo estos dos)

    private String yapeReference;  // Número de operación Yape
    private String yapePhone;  // Teléfono desde donde se hizo Yape

    private LocalDateTime paidAt;
    private String receivedBy;  // Keycloak ID del OPERATOR
}

// 4. ExpenseEntity.java
@Entity
@Table(name = "expenses", schema = "finance")
public class ExpenseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String expenseCode;  // EXP-2026-000001

    @Enumerated(EnumType.STRING)
    private ExpenseType type;  // PURCHASE, SALARY, MAINTENANCE_MATERIALS, UTILITY_BILLS, ADMINISTRATIVE

    @Column(nullable = false)
    private BigDecimal amount;

    @Indexed
    private LocalDate expenseDate;

    private String sourceType;  // "PURCHASE", "SALARY", "MAINTENANCE"
    private Long sourceId;  // ID de la compra, mantenimiento, etc.

    private String description;

    // Para salarios: referencia al usuario (está en vg-ms-users)
    private Long userId;  // Empleado/operario que recibió el pago

    // Para compras: referencia al proveedor (está en vg-ms-inventory)
    private Long supplierId;
    private Long purchaseId;

    private String authorizedBy;  // Keycloak ID del ADMIN

    private LocalDateTime createdAt;
}

// 3. PaymentDetailEntity.java
@Entity
@Table(name = "payment_details", schema = "payments")
public class PaymentDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentDetailType type;  // MONTHLY_FEE, RECONNECTION_FEE

    private String description;  // "Pago mensual Enero 2026"

    @Column(nullable = false)
    private BigDecimal amount;
}
```

---

## 6️⃣ VG-MS-DISTRIBUTION

### Base de Datos: 🟩 MongoDB

**¿Por qué MongoDB?**

- ✅ Estructura flexible (programas varían por zona/temporada)
- ✅ Documentos independientes (lectura > escritura)
- ✅ Sin transacciones complejas
- ✅ Búsquedas simples por fecha/zona
- ✅ Fácil cambiar estructura sin migrations

**Collections MongoDB (3):**

```java
// 1. DistributionProgramDocument.java
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

// 2. DistributionRouteDocument.java
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

// 3. DistributionScheduleDocument.java
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

**¿Por qué PostgreSQL?**

- ✅ Transacciones ACID (compras, kardex)
- ✅ Relaciones complejas (producto → categoría → movimiento → compra)
- ✅ Cálculos de stock (SUM, AVG)
- ✅ Reportes financieros complejos
- ✅ Integridad referencial crítica

**Entidades Java (6):**

```java
// 1. ProductEntity.java
@Entity
@Table(name = "products", schema = "inventory")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String productCode;  // PROD-00001

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Long categoryId;

    private String unit;  // UNIDAD, METRO, KILO, etc.

    @Column(nullable = false)
    private Integer currentStock;

    private Integer minStock;
    private Integer maxStock;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private LocalDateTime createdAt;
    private String status;  // ACTIVE, INACTIVE
}

// 2. ProductCategoryEntity.java
@Entity
@Table(name = "product_categories", schema = "inventory")
public class ProductCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;  // TUBERIAS, VALVULAS, HERRAMIENTAS

    private String name;
    private String description;

    private LocalDateTime createdAt;
}

// 3. InventoryMovementEntity.java
@Entity
@Table(name = "inventory_movements", schema = "inventory")
public class InventoryMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    private MovementType type;  // ENTRY, EXIT, ADJUSTMENT

    @Column(nullable = false)
    private Integer quantity;

    private Integer previousStock;
    private Integer currentStock;

    private String reason;  // "Compra #12", "Uso en mantenimiento WB-RINC-001"
    private String referenceId;  // ID de la compra o mantenimiento

    private LocalDateTime performedAt;
    private String performedBy;
}

// 4. PurchaseEntity.java
@Entity
@Table(name = "purchases", schema = "inventory")
public class PurchaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String purchaseCode;  // PUR-2026-00001

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;  // DRAFT, PENDING, RECEIVED, CANCELLED

    private LocalDate purchaseDate;
    private LocalDate receivedDate;

    private String authorizedBy;
    private String notes;

    private LocalDateTime createdAt;
}

// 5. PurchaseDetailEntity.java
@Entity
@Table(name = "purchase_details", schema = "inventory")
public class PurchaseDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_id", nullable = false)
    private Long purchaseId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal;
}

// 6. SupplierEntity.java
@Entity
@Table(name = "suppliers", schema = "inventory")
public class SupplierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ruc;

    private String businessName;
    private String contactName;
    private String phone;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private SupplierStatus status;  // ACTIVE, INACTIVE, BLOCKED

    private LocalDateTime createdAt;
}
```

---

## 8️⃣ VG-MS-WATER-QUALITY

### Base de Datos: 🟩 MongoDB

**¿Por qué MongoDB?**

- ✅ Datos de series temporales (muchos registros diarios)
- ✅ Documentos independientes (sin relaciones complejas)
- ✅ Alta frecuencia de escritura
- ✅ Estructura flexible (parámetros varían)
- ✅ TTL para auto-borrado de datos antiguos

**Collections MongoDB (4):**

```java
// 1. TestingPointDocument.java
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

// 2. QualityTestDocument.java
@Document(collection = "quality_tests")
public class QualityTestDocument {
    @Id
    private String id;

    @Indexed
    private String testingPointId;

    @Indexed
    private LocalDateTime testedAt;

    private String performedBy;  // Keycloak ID

    // Parámetros medidos (flexibles según tipo de test)
    private Map<String, TestParameter> parameters;

    @Indexed
    private String result;  // COMPLIANT, NON_COMPLIANT, REQUIRES_REVIEW

    private String observations;
    private List<String> attachments;  // URLs a S3

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

// 3. DailyRecordDocument.java
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

    // Estadísticas rápidas
    private Map<String, Double> averageParameters;

    private String recordedBy;
    private LocalDateTime createdAt;

    // TTL: auto-borrar después de 2 años
    @Indexed(expireAfterSeconds = 63072000)  // 2 años
    private LocalDateTime expiresAt;
}

// 4. UserWaterQualityDocument.java
@Document(collection = "water_quality_users")
public class UserWaterQualityDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String keycloakId;

    private String fullName;

    @Indexed
    private String role;  // TECHNICIAN, SUPERVISOR, ADMIN

    private List<String> authorizedZones;  // Zonas que puede inspeccionar

    @Indexed
    private String status;  // ACTIVE, INACTIVE

    private LocalDateTime createdAt;
}
```

---

## 9️⃣ VG-MS-CLAIMS-INCIDENTS

### Base de Datos: 🟩 MongoDB

**¿Por qué MongoDB?**

- ✅ Estructura variable (cada reclamo/incidente es diferente)
- ✅ Búsqueda de texto completo (descripción, respuestas)
- ✅ Documentos independientes
- ✅ Fácil agregar campos custom
- ✅ Sin transacciones complejas

**Collections MongoDB (6):**

```java
// 1. ComplaintDocument.java
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

    private List<String> attachments;  // URLs a S3

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;

    private String assignedTo;  // Keycloak ID del OPERATOR
}

// 2. ComplaintCategoryDocument.java
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

// 3. ComplaintResponseDocument.java
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

// 4. IncidentDocument.java
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

// 5. IncidentResolutionDocument.java
@Document(collection = "incident_resolutions")
public class IncidentResolutionDocument {
    @Id
    private String id;

    @Indexed
    private String incidentId;

    @TextIndexed
    private String resolution;

    private List<String> actionsToken;  // ["Cambio de válvula", "Limpieza de tubería"]

    private List<String> materialsUsed;  // IDs de productos del inventario

    private LocalDateTime resolvedAt;
    private String resolvedBy;

    private List<String> attachments;
}

// 6. IncidentTypeDocument.java
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

**¿Por qué MongoDB?**

- ✅ Alta frecuencia de escritura (muchas notificaciones)
- ✅ Logs de envíos (lectura ocasional)
- ✅ TTL para auto-borrado (evitar crecimiento infinito)
- ✅ Estructura flexible (SMS, Email, WhatsApp tienen campos diferentes)
- ✅ Sin relaciones complejas

**Collections MongoDB (3):**

```java
// 1. NotificationDocument.java
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
    private String type;  // RECEIPT, PAYMENT_CONFIRMATION, INCIDENT_ALERT, etc.

    private String subject;  // Solo para EMAIL
    private String message;

    @Indexed
    private String status;  // PENDING, SENT, FAILED, DELIVERED

    private String provider;  // TWILIO, AWS_SES, WHATSAPP_API
    private String providerId;  // ID del proveedor
    private String errorMessage;

    private Integer retryCount;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @Indexed
    private LocalDateTime createdAt;

    // TTL: auto-borrar después de 6 meses
    @Indexed(expireAfterSeconds = 15552000)  // 6 meses
    private LocalDateTime expiresAt;
}

// 2. NotificationTemplateDocument.java
@Document(collection = "notification_templates")
public class NotificationTemplateDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code;  // RECEIPT_SMS, PAYMENT_CONFIRMATION_EMAIL

    @Indexed
    private String channel;  // SMS, EMAIL, WHATSAPP

    private String subject;  // Solo para EMAIL
    private String template;  // "Hola {userName}, tu recibo {receiptCode} por S/{amount}..."

    private List<String> variables;  // ["userName", "receiptCode", "amount"]

    @Indexed
    private String status;  // ACTIVE, INACTIVE

    private LocalDateTime updatedAt;
    private String updatedBy;
}

// 3. NotificationPreferenceDocument.java
@Document(collection = "notification_preferences")
public class NotificationPreferenceDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    // Preferencias por tipo de notificación
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

**¿Por qué?**

- Solo enruta peticiones
- Sin lógica de negocio
- Sin persistencia necesaria

**Collections/Entidades Java: NINGUNA**

---

## 📊 RESUMEN TÉCNICO

### PostgreSQL (4 microservicios)

**Cuándo usar PostgreSQL:**

- ✅ Transacciones ACID obligatorias
- ✅ Relaciones complejas (múltiples JOINs)
- ✅ Reportes financieros/complejos
- ✅ Integridad referencial crítica
- ✅ Datos altamente estructurados

**Microservicios:**

1. **vg-ms-users** - 3 entidades (users, code_counters, audits)
2. **vg-ms-infrastructure** - 3 entidades (water_boxes, assignments, transfers)
3. **vg-ms-payments-billing** - 3 entidades (receipts, payments, payment_details)
4. **vg-ms-inventory-purchases** - 6 entidades (products, categories, movements, purchases, details, suppliers)

**Total: 15 entidades PostgreSQL**

---

### MongoDB (5 microservicios)

**Cuándo usar MongoDB:**

- ✅ Estructura flexible/variable
- ✅ Documentos independientes
- ✅ Alta frecuencia escritura
- ✅ Series temporales
- ✅ Búsqueda de texto completo
- ✅ TTL para auto-borrado

**Microservicios:**

1. **vg-ms-organizations** - 4 collections (organizations, zones, streets, parameters)
2. **vg-ms-distribution** - 3 collections (programs, routes, schedules)
3. **vg-ms-water-quality** - 4 collections (testing_points, tests, daily_records, users)
4. **vg-ms-claims-incidents** - 6 collections (complaints, categories, responses, incidents, resolutions, types)
5. **vg-ms-notification** - 3 collections (notifications, templates, preferences)

**Total: 20 collections MongoDB**

---

## 🎯 RECOMENDACIONES FINALES

### 1. **Redis (Cache + Counters)**

Usar en TODOS los microservicios para:

- ✅ Cache de consultas frecuentes
- ✅ Generación de códigos secuenciales (thread-safe)
- ✅ Rate limiting
- ✅ Sesiones distribuidas

### 2. **RabbitMQ (Eventos)**

Comunicación asíncrona entre microservicios:

- `user.created` → Notification envía bienvenida
- `payment.received` → Infrastructure verifica cortes pendientes
- `incident.created` → Notification alerta a usuarios afectados

### 3. **Keycloak (Auth)**

NO persistir credenciales en ninguna base de datos:

- ✅ Autenticación centralizada
- ✅ Tokens JWT
- ✅ Roles y permisos

### 4. **Backup Strategy**

- PostgreSQL: pg_dump diario + WAL archiving
- MongoDB: mongodump diario + oplog

---

## 📝 PRÓXIMOS PASOS

1. ✅ Definir base de datos por microservicio (COMPLETADO)
2. ⏭️ Generar estructura de carpetas completa
3. ⏭️ Crear templates de código base
4. ⏭️ Implementar CodeGeneratorService para códigos legibles
5. ⏭️ Crear schemas de bases de datos (Flyway + MongoDB)
6. ⏭️ Implementar SecurityConfig + JWT validation
7. ⏭️ Configurar RabbitMQ exchanges y queues
8. ⏭️ Implementar primeros endpoints (Authentication + Users)

---

**Generado:** 20 Enero 2026
**Autor:** GitHub Copilot
**Versión:** 1.0

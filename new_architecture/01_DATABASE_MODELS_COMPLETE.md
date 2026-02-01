# 01 - MODELOS DE BASE DE DATOS COMPLETOS

Este documento contiene **TODAS** las clases Entity (PostgreSQL) y Document (MongoDB) de los 11 microservicios.

---

## 📊 RESUMEN POR MICROSERVICIO

| Microservicio              | Base de Datos | Entities/Documents | Total |
|----------------------------|---------------|-------------------|-------|
| vg-ms-users                | PostgreSQL    | 1 entity          | 1     |
| vg-ms-authentication       | N/A (Keycloak)| 0                 | 0     |
| vg-ms-organizations        | MongoDB       | 5 documents       | 5     |
| vg-ms-commercial-operations| PostgreSQL    | 8 entities        | 8     |
| vg-ms-water-quality        | MongoDB       | 2 documents       | 2     |
| vg-ms-distribution         | MongoDB       | 3 documents       | 3     |
| vg-ms-inventory-purchases  | PostgreSQL    | 6 entities        | 6     |
| vg-ms-claims-incidents     | MongoDB       | 6 documents       | 6     |
| vg-ms-infrastructure       | PostgreSQL    | 3 entities        | 3     |
| vg-ms-notification         | N/A           | 0                 | 0     |
| vg-ms-gateway              | N/A           | 0                 | 0     |
| **TOTAL**                  |               |                   | **34**|

---

## 1️⃣ vg-ms-users (PostgreSQL)

### UserEntity.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.time.LocalDateTime;

@Table("users")
public class UserEntity {
    
    @Id
    private String id;                          // UUID
    
    @Column("organization_id")
    private String organizationId;              // FK a organizations (MongoDB)
    
    @Column("record_status")
    private String recordStatus;                // ACTIVE, INACTIVE
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos del usuario
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    @Column("document_type")
    private String documentType;                // DNI, RUC, CE
    
    @Column("document_number")
    private String documentNumber;              // UNIQUE
    
    @Column("email")
    private String email;                       // OPCIONAL (nullable)
    
    @Column("phone")
    private String phone;                       // OPCIONAL (nullable)
    
    @Column("address")
    private String address;
    
    @Column("zone_id")
    private String zoneId;                      // FK a zones (MongoDB)
    
    @Column("street_id")
    private String streetId;                    // FK a streets (MongoDB)
    
    @Column("role")
    private String role;                        // SUPER_ADMIN, ADMIN, CLIENT
    
    // Constructors, Getters, Setters
}
```

### Enums relacionados:

```java
// domain/valueobjects/Role.java
public enum Role {
    SUPER_ADMIN,    // Acceso total
    ADMIN,          // Administrador de organización
    CLIENT          // Cliente/Usuario final
}

// domain/valueobjects/DocumentType.java
public enum DocumentType {
    DNI,            // Documento Nacional de Identidad
    RUC,            // Registro Único de Contribuyentes
    CE              // Carné de Extranjería
}

// shared/valueobjects/RecordStatus.java
public enum RecordStatus {
    ACTIVE,         // Registro activo
    INACTIVE        // Registro inactivo (soft delete)
}
```

---

## 2️⃣ vg-ms-authentication (Keycloak)

**NO tiene persistence**. Todo se maneja en Keycloak.

---

## 3️⃣ vg-ms-organizations (MongoDB)

### OrganizationDocument.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "organizations")
public class OrganizationDocument {
    
    @Id
    private String id;
    
    @Field("organization_name")
    private String organizationName;
    
    @Field("district")
    private String district;
    
    @Field("province")
    private String province;
    
    @Field("department")
    private String department;
    
    @Field("address")
    private String address;
    
    @Field("phone")
    private String phone;
    
    @Field("email")
    private String email;
    
    // Auditoría
    @Field("record_status")
    private String recordStatus;                // ACTIVE, INACTIVE
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Constructors, Getters, Setters
}
```

### ZoneDocument.java

```java
@Document(collection = "zones")
public class ZoneDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;              // FK a organizations
    
    @Field("zone_name")
    private String zoneName;                    // Nombre de la zona
    
    @Field("description")
    private String description;
    
    // Auditoría
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
}
```

### StreetDocument.java

```java
@Document(collection = "streets")
public class StreetDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("zone_id")
    private String zoneId;                      // FK a zones
    
    @Field("street_type")
    private String streetType;                  // JR, AV, CALLE, PASAJE
    
    @Field("street_name")
    private String streetName;
    
    // Auditoría
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
}
```

### FareDocument.java

```java
@Document(collection = "fares")
public class FareDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("fare_type")
    private String fareType;                    // MONTHLY_FEE, INSTALLATION_FEE, etc.
    
    @Field("amount")
    private Double amount;                      // Monto de la tarifa
    
    @Field("description")
    private String description;
    
    @Field("valid_from")
    private LocalDateTime validFrom;            // Vigencia desde
    
    @Field("valid_to")
    private LocalDateTime validTo;              // Vigencia hasta (nullable)
    
    // Auditoría
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
}
```

### ParameterDocument.java

```java
@Document(collection = "parameters")
public class ParameterDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("parameter_type")
    private String parameterType;               // BILLING_DAY, GRACE_PERIOD, etc.
    
    @Field("parameter_value")
    private String parameterValue;              // Valor del parámetro
    
    @Field("description")
    private String description;
    
    // Auditoría
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
}
```

### Enums:

```java
public enum FareType {
    MONTHLY_FEE,        // Tarifa mensual
    INSTALLATION_FEE,   // Instalación nueva
    RECONNECTION_FEE,   // Reconexión
    LATE_FEE,           // Mora
    TRANSFER_FEE        // Transferencia
}

public enum StreetType {
    JR,             // Jirón
    AV,             // Avenida
    CALLE,          // Calle
    PASAJE          // Pasaje
}

public enum ParameterType {
    BILLING_DAY,        // Día de facturación
    GRACE_PERIOD,       // Días de gracia
    LATE_INTEREST_RATE, // Tasa de interés por mora
    MAX_DEBT_AMOUNT     // Monto máximo de deuda antes de corte
}
```

---

## 4️⃣ vg-ms-commercial-operations (PostgreSQL)

**Responsabilidad**: Facturación (Recibos), Pagos, Deudas, Cortes de Servicio, Caja Chica.

### PaymentEntity.java

```java
@Table("payments")
public class PaymentEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("user_id")
    private String userId;                      // FK a users
    
    @Column("payment_date")
    private LocalDateTime paymentDate;
    
    @Column("total_amount")
    private Double totalAmount;
    
    @Column("payment_method")
    private String paymentMethod;               // CASH, BANK_TRANSFER, CARD, YAPE, PLIN
    
    @Column("payment_status")
    private String paymentStatus;               // PENDING, COMPLETED, CANCELLED, FAILED
    
    @Column("receipt_number")
    private String receiptNumber;               // Número de recibo/boleta
    
    @Column("notes")
    private String notes;
}
```

### PaymentDetailEntity.java

```java
@Table("payment_details")
public class PaymentDetailEntity {
    
    @Id
    private String id;
    
    @Column("payment_id")
    private String paymentId;                   // FK a payments
    
    @Column("payment_type")
    private String paymentType;                 // MONTHLY_FEE, INSTALLATION_FEE, etc.
    
    @Column("description")
    private String description;
    
    @Column("amount")
    private Double amount;
    
    @Column("period_month")
    private Integer periodMonth;                // Mes (1-12) si aplica
    
    @Column("period_year")
    private Integer periodYear;                 // Año
    
    @Column("created_at")
    private LocalDateTime createdAt;
}
```

### DebtEntity.java

```java
@Table("debts")
public class DebtEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("user_id")
    private String userId;                      // FK a users
    
    @Column("period_month")
    private Integer periodMonth;
    
    @Column("period_year")
    private Integer periodYear;
    
    @Column("original_amount")
    private Double originalAmount;
    
    @Column("pending_amount")
    private Double pendingAmount;
    
    @Column("late_fee")
    private Double lateFee;                     // Mora acumulada
    
    @Column("debt_status")
    private String debtStatus;                  // PENDING, PARTIAL, PAID, CANCELLED
    
    @Column("due_date")
    private LocalDateTime dueDate;
}
```

### ReceiptEntity.java

```java
@Table("receipts")
public class ReceiptEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("receipt_number")
    private String receiptNumber;               // N° ###### - YYYY (auto-generado)
    
    @Column("user_id")
    private String userId;                      // FK a users
    
    @Column("period_month")
    private Integer periodMonth;                // 1-12
    
    @Column("period_year")
    private Integer periodYear;
    
    @Column("issue_date")
    private LocalDateTime issueDate;
    
    @Column("due_date")
    private LocalDateTime dueDate;
    
    @Column("total_amount")
    private Double totalAmount;
    
    @Column("paid_amount")
    private Double paidAmount;                  // Pagado parcial/total
    
    @Column("pending_amount")
    private Double pendingAmount;
    
    @Column("receipt_status")
    private String receiptStatus;               // PENDING, PARTIAL, PAID, OVERDUE, CANCELLED
    
    @Column("notes")
    private String notes;
}
```

### ReceiptDetailEntity.java

```java
@Table("receipt_details")
public class ReceiptDetailEntity {
    
    @Id
    private String id;
    
    @Column("receipt_id")
    private String receiptId;                   // FK a receipts
    
    @Column("concept_type")
    private String conceptType;                 // MONTHLY_FEE, LATE_FEE, ASSEMBLY_FINE, WORK_FINE, RECONNECTION_FEE
    
    @Column("description")
    private String description;
    
    @Column("amount")
    private Double amount;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}
```

### ServiceCutEntity.java

```java
@Table("service_cuts")
public class ServiceCutEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("user_id")
    private String userId;                      // FK a users
    
    @Column("water_box_id")
    private String waterBoxId;                  // FK a water_boxes (vg-ms-infrastructure)
    
    @Column("cut_date")
    private LocalDateTime cutDate;
    
    @Column("cut_reason")
    private String cutReason;                   // DEBT, VIOLATION, MAINTENANCE, REQUEST
    
    @Column("debt_amount")
    private Double debtAmount;                  // Monto adeudado al momento del corte
    
    @Column("reconnection_date")
    private LocalDateTime reconnectionDate;     // nullable
    
    @Column("reconnection_fee_paid")
    private Boolean reconnectionFeePaid;
    
    @Column("cut_status")
    private String cutStatus;                   // ACTIVE, RECONNECTED, CANCELLED
    
    @Column("notes")
    private String notes;
}
```

### PettyCashEntity.java

```java
@Table("petty_cash")
public class PettyCashEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("responsible_user_id")
    private String responsibleUserId;           // FK a users (Quien maneja la caja)
    
    @Column("current_balance")
    private Double currentBalance;
    
    @Column("max_amount_limit")
    private Double maxAmountLimit;
    
    @Column("petty_cash_status")
    private String pettyCashStatus;             // ACTIVE, CLOSED, AUDITED
}
```

### PettyCashMovementEntity.java

```java
@Table("petty_cash_movements")
public class PettyCashMovementEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    // Campos específicos
    @Column("petty_cash_id")
    private String pettyCashId;                 // FK a petty_cash
    
    @Column("movement_date")
    private LocalDateTime movementDate;
    
    @Column("movement_type")
    private String movementType;                // IN, OUT
    
    @Column("amount")
    private Double amount;
    
    @Column("category")
    private String category;                    // SUPPLIES, TRANSPORT, FOOD, EMERGENCY, OTHER
    
    @Column("description")
    private String description;
    
    @Column("receipt_number")
    private String receiptNumber;               // Número de comprobante
    
    @Column("previous_balance")
    private Double previousBalance;
    
    @Column("new_balance")
    private Double newBalance;
}
```

### Enums:

```java
// ReceiptStatus
public enum ReceiptStatus {
    PENDING,        // Pendiente de pago
    PARTIAL,        // Pago parcial
    PAID,           // Pagado totalmente
    OVERDUE,        // Vencido (mora)
    CANCELLED       // Cancelado
}

// ConceptType (para Receipt Details)
public enum ConceptType {
    MONTHLY_FEE,        // Cuota mensual
    LATE_FEE,           // Mora
    ASSEMBLY_FINE,      // Multa por asamblea
    WORK_FINE,          // Multa por faena
    RECONNECTION_FEE,   // Reconexión
    TRANSFER_FEE,       // Transferencia
    OTHER               // Otro
}

// CutReason
public enum CutReason {
    DEBT,           // Deuda
    VIOLATION,      // Violación de normas
    MAINTENANCE,    // Mantenimiento
    REQUEST         // Solicitud del usuario
}

// CutStatus
public enum CutStatus {
    ACTIVE,         // Cortado actualmente
    RECONNECTED,    // Reconectado
    CANCELLED       // Cancelado
}

// PettyCashStatus
public enum PettyCashStatus {
    ACTIVE,         // Activo
    CLOSED,         // Cerrado
    AUDITED         // Auditado
}

// PettyCashMovementType
public enum PettyCashMovementType {
    IN,             // Entrada (reposición)
    OUT             // Salida (gasto)
}

// PettyCashCategory
public enum PettyCashCategory {
    SUPPLIES,       // Útiles/insumos
    TRANSPORT,      // Transporte
    FOOD,           // Alimentación (reuniones)
    EMERGENCY,      // Emergencia
    OTHER           // Otro
}

// ═══════════════════════════════════════════════════════════════
// ENUMS ORIGINALES (Payments, Debts)
// ═══════════════════════════════════════════════════════════════

public enum PaymentType {
    MONTHLY_FEE,        // Cuota mensual
    INSTALLATION_FEE,   // Instalación
    RECONNECTION_FEE,   // Reconexión
    LATE_FEE,           // Mora
    TRANSFER_FEE,       // Transferencia
    OTHER               // Otro
}

public enum PaymentMethod {
    CASH,               // Efectivo
    BANK_TRANSFER,      // Transferencia bancaria
    CARD,               // Tarjeta
    YAPE,               // Yape
    PLIN                // Plin
}

public enum PaymentStatus {
    PENDING,            // Pendiente
    COMPLETED,          // Completado
    CANCELLED,          // Cancelado
    FAILED              // Fallido
}

public enum DebtStatus {
    PENDING,            // Pendiente total
    PARTIAL,            // Pago parcial
    PAID,               // Pagado total
    CANCELLED           // Cancelado
}
```

---

## 5️⃣ vg-ms-water-quality (MongoDB)

### TestingPointDocument.java

```java
@Document(collection = "testing_points")
public class TestingPointDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("point_name")
    private String pointName;
    
    @Field("point_type")
    private String pointType;                   // RESERVOIR, TAP, WELL, SOURCE
    
    @Field("location")
    private String location;                    // Descripción de ubicación
    
    @Field("latitude")
    private Double latitude;
    
    @Field("longitude")
    private Double longitude;
    
    @Field("zone_id")
    private String zoneId;                      // FK a zones
}
```

### QualityTestDocument.java

```java
@Document(collection = "quality_tests")
public class QualityTestDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("testing_point_id")
    private String testingPointId;              // FK a testing_points
    
    @Field("test_date")
    private LocalDateTime testDate;
    
    @Field("test_type")
    private String testType;                    // CHLORINE, PH, TURBIDITY, BACTERIOLOGICAL, CHEMICAL
    
    @Field("chlorine_level")
    private Double chlorineLevel;               // mg/L (nullable)
    
    @Field("ph_level")
    private Double phLevel;                     // (nullable)
    
    @Field("turbidity_level")
    private Double turbidityLevel;              // NTU (nullable)
    
    @Field("test_result")
    private String testResult;                  // APPROVED, REJECTED, REQUIRES_TREATMENT
    
    @Field("tested_by_user_id")
    private String testedByUserId;              // FK a users
    
    @Field("observations")
    private String observations;
    
    @Field("treatment_applied")
    private Boolean treatmentApplied;           // ¿Se aplicó tratamiento?
    
    @Field("treatment_description")
    private String treatmentDescription;
}
```

### Enums:

```java
public enum PointType {
    RESERVOIR,          // Reservorio
    TAP,                // Grifo/Caño
    WELL,               // Pozo
    SOURCE              // Fuente/Manantial
}

public enum TestType {
    CHLORINE,           // Cloro residual
    PH,                 // pH
    TURBIDITY,          // Turbiedad
    BACTERIOLOGICAL,    // Bacteriológico
    CHEMICAL            // Químico completo
}

public enum TestResult {
    APPROVED,           // Aprobado
    REJECTED,           // Rechazado
    REQUIRES_TREATMENT  // Requiere tratamiento
}
```

---

## 6️⃣ vg-ms-distribution (MongoDB)

### DistributionProgramDocument.java

```java
@Document(collection = "distribution_programs")
public class DistributionProgramDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("program_name")
    private String programName;
    
    @Field("description")
    private String description;
    
    @Field("distribution_status")
    private String distributionStatus;          // ACTIVE, INACTIVE, SUSPENDED
    
    @Field("start_date")
    private LocalDateTime startDate;
    
    @Field("end_date")
    private LocalDateTime endDate;              // nullable
}
```

### DistributionRouteDocument.java

```java
@Document(collection = "distribution_routes")
public class DistributionRouteDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("route_code")
    private String routeCode;                   // UNIQUE: RUTA-A
    
    @Field("route_name")
    private String routeName;
    
    @Field("zone_ids")
    private List<String> zoneIds;               // Array de zonas cubiertas
    
    @Field("route_name")
    private String routeName;
    
    @Field("distribution_status")
    private String distributionStatus;
}
```

### DistributionScheduleDocument.java

```java
@Document(collection = "distribution_schedules")
public class DistributionScheduleDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("route_id")
    private String routeId;                     // FK a distribution_routes
    
    @Field("day_of_week")
    private String dayOfWeek;                   // MONDAY, TUESDAY, etc.
    
    @Field("start_time")
    private String startTime;                   // HH:mm formato
    
    @Field("end_time")
    private String endTime;
}
```

### Enums:

```java
public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

public enum DistributionStatus {
    ACTIVE,             // Activo
    INACTIVE,           // Inactivo
    SUSPENDED           // Suspendido temporalmente
}
```

---

## 7️⃣ vg-ms-inventory-purchases (PostgreSQL)

### SupplierEntity.java

```java
@Table("suppliers")
public class SupplierEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("supplier_name")
    private String supplierName;
    
    @Column("ruc")
    private String ruc;
    
    @Column("address")
    private String address;
    
    @Column("phone")
    private String phone;
    
    @Column("email")
    private String email;
    
    @Column("contact_person")
    private String contactPerson;
}
```

### MaterialEntity.java

```java
@Table("materials")
public class MaterialEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("material_code")
    private String materialCode;                // UNIQUE: MAT-001
    
    @Column("material_name")
    private String materialName;
    
    @Column("category_id")
    private String categoryId;                  // FK a product_categories
    
    @Column("unit")
    private String unit;                        // UNIT, METERS, KG, LITERS
    
    @Column("current_stock")
    private Double currentStock;
    
    @Column("min_stock")
    private Double minStock;                    // Stock mínimo
    
    @Column("unit_price")
    private Double unitPrice;
}
```

### ProductCategoryEntity.java

```java
@Table("product_categories")
public class ProductCategoryEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("category_name")
    private String categoryName;
    
    @Column("description")
    private String description;
}
```

### PurchaseEntity.java

```java
@Table("purchases")
public class PurchaseEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("purchase_code")
    private String purchaseCode;                // UNIQUE: COMP-2024-001
    
    @Column("supplier_id")
    private String supplierId;                  // FK a suppliers
    
    @Column("purchase_date")
    private LocalDateTime purchaseDate;
    
    @Column("total_amount")
    private Double totalAmount;
    
    @Column("purchase_status")
    private String purchaseStatus;              // PENDING, RECEIVED, CANCELLED
    
    @Column("invoice_number")
    private String invoiceNumber;
}
```

### PurchaseDetailEntity.java

```java
@Table("purchase_details")
public class PurchaseDetailEntity {
    
    @Id
    private String id;
    
    @Column("purchase_id")
    private String purchaseId;                  // FK a purchases
    
    @Column("material_id")
    private String materialId;                  // FK a materials
    
    @Column("quantity")
    private Double quantity;
    
    @Column("unit_price")
    private Double unitPrice;
    
    @Column("subtotal")
    private Double subtotal;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}
```

### InventoryMovementEntity.java

```java
@Table("inventory_movements")
public class InventoryMovementEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    // Campos específicos (Kardex)
    @Column("material_id")
    private String materialId;                  // FK a materials
    
    @Column("movement_type")
    private String movementType;                // IN, OUT, ADJUSTMENT
    
    @Column("quantity")
    private Double quantity;
    
    @Column("unit_price")
    private Double unitPrice;
    
    @Column("previous_stock")
    private Double previousStock;
    
    @Column("new_stock")
    private Double newStock;
    
    @Column("reference_id")
    private String referenceId;                 // ID de la compra, incidente, etc.
    
    @Column("reference_type")
    private String referenceType;               // PURCHASE, INCIDENT_RESOLUTION, ADJUSTMENT
    
    @Column("notes")
    private String notes;
}
```

### Enums:

```java
public enum MovementType {
    IN,                 // Entrada (compras)
    OUT,                // Salida (consumo)
    ADJUSTMENT          // Ajuste de inventario
}

public enum PurchaseStatus {
    PENDING,            // Pendiente
    RECEIVED,           // Recibido
    CANCELLED           // Cancelado
}

public enum Unit {
    UNIT,               // Unidad
    METERS,             // Metros
    KG,                 // Kilogramos
    LITERS              // Litros
}
```

---

## 8️⃣ vg-ms-claims-incidents (MongoDB)

### ComplaintDocument.java

```java
@Document(collection = "complaints")
public class ComplaintDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("complaint_code")
    private String complaintCode;               // UNIQUE: QUEJAS-2024-001
    
    @Field("user_id")
    private String userId;                      // FK a users (quien reporta)
    
    @Field("category_id")
    private String categoryId;                  // FK a complaint_categories
    
    @Field("complaint_date")
    private LocalDateTime complaintDate;
    
    @Field("complaint_priority")
    private String complaintPriority;           // LOW, MEDIUM, HIGH, URGENT
    
    @Field("complaint_status")
    private String complaintStatus;             // RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
    
    @Field("description")
    private String description;
    
    @Field("assigned_to_user_id")
    private String assignedToUserId;            // FK a users (técnico asignado)
}
```

### ComplaintCategoryDocument.java

```java
@Document(collection = "complaint_categories")
public class ComplaintCategoryDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("category_code")
    private String categoryCode;
    
    @Field("category_name")
    private String categoryName;
    
    @Field("description")
    private String description;
}
```

### ComplaintResponseDocument.java

```java
@Document(collection = "complaint_responses")
public class ComplaintResponseDocument {
    
    @Id
    private String id;
    
    @Field("complaint_id")
    private String complaintId;                 // FK a complaints
    
    @Field("response_date")
    private LocalDateTime responseDate;
    
    @Field("response_type")
    private String responseType;                // INVESTIGACION, SOLUCION, SEGUIMIENTO
    
    @Field("response_description")
    private String responseDescription;
    
    @Field("responded_by_user_id")
    private String respondedByUserId;           // FK a users
    
    @Field("created_at")
    private LocalDateTime createdAt;
}
```

### IncidentDocument.java

```java
@Document(collection = "incidents")
public class IncidentDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("incident_code")
    private String incidentCode;                // UNIQUE: INC-2024-001
    
    @Field("incident_type_id")
    private String incidentTypeId;              // FK a incident_types
    
    @Field("incident_date")
    private LocalDateTime incidentDate;
    
    @Field("location")
    private String location;
    
    @Field("zone_id")
    private String zoneId;                      // FK a zones
    
    @Field("street_id")
    private String streetId;                    // FK a streets
    
    @Field("incident_severity")
    private String incidentSeverity;            // LOW, MEDIUM, HIGH, CRITICAL
    
    @Field("incident_status")
    private String incidentStatus;              // REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    
    @Field("description")
    private String description;
    
    @Field("reported_by_user_id")
    private String reportedByUserId;
    
    @Field("assigned_to_user_id")
    private String assignedToUserId;
}
```

### IncidentTypeDocument.java

```java
@Document(collection = "incident_types")
public class IncidentTypeDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("record_status")
    private String recordStatus;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Field("type_code")
    private String typeCode;
    
    @Field("type_name")
    private String typeName;
    
    @Field("description")
    private String description;
}
```

### IncidentResolutionDocument.java

```java
@Document(collection = "incident_resolutions")
public class IncidentResolutionDocument {
    
    @Id
    private String id;
    
    @Field("incident_id")
    private String incidentId;                  // FK a incidents
    
    @Field("resolution_date")
    private LocalDateTime resolutionDate;
    
    @Field("resolution_type")
    private String resolutionType;              // REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
    
    @Field("resolution_description")
    private String resolutionDescription;
    
    @Field("materials_used")
    private List<MaterialUsedEmbedded> materialsUsed;  // Embedded documents
    
    @Field("total_cost")
    private Double totalCost;
    
    @Field("resolved_by_user_id")
    private String resolvedByUserId;
    
    @Field("created_at")
    private LocalDateTime createdAt;
}

// Embedded class
class MaterialUsedEmbedded {
    private String materialId;
    private Double quantity;
    private String unit;
    private Double unitCost;
}
```

### Enums:

```java
public enum ComplaintPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

public enum ComplaintStatus {
    RECEIVED,           // Recibido
    IN_PROGRESS,        // En proceso
    RESOLVED,           // Resuelto
    CLOSED              // Cerrado
}

public enum ResponseType {
    INVESTIGACION,      // Investigación
    SOLUCION,           // Solución
    SEGUIMIENTO         // Seguimiento
}

public enum IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

public enum IncidentStatus {
    REPORTED,           // Reportado
    ASSIGNED,           // Asignado
    IN_PROGRESS,        // En progreso
    RESOLVED,           // Resuelto
    CLOSED              // Cerrado
}

public enum ResolutionType {
    REPARACION_TEMPORAL,    // Reparación temporal
    REPARACION_COMPLETA,    // Reparación completa
    REEMPLAZO               // Reemplazo de componente
}
```

---

## 9️⃣ vg-ms-infrastructure (PostgreSQL)

### WaterBoxEntity.java

```java
@Table("water_boxes")
public class WaterBoxEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("updated_by")
    private String updatedBy;
    
    // Campos específicos
    @Column("box_code")
    private String boxCode;                     // UNIQUE: BOX-001
    
    @Column("box_type")
    private String boxType;                     // RESIDENTIAL, COMMERCIAL, COMMUNAL, INSTITUTIONAL
    
    @Column("installation_date")
    private LocalDateTime installationDate;
    
    @Column("zone_id")
    private String zoneId;                      // FK a zones
    
    @Column("street_id")
    private String streetId;                    // FK a streets
    
    @Column("address")
    private String address;
    
    @Column("current_assignment_id")
    private String currentAssignmentId;         // FK a water_box_assignments (último asignado)
    
    @Column("is_active")
    private Boolean isActive;                   // Activo o cortado
}
```

### WaterBoxAssignmentEntity.java

```java
@Table("water_box_assignments")
public class WaterBoxAssignmentEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("record_status")
    private String recordStatus;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    // Campos específicos
    @Column("water_box_id")
    private String waterBoxId;                  // FK a water_boxes
    
    @Column("user_id")
    private String userId;                      // FK a users
    
    @Column("assignment_date")
    private LocalDateTime assignmentDate;
    
    @Column("assignment_status")
    private String assignmentStatus;            // ACTIVE, INACTIVE, TRANSFERRED
    
    @Column("end_date")
    private LocalDateTime endDate;              // nullable - fecha de fin de asignación
}
```

### WaterBoxTransferEntity.java

```java
@Table("water_box_transfers")
public class WaterBoxTransferEntity {
    
    @Id
    private String id;
    
    @Column("organization_id")
    private String organizationId;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("created_by")
    private String createdBy;
    
    // Campos específicos
    @Column("water_box_id")
    private String waterBoxId;                  // FK a water_boxes
    
    @Column("from_user_id")
    private String fromUserId;                  // FK a users (usuario anterior)
    
    @Column("to_user_id")
    private String toUserId;                    // FK a users (nuevo usuario)
    
    @Column("transfer_date")
    private LocalDateTime transferDate;
    
    @Column("transfer_fee")
    private Double transferFee;                 // Tarifa de transferencia
    
    @Column("notes")
    private String notes;
}
```

### Enums:

```java
public enum BoxType {
    RESIDENTIAL,        // Residencial
    COMMERCIAL,         // Comercial
    COMMUNAL,           // Comunal
    INSTITUTIONAL       // Institucional
}

public enum AssignmentStatus {
    ACTIVE,             // Activo
    INACTIVE,           // Inactivo
    TRANSFERRED         // Transferido
}
```

---

## 🔟 vg-ms-notification (Node.js/TypeScript)

**NO tiene persistence**. Solo envía mensajes vía Twilio.

---

## 1️⃣1️⃣ vg-ms-gateway

**NO tiene persistence**. Es un proxy de enrutamiento.

---

## ✅ RESUMEN TOTAL

**PostgreSQL**: 16 tablas
- vg-ms-users: 1
- vg-ms-payments-billing: 3
- vg-ms-inventory-purchases: 6
- vg-ms-infrastructure: 3

**MongoDB**: 13 collections
- vg-ms-organizations: 5
- vg-ms-water-quality: 2
- vg-ms-distribution: 3
- vg-ms-claims-incidents: 6

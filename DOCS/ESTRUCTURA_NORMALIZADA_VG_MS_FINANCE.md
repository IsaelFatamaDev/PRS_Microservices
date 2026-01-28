# 📊 ESTRUCTURA NORMALIZADA FINAL - VG-MS-FINANCE

## Fecha: 20 Enero 2026

---

## 🎯 PRINCIPIOS DE NORMALIZACIÓN

### 1. **Ingresos = Pagos registrados**

- ❌ NO crear tabla `incomes` separada
- ✅ Los `payments` YA SON los ingresos
- ✅ Calcular totales con queries sobre `payments`

### 2. **Empleados/Operarios = Usuarios con roles**

- ❌ NO crear tabla `employees` separada
- ✅ Están en `vg-ms-users` con roles (OPERATOR, ADMIN)
- ✅ Referenciar por `userId` en egresos

### 3. **Balance = Vista calculada, NO tabla**

- ❌ NO crear tabla `monthly_balances`
- ✅ Calcular en tiempo real con queries
- ✅ Cachear en Redis si es necesario

### 4. **Categorías = Enum, NO tabla**

- ❌ NO crear tabla `expense_categories`
- ✅ Usar `ExpenseType` enum
- ✅ Más simple, más rápido

---

## 📋 ESTRUCTURA FINAL DE VG-MS-FINANCE

### Base de Datos: 🟦 PostgreSQL

### **4 Tablas esenciales:**

```sql
finance/
├── receipts              (Recibos de usuarios)
├── payments              (Pagos realizados = INGRESOS)
├── payment_details       (Detalle de cada pago)
└── expenses              (Egresos: compras, salarios, mantenimientos)
```

---

## 1️⃣ RECEIPTS (Recibos)

```java
@Entity
@Table(name = "receipts", schema = "finance")
public class ReceiptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String receiptCode;  // REC-2026-000001

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;  // Usuario está en vg-ms-users

    @ManyToOne
    @JoinColumn(name = "water_box_id", nullable = false)
    private Long waterBoxId;  // Caja está en vg-ms-infrastructure

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private Long zoneId;  // Zona está en vg-ms-organizations

    // Meses cubiertos (ARRAY PostgreSQL)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] monthsOwed;  // {"2026-01", "2026-02"}

    // Montos
    @Column(nullable = false)
    private BigDecimal monthlyFee;

    @Column(nullable = false)
    private Integer monthsCount;

    @Column(nullable = false)
    private BigDecimal subtotal;  // monthlyFee * monthsCount

    @Column(nullable = false)
    private BigDecimal reconnectionFee;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;  // PENDING, PAID, OVERDUE, CANCELLED

    private LocalDate dueDate;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;

    private String pdfUrl;  // S3
}
```

---

## 2️⃣ PAYMENTS (Pagos = INGRESOS)

```java
@Entity
@Table(name = "payments", schema = "finance")
@Index(name = "idx_payment_date", columnList = "paidAt")  // Para reportes
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
    private Long userId;  // Usuario en vg-ms-users

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;  // CASH, YAPE

    private String yapeReference;
    private String yapePhone;

    @Column(nullable = false)
    private LocalDateTime paidAt;  // ⭐ CLAVE para calcular ingresos

    private String receivedBy;  // Keycloak ID (usuario en vg-ms-users con rol OPERATOR)

    private String pdfUrl;
}
```

**⭐ IMPORTANTE:** Esta tabla ES el registro de ingresos.

**Query para obtener ingresos del mes:**

```sql
SELECT
    SUM(amount_paid) as total_income,
    COUNT(*) as total_payments
FROM finance.payments
WHERE paid_at >= '2026-01-01'
  AND paid_at < '2026-02-01';
```

---

## 3️⃣ PAYMENT_DETAILS (Detalles de Pago)

```java
@Entity
@Table(name = "payment_details", schema = "finance")
public class PaymentDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentDetailType type;  // MONTHLY_FEE, RECONNECTION_FEE

    private String month;  // "2026-01" (si aplica)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;
}
```

---

## 4️⃣ EXPENSES (Egresos)

```java
@Entity
@Table(name = "expenses", schema = "finance")
@Index(name = "idx_expense_date", columnList = "expenseDate")
@Index(name = "idx_expense_type", columnList = "type")
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

    @Column(nullable = false)
    private LocalDate expenseDate;  // ⭐ CLAVE para reportes

    // Trazabilidad: ¿De dónde viene este gasto?
    private String sourceType;  // "PURCHASE", "SALARY", "MAINTENANCE", "UTILITY", "ADMINISTRATIVE"
    private Long sourceId;      // ID de la compra, mantenimiento, etc.

    private String description;

    // ⭐ Para SALARIOS: referencia al usuario (está en vg-ms-users)
    private Long userId;  // Empleado/operario que recibió el salario

    // ⭐ Para COMPRAS: referencias a proveedor y compra (están en vg-ms-inventory)
    private Long supplierId;
    private Long purchaseId;

    // ⭐ Para MANTENIMIENTOS: referencia a mantenimiento (está en vg-ms-infrastructure)
    // OPCIONAL: Solo si el mantenimiento usó materiales con costo

    private String authorizedBy;  // Keycloak ID del ADMIN (usuario en vg-ms-users)

    private LocalDateTime createdAt;
}

// Enum de tipos de egresos
public enum ExpenseType {
    PURCHASE,               // Compra de materiales
    SALARY,                 // Pago de salarios
    MAINTENANCE_MATERIALS,  // Materiales usados en mantenimientos
    UTILITY_BILLS,          // Servicios (luz, agua, internet)
    ADMINISTRATIVE          // Gastos administrativos
}
```

**Query para obtener egresos del mes:**

```sql
SELECT
    type,
    SUM(amount) as total,
    COUNT(*) as count
FROM finance.expenses
WHERE expense_date >= '2026-01-01'
  AND expense_date < '2026-02-01'
GROUP BY type;
```

---

## 📊 CÁLCULO DE BALANCE (Sin tabla separada)

### Service para obtener balance

```java
@Service
public class BalanceService {

    public MonthlyBalanceDTO getMonthlyBalance(String period) {
        // period = "2026-01"
        LocalDate startDate = LocalDate.parse(period + "-01");
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        // 1. INGRESOS = Suma de payments
        BigDecimal totalIncome = paymentRepository.sumAmountPaidBetweenDates(
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );

        // Desglose de ingresos
        BigDecimal monthlyPayments = paymentDetailRepository
            .sumByTypeAndDateRange(
                PaymentDetailType.MONTHLY_FEE,
                startDate,
                endDate
            );

        BigDecimal reconnectionFees = paymentDetailRepository
            .sumByTypeAndDateRange(
                PaymentDetailType.RECONNECTION_FEE,
                startDate,
                endDate
            );

        // 2. EGRESOS = Suma de expenses
        BigDecimal totalExpenses = expenseRepository.sumAmountBetweenDates(
            startDate,
            endDate
        );

        // Desglose de egresos por tipo
        Map<ExpenseType, BigDecimal> expensesByType = expenseRepository
            .sumAmountByTypeAndDateRange(startDate, endDate);

        BigDecimal purchases = expensesByType.getOrDefault(ExpenseType.PURCHASE, BigDecimal.ZERO);
        BigDecimal salaries = expensesByType.getOrDefault(ExpenseType.SALARY, BigDecimal.ZERO);
        BigDecimal maintenanceCosts = expensesByType.getOrDefault(ExpenseType.MAINTENANCE_MATERIALS, BigDecimal.ZERO);
        BigDecimal utilities = expensesByType.getOrDefault(ExpenseType.UTILITY_BILLS, BigDecimal.ZERO);
        BigDecimal administrative = expensesByType.getOrDefault(ExpenseType.ADMINISTRATIVE, BigDecimal.ZERO);

        // 3. Calcular balance neto
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // 4. Construir DTO
        return MonthlyBalanceDTO.builder()
            .period(period)
            .totalIncome(totalIncome)
            .totalExpenses(totalExpenses)
            .netBalance(netBalance)
            .monthlyPayments(monthlyPayments)
            .reconnectionFees(reconnectionFees)
            .purchases(purchases)
            .salaries(salaries)
            .maintenanceCosts(maintenanceCosts)
            .utilities(utilities)
            .administrative(administrative)
            .build();
    }
}
```

### Queries personalizadas en Repository

```java
@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM PaymentEntity p " +
           "WHERE p.paidAt >= :startDate AND p.paidAt <= :endDate")
    BigDecimal sumAmountPaidBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e " +
           "WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    BigDecimal sumAmountBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e.type, SUM(e.amount) FROM ExpenseEntity e " +
           "WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate " +
           "GROUP BY e.type")
    Map<ExpenseType, BigDecimal> sumAmountByTypeAndDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
```

---

## 🔄 FLUJOS DE INTEGRACIÓN NORMALIZADOS

### 1. Usuario paga recibo

```java
@Service
@Transactional
public class PaymentService {

    public PaymentResponse registerPayment(PaymentRequest request) {
        // 1. Crear pago
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentCode(generatePaymentCode());
        payment.setUserId(request.getUserId());
        payment.setAmountPaid(request.getTotalAmount());
        payment.setMethod(request.getPaymentMethod());
        payment.setPaidAt(LocalDateTime.now());  // ⭐ Auto-registra como ingreso
        payment.setReceivedBy(getCurrentOperatorKeycloakId());

        paymentRepository.save(payment);

        // 2. Crear detalles
        createPaymentDetails(payment, request.getSelectedMonths());

        // 3. Actualizar recibo
        updateReceiptStatus(payment.getReceiptId(), PaymentStatus.PAID);

        // ✅ NO necesita crear IncomeEntity porque payments YA SON ingresos

        return new PaymentResponse(payment);
    }
}
```

### 2. Se paga un salario

```java
@PostMapping("/expenses/salary")
@Transactional
public ResponseEntity<ExpenseResponse> paySalary(@RequestBody SalaryPaymentRequest request) {

    // 1. Obtener empleado de vg-ms-users (con llamada HTTP o RabbitMQ)
    UserDTO employee = userServiceClient.getUserById(request.getUserId());

    // Validar que tenga rol OPERATOR
    if (!employee.getRoles().contains("OPERATOR")) {
        throw new BadRequestException("Usuario no es operario");
    }

    // 2. Crear egreso
    ExpenseEntity expense = new ExpenseEntity();
    expense.setExpenseCode(generateExpenseCode());
    expense.setType(ExpenseType.SALARY);
    expense.setAmount(request.getNetSalary());
    expense.setExpenseDate(LocalDate.now());
    expense.setSourceType("SALARY");
    expense.setUserId(employee.getId());  // ⭐ Referencia a vg-ms-users
    expense.setDescription("Salario " + employee.getFirstName() + " " + employee.getLastName() + " - " + request.getPeriod());
    expense.setAuthorizedBy(getCurrentAdminKeycloakId());

    expenseRepository.save(expense);

    // 3. Publicar evento (opcional)
    rabbitMQ.publish("salary.paid", new SalaryPaidEvent(employee.getId(), request.getNetSalary()));

    return ResponseEntity.ok(new ExpenseResponse(expense));
}
```

### 3. Se completa una compra (desde vg-ms-inventory)

```java
// En vg-ms-inventory-purchases
@Transactional
public void completePurchase(Long purchaseId) {
    PurchaseEntity purchase = purchaseRepository.findById(purchaseId).orElseThrow();
    purchase.setStatus(PurchaseStatus.RECEIVED);

    // Publicar evento para vg-ms-finance
    rabbitMQ.publish("purchase.completed", new PurchaseCompletedEvent(
        purchase.getId(),
        purchase.getTotalAmount(),
        purchase.getSupplierId()
    ));
}

// En vg-ms-finance (Consumer)
@RabbitListener(queues = "finance.expense.queue")
public void handlePurchaseCompleted(PurchaseCompletedEvent event) {
    ExpenseEntity expense = new ExpenseEntity();
    expense.setExpenseCode(generateExpenseCode());
    expense.setType(ExpenseType.PURCHASE);
    expense.setAmount(event.getTotalAmount());
    expense.setExpenseDate(LocalDate.now());
    expense.setSourceType("PURCHASE");
    expense.setSourceId(event.getPurchaseId());
    expense.setSupplierId(event.getSupplierId());  // ⭐ Referencia a vg-ms-inventory
    expense.setPurchaseId(event.getPurchaseId());
    expense.setDescription("Compra materiales");
    expense.setAuthorizedBy(getCurrentAdminKeycloakId());

    expenseRepository.save(expense);
}
```

### 4. Se completa un mantenimiento (desde vg-ms-infrastructure)

```java
// En vg-ms-infrastructure
@Transactional
public void completeMaintenance(MaintenanceRequest request) {
    MaintenanceLogEntity maintenance = createMaintenance(request);

    // ⭐ Solo registrar materiales SI se usaron
    if (request.getMaterials() != null && !request.getMaterials().isEmpty()) {
        BigDecimal totalCost = registerMaterialsUsed(maintenance, request.getMaterials());

        // ⭐ Solo publicar evento SI hay costo
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            rabbitMQ.publish("maintenance.completed", new MaintenanceCompletedEvent(
                maintenance.getId(),
                totalCost
            ));
        }
    }

    // Si NO usó materiales, no hay egreso, no hay evento
public void handleMaintenanceCompleted(MaintenanceCompletedEvent event) {
    ExpenseEntity expense = new ExpenseEntity();
    expense.setExpenseCode(generateExpenseCode());
    expense.setType(ExpenseType.MAINTENANCE_MATERIALS);
    expense.setAmount(event.getTotalCost());
    expense.setExpenseDate(LocalDate.now());
    expense.setSourceType("MAINTENANCE");
    expense.setSourceId(event.getMaintenanceId());
    expense.setMaintenanceId(event.getMaintenanceId());  // ⭐ Referencia a vg-ms-infrastructure
    expense.setDescription("Materiales mantenimiento");
    expense.setAuthorizedBy(getCurrentOperatorKeycloakId());

    expenseRepository.save(expense);
}
```

---

## 🗄️ ESQUEMA POSTGRESQL COMPLETO

```sql
-- Schema
CREATE SCHEMA IF NOT EXISTS finance;

-- 1. Recibos
CREATE TABLE finance.receipts (
    id BIGSERIAL PRIMARY KEY,
    receipt_code VARCHAR(20) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,  -- Referencia a vg-ms-users
    water_box_id BIGINT NOT NULL,  -- Referencia a vg-ms-infrastructure
    zone_id BIGINT NOT NULL,  -- Referencia a vg-ms-organizations
    months_owed TEXT[] NOT NULL,
    monthly_fee NUMERIC(10, 2) NOT NULL,
    months_count INTEGER NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    reconnection_fee NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    due_date DATE,
    issued_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    pdf_url TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_receipts_user ON finance.receipts(user_id);
CREATE INDEX idx_receipts_status ON finance.receipts(payment_status);

-- 2. Pagos (= INGRESOS)
CREATE TABLE finance.payments (
    id BIGSERIAL PRIMARY KEY,
    payment_code VARCHAR(20) UNIQUE NOT NULL,
    receipt_id BIGINT NOT NULL REFERENCES finance.receipts(id),
    user_id BIGINT NOT NULL,  -- Referencia a vg-ms-users
    amount_paid NUMERIC(10, 2) NOT NULL,
    method VARCHAR(20) NOT NULL,
    yape_reference VARCHAR(50),
    yape_phone VARCHAR(15),
    paid_at TIMESTAMP NOT NULL,  -- ⭐ CLAVE para reportes de ingresos
    received_by VARCHAR(255),  -- Keycloak ID
    pdf_url TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_payments_user ON finance.payments(user_id);
CREATE INDEX idx_payments_date ON finance.payments(paid_at);  -- Para reportes

-- 3. Detalles de pago
CREATE TABLE finance.payment_details (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES finance.payments(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    month VARCHAR(7),
    description TEXT,
    amount NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_payment_details_payment ON finance.payment_details(payment_id);
CREATE INDEX idx_payment_details_type ON finance.payment_details(type);

-- 4. Egresos
CREATE TABLE finance.expenses (
    id BIGSERIAL PRIMARY KEY,
    expense_code VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(30) NOT NULL,  -- PURCHASE, SALARY, MAINTENANCE_MATERIALS, etc.
    amount NUMERIC(10, 2) NOT NULL,
    expense_date DATE NOT NULL,  -- ⭐ CLAVE para reportes de egresos
    source_type VARCHAR(30),
    source_id BIGINT,
    description TEXT,

    -- Referencias a otros microservicios
    user_id BIGINT,  -- Para SALARY (vg-ms-users)
    supplier_id BIGINT,  -- Para PURCHASE (vg-ms-inventory)
    purchase_id BIGINT,  -- Para PURCHASE (vg-ms-inventory)
    maintenance_id BIGINT,  -- Para MAINTENANCE (vg-ms-infrastructure)

    authorized_by VARCHAR(255),  -- Keycloak ID
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_expenses_type ON finance.expenses(type);
CREATE INDEX idx_expenses_date ON finance.expenses(expense_date);  -- Para reportes
CREATE INDEX idx_expenses_user ON finance.expenses(user_id);
```

---

## 📊 QUERIES DE EJEMPLO PARA REPORTES

### Balance Mensual Completo

```sql
-- Ingresos del mes
SELECT
    DATE_TRUNC('month', paid_at) as month,
    SUM(amount_paid) as total_income,
    COUNT(*) as total_payments
FROM finance.payments
WHERE paid_at >= '2026-01-01' AND paid_at < '2026-02-01'
GROUP BY DATE_TRUNC('month', paid_at);

-- Egresos del mes por tipo
SELECT
    type,
    SUM(amount) as total_expense,
    COUNT(*) as count
FROM finance.expenses
WHERE expense_date >= '2026-01-01' AND expense_date < '2026-02-01'
GROUP BY type;

-- Balance neto
SELECT
    (SELECT COALESCE(SUM(amount_paid), 0) FROM finance.payments
     WHERE paid_at >= '2026-01-01' AND paid_at < '2026-02-01') as income,
    (SELECT COALESCE(SUM(amount), 0) FROM finance.expenses
     WHERE expense_date >= '2026-01-01' AND expense_date < '2026-02-01') as expenses,
    (SELECT COALESCE(SUM(amount_paid), 0) FROM finance.payments
     WHERE paid_at >= '2026-01-01' AND paid_at < '2026-02-01') -
    (SELECT COALESCE(SUM(amount), 0) FROM finance.expenses
     WHERE expense_date >= '2026-01-01' AND expense_date < '2026-02-01') as net_balance;
```

### Top Gastos del Mes

```sql
SELECT
    expense_code,
    type,
    amount,
    description,
    expense_date
FROM finance.expenses
WHERE expense_date >= '2026-01-01' AND expense_date < '2026-02-01'
ORDER BY amount DESC
LIMIT 10;
```

### Salarios Pagados

```sql
SELECT
    e.expense_code,
    e.user_id,  -- Obtener nombre del usuario desde vg-ms-users
    e.amount,
    e.expense_date,
    e.description
FROM finance.expenses e
WHERE e.type = 'SALARY'
  AND e.expense_date >= '2026-01-01' AND e.expense_date < '2026-02-01'
ORDER BY e.expense_date DESC;
```

---

## 🎯 RESUMEN DE NORMALIZACIÓN

### ✅ Lo que SÍ tenemos

| Tabla | Función | Dependencias Externas |
|-------|---------|----------------------|
| `receipts` | Recibos mensuales | userId (vg-ms-users), waterBoxId (vg-ms-infrastructure), zoneId (vg-ms-organizations) |
| `payments` | **Pagos = INGRESOS** | userId (vg-ms-users), receivedBy (vg-ms-users) |
| `payment_details` | Desglose de pagos | paymentId (local) |
| `expenses` | **EGRESOS** | userId (vg-ms-users), supplierId/purchaseId (vg-ms-inventory), maintenanceId (vg-ms-infrastructure) |

### ❌ Lo que NO necesitamos

| Tabla Eliminada | Por qué NO es necesaria |
|-----------------|-------------------------|
| `incomes` | payments YA SON los ingresos |
| `employees` | Están en vg-ms-users con roles |
| `salary_payments` | Se maneja con expenses type=SALARY |
| `monthly_balances` | Se calcula en tiempo real con queries |
| `expense_categories` | Se usa enum ExpenseType |
| `budget_plans` | Se puede agregar después si es necesario |

### 🔗 Referencias Entre Microservicios

```
vg-ms-finance
    ↓ references
    ├─ vg-ms-users (userId, receivedBy, authorizedBy)
    ├─ vg-ms-infrastructure (waterBoxId, maintenanceId)
    ├─ vg-ms-organizations (zoneId)
    └─ vg-ms-inventory-purchases (supplierId, purchaseId)
```

---

## 📈 VENTAJAS DE ESTA ESTRUCTURA

1. ✅ **Normalización correcta** - Sin redundancia de datos
2. ✅ **Simplicidad** - Solo 4 tablas esenciales
3. ✅ **Trazabilidad total** - Cada egreso sabe de dónde viene
4. ✅ **Performance** - Queries eficientes con índices correctos
5. ✅ **Escalabilidad** - Fácil agregar nuevos tipos de egresos
6. ✅ **Integridad** - Referencias claras entre microservicios

---

**Generado:** 20 Enero 2026
**Autor:** GitHub Copilot
**Versión:** 2.0 (Normalizado)

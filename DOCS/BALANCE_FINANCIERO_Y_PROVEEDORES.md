# 💰 BALANCE FINANCIERO, PROVEEDORES Y EGRESOS

## Fecha: 20 Enero 2026

---

## 🎯 PREGUNTAS CLAVE

### 1. ¿Es necesario registro de proveedores?

**✅ SÍ, ABSOLUTAMENTE NECESARIO**

### 2. ¿Cómo se manejan materiales en reposiciones?

**✅ Flujo: Incidente → Uso materiales → Registro en kardex → Egreso financiero**

### 3. ¿Cómo manejar balance (ingresos/egresos)?

**✅ FALTA: Nuevo microservicio vg-ms-finance**

---

## 1️⃣ PROVEEDORES (SupplierEntity) - ¿POR QUÉ SON NECESARIOS?

### ✅ Razones para mantener registro de proveedores

#### A) **Trazabilidad de Compras**

```
Compra #PUR-2026-00001
├─ Proveedor: Ferretería Los Andes (RUC: 20123456789)
├─ Productos: 10 tubos PVC 1/2"
├─ Monto: S/250
└─ Fecha: 15 Enero 2026
```

Sin proveedores → ¿A quién le compramos? ¿Cuánto pagamos?

#### B) **Historial de Precios**

```sql
SELECT p.name, s.business_name, pd.unit_price, pur.purchase_date
FROM purchases pur
JOIN suppliers s ON pur.supplier_id = s.id
JOIN purchase_details pd ON pd.purchase_id = pur.id
JOIN products p ON pd.product_id = p.id
WHERE p.name = 'Tubo PVC 1/2"'
ORDER BY pur.purchase_date DESC;
```

Resultado:

| Proveedor | Precio | Fecha |
|-----------|--------|-------|
| Ferretería Los Andes | S/25 | 15-Ene-2026 |
| Comercial Huancayo | S/28 | 10-Dic-2025 |
| Ferretería Los Andes | S/24 | 05-Nov-2025 |

✅ **Decisión:** Ferretería Los Andes tiene mejor precio

#### C) **Información de Contacto**

```java
@Entity
public class SupplierEntity {
    private String businessName;  // "Ferretería Los Andes SAC"
    private String ruc;           // "20123456789"
    private String contactName;   // "Juan Pérez"
    private String phone;         // "964123456"
    private String email;         // "ventas@losandes.com"
    private String address;       // "Jr. Comercio 123, Huancayo"
}
```

Cuando se necesita hacer un pedido urgente → tienes todos los datos

#### D) **Reportes Financieros**

- ¿Cuánto gastamos con cada proveedor?
- ¿Quién nos da mejores precios?
- ¿Tenemos deudas pendientes?

### 🎯 Decisión: **SÍ mantener SupplierEntity**

---

## 2️⃣ FLUJO DE MATERIALES EN REPOSICIONES/MANTENIMIENTOS

### Escenario Real

**Usuario JASS-RINC-00005 no paga 3 meses → Se corta caja WB-RINC-005**

Usuario paga S/74:

- 3 meses × S/8 = S/24
- Reposición = **S/50**

**Operario va a reconectar la caja:**

1. **Verifica estado de la caja**
2. **Descubre:** Válvula dañada, necesita cambio
3. **Usa materiales del inventario:**
   - 1 Válvula de paso 1/2" (PROD-00015)
   - 1 Teflón (PROD-00032)
   - 2 Abrazaderas (PROD-00041)

### 📊 Modelo Completo

#### A) **Incident/Maintenance Entity**

```java
@Entity
@Table(name = "maintenance_logs", schema = "infrastructure")
public class MaintenanceLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String maintenanceCode;  // MAINT-2026-00001

    @ManyToOne
    @JoinColumn(name = "water_box_id", nullable = false)
    private Long waterBoxId;

    @Enumerated(EnumType.STRING)
    private MaintenanceType type;  // RECONNECTION, REPAIR, PREVENTIVE, EMERGENCY

    private String description;  // "Reconexión + cambio válvula dañada"

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;  // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String performedBy;  // Keycloak ID del OPERATOR

    // Relación con pago de reposición
    private Long reconnectionPaymentId;  // Referencia al PaymentEntity

    private String notes;
}

// Nueva tabla intermedia: Materiales usados
@Entity
@Table(name = "maintenance_materials", schema = "infrastructure")
public class MaintenanceMaterialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "maintenance_id", nullable = false)
    private Long maintenanceId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantityUsed;

    @Column(nullable = false)
    private BigDecimal unitCost;  // Costo del producto en ese momento

    @Column(nullable = false)
    private BigDecimal totalCost;  // quantityUsed * unitCost
}
```

#### B) **Flujo Completo:**

```java
@Service
public class MaintenanceService {

    @Transactional
    public MaintenanceLogEntity completeReconnection(
        Long waterBoxId,
        Long paymentId,
        List<MaterialUsageDTO> materialsUsed
    ) {
        // 1. Crear log de mantenimiento
        MaintenanceLogEntity maintenance = new MaintenanceLogEntity();
        maintenance.setMaintenanceCode(generateMaintenanceCode());
        maintenance.setWaterBoxId(waterBoxId);
        maintenance.setType(MaintenanceType.RECONNECTION);
        maintenance.setDescription("Reconexión por pago de reposición");
        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setCompletedAt(LocalDateTime.now());
        maintenance.setPerformedBy(getCurrentOperatorKeycloakId());
        maintenance.setReconnectionPaymentId(paymentId);

        maintenanceLogRepository.save(maintenance);

        // 2. Registrar materiales usados
        BigDecimal totalMaterialsCost = BigDecimal.ZERO;

        for (MaterialUsageDTO material : materialsUsed) {
            // 2.1 Obtener producto
            ProductEntity product = productRepository.findById(material.getProductId())
                .orElseThrow();

            // 2.2 Verificar stock
            if (product.getCurrentStock() < material.getQuantity()) {
                throw new InsufficientStockException(
                    "Stock insuficiente de " + product.getName()
                );
            }

            // 2.3 Registrar material usado en mantenimiento
            MaintenanceMaterialEntity maintenanceMaterial = new MaintenanceMaterialEntity();
            maintenanceMaterial.setMaintenanceId(maintenance.getId());
            maintenanceMaterial.setProductId(product.getId());
            maintenanceMaterial.setQuantityUsed(material.getQuantity());
            maintenanceMaterial.setUnitCost(product.getUnitPrice());
            maintenanceMaterial.setTotalCost(
                product.getUnitPrice().multiply(new BigDecimal(material.getQuantity()))
            );

            maintenanceMaterialRepository.save(maintenanceMaterial);

            // 2.4 Crear movimiento de inventario (SALIDA)
            InventoryMovementEntity movement = new InventoryMovementEntity();
            movement.setProductId(product.getId());
            movement.setType(MovementType.EXIT);
            movement.setQuantity(material.getQuantity());
            movement.setPreviousStock(product.getCurrentStock());
            movement.setCurrentStock(product.getCurrentStock() - material.getQuantity());
            movement.setReason("Usado en mantenimiento " + maintenance.getMaintenanceCode());
            movement.setReferenceId(maintenance.getId().toString());
            movement.setPerformedAt(LocalDateTime.now());
            movement.setPerformedBy(getCurrentOperatorKeycloakId());

            inventoryMovementRepository.save(movement);

            // 2.5 Actualizar stock del producto
            product.setCurrentStock(product.getCurrentStock() - material.getQuantity());
            productRepository.save(product);

            // 2.6 Acumular costo total
            totalMaterialsCost = totalMaterialsCost.add(maintenanceMaterial.getTotalCost());
        }

        // 3. IMPORTANTE: Registrar EGRESO en finanzas
        financeService.registerExpense(new ExpenseDTO(
            ExpenseType.MAINTENANCE_MATERIALS,
            totalMaterialsCost,
            "Materiales usados en " + maintenance.getMaintenanceCode(),
            maintenance.getId()
        ));

        // 4. Reactivar caja de agua
        WaterBoxEntity waterBox = waterBoxRepository.findById(waterBoxId).orElseThrow();
        waterBox.setStatus(WaterBoxStatus.ACTIVE);
        waterBoxRepository.save(waterBox);

        // 5. Publicar evento
        rabbitMQPublisher.publish("waterbox.reconnected", new WaterBoxReconnectedEvent(
            waterBoxId,
            maintenance.getMaintenanceCode(),
            totalMaterialsCost
        ));

        return maintenance;
    }
}
```

### 📋 Caso Completo

**Entrada:**

```json
POST /api/maintenance/reconnections
{
  "waterBoxId": 5,
  "paymentId": 123,
  "materialsUsed": [
    {"productId": 15, "quantity": 1},  // Válvula
    {"productId": 32, "quantity": 1},  // Teflón
    {"productId": 41, "quantity": 2}   // Abrazaderas
  ]
}
```

**Lo que pasa:**

1. ✅ Crea log de mantenimiento: `MAINT-2026-00001`
2. ✅ Registra materiales usados con sus costos
3. ✅ Crea movimientos de inventario (EXIT)
4. ✅ Reduce stock de productos
5. ✅ **Registra EGRESO en finanzas: S/35.50**
6. ✅ Reactiva caja: `WB-RINC-005` → ACTIVE

---

## 3️⃣ BALANCE FINANCIERO - NUEVO MICROSERVICIO

### ⚠️ PROBLEMA ACTUAL

El sistema actual solo registra **INGRESOS** (pagos de usuarios).

**Falta:**

- ❌ Egresos (compras, salarios, mantenimientos)
- ❌ Balance general
- ❌ Reportes financieros
- ❌ Flujo de caja
- ❌ Presupuestos

### ✅ SOLUCIÓN: Crear **vg-ms-finance**

---

## 4️⃣ NUEVO MICROSERVICIO: VG-MS-FINANCE

### Base de Datos: 🟦 PostgreSQL

**¿Por qué PostgreSQL?**

- ✅ Transacciones ACID (dinero de por medio)
- ✅ Reportes financieros complejos
- ✅ Agregaciones (SUM, AVG) eficientes
- ✅ Relaciones con otros microservicios

### Entidades (4)

```java
// 1. IncomeEntity.java
@Entity
@Table(name = "incomes", schema = "finance")
public class IncomeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String incomeCode;  // INC-2026-000001

    @Enumerated(EnumType.STRING)
    private IncomeType type;  // MONTHLY_PAYMENT, RECONNECTION_FEE, REGISTRATION_FEE

    @Column(nullable = false)
    private BigDecimal amount;

    @Indexed
    private LocalDate incomeDate;

    private String sourceType;  // "PAYMENT"
    private Long sourceId;  // ID del PaymentEntity

    private String description;  // "Pago mensual usuario JASS-RINC-00001"

    private String receivedBy;  // Keycloak ID del OPERATOR

    private LocalDateTime createdAt;
}

// 2. ExpenseEntity.java
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

    private String sourceType;  // "PURCHASE", "SALARY_PAYMENT", "MAINTENANCE"
    private Long sourceId;  // ID de la compra, pago de salario, mantenimiento

    private String description;

    private String authorizedBy;  // Keycloak ID del ADMIN

    // Para salarios
    private Long employeeId;  // Si es un pago de salario

    // Para compras
    private Long supplierId;  // Si es una compra
    private Long purchaseId;  // Referencia a PurchaseEntity

    private LocalDateTime createdAt;
}

// 3. SalaryPaymentEntity.java
@Entity
@Table(name = "salary_payments", schema = "finance")
public class SalaryPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String paymentCode;  // SAL-2026-000001

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Long employeeId;  // Referencia a EmployeeEntity

    @Column(nullable = false)
    private BigDecimal grossSalary;  // Salario bruto

    private BigDecimal deductions;  // Descuentos (AFP, ONP, etc.)

    @Column(nullable = false)
    private BigDecimal netSalary;  // Salario neto

    @Indexed
    private LocalDate paymentDate;

    private String paymentPeriod;  // "2026-01" (mes pagado)

    @Enumerated(EnumType.STRING)
    private SalaryPaymentStatus status;  // PENDING, PAID, CANCELLED

    private String paidBy;  // Keycloak ID del ADMIN

    private String notes;

    private LocalDateTime createdAt;
}

// 4. EmployeeEntity.java
@Entity
@Table(name = "employees", schema = "finance")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String employeeCode;  // EMP-00001

    private String keycloakId;  // UUID de Keycloak

    private String firstName;
    private String lastName;
    private String dni;
    private String phone;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;  // OPERATOR, TECHNICIAN, ACCOUNTANT

    @Column(nullable = false)
    private BigDecimal monthlySalary;

    private LocalDate hiredAt;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;  // ACTIVE, INACTIVE, ON_LEAVE

    private LocalDateTime createdAt;
}

// 5. BalanceEntity.java (Vista materializada o tabla de resumen)
@Entity
@Table(name = "monthly_balances", schema = "finance")
public class MonthlyBalanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Indexed
    private String period;  // "2026-01"

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;  // totalIncome - totalExpenses

    // Desglose de ingresos
    private BigDecimal monthlyPayments;
    private BigDecimal reconnectionFees;
    private BigDecimal registrationFees;

    // Desglose de egresos
    private BigDecimal purchases;
    private BigDecimal salaries;
    private BigDecimal maintenanceCosts;
    private BigDecimal utilities;
    private BigDecimal administrative;

    private LocalDateTime calculatedAt;
}
```

---

## 5️⃣ FLUJOS DE INTEGRACIÓN

### A) **Cuando se registra un pago (vg-ms-payments):**

```java
// En PaymentService después de guardar el pago:

// 1. Registrar INGRESO en finanzas
IncomeDTO income = new IncomeDTO();
income.setType(IncomeType.MONTHLY_PAYMENT);
income.setAmount(payment.getAmountPaid());
income.setIncomeDate(LocalDate.now());
income.setSourceType("PAYMENT");
income.setSourceId(payment.getId());
income.setDescription("Pago " + payment.getPaymentCode() + " - Usuario " + user.getUserCode());
income.setReceivedBy(getCurrentOperatorKeycloakId());

// Llamada a vg-ms-finance
financeServiceClient.registerIncome(income);

// O mejor: Publicar evento RabbitMQ
rabbitMQPublisher.publish("payment.received", new PaymentReceivedEvent(
    payment.getId(),
    payment.getAmountPaid(),
    payment.getPaidAt()
));
```

### B) **Cuando se completa una compra (vg-ms-inventory):**

```java
// En PurchaseService después de recibir la compra:

// 1. Registrar EGRESO en finanzas
ExpenseDTO expense = new ExpenseDTO();
expense.setType(ExpenseType.PURCHASE);
expense.setAmount(purchase.getTotalAmount());
expense.setExpenseDate(LocalDate.now());
expense.setSourceType("PURCHASE");
expense.setSourceId(purchase.getId());
expense.setSupplierId(purchase.getSupplierId());
expense.setDescription("Compra " + purchase.getPurchaseCode());
expense.setAuthorizedBy(getCurrentAdminKeycloakId());

// Publicar evento
rabbitMQPublisher.publish("purchase.completed", new PurchaseCompletedEvent(
    purchase.getId(),
    purchase.getTotalAmount(),
    purchase.getSupplierId()
));
```

### C) **Cuando se paga un salario (vg-ms-finance):**

```java
@PostMapping("/salaries/pay")
public ResponseEntity<SalaryPaymentResponse> paySalary(@RequestBody SalaryPaymentRequest request) {

    // 1. Obtener empleado
    EmployeeEntity employee = employeeRepository.findById(request.getEmployeeId())
        .orElseThrow();

    // 2. Crear pago de salario
    SalaryPaymentEntity salaryPayment = new SalaryPaymentEntity();
    salaryPayment.setPaymentCode(generateSalaryPaymentCode());
    salaryPayment.setEmployeeId(employee.getId());
    salaryPayment.setGrossSalary(employee.getMonthlySalary());
    salaryPayment.setDeductions(request.getDeductions());
    salaryPayment.setNetSalary(employee.getMonthlySalary().subtract(request.getDeductions()));
    salaryPayment.setPaymentDate(LocalDate.now());
    salaryPayment.setPaymentPeriod(request.getPeriod());  // "2026-01"
    salaryPayment.setStatus(SalaryPaymentStatus.PAID);
    salaryPayment.setPaidBy(getCurrentAdminKeycloakId());

    salaryPaymentRepository.save(salaryPayment);

    // 3. Registrar como EGRESO
    ExpenseEntity expense = new ExpenseEntity();
    expense.setExpenseCode(generateExpenseCode());
    expense.setType(ExpenseType.SALARY);
    expense.setAmount(salaryPayment.getNetSalary());
    expense.setExpenseDate(LocalDate.now());
    expense.setSourceType("SALARY_PAYMENT");
    expense.setSourceId(salaryPayment.getId());
    expense.setEmployeeId(employee.getId());
    expense.setDescription("Salario " + employee.getFirstName() + " " + employee.getLastName() + " - " + request.getPeriod());
    expense.setAuthorizedBy(getCurrentAdminKeycloakId());

    expenseRepository.save(expense);

    // 4. Publicar evento
    rabbitMQPublisher.publish("salary.paid", new SalaryPaidEvent(
        employee.getId(),
        salaryPayment.getNetSalary(),
        request.getPeriod()
    ));

    return ResponseEntity.ok(new SalaryPaymentResponse(salaryPayment));
}
```

---

## 6️⃣ REPORTES FINANCIEROS

### A) **Balance Mensual**

```java
@GetMapping("/balance/monthly/{period}")
public ResponseEntity<MonthlyBalanceResponse> getMonthlyBalance(@PathVariable String period) {

    // Periodo: "2026-01"

    // 1. Calcular ingresos del mes
    List<IncomeEntity> incomes = incomeRepository.findByIncomeDateBetween(
        LocalDate.parse(period + "-01"),
        LocalDate.parse(period + "-01").with(TemporalAdjusters.lastDayOfMonth())
    );

    BigDecimal totalIncome = incomes.stream()
        .map(IncomeEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal monthlyPayments = incomes.stream()
        .filter(i -> i.getType() == IncomeType.MONTHLY_PAYMENT)
        .map(IncomeEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal reconnectionFees = incomes.stream()
        .filter(i -> i.getType() == IncomeType.RECONNECTION_FEE)
        .map(IncomeEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 2. Calcular egresos del mes
    List<ExpenseEntity> expenses = expenseRepository.findByExpenseDateBetween(
        LocalDate.parse(period + "-01"),
        LocalDate.parse(period + "-01").with(TemporalAdjusters.lastDayOfMonth())
    );

    BigDecimal totalExpenses = expenses.stream()
        .map(ExpenseEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal purchases = expenses.stream()
        .filter(e -> e.getType() == ExpenseType.PURCHASE)
        .map(ExpenseEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal salaries = expenses.stream()
        .filter(e -> e.getType() == ExpenseType.SALARY)
        .map(ExpenseEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal maintenanceCosts = expenses.stream()
        .filter(e -> e.getType() == ExpenseType.MAINTENANCE_MATERIALS)
        .map(ExpenseEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 3. Calcular balance neto
    BigDecimal netBalance = totalIncome.subtract(totalExpenses);

    // 4. Construir respuesta
    MonthlyBalanceResponse response = new MonthlyBalanceResponse();
    response.setPeriod(period);
    response.setTotalIncome(totalIncome);
    response.setTotalExpenses(totalExpenses);
    response.setNetBalance(netBalance);
    response.setMonthlyPayments(monthlyPayments);
    response.setReconnectionFees(reconnectionFees);
    response.setPurchases(purchases);
    response.setSalaries(salaries);
    response.setMaintenanceCosts(maintenanceCosts);

    return ResponseEntity.ok(response);
}
```

**Respuesta JSON:**

```json
{
  "period": "2026-01",
  "totalIncome": 12450.00,
  "totalExpenses": 8320.50,
  "netBalance": 4129.50,
  "incomeBreakdown": {
    "monthlyPayments": 11200.00,
    "reconnectionFees": 1250.00,
    "registrationFees": 0.00
  },
  "expenseBreakdown": {
    "purchases": 3500.00,
    "salaries": 4200.00,
    "maintenanceCosts": 420.50,
    "utilities": 150.00,
    "administrative": 50.00
  }
}
```

### B) **Balance Anual**

```java
@GetMapping("/balance/yearly/{year}")
public ResponseEntity<YearlyBalanceResponse> getYearlyBalance(@PathVariable int year) {

    List<MonthlyBalanceResponse> monthlyBalances = new ArrayList<>();

    for (int month = 1; month <= 12; month++) {
        String period = String.format("%d-%02d", year, month);
        MonthlyBalanceResponse monthlyBalance = getMonthlyBalance(period).getBody();
        monthlyBalances.add(monthlyBalance);
    }

    BigDecimal totalIncome = monthlyBalances.stream()
        .map(MonthlyBalanceResponse::getTotalIncome)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalExpenses = monthlyBalances.stream()
        .map(MonthlyBalanceResponse::getTotalExpenses)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    YearlyBalanceResponse response = new YearlyBalanceResponse();
    response.setYear(year);
    response.setTotalIncome(totalIncome);
    response.setTotalExpenses(totalExpenses);
    response.setNetBalance(totalIncome.subtract(totalExpenses));
    response.setMonthlyBalances(monthlyBalances);

    return ResponseEntity.ok(response);
}
```

### C) **Dashboard Financiero (Frontend)**

```typescript
@Component({
  selector: 'app-financial-dashboard',
  template: `
    <div class="dashboard">
      <h1>Dashboard Financiero - {{ currentPeriod }}</h1>

      <!-- Resumen General -->
      <div class="summary-cards">
        <div class="card income">
          <h3>💰 Ingresos</h3>
          <p class="amount">S/ {{ totalIncome | number:'1.2-2' }}</p>
        </div>

        <div class="card expenses">
          <h3>💸 Egresos</h3>
          <p class="amount">S/ {{ totalExpenses | number:'1.2-2' }}</p>
        </div>

        <div class="card balance" [class.positive]="netBalance >= 0" [class.negative]="netBalance < 0">
          <h3>📊 Balance Neto</h3>
          <p class="amount">S/ {{ netBalance | number:'1.2-2' }}</p>
        </div>
      </div>

      <!-- Desglose de Ingresos -->
      <div class="breakdown">
        <h2>Ingresos por Tipo</h2>
        <div class="chart">
          <canvas id="incomeChart"></canvas>
        </div>
        <ul>
          <li>Pagos mensuales: S/ {{ monthlyPayments | number:'1.2-2' }}</li>
          <li>Reposiciones: S/ {{ reconnectionFees | number:'1.2-2' }}</li>
        </ul>
      </div>

      <!-- Desglose de Egresos -->
      <div class="breakdown">
        <h2>Egresos por Tipo</h2>
        <div class="chart">
          <canvas id="expenseChart"></canvas>
        </div>
        <ul>
          <li>Compras: S/ {{ purchases | number:'1.2-2' }}</li>
          <li>Salarios: S/ {{ salaries | number:'1.2-2' }}</li>
          <li>Mantenimientos: S/ {{ maintenanceCosts | number:'1.2-2' }}</li>
          <li>Servicios: S/ {{ utilities | number:'1.2-2' }}</li>
          <li>Administrativos: S/ {{ administrative | number:'1.2-2' }}</li>
        </ul>
      </div>

      <!-- Flujo de Caja Mensual -->
      <div class="cash-flow">
        <h2>Flujo de Caja - Últimos 12 Meses</h2>
        <div class="chart">
          <canvas id="cashFlowChart"></canvas>
        </div>
      </div>

      <!-- Tabla de Transacciones Recientes -->
      <div class="transactions">
        <h2>Transacciones Recientes</h2>
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Tipo</th>
              <th>Descripción</th>
              <th>Ingreso</th>
              <th>Egreso</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let transaction of recentTransactions">
              <td>{{ transaction.date | date:'dd/MM/yyyy' }}</td>
              <td>{{ transaction.type }}</td>
              <td>{{ transaction.description }}</td>
              <td class="income">{{ transaction.income ? 'S/ ' + (transaction.income | number:'1.2-2') : '-' }}</td>
              <td class="expense">{{ transaction.expense ? 'S/ ' + (transaction.expense | number:'1.2-2') : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class FinancialDashboardComponent implements OnInit {
  currentPeriod: string = '2026-01';
  totalIncome: number = 0;
  totalExpenses: number = 0;
  netBalance: number = 0;

  monthlyPayments: number = 0;
  reconnectionFees: number = 0;

  purchases: number = 0;
  salaries: number = 0;
  maintenanceCosts: number = 0;
  utilities: number = 0;
  administrative: number = 0;

  recentTransactions: any[] = [];

  ngOnInit() {
    this.loadMonthlyBalance();
    this.loadRecentTransactions();
  }

  loadMonthlyBalance() {
    this.financeService.getMonthlyBalance(this.currentPeriod).subscribe(response => {
      this.totalIncome = response.totalIncome;
      this.totalExpenses = response.totalExpenses;
      this.netBalance = response.netBalance;

      this.monthlyPayments = response.incomeBreakdown.monthlyPayments;
      this.reconnectionFees = response.incomeBreakdown.reconnectionFees;

      this.purchases = response.expenseBreakdown.purchases;
      this.salaries = response.expenseBreakdown.salaries;
      this.maintenanceCosts = response.expenseBreakdown.maintenanceCosts;
      this.utilities = response.expenseBreakdown.utilities;
      this.administrative = response.expenseBreakdown.administrative;

      this.renderCharts();
    });
  }

  loadRecentTransactions() {
    this.financeService.getRecentTransactions(20).subscribe(transactions => {
      this.recentTransactions = transactions;
    });
  }

  renderCharts() {
    // Implementar con Chart.js o similar
  }
}
```

---

## 7️⃣ RESUMEN ARQUITECTURA ACTUALIZADA

### Microservicios: **12 totales** (se agrega vg-ms-finance)

| # | Microservicio | Base de Datos | Función |
|---|---------------|---------------|---------|
| 1 | vg-ms-authentication | ❌ Ninguna | Keycloak proxy |
| 2 | vg-ms-users | 🟦 PostgreSQL | Gestión de usuarios |
| 3 | vg-ms-organizations | 🟩 MongoDB | JASS, zonas, calles |
| 4 | vg-ms-infrastructure | 🟦 PostgreSQL | Cajas de agua, asignaciones |
| 5 | vg-ms-payments-billing | 🟦 PostgreSQL | Recibos y pagos |
| 6 | **vg-ms-finance** | 🟦 **PostgreSQL** | **Balance, ingresos, egresos** |
| 7 | vg-ms-distribution | 🟩 MongoDB | Programación de distribución |
| 8 | vg-ms-inventory-purchases | 🟦 PostgreSQL | Inventario, compras, proveedores |
| 9 | vg-ms-water-quality | 🟩 MongoDB | Calidad del agua |
| 10 | vg-ms-claims-incidents | 🟩 MongoDB | Reclamos e incidentes |
| 11 | vg-ms-notification | 🟩 MongoDB | Notificaciones SMS/Email |
| 12 | vg-ms-gateway | ❌ Ninguna | API Gateway |

---

## 8️⃣ EVENTOS RABBITMQ PARA FINANZAS

```yaml
exchanges:
  - name: finance.exchange
    type: topic
    queues:
      - name: finance.income.queue
        routing_key: payment.received
      - name: finance.expense.queue
        routing_keys:
          - purchase.completed
          - salary.paid
          - maintenance.completed
```

**Eventos publicados:**

| Evento | Microservicio Origen | Consume vg-ms-finance |
|--------|---------------------|----------------------|
| `payment.received` | vg-ms-payments | → Crea IncomeEntity |
| `purchase.completed` | vg-ms-inventory | → Crea ExpenseEntity (PURCHASE) |
| `salary.paid` | vg-ms-finance | → Crea ExpenseEntity (SALARY) |
| `maintenance.completed` | vg-ms-infrastructure | → Crea ExpenseEntity (MAINTENANCE) |

---

## 9️⃣ PRÓXIMOS PASOS

### Prioridad ALTA

1. ✅ Mantener SupplierEntity (CONFIRMADO)
2. ✅ Crear MaintenanceLogEntity y MaintenanceMaterialEntity
3. ✅ Diseñar vg-ms-finance completo
4. ⏭️ Implementar integración de eventos (RabbitMQ)
5. ⏭️ Crear endpoints de balance financiero
6. ⏭️ Implementar dashboard financiero en frontend
7. ⏭️ Crear reportes PDF de balance mensual/anual

### Nuevas Entidades Agregadas

**vg-ms-infrastructure:**

- MaintenanceLogEntity (logs de mantenimientos/reposiciones)
- MaintenanceMaterialEntity (materiales usados en cada mantenimiento)

**vg-ms-finance:** (NUEVO MICROSERVICIO)

- IncomeEntity (ingresos)
- ExpenseEntity (egresos)
- SalaryPaymentEntity (pagos de salarios)
- EmployeeEntity (empleados/operarios)
- MonthlyBalanceEntity (balance mensual)

---

## 🎯 CONCLUSIONES

### 1. Proveedores → ✅ SÍ NECESARIOS

- Trazabilidad de compras
- Historial de precios
- Información de contacto
- Reportes financieros

### 2. Materiales en Reposiciones → ✅ FLUJO COMPLETO

- Mantenimiento registra materiales usados
- Inventario reduce stock
- **Finanzas registra egreso**

### 3. Balance Financiero → ✅ NUEVO MICROSERVICIO

- **vg-ms-finance** maneja TODO el balance
- Ingresos: pagos de usuarios
- Egresos: compras, salarios, mantenimientos
- Reportes mensuales/anuales
- Dashboard con gráficos

---

**Generado:** 20 Enero 2026
**Autor:** GitHub Copilot
**Versión:** 1.0

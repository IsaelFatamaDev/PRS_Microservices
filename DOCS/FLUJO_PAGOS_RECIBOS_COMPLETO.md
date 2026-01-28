# 💰 FLUJO COMPLETO DE PAGOS Y RECIBOS

## Fecha: 20 Enero 2026

---

## 📋 MODELO DE DATOS AJUSTADO

### 1. Recibo (Receipt)

```java
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

    // Meses cubiertos por este recibo (ARRAY PostgreSQL)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] monthsOwed;  // {"2026-01", "2026-02", "2026-03"}

    // Montos
    @Column(nullable = false)
    private BigDecimal monthlyFee;  // S/8, S/10, S/20 (según zona)

    @Column(nullable = false)
    private Integer monthsCount;  // Cantidad de meses: 3

    @Column(nullable = false)
    private BigDecimal subtotal;  // monthlyFee * monthsCount = S/24

    @Column(nullable = false)
    private BigDecimal reconnectionFee;  // S/50 o 0

    @Column(nullable = false)
    private BigDecimal totalAmount;  // subtotal + reconnectionFee

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;  // PENDING, PAID, OVERDUE, CANCELLED

    private LocalDate dueDate;  // Fecha límite de pago
    private LocalDateTime issuedAt;  // Cuándo se generó
    private LocalDateTime paidAt;  // Cuándo se pagó

    private String pdfUrl;  // URL del PDF en S3
}
```

### 2. Pago (Payment)

```java
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

    private String pdfUrl;  // URL del comprobante PDF
}
```

### 3. Detalle de Pago (Payment Detail)

```java
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

    private String month;  // "2026-01" (si aplica)
    private String description;  // "Pago mensual Enero 2026"

    @Column(nullable = false)
    private BigDecimal amount;
}
```

---

## 🔄 FLUJO COMPLETO

### PASO 1: Generación Automática de Recibos (Cron Job)

**¿Cuándo?** El día 1 de cada mes a las 00:00

```java
@Scheduled(cron = "0 0 0 1 * *")  // Día 1 de cada mes
public void generateMonthlyReceipts() {
    // 1. Obtener TODOS los usuarios activos
    List<UserEntity> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

    for (UserEntity user : activeUsers) {
        // 2. Verificar si ya tiene recibo pendiente para este mes
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        boolean hasReceiptForMonth = receiptRepository.existsByUserIdAndMonthsOwedContains(
            user.getId(),
            currentMonth
        );

        if (!hasReceiptForMonth) {
            // 3. Obtener tarifa de su zona
            ZoneFareHistoryDocument currentFare = getFareForZone(user.getZoneId());

            // 4. Verificar si tiene corte (reconnectionFee)
            boolean hasWaterBoxCut = waterBoxService.isWaterBoxCut(user.getWaterBoxId());

            // 5. Crear recibo
            ReceiptEntity receipt = new ReceiptEntity();
            receipt.setReceiptCode(generateReceiptCode());
            receipt.setUserId(user.getId());
            receipt.setWaterBoxId(user.getWaterBoxId());
            receipt.setZoneId(user.getZoneId());
            receipt.setMonthsOwed(new String[]{currentMonth});  // ["2026-02"]
            receipt.setMonthlyFee(currentFare.getMonthlyFee());
            receipt.setMonthsCount(1);
            receipt.setSubtotal(currentFare.getMonthlyFee());  // S/8 * 1
            receipt.setReconnectionFee(hasWaterBoxCut ? new BigDecimal("50.00") : BigDecimal.ZERO);
            receipt.setTotalAmount(receipt.getSubtotal().add(receipt.getReconnectionFee()));
            receipt.setPaymentStatus(PaymentStatus.PENDING);
            receipt.setDueDate(LocalDate.now().plusDays(15));  // 15 días para pagar
            receipt.setIssuedAt(LocalDateTime.now());

            receiptRepository.save(receipt);

            // 6. Generar PDF
            String pdfUrl = pdfService.generateReceiptPDF(receipt);
            receipt.setPdfUrl(pdfUrl);
            receiptRepository.save(receipt);

            // 7. Notificar al usuario por SMS
            notificationService.sendReceiptNotification(user, receipt);
        }
    }
}
```

---

### PASO 2: Consultar Meses Adeudados (Endpoint para Frontend)

```java
@GetMapping("/users/{userId}/unpaid-months")
public ResponseEntity<UnpaidMonthsResponse> getUnpaidMonths(@PathVariable Long userId) {

    // 1. Obtener TODOS los recibos pendientes del usuario
    List<ReceiptEntity> pendingReceipts = receiptRepository.findByUserIdAndPaymentStatus(
        userId,
        PaymentStatus.PENDING
    );

    // 2. Extraer todos los meses pendientes
    Set<String> unpaidMonths = new HashSet<>();
    for (ReceiptEntity receipt : pendingReceipts) {
        unpaidMonths.addAll(Arrays.asList(receipt.getMonthsOwed()));
    }

    // 3. Ordenar meses cronológicamente
    List<String> sortedUnpaidMonths = unpaidMonths.stream()
        .sorted()
        .collect(Collectors.toList());

    // 4. Obtener tarifa actual de la zona del usuario
    UserEntity user = userRepository.findById(userId).orElseThrow();
    ZoneFareHistoryDocument currentFare = getFareForZone(user.getZoneId());

    // 5. Verificar si tiene corte
    boolean hasWaterBoxCut = waterBoxService.isWaterBoxCut(user.getWaterBoxId());
    BigDecimal reconnectionFee = hasWaterBoxCut ? new BigDecimal("50.00") : BigDecimal.ZERO;

    // 6. Construir respuesta
    UnpaidMonthsResponse response = new UnpaidMonthsResponse();
    response.setUserId(userId);
    response.setUserCode(user.getUserCode());
    response.setUnpaidMonths(sortedUnpaidMonths);  // ["2025-12", "2026-01", "2026-02"]
    response.setMonthlyFee(currentFare.getMonthlyFee());  // S/8
    response.setReconnectionFee(reconnectionFee);  // S/50 o S/0
    response.setHasWaterBoxCut(hasWaterBoxCut);
    response.setPendingReceipts(pendingReceipts.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList())
    );

    return ResponseEntity.ok(response);
}
```

**Respuesta JSON:**

```json
{
  "userId": 123,
  "userCode": "JASS-RINC-00001",
  "unpaidMonths": ["2025-12", "2026-01", "2026-02"],
  "monthlyFee": 8.00,
  "reconnectionFee": 50.00,
  "hasWaterBoxCut": true,
  "pendingReceipts": [
    {
      "receiptCode": "REC-2025-000100",
      "monthsOwed": ["2025-12"],
      "totalAmount": 8.00,
      "dueDate": "2026-01-15",
      "status": "PENDING"
    },
    {
      "receiptCode": "REC-2026-000001",
      "monthsOwed": ["2026-01"],
      "totalAmount": 8.00,
      "dueDate": "2026-02-15",
      "status": "PENDING"
    },
    {
      "receiptCode": "REC-2026-000050",
      "monthsOwed": ["2026-02"],
      "totalAmount": 58.00,
      "dueDate": "2026-03-15",
      "status": "PENDING"
    }
  ]
}
```

---

### PASO 3: Frontend - Selección de Meses a Pagar

```typescript
// Angular Component
@Component({
  selector: 'app-payment-form',
  template: `
    <div class="payment-form">
      <h2>Pagar Recibos de Agua</h2>

      <!-- Usuario -->
      <div class="user-info">
        <p><strong>Usuario:</strong> {{ userCode }}</p>
        <p><strong>Tarifa mensual:</strong> S/ {{ monthlyFee }}</p>
        <p *ngIf="hasWaterBoxCut" class="alert-warning">
          ⚠️ Tu caja de agua está cortada. Reposición: S/ {{ reconnectionFee }}
        </p>
      </div>

      <!-- Meses pendientes -->
      <div class="unpaid-months">
        <h3>Meses pendientes</h3>
        <div class="month-selector">
          <label *ngFor="let month of unpaidMonths">
            <input
              type="checkbox"
              [value]="month"
              (change)="onMonthToggle(month)"
              [checked]="selectedMonths.includes(month)"
            />
            {{ formatMonth(month) }}
          </label>
        </div>
      </div>

      <!-- Resumen de pago -->
      <div class="payment-summary">
        <h3>Resumen</h3>
        <p>Meses seleccionados: {{ selectedMonths.length }}</p>
        <p>Subtotal: S/ {{ calculateSubtotal() }}</p>
        <p *ngIf="hasWaterBoxCut">Reposición: S/ {{ reconnectionFee }}</p>
        <p class="total"><strong>TOTAL: S/ {{ calculateTotal() }}</strong></p>
      </div>

      <!-- Método de pago -->
      <div class="payment-method">
        <h3>Método de pago</h3>
        <label>
          <input type="radio" name="method" value="CASH" [(ngModel)]="paymentMethod" />
          💵 Efectivo
        </label>
        <label>
          <input type="radio" name="method" value="YAPE" [(ngModel)]="paymentMethod" />
          📱 Yape
        </label>

        <!-- Campos adicionales para Yape -->
        <div *ngIf="paymentMethod === 'YAPE'" class="yape-fields">
          <input
            type="text"
            placeholder="Número de operación"
            [(ngModel)]="yapeReference"
          />
          <input
            type="tel"
            placeholder="Teléfono"
            [(ngModel)]="yapePhone"
          />
        </div>
      </div>

      <!-- Botón de pago -->
      <button
        class="btn-pay"
        [disabled]="selectedMonths.length === 0"
        (click)="processPayment()"
      >
        Registrar Pago
      </button>
    </div>
  `
})
export class PaymentFormComponent implements OnInit {
  userId: number;
  userCode: string;
  unpaidMonths: string[] = [];
  selectedMonths: string[] = [];
  monthlyFee: number = 0;
  reconnectionFee: number = 0;
  hasWaterBoxCut: boolean = false;
  paymentMethod: 'CASH' | 'YAPE' = 'CASH';
  yapeReference: string = '';
  yapePhone: string = '';

  ngOnInit() {
    this.loadUnpaidMonths();
  }

  loadUnpaidMonths() {
    this.paymentService.getUnpaidMonths(this.userId).subscribe(response => {
      this.userCode = response.userCode;
      this.unpaidMonths = response.unpaidMonths;
      this.monthlyFee = response.monthlyFee;
      this.reconnectionFee = response.reconnectionFee;
      this.hasWaterBoxCut = response.hasWaterBoxCut;
    });
  }

  onMonthToggle(month: string) {
    const index = this.selectedMonths.indexOf(month);
    if (index > -1) {
      this.selectedMonths.splice(index, 1);
    } else {
      this.selectedMonths.push(month);
    }
    // Ordenar seleccionados
    this.selectedMonths.sort();
  }

  calculateSubtotal(): number {
    return this.selectedMonths.length * this.monthlyFee;
  }

  calculateTotal(): number {
    let total = this.calculateSubtotal();
    if (this.hasWaterBoxCut && this.selectedMonths.length > 0) {
      total += this.reconnectionFee;
    }
    return total;
  }

  formatMonth(month: string): string {
    // "2026-01" -> "Enero 2026"
    const [year, monthNum] = month.split('-');
    const months = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    return `${months[parseInt(monthNum) - 1]} ${year}`;
  }

  processPayment() {
    const paymentRequest = {
      userId: this.userId,
      selectedMonths: this.selectedMonths,
      paymentMethod: this.paymentMethod,
      yapeReference: this.paymentMethod === 'YAPE' ? this.yapeReference : null,
      yapePhone: this.paymentMethod === 'YAPE' ? this.yapePhone : null,
      totalAmount: this.calculateTotal()
    };

    this.paymentService.registerPayment(paymentRequest).subscribe(response => {
      alert('Pago registrado exitosamente');
      // Descargar comprobante
      this.downloadPaymentPDF(response.paymentCode);
    });
  }

  downloadPaymentPDF(paymentCode: string) {
    window.open(`/api/payments/${paymentCode}/pdf`, '_blank');
  }
}
```

---

### PASO 4: Backend - Registrar Pago

```java
@PostMapping("/payments/register")
public ResponseEntity<PaymentResponse> registerPayment(@RequestBody PaymentRequest request) {

    // 1. Validar que los meses seleccionados existen en recibos pendientes
    List<ReceiptEntity> receiptsToPayFor = receiptRepository
        .findByUserIdAndPaymentStatusAndMonthsOwedIn(
            request.getUserId(),
            PaymentStatus.PENDING,
            request.getSelectedMonths()
        );

    if (receiptsToPayFor.isEmpty()) {
        throw new BadRequestException("No hay recibos pendientes para los meses seleccionados");
    }

    // 2. Opción A: ¿Crear UN solo recibo consolidado para múltiples meses?
    // O Opción B: ¿Marcar como pagados los recibos existentes?

    // OPCIÓN RECOMENDADA: Crear un NUEVO recibo consolidado
    UserEntity user = userRepository.findById(request.getUserId()).orElseThrow();
    ZoneFareHistoryDocument currentFare = getFareForZone(user.getZoneId());

    ReceiptEntity consolidatedReceipt = new ReceiptEntity();
    consolidatedReceipt.setReceiptCode(generateReceiptCode());
    consolidatedReceipt.setUserId(user.getId());
    consolidatedReceipt.setWaterBoxId(user.getWaterBoxId());
    consolidatedReceipt.setZoneId(user.getZoneId());
    consolidatedReceipt.setMonthsOwed(request.getSelectedMonths().toArray(new String[0]));
    consolidatedReceipt.setMonthlyFee(currentFare.getMonthlyFee());
    consolidatedReceipt.setMonthsCount(request.getSelectedMonths().size());
    consolidatedReceipt.setSubtotal(
        currentFare.getMonthlyFee().multiply(new BigDecimal(request.getSelectedMonths().size()))
    );

    boolean hasWaterBoxCut = waterBoxService.isWaterBoxCut(user.getWaterBoxId());
    consolidatedReceipt.setReconnectionFee(
        hasWaterBoxCut ? new BigDecimal("50.00") : BigDecimal.ZERO
    );
    consolidatedReceipt.setTotalAmount(
        consolidatedReceipt.getSubtotal().add(consolidatedReceipt.getReconnectionFee())
    );
    consolidatedReceipt.setPaymentStatus(PaymentStatus.PAID);
    consolidatedReceipt.setIssuedAt(LocalDateTime.now());
    consolidatedReceipt.setPaidAt(LocalDateTime.now());

    receiptRepository.save(consolidatedReceipt);

    // 3. Marcar recibos antiguos como CANCELLED
    for (ReceiptEntity oldReceipt : receiptsToPayFor) {
        oldReceipt.setPaymentStatus(PaymentStatus.CANCELLED);
        receiptRepository.save(oldReceipt);
    }

    // 4. Crear el pago
    PaymentEntity payment = new PaymentEntity();
    payment.setPaymentCode(generatePaymentCode());
    payment.setReceiptId(consolidatedReceipt.getId());
    payment.setUserId(user.getId());
    payment.setAmountPaid(request.getTotalAmount());
    payment.setMethod(request.getPaymentMethod());
    payment.setYapeReference(request.getYapeReference());
    payment.setYapePhone(request.getYapePhone());
    payment.setPaidAt(LocalDateTime.now());
    payment.setReceivedBy(getCurrentOperatorKeycloakId());

    paymentRepository.save(payment);

    // 5. Crear detalles del pago
    for (String month : request.getSelectedMonths()) {
        PaymentDetailEntity detail = new PaymentDetailEntity();
        detail.setPaymentId(payment.getId());
        detail.setType(PaymentDetailType.MONTHLY_FEE);
        detail.setMonth(month);
        detail.setDescription("Pago mensual " + formatMonth(month));
        detail.setAmount(currentFare.getMonthlyFee());
        paymentDetailRepository.save(detail);
    }

    if (hasWaterBoxCut) {
        PaymentDetailEntity reconnectionDetail = new PaymentDetailEntity();
        reconnectionDetail.setPaymentId(payment.getId());
        reconnectionDetail.setType(PaymentDetailType.RECONNECTION_FEE);
        reconnectionDetail.setDescription("Reposición por corte");
        reconnectionDetail.setAmount(new BigDecimal("50.00"));
        paymentDetailRepository.save(reconnectionDetail);

        // 6. Reactivar caja de agua
        waterBoxService.reconnectWaterBox(user.getWaterBoxId());
    }

    // 7. Generar PDF del pago
    String pdfUrl = pdfService.generatePaymentPDF(payment, consolidatedReceipt);
    payment.setPdfUrl(pdfUrl);
    paymentRepository.save(payment);

    consolidatedReceipt.setPdfUrl(pdfUrl);
    receiptRepository.save(consolidatedReceipt);

    // 8. Notificar al usuario
    notificationService.sendPaymentConfirmation(user, payment);

    // 9. Publicar evento (RabbitMQ)
    rabbitMQPublisher.publish("payment.received", new PaymentReceivedEvent(
        payment.getId(),
        user.getId(),
        payment.getAmountPaid(),
        request.getSelectedMonths()
    ));

    return ResponseEntity.ok(new PaymentResponse(
        payment.getPaymentCode(),
        consolidatedReceipt.getReceiptCode(),
        payment.getAmountPaid(),
        payment.getPdfUrl()
    ));
}
```

---

### PASO 5: Generación de PDFs

```java
@Service
public class PDFService {

    public String generateReceiptPDF(ReceiptEntity receipt) {
        // 1. Obtener datos del usuario
        UserEntity user = userRepository.findById(receipt.getUserId()).orElseThrow();
        ZoneDocument zone = zoneRepository.findById(receipt.getZoneId()).orElseThrow();
        WaterBoxEntity waterBox = waterBoxRepository.findById(receipt.getWaterBoxId()).orElseThrow();

        // 2. Generar PDF con librería (iText, Apache PDFBox, etc.)
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Encabezado
        document.add(new Paragraph("JASS - Recibo de Agua"));
        document.add(new Paragraph("Código: " + receipt.getReceiptCode()));
        document.add(new Paragraph("Fecha: " + receipt.getIssuedAt().format(formatter)));
        document.add(new Paragraph("\n"));

        // Datos del usuario
        document.add(new Paragraph("DATOS DEL USUARIO"));
        document.add(new Paragraph("Usuario: " + user.getUserCode()));
        document.add(new Paragraph("Nombre: " + user.getFirstName() + " " + user.getLastName()));
        document.add(new Paragraph("DNI: " + user.getDni()));
        document.add(new Paragraph("Caja de agua: " + waterBox.getBoxCode()));
        document.add(new Paragraph("Zona: " + zone.getName()));
        document.add(new Paragraph("\n"));

        // Detalle de pago
        document.add(new Paragraph("DETALLE"));

        PdfPTable table = new PdfPTable(3);
        table.addCell("Concepto");
        table.addCell("Cantidad");
        table.addCell("Monto");

        // Meses
        for (String month : receipt.getMonthsOwed()) {
            table.addCell("Pago mensual " + formatMonth(month));
            table.addCell("1");
            table.addCell("S/ " + receipt.getMonthlyFee());
        }

        // Reposición
        if (receipt.getReconnectionFee().compareTo(BigDecimal.ZERO) > 0) {
            table.addCell("Reposición por corte");
            table.addCell("1");
            table.addCell("S/ " + receipt.getReconnectionFee());
        }

        table.addCell("");
        table.addCell("TOTAL");
        table.addCell("S/ " + receipt.getTotalAmount());

        document.add(table);

        // Pie de página
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Vencimiento: " + receipt.getDueDate().format(formatter)));
        document.add(new Paragraph("Estado: " + receipt.getPaymentStatus()));

        document.close();

        // 3. Subir a S3
        String filename = "receipts/" + receipt.getReceiptCode() + ".pdf";
        String pdfUrl = s3Service.uploadFile(filename, baos.toByteArray(), "application/pdf");

        return pdfUrl;
    }

    public String generatePaymentPDF(PaymentEntity payment, ReceiptEntity receipt) {
        // Similar al anterior, pero con datos de pago
        // Incluye: método de pago, referencia Yape, operador que recibió, etc.
        // ...
    }
}
```

---

### PASO 6: Descargar Recibos (Endpoint)

```java
@GetMapping("/receipts/{receiptCode}/pdf")
public ResponseEntity<byte[]> downloadReceiptPDF(@PathVariable String receiptCode) {
    ReceiptEntity receipt = receiptRepository.findByReceiptCode(receiptCode)
        .orElseThrow(() -> new NotFoundException("Recibo no encontrado"));

    // Si el PDF ya existe, devolverlo
    if (receipt.getPdfUrl() != null) {
        byte[] pdfBytes = s3Service.downloadFile(receipt.getPdfUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
            ContentDisposition.attachment()
                .filename(receiptCode + ".pdf")
                .build()
        );

        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    } else {
        throw new NotFoundException("PDF no disponible");
    }
}

@GetMapping("/payments/{paymentCode}/pdf")
public ResponseEntity<byte[]> downloadPaymentPDF(@PathVariable String paymentCode) {
    // Similar al anterior
}

@GetMapping("/users/{userId}/receipts")
public ResponseEntity<List<ReceiptDTO>> getUserReceipts(
    @PathVariable Long userId,
    @RequestParam(required = false) PaymentStatus status
) {
    List<ReceiptEntity> receipts;

    if (status != null) {
        receipts = receiptRepository.findByUserIdAndPaymentStatusOrderByIssuedAtDesc(userId, status);
    } else {
        receipts = receiptRepository.findByUserIdOrderByIssuedAtDesc(userId);
    }

    return ResponseEntity.ok(
        receipts.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList())
    );
}
```

---

## 📊 CASOS DE USO COMPLETOS

### Caso 1: Usuario debe 3 meses (Dic 2025, Ene 2026, Feb 2026)

**1. Sistema genera recibos automáticamente:**

- `REC-2025-000100` → ["2025-12"] → S/8 → PENDING
- `REC-2026-000001` → ["2026-01"] → S/8 → PENDING
- `REC-2026-000050` → ["2026-02"] → S/8 → PENDING

**2. Usuario consulta deuda:**

```http
GET /api/users/123/unpaid-months
```

```json
{
  "unpaidMonths": ["2025-12", "2026-01", "2026-02"],
  "monthlyFee": 8.00,
  "reconnectionFee": 0.00
}
```

**3. Usuario decide pagar solo Dic y Ene:**

- Selecciona en frontend: ✅ Diciembre 2025, ✅ Enero 2026
- Total: S/16

**4. Backend registra pago:**

- Crea recibo consolidado: `REC-2026-000200` → ["2025-12", "2026-01"] → S/16 → PAID
- Cancela recibos antiguos: `REC-2025-000100` y `REC-2026-000001` → CANCELLED
- Crea pago: `PAY-2026-000150` → S/16 → YAPE
- Genera PDF

**5. Usuario descarga comprobante:**

```http
GET /api/payments/PAY-2026-000150/pdf
```

**6. Próximo mes:**

- Sistema verifica: Febrero 2026 ya tiene recibo pendiente (`REC-2026-000050`)
- NO genera duplicado

---

### Caso 2: Usuario con corte paga reposición

**1. Usuario tiene corte:**

- Caja de agua: `WB-RINC-001` → Status: CUT
- Debe Enero y Febrero 2026

**2. Consulta deuda:**

```json
{
  "unpaidMonths": ["2026-01", "2026-02"],
  "monthlyFee": 8.00,
  "reconnectionFee": 50.00,
  "hasWaterBoxCut": true
}
```

**3. Usuario selecciona pagar ambos meses:**

- Total: (S/8 × 2) + S/50 = **S/66**

**4. Backend registra pago:**

- Crea recibo: `REC-2026-000300` → ["2026-01", "2026-02"] → S/66 → PAID
- Crea pago: `PAY-2026-000200` → S/66 → CASH
- Detalles:
  - Enero 2026: S/8
  - Febrero 2026: S/8
  - Reposición: S/50
- **Reactiva caja:** `WB-RINC-001` → Status: ACTIVE
- Genera PDF con todos los detalles

---

### Caso 3: Consultar historial de pagos

```http
GET /api/users/123/receipts?status=PAID
```

```json
[
  {
    "receiptCode": "REC-2026-000300",
    "monthsOwed": ["2026-01", "2026-02"],
    "totalAmount": 66.00,
    "status": "PAID",
    "paidAt": "2026-02-15T10:30:00",
    "pdfUrl": "https://s3.amazonaws.com/receipts/REC-2026-000300.pdf"
  },
  {
    "receiptCode": "REC-2025-000500",
    "monthsOwed": ["2025-11"],
    "totalAmount": 8.00,
    "status": "PAID",
    "paidAt": "2025-11-20T14:15:00",
    "pdfUrl": "https://s3.amazonaws.com/receipts/REC-2025-000500.pdf"
  }
]
```

---

## 🎯 RESUMEN FINAL

### ✅ Lo que el sistema hace automáticamente

1. **Genera recibos** el día 1 de cada mes
2. **Detecta meses adeudados** consultando recibos PENDING
3. **Calcula tarifa** según zona del usuario
4. **Verifica cortes** y agrega reposición si aplica
5. **Genera PDFs** para recibos y pagos
6. **Notifica por SMS** nuevos recibos y confirmaciones de pago
7. **Publica eventos** para otros microservicios

### ✅ Lo que el frontend permite

1. **Consultar meses adeudados** de cualquier usuario
2. **Seleccionar meses** a pagar (uno o varios)
3. **Ver cálculo en tiempo real** del total
4. **Elegir método de pago:** CASH o YAPE
5. **Ingresar datos Yape** (número operación, teléfono)
6. **Descargar PDFs** de recibos y pagos

### ✅ Métodos de pago (solo 2)

```java
public enum PaymentMethod {
    CASH,    // Efectivo
    YAPE     // Yape
}
```

---

## 📝 PRÓXIMOS PASOS

1. ✅ Ajustar enums de PaymentMethod (COMPLETADO)
2. ⏭️ Implementar generación de PDFs
3. ⏭️ Crear cron job para recibos mensuales
4. ⏭️ Crear endpoints de consulta y registro
5. ⏭️ Implementar frontend de selección de meses
6. ⏭️ Configurar S3 para almacenar PDFs
7. ⏭️ Integrar notificaciones SMS

---

**Generado:** 20 Enero 2026
**Autor:** GitHub Copilot
**Versión:** 1.0

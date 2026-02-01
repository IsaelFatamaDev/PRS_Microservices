# 🔢 ESTRATEGIA DE CÓDIGOS ÚNICOS - SIMPLIFICADA

## ✅ ÚNICA ENTIDAD CON CÓDIGO AUTO-GENERADO

### Receipt (Recibos Mensuales)
- **Formato**: `N° ###### - YYYY`
- **Ejemplo**: `N° 000001 - 2024`
- **Ubicación**: `vg-ms-commercial-operations`
- **Campo**: `receipt_number`
- **Generación**: Automática via trigger PostgreSQL

---

## ❌ ENTIDADES SIN CÓDIGO (Usar UUID)

Todas las demás entidades usarán **UUID** como identificador único:

### vg-ms-users
- User → Solo `id` (UUID)

### vg-ms-commercial-operations
- Payment → Solo `id` (UUID)
- PaymentDetail → Solo `id` (UUID)
- Debt → Solo `id` (UUID)
- ReceiptDetail → Solo `id` (UUID)
- ServiceCut → Solo `id` (UUID)
- PettyCash → Solo `id` (UUID)
- PettyCashMovement → Solo `id` (UUID)

### vg-ms-organizations (MongoDB)
- Organization → Solo `_id` (ObjectId/String)
- Zone → Solo `_id`
- Street → Solo `_id`
- Fare → Solo `_id`
- Parameter → Solo `_id`

### vg-ms-infrastructure
- WaterBox → Solo `id` (UUID)
- WaterBoxAssignment → Solo `id` (UUID)
- WaterBoxTransfer → Solo `id` (UUID)

### vg-ms-inventory-purchases
- Supplier → Solo `id` (UUID)
- Material → Solo `id` (UUID)
- ProductCategory → Solo `id` (UUID)
- Purchase → Solo `id` (UUID)
- PurchaseDetail → Solo `id` (UUID)
- InventoryMovement → Solo `id` (UUID)

### vg-ms-water-quality (MongoDB)
- TestingPoint → Solo `_id`
- QualityTest → Solo `_id`

### vg-ms-distribution (MongoDB)
- DistributionProgram → Solo `_id`
- DistributionRoute → Solo `_id`
- DistributionSchedule → Solo `_id`

### vg-ms-claims-incidents (MongoDB)
- Complaint → Solo `_id`
- ComplaintCategory → Solo `_id`
- ComplaintResponse → Solo `_id`
- Incident → Solo `_id`
- IncidentType → Solo `_id`
- IncidentResolution → Solo `_id`

---

## 🛠️ IMPLEMENTACIÓN: Receipt Number

### PostgreSQL (vg-ms-commercial-operations)

```sql
-- Secuencia
CREATE SEQUENCE seq_receipt_number START 1 INCREMENT 1;

-- Función para generar número de recibo
CREATE OR REPLACE FUNCTION generate_receipt_number()
RETURNS TRIGGER AS $$
DECLARE
    year_part TEXT;
    sequence_part TEXT;
BEGIN
    IF NEW.receipt_number IS NULL THEN
        year_part := EXTRACT(YEAR FROM NEW.issue_date)::TEXT;
        sequence_part := LPAD(nextval('seq_receipt_number')::TEXT, 6, '0');
        NEW.receipt_number := 'N° ' || sequence_part || ' - ' || year_part;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_generate_receipt_number
    BEFORE INSERT ON receipts
    FOR EACH ROW
    EXECUTE FUNCTION generate_receipt_number();
```

### Ejemplo de Uso

```java
// Al crear un recibo
Receipt receipt = new Receipt();
receipt.setUserId(userId);
receipt.setIssueDate(LocalDateTime.now());
receipt.setTotalAmount(50.00);
// NO setear receipt_number, se genera automáticamente

receiptRepository.save(receipt);
// Resultado: receipt_number = "N° 000001 - 2024"
```

---

## 📝 RESUMEN

**Total de códigos auto-generados**: 1 (solo Receipts)
**Formato**: `N° ###### - YYYY`
**Ventajas**:
- ✅ Simplicidad extrema
- ✅ Solo lo que el usuario ve (recibo impreso)
- ✅ No necesita servicio compartido
- ✅ No necesita contadores en MongoDB
- ✅ UUIDs son perfectos para el resto

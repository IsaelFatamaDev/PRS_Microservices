# 📋 ESTRATEGIA DE CÓDIGOS LEGIBLES EN JASS

> **Pregunta:** ¿Se debería tener un userCode o es decir un código por todas las tablas?
> **Respuesta:** ✅ **SÍ, ABSOLUTAMENTE - Códigos legibles en TODAS las entidades principales**

---

## 🎯 ¿POR QUÉ CÓDIGOS LEGIBLES?

### Problema con solo UUIDs

```
Usuario: "Tengo un problema con mi caja"
Admin: "¿Cuál es tu caja?"
Usuario: "8f3a9b2c-4d5e-6f7g-8h9i-0j1k2l3m4n5o"
Admin: "???" 🤯
```

### Solución con códigos legibles

```
Usuario: "Tengo un problema con mi caja WB-001"
Admin: "Perfecto, lo busco ahora mismo" ✅
```

---

## ✅ CÓDIGOS RECOMENDADOS POR ENTIDAD

### 1. **USERS** (Usuarios)

```sql
CREATE TABLE users.users (
    id UUID PRIMARY KEY,  -- Base de datos interna
    user_code VARCHAR(20) UNIQUE NOT NULL,  -- Para comunicación humana

    -- Formato: JASS-{ORG_CODE}-{SEQUENTIAL}
    -- Ejemplo: JASS-RINC-00001
    -- Ejemplo: JASS-ROMA-00042
);
```

**Ventajas:**

- ✅ Fácil de recordar: "Soy el usuario JASS-RINC-00001"
- ✅ Identifica la organización: RINC = Rinconada
- ✅ Secuencial: Fácil de buscar y ordenar
- ✅ Único a nivel sistema

---

### 2. **WATER BOXES** (Cajas de Agua)

```sql
CREATE TABLE infrastructure.water_boxes (
    id BIGSERIAL PRIMARY KEY,
    box_code VARCHAR(50) UNIQUE NOT NULL,

    -- Formato: WB-{ZONE_CODE}-{SEQUENTIAL}
    -- Ejemplo: WB-RINC-001
    -- Ejemplo: WB-BELLA-025
    -- Ejemplo: WB-ROMA-100
);
```

**Ventajas:**

- ✅ Identifica la zona: RINC = Rinconada, BELLA = Bellavista
- ✅ Fácil para técnicos: "Voy a revisar WB-RINC-001"
- ✅ Ayuda en distribución: "Ruta de cajas WB-RINC-*"

**Alternativa simple (si no se quiere zona):**

```
WB-001, WB-002, WB-003, ...
```

---

### 3. **RECEIPTS** (Recibos)

```sql
CREATE TABLE payments.receipts (
    id UUID PRIMARY KEY,
    receipt_code VARCHAR(50) UNIQUE NOT NULL,

    -- Formato: REC-{YEAR}-{MONTH}-{SEQUENTIAL}
    -- Ejemplo: REC-2026-01-0001
    -- Ejemplo: REC-2026-12-9999
);
```

**Ventajas:**

- ✅ Ordenamiento cronológico natural
- ✅ Fácil identificar período: REC-2026-01 = Enero 2026
- ✅ Auditoría: "Buscar todos los recibos de enero 2026"
- ✅ Correlativo por mes

**Alternativa más simple:**

```
REC-2026-000001
REC-2026-000002
```

---

### 4. **PAYMENTS** (Pagos)

```sql
CREATE TABLE payments.payments (
    id UUID PRIMARY KEY,
    payment_code VARCHAR(50) UNIQUE NOT NULL,

    -- Formato: PAY-{YEAR}-{SEQUENTIAL}
    -- Ejemplo: PAY-2026-000001
    -- Ejemplo: PAY-2026-009999
);
```

**Ventajas:**

- ✅ Referencia en banco: "Operación PAY-2026-000123"
- ✅ Búsqueda rápida: "Verificar pago PAY-2026-000456"
- ✅ Comprobantes: "Comprobante Nro PAY-2026-000789"

---

### 5. **ORGANIZATIONS** (Organizaciones)

```json
{
  "_id": "org_001",
  "organizationCode": "JASS-RINCONADA",  // ✅ Código legible
  "organizationName": "JASS Rinconada - Bellavista"
}

{
  "_id": "org_002",
  "organizationCode": "JASS-ROMA",  // ✅ Código legible
  "organizationName": "JASS Roma"
}
```

**Ventajas:**

- ✅ URLs amigables: `/organizations/JASS-ROMA`
- ✅ Reportes: "JASS-ROMA recaudó S/50,000"
- ✅ Filtros: "Mostrar todas las JASS-*"

---

### 6. **ZONES** (Zonas)

```json
{
  "_id": "zone_001",
  "zoneCode": "RINC",  // ✅ Código corto
  "zoneName": "Rinconada"
}

{
  "_id": "zone_002",
  "zoneCode": "BELLA",  // ✅ Código corto
  "zoneName": "Bellavista de Conta"
}

{
  "_id": "zone_003",
  "zoneCode": "ROMA-C",  // ✅ Con sufijo
  "zoneName": "Roma Centro"
}
```

**Ventajas:**

- ✅ Códigos cortos: Fácil de teclear
- ✅ Composición: WB-RINC-001, WB-BELLA-025
- ✅ Reportes por zona

---

### 7. **STREETS** (Calles)

```json
{
  "_id": "street_001",
  "streetCode": "STR-RINC-001",  // ✅ Con zona
  "streetName": "Jr. Los Jazmines"
}
```

**Ventajas:**

- ✅ Identificación geográfica
- ✅ Útil para distribución

---

### 8. **DISTRIBUTION PROGRAMS** (Programas)

```json
{
  "_id": "prog_001",
  "programCode": "DIST-2026-01",  // ✅ Año-Mes
  "programName": "Distribución Enero 2026"
}
```

---

### 9. **INCIDENTS** (Incidentes)

```json
{
  "_id": "inc_001",
  "incidentCode": "INC-2026-000001",  // ✅ Correlativo
  "incidentType": "FUGA",
  "description": "Fuga en Jr. Los Jazmines"
}
```

**Ventajas:**

- ✅ Ticket de seguimiento: "Incidente INC-2026-000123"
- ✅ Comunicación con usuarios: "Su reclamo INC-2026-000456 fue atendido"

---

### 10. **COMPLAINTS** (Reclamos)

```json
{
  "_id": "comp_001",
  "complaintCode": "CLM-2026-000001",  // ✅ Correlativo
  "complaintType": "CALIDAD_AGUA"
}
```

---

## 📊 TABLA RESUMEN DE CÓDIGOS

| Entidad | Prefijo | Formato | Ejemplo | ¿Necesario? |
|---------|---------|---------|---------|-------------|
| **Users** | JASS | `JASS-{ORG}-{SEQ}` | JASS-RINC-00001 | ✅ **SÍ** |
| **Water Boxes** | WB | `WB-{ZONE}-{SEQ}` | WB-RINC-001 | ✅ **SÍ** |
| **Receipts** | REC | `REC-{YEAR}-{SEQ}` | REC-2026-000001 | ✅ **SÍ** |
| **Payments** | PAY | `PAY-{YEAR}-{SEQ}` | PAY-2026-000001 | ✅ **SÍ** |
| **Organizations** | JASS | `JASS-{NAME}` | JASS-ROMA | ✅ **SÍ** |
| **Zones** | (corto) | `{ABBR}` | RINC, BELLA | ✅ **SÍ** |
| **Streets** | STR | `STR-{ZONE}-{SEQ}` | STR-RINC-001 | ⚠️ Opcional |
| **Programs** | DIST | `DIST-{YEAR}-{MONTH}` | DIST-2026-01 | ⚠️ Opcional |
| **Incidents** | INC | `INC-{YEAR}-{SEQ}` | INC-2026-000001 | ✅ **SÍ** |
| **Complaints** | CLM | `CLM-{YEAR}-{SEQ}` | CLM-2026-000001 | ✅ **SÍ** |
| **Materials** | MAT | `MAT-{CATEGORY}-{SEQ}` | MAT-TUBE-001 | ⚠️ Opcional |
| **Purchases** | PUR | `PUR-{YEAR}-{SEQ}` | PUR-2026-000001 | ⚠️ Opcional |

---

## 🔧 IMPLEMENTACIÓN TÉCNICA

### Opción 1: Contador en Base de Datos

**Para PostgreSQL:**

```sql
-- Tabla de secuencias
CREATE TABLE counters.code_sequences (
    entity_type VARCHAR(50) PRIMARY KEY,
    current_value INTEGER NOT NULL DEFAULT 0,
    prefix VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Función para generar código
CREATE OR REPLACE FUNCTION generate_code(
    p_entity_type VARCHAR,
    p_prefix VARCHAR,
    p_length INTEGER DEFAULT 6
) RETURNS VARCHAR AS $$
DECLARE
    v_next_value INTEGER;
    v_code VARCHAR;
BEGIN
    -- Incrementar y obtener siguiente valor
    UPDATE counters.code_sequences
    SET current_value = current_value + 1,
        updated_at = CURRENT_TIMESTAMP
    WHERE entity_type = p_entity_type
    RETURNING current_value INTO v_next_value;

    -- Si no existe, crear
    IF v_next_value IS NULL THEN
        INSERT INTO counters.code_sequences (entity_type, prefix, current_value)
        VALUES (p_entity_type, p_prefix, 1)
        RETURNING current_value INTO v_next_value;
    END IF;

    -- Formatear código
    v_code := p_prefix || '-' || LPAD(v_next_value::TEXT, p_length, '0');

    RETURN v_code;
END;
$$ LANGUAGE plpgsql;

-- Uso
SELECT generate_code('PAYMENT', 'PAY-2026', 6);
-- Resultado: PAY-2026-000001
```

**Para MongoDB:**

```javascript
// collection: code_counters
{
  "_id": "PAYMENT_2026",
  "sequence": 1,
  "prefix": "PAY-2026"
}

// Service para generar código
async function generatePaymentCode(year) {
  const counter = await db.collection('code_counters').findOneAndUpdate(
    { _id: `PAYMENT_${year}` },
    {
      $inc: { sequence: 1 },
      $set: { prefix: `PAY-${year}` }
    },
    {
      upsert: true,
      returnDocument: 'after'
    }
  );

  const seq = String(counter.sequence).padStart(6, '0');
  return `${counter.prefix}-${seq}`;
}

// Resultado: PAY-2026-000001
```

---

### Opción 2: Código Generado en Aplicación (Java)

```java
@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private final CodeCounterRepository counterRepository;

    public String generateUserCode(String organizationCode) {
        String key = "USER_" + organizationCode;
        Integer nextValue = counterRepository.getNextValue(key);

        return String.format("JASS-%s-%05d",
            organizationCode,
            nextValue
        );
        // Resultado: JASS-RINC-00001
    }

    public String generateReceiptCode(int year) {
        String key = "RECEIPT_" + year;
        Integer nextValue = counterRepository.getNextValue(key);

        return String.format("REC-%d-%06d",
            year,
            nextValue
        );
        // Resultado: REC-2026-000001
    }

    public String generateWaterBoxCode(String zoneCode) {
        String key = "WATERBOX_" + zoneCode;
        Integer nextValue = counterRepository.getNextValue(key);

        return String.format("WB-%s-%03d",
            zoneCode,
            nextValue
        );
        // Resultado: WB-RINC-001
    }
}
```

---

### Opción 3: Redis para Alta Concurrencia

```java
@Service
@RequiredArgsConstructor
public class RedisCodeGeneratorService {

    private final RedisTemplate<String, Integer> redisTemplate;

    public String generatePaymentCode(int year) {
        String key = "payment:counter:" + year;
        Long nextValue = redisTemplate.opsForValue().increment(key);

        // Expira el contador al final del año
        if (nextValue == 1) {
            redisTemplate.expire(key, Duration.ofDays(365));
        }

        return String.format("PAY-%d-%06d", year, nextValue);
    }
}
```

---

## ✅ VENTAJAS GENERALES

### 1. **Comunicación Efectiva**

```
❌ MAL: "El UUID 8f3a9b2c... tiene un problema"
✅ BIEN: "La caja WB-RINC-001 tiene una fuga"
```

### 2. **Soporte Técnico**

```
Usuario: "Mi recibo es REC-2026-000123"
Admin: [busca en 2 segundos] "Encontrado, debes S/8.00"
```

### 3. **Reportes Legibles**

```
Recaudación Enero 2026:
- REC-2026-000001: S/8.00
- REC-2026-000002: S/10.00
- REC-2026-000003: S/20.00
Total: S/38.00
```

### 4. **URLs Amigables**

```
❌ MAL: https://jass.gob.pe/users/8f3a9b2c-4d5e-6f7g
✅ BIEN: https://jass.gob.pe/users/JASS-RINC-00001
```

### 5. **Búsqueda Intuitiva**

```sql
-- Buscar todos los pagos de enero 2026
SELECT * FROM payments
WHERE payment_code LIKE 'PAY-2026-01%';

-- Buscar todas las cajas de Rinconada
SELECT * FROM water_boxes
WHERE box_code LIKE 'WB-RINC%';
```

### 6. **Auditoría**

```
Log: "Usuario JASS-RINC-00001 pagó recibo REC-2026-000123 con PAY-2026-000456"
```

---

## 📋 RECOMENDACIÓN FINAL

### ✅ CÓDIGOS OBLIGATORIOS (Alta Prioridad)

1. **Users**: `JASS-{ORG}-{SEQ}` → JASS-RINC-00001
2. **Water Boxes**: `WB-{ZONE}-{SEQ}` → WB-RINC-001
3. **Receipts**: `REC-{YEAR}-{SEQ}` → REC-2026-000001
4. **Payments**: `PAY-{YEAR}-{SEQ}` → PAY-2026-000001
5. **Organizations**: `JASS-{NAME}` → JASS-ROMA
6. **Zones**: `{ABBR}` → RINC, BELLA
7. **Incidents**: `INC-{YEAR}-{SEQ}` → INC-2026-000001
8. **Complaints**: `CLM-{YEAR}-{SEQ}` → CLM-2026-000001

### ⚠️ CÓDIGOS OPCIONALES (Media Prioridad)

- Streets, Programs, Materials, Purchases

### ❌ NO NECESARIOS (Baja Prioridad)

- Tablas de configuración (parameters, categories)
- Tablas de auditoría (logs, audit_trail)

---

## 🎯 RESPUESTA FINAL

### Pregunta: ¿Se debería tener un código por todas las tablas?

**Respuesta corta:** ✅ **SÍ, en las entidades principales que los usuarios interactúan**

**Respuesta larga:**

1. ✅ **SÍ** para entidades que se comunican (users, boxes, receipts, payments)
2. ✅ **SÍ** para entidades que se buscan frecuentemente (incidents, complaints)
3. ⚠️ **OPCIONAL** para entidades administrativas (streets, programs)
4. ❌ **NO** para tablas técnicas (logs, audits, sessions)

**Regla práctica:**
> Si un usuario o ADMIN va a decir este código en voz alta o escribirlo en un papel → **NECESITA CÓDIGO LEGIBLE**

---

**Documento creado por:** GitHub Copilot AI
**Fecha:** 20 de Enero de 2026
**Versión:** 1.0
**Estado:** Guía de implementación

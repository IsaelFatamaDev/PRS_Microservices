# 02 - SCRIPTS DE BASE DE DATOS COMPLETOS

Este documento contiene **TODOS** los scripts SQL (PostgreSQL) y MongoDB (índices + validaciones) para los 11 microservicios.

---

## 📊 ÍNDICE DE SCRIPTS

### PostgreSQL (Flyway Migrations)
1. **vg-ms-users** - 1 migration
2. **vg-ms-commercial-operations** - 8 migrations
3. **vg-ms-inventory-purchases** - 6 migrations
4. **vg-ms-infrastructure** - 3 migrations

### MongoDB (Índices y Validaciones)
1. **vg-ms-organizations** - 5 collections
2. **vg-ms-water-quality** - 2 collections
3. **vg-ms-distribution** - 3 collections
4. **vg-ms-claims-incidents** - 6 collections

---

# POSTGRESQL SCRIPTS

## 1️⃣ vg-ms-users

### V1__create_users_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V1__create_users_table.sql
-- DATABASE: vg_users (PostgreSQL)
-- DESCRIPTION: Tabla principal de usuarios del sistema
-- ══════════════════════════════════════════════════════════════

-- Crear extensión para UUIDs (si no existe)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla: users
CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(150) NOT NULL,
    document_type           VARCHAR(10) NOT NULL,
    document_number         VARCHAR(20) UNIQUE NOT NULL,
    email                   VARCHAR(255),                    -- NULLABLE
    phone                   VARCHAR(20),                     -- NULLABLE
    address                 TEXT NOT NULL,
    zone_id                 UUID NOT NULL,
    street_id               UUID,
    role                    VARCHAR(20) NOT NULL,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_document_type CHECK (document_type IN ('DNI', 'RUC', 'CE')),
    CONSTRAINT check_role CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'CLIENT')),
    CONSTRAINT check_contact CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

-- Índices de rendimiento
CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_status ON users(record_status);
CREATE INDEX idx_users_document ON users(document_number);
CREATE INDEX idx_users_zone ON users(zone_id);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_users_role ON users(role);

-- Comentarios
COMMENT ON TABLE users IS 'Usuarios del sistema JASS';
COMMENT ON COLUMN users.email IS 'Email del usuario (OPCIONAL para zonas rurales)';
COMMENT ON COLUMN users.phone IS 'Teléfono del usuario (OPCIONAL para zonas rurales)';
COMMENT ON CONSTRAINT check_contact ON users IS 'Al menos email O phone debe estar presente';
```

---

## 4️⃣ vg-ms-commercial-operations

### V1__create_payments_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V1__create_payments_table.sql
-- DATABASE: vg_payments (PostgreSQL)
-- DESCRIPTION: Tabla de pagos realizados
-- ══════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE payments (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    user_id                 UUID NOT NULL,
    payment_date            TIMESTAMP NOT NULL,
    total_amount            NUMERIC(10, 2) NOT NULL,
    payment_method          VARCHAR(20) NOT NULL,
    payment_status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    receipt_number          VARCHAR(50),
    notes                   TEXT,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_payment_method CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'CARD', 'YAPE', 'PLIN')),
    CONSTRAINT check_payment_status CHECK (payment_status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED')),
    CONSTRAINT check_total_amount CHECK (total_amount > 0)
);

-- Índices
CREATE INDEX idx_payments_organization ON payments(organization_id);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_date ON payments(payment_date DESC);
CREATE INDEX idx_payments_created ON payments(created_at DESC);

COMMENT ON TABLE payments IS 'Registro de pagos de usuarios';
```

### V2__create_payment_details_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V2__create_payment_details_table.sql
-- DATABASE: vg_payments (PostgreSQL)
-- DESCRIPTION: Detalles/líneas de cada pago
-- ══════════════════════════════════════════════════════════════

CREATE TABLE payment_details (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id              UUID NOT NULL,
    payment_type            VARCHAR(30) NOT NULL,
    description             TEXT,
    amount                  NUMERIC(10, 2) NOT NULL,
    period_month            INT,
    period_year             INT,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_payment_details_payment FOREIGN KEY (payment_id) 
        REFERENCES payments(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT check_payment_type CHECK (payment_type IN (
        'MONTHLY_FEE', 'INSTALLATION_FEE', 'RECONNECTION_FEE', 
        'LATE_FEE', 'TRANSFER_FEE', 'OTHER'
    )),
    CONSTRAINT check_amount CHECK (amount > 0),
    CONSTRAINT check_period_month CHECK (period_month IS NULL OR (period_month BETWEEN 1 AND 12)),
    CONSTRAINT check_period_year CHECK (period_year IS NULL OR (period_year >= 2020))
);

-- Índices
CREATE INDEX idx_payment_details_payment ON payment_details(payment_id);
CREATE INDEX idx_payment_details_type ON payment_details(payment_type);
CREATE INDEX idx_payment_details_period ON payment_details(period_year, period_month);

COMMENT ON TABLE payment_details IS 'Desglose de conceptos por cada pago';
```

### V3__create_debts_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V3__create_debts_table.sql
-- DATABASE: vg_payments (PostgreSQL)
-- DESCRIPTION: Deudas pendientes de usuarios
-- ══════════════════════════════════════════════════════════════

CREATE TABLE debts (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    user_id                 UUID NOT NULL,
    period_month            INT NOT NULL,
    period_year             INT NOT NULL,
    original_amount         NUMERIC(10, 2) NOT NULL,
    pending_amount          NUMERIC(10, 2) NOT NULL,
    late_fee                NUMERIC(10, 2) DEFAULT 0,
    debt_status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    due_date                TIMESTAMP NOT NULL,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_debt_status CHECK (debt_status IN ('PENDING', 'PARTIAL', 'PAID', 'CANCELLED')),
    CONSTRAINT check_period_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT check_period_year CHECK (period_year >= 2020),
    CONSTRAINT check_amounts CHECK (
        original_amount > 0 AND 
        pending_amount >= 0 AND 
        pending_amount <= original_amount AND
        late_fee >= 0
    )
);

-- Índices
CREATE INDEX idx_debts_organization ON debts(organization_id);
CREATE INDEX idx_debts_user ON debts(user_id);
CREATE INDEX idx_debts_status ON debts(debt_status);
CREATE INDEX idx_debts_period ON debts(period_year DESC, period_month DESC);
CREATE INDEX idx_debts_due_date ON debts(due_date);
CREATE UNIQUE INDEX idx_debts_user_period ON debts(user_id, period_year, period_month) 
    WHERE record_status = 'ACTIVE';

COMMENT ON TABLE debts IS 'Deudas pendientes y mora de usuarios';
```

### V4__create_receipts_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V4__create_receipts_table.sql
-- DATABASE: vg_commercial (PostgreSQL)
-- DESCRIPTION: Recibos/Facturas mensuales
-- ══════════════════════════════════════════════════════════════

CREATE TABLE receipts (\n    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    receipt_number          VARCHAR(50) UNIQUE NOT NULL,     -- N° ###### - YYYY
    user_id                 UUID NOT NULL,
    period_month            INT NOT NULL,
    period_year             INT NOT NULL,
    issue_date              TIMESTAMP NOT NULL,
    due_date                TIMESTAMP NOT NULL,
    total_amount            NUMERIC(10, 2) NOT NULL,
    paid_amount             NUMERIC(10, 2) DEFAULT 0,
    pending_amount          NUMERIC(10, 2) NOT NULL,
    receipt_status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes                   TEXT,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_receipt_status CHECK (receipt_status IN ('PENDING', 'PARTIAL', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT check_period_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT check_period_year CHECK (period_year >= 2020),
    CONSTRAINT check_amounts CHECK (
        total_amount > 0 AND 
        paid_amount >= 0 AND 
        pending_amount >= 0 AND
        paid_amount + pending_amount = total_amount
    )
);

-- ═══════════════════════════════════════════════════════════════
-- SISTEMA DE GENERACIÓN AUTOMÁTICA DE NÚMERO DE RECIBO
-- Formato: N° ###### - YYYY
-- ═══════════════════════════════════════════════════════════════

CREATE SEQUENCE seq_receipt_number START 1 INCREMENT 1;

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

CREATE TRIGGER trg_generate_receipt_number
    BEFORE INSERT ON receipts
    FOR EACH ROW
    EXECUTE FUNCTION generate_receipt_number();

-- Índices
CREATE INDEX idx_receipts_organization ON receipts(organization_id);
CREATE INDEX idx_receipts_user ON receipts(user_id);
CREATE INDEX idx_receipts_status ON receipts(receipt_status);
CREATE INDEX idx_receipts_period ON receipts(period_year DESC, period_month DESC);
CREATE INDEX idx_receipts_due_date ON receipts(due_date);
CREATE UNIQUE INDEX idx_receipts_user_period ON receipts(user_id, period_year, period_month)
    WHERE record_status = 'ACTIVE';

COMMENT ON TABLE receipts IS 'Recibos mensuales de facturación';
COMMENT ON COLUMN receipts.receipt_number IS 'Número de recibo N° ###### - YYYY generado automáticamente';
```

### V5__create_receipt_details_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V5__create_receipt_details_table.sql
-- DATABASE: vg_commercial (PostgreSQL)
-- DESCRIPTION: Detalles/líneas de recibos
-- ══════════════════════════════════════════════════════════════

CREATE TABLE receipt_details (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    receipt_id              UUID NOT NULL,
    concept_type            VARCHAR(30) NOT NULL,
    description             TEXT,
    amount                  NUMERIC(10, 2) NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_receipt_details_receipt FOREIGN KEY (receipt_id)
        REFERENCES receipts(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT check_concept_type CHECK (concept_type IN (
        'MONTHLY_FEE', 'LATE_FEE', 'ASSEMBLY_FINE', 
        'WORK_FINE', 'RECONNECTION_FEE', 'TRANSFER_FEE', 'OTHER'
    )),
    CONSTRAINT check_amount CHECK (amount > 0)
);

-- Índices
CREATE INDEX idx_receipt_details_receipt ON receipt_details(receipt_id);
CREATE INDEX idx_receipt_details_concept ON receipt_details(concept_type);

COMMENT ON TABLE receipt_details IS 'Desglose de conceptos por cada recibo';
```

### V6__create_service_cuts_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V6__create_service_cuts_table.sql
-- DATABASE: vg_commercial (PostgreSQL)
-- DESCRIPTION: Cortes de servicio por morosidad u otras razones
-- ══════════════════════════════════════════════════════════════

CREATE TABLE service_cuts (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    user_id                 UUID NOT NULL,
    water_box_id            UUID NOT NULL,                   -- FK a vg-ms-infrastructure
    cut_date                TIMESTAMP NOT NULL,
    cut_reason              VARCHAR(30) NOT NULL,
    debt_amount             NUMERIC(10, 2) DEFAULT 0,
    reconnection_date       TIMESTAMP,
    reconnection_fee_paid   BOOLEAN DEFAULT FALSE,
    cut_status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes                   TEXT,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_cut_reason CHECK (cut_reason IN ('DEBT', 'VIOLATION', 'MAINTENANCE', 'REQUEST')),
    CONSTRAINT check_cut_status CHECK (cut_status IN ('ACTIVE', 'RECONNECTED', 'CANCELLED')),
    CONSTRAINT check_debt_amount CHECK (debt_amount >= 0)
);

-- Índices
CREATE INDEX idx_cuts_organization ON service_cuts(organization_id);
CREATE INDEX idx_cuts_user ON service_cuts(user_id);
CREATE INDEX idx_cuts_water_box ON service_cuts(water_box_id);
CREATE INDEX idx_cuts_status ON service_cuts(cut_status);
CREATE INDEX idx_cuts_date ON service_cuts(cut_date DESC);
CREATE INDEX idx_cuts_reason ON service_cuts(cut_reason);

COMMENT ON TABLE service_cuts IS 'Registro de cortes de servicio';
```

### V7__create_petty_cash_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V7__create_petty_cash_table.sql
-- DATABASE: vg_commercial (PostgreSQL)
-- DESCRIPTION: Caja chica de la organización
-- ══════════════════════════════════════════════════════════════

CREATE TABLE petty_cash (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    responsible_user_id     UUID NOT NULL,
    current_balance         NUMERIC(10, 2) DEFAULT 0,
    max_amount_limit        NUMERIC(10, 2) NOT NULL,
    petty_cash_status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_petty_cash_status CHECK (petty_cash_status IN ('ACTIVE', 'CLOSED', 'AUDITED')),
    CONSTRAINT check_balance CHECK (current_balance >= 0),
    CONSTRAINT check_limit CHECK (max_amount_limit > 0)
);

-- Índices
CREATE INDEX idx_petty_cash_organization ON petty_cash(organization_id);
CREATE INDEX idx_petty_cash_responsible ON petty_cash(responsible_user_id);
CREATE INDEX idx_petty_cash_status ON petty_cash(petty_cash_status);

COMMENT ON TABLE petty_cash IS 'Caja chica para gastos menores';
```

### V8__create_petty_cash_movements_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V8__create_petty_cash_movements_table.sql
-- DATABASE: vg_commercial (PostgreSQL)
-- DESCRIPTION: Movimientos de caja chica (entradas/salidas)
-- ══════════════════════════════════════════════════════════════

CREATE TABLE petty_cash_movements (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    
    -- Campos específicos
    petty_cash_id           UUID NOT NULL,
    movement_date           TIMESTAMP NOT NULL,
    movement_type           VARCHAR(10) NOT NULL,
    amount                  NUMERIC(10, 2) NOT NULL,
    category                VARCHAR(30) NOT NULL,
    description             TEXT,
    receipt_number          VARCHAR(50),
    previous_balance        NUMERIC(10, 2) NOT NULL,
    new_balance             NUMERIC(10, 2) NOT NULL,
    
    -- Foreign Keys
    CONSTRAINT fk_movements_petty_cash FOREIGN KEY (petty_cash_id)
        REFERENCES petty_cash(id),
    
    -- Constraints
    CONSTRAINT check_movement_type CHECK (movement_type IN ('IN', 'OUT')),
    CONSTRAINT check_category CHECK (category IN ('SUPPLIES', 'TRANSPORT', 'FOOD', 'EMERGENCY', 'OTHER')),
    CONSTRAINT check_amount CHECK (amount > 0),
    CONSTRAINT check_balances CHECK (previous_balance >= 0 AND new_balance >= 0)
);

-- Índices
CREATE INDEX idx_movements_organization ON petty_cash_movements(organization_id);
CREATE INDEX idx_movements_petty_cash ON petty_cash_movements(petty_cash_id);
CREATE INDEX idx_movements_type ON petty_cash_movements(movement_type);
CREATE INDEX idx_movements_category ON petty_cash_movements(category);
CREATE INDEX idx_movements_date ON petty_cash_movements(movement_date DESC);

COMMENT ON TABLE petty_cash_movements IS 'Movimientos de entrada/salida de caja chica';
```

---

## 7️⃣ vg-ms-inventory-purchases

### V1__create_suppliers_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V1__create_suppliers_table.sql
-- DATABASE: vg_inventory (PostgreSQL)
-- DESCRIPTION: Proveedores de materiales
-- ══════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE suppliers (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    supplier_code           VARCHAR(50) UNIQUE NOT NULL,
    supplier_name           VARCHAR(200) NOT NULL,
    ruc                     VARCHAR(11) UNIQUE NOT NULL,
    address                 TEXT,
    phone                   VARCHAR(20),
    email                   VARCHAR(255),
    contact_person          VARCHAR(200),
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_ruc_format CHECK (LENGTH(ruc) = 11 AND ruc ~ '^[0-9]+$')
);

-- Índices
CREATE INDEX idx_suppliers_organization ON suppliers(organization_id);
CREATE INDEX idx_suppliers_status ON suppliers(record_status);
CREATE INDEX idx_suppliers_ruc ON suppliers(ruc);

COMMENT ON TABLE suppliers IS 'Proveedores de materiales e insumos';
```

### V2__create_product_categories_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V2__create_product_categories_table.sql
-- DATABASE: vg_inventory (PostgreSQL)
-- ══════════════════════════════════════════════════════════════

CREATE TABLE product_categories (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    category_code           VARCHAR(50) UNIQUE NOT NULL,
    category_name           VARCHAR(200) NOT NULL,
    description             TEXT,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE'))
);

-- Índices
CREATE INDEX idx_categories_organization ON product_categories(organization_id);
CREATE INDEX idx_categories_status ON product_categories(record_status);

COMMENT ON TABLE product_categories IS 'Categorías de productos/materiales';
```

### V3__create_materials_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V3__create_materials_table.sql
-- DATABASE: vg_inventory (PostgreSQL)
-- ══════════════════════════════════════════════════════════════

CREATE TABLE materials (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    material_code           VARCHAR(50) UNIQUE NOT NULL,
    material_name           VARCHAR(200) NOT NULL,
    category_id             UUID NOT NULL,
    unit                    VARCHAR(20) NOT NULL,
    current_stock           NUMERIC(10, 2) DEFAULT 0,
    min_stock               NUMERIC(10, 2) DEFAULT 0,
    unit_price              NUMERIC(10, 2) DEFAULT 0,
    
    -- Foreign Keys
    CONSTRAINT fk_materials_category FOREIGN KEY (category_id) 
        REFERENCES product_categories(id),
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_unit CHECK (unit IN ('UNIT', 'METERS', 'KG', 'LITERS')),
    CONSTRAINT check_stock CHECK (current_stock >= 0 AND min_stock >= 0),
    CONSTRAINT check_price CHECK (unit_price >= 0)
);

-- Índices
CREATE INDEX idx_materials_organization ON materials(organization_id);
CREATE INDEX idx_materials_status ON materials(record_status);
CREATE INDEX idx_materials_category ON materials(category_id);
CREATE INDEX idx_materials_stock_alert ON materials(current_stock) WHERE current_stock < min_stock;

COMMENT ON TABLE materials IS 'Catálogo de materiales e insumos';
```

### V4__create_purchases_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V4__create_purchases_table.sql
-- DATABASE: vg_inventory (PostgreSQL)
-- ══════════════════════════════════════════════════════════════

CREATE TABLE purchases (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    purchase_code           VARCHAR(50) UNIQUE NOT NULL,
    supplier_id             UUID NOT NULL,
    purchase_date           TIMESTAMP NOT NULL,
    total_amount            NUMERIC(10, 2) NOT NULL,
    purchase_status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invoice_number          VARCHAR(50),
    
    -- Foreign Keys
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) 
        REFERENCES suppliers(id),
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_purchase_status CHECK (purchase_status IN ('PENDING', 'RECEIVED', 'CANCELLED')),
    CONSTRAINT check_total_amount CHECK (total_amount > 0)
);

-- Índices
CREATE INDEX idx_purchases_organization ON purchases(organization_id);
CREATE INDEX idx_purchases_supplier ON purchases(supplier_id);
CREATE INDEX idx_purchases_status ON purchases(purchase_status);
CREATE INDEX idx_purchases_date ON purchases(purchase_date DESC);

COMMENT ON TABLE purchases IS 'Órdenes de compra a proveedores';
```

### V5__create_purchase_details_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V5__create_purchase_details_table.sql
-- DATABASE: vg_inventory (PostgreSQL)
-- ══════════════════════════════════════════════════════════════

CREATE TABLE purchase_details (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_id             UUID NOT NULL,
    material_id             UUID NOT NULL,
    quantity                NUMERIC(10, 2) NOT NULL,
    unit_price              NUMERIC(10, 2) NOT NULL,
    subtotal                NUMERIC(10, 2) NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_purchase_details_purchase FOREIGN KEY (purchase_id) 
        REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_details_material FOREIGN KEY (material_id) 
        REFERENCES materials(id),
    
    -- Constraints
    CONSTRAINT check_quantity CHECK (quantity > 0),
    CONSTRAINT check_unit_price CHECK (unit_price >= 0),
    CONSTRAINT check_subtotal CHECK (subtotal >= 0)
);

-- Índices
CREATE INDEX idx_purchase_details_purchase ON purchase_details(purchase_id);
CREATE INDEX idx_purchase_details_material ON purchase_details(material_id);

COMMENT ON TABLE purchase_details IS 'Líneas de detalle de cada orden de compra';
```

### V6__create_inventory_movements_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V6__create_inventory_movements_table.sql
-- DATABASE: vg_inventory (PostgreSQL)
-- DESCRIPTION: Kardex - Movimientos de inventario
-- ══════════════════════════════════════════════════════════════

CREATE TABLE inventory_movements (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    
    -- Campos específicos (Kardex)
    material_id             UUID NOT NULL,
    movement_type           VARCHAR(20) NOT NULL,
    quantity                NUMERIC(10, 2) NOT NULL,
    unit_price              NUMERIC(10, 2) DEFAULT 0,
    previous_stock          NUMERIC(10, 2) NOT NULL,
    new_stock               NUMERIC(10, 2) NOT NULL,
    reference_id            UUID,
    reference_type          VARCHAR(50),
    notes                   TEXT,
    
    -- Foreign Keys
    CONSTRAINT fk_movements_material FOREIGN KEY (material_id) 
        REFERENCES materials(id),
    
    -- Constraints
    CONSTRAINT check_movement_type CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT')),
    CONSTRAINT check_quantity CHECK (quantity != 0),
    CONSTRAINT check_stocks CHECK (previous_stock >= 0 AND new_stock >= 0)
);

-- Índices
CREATE INDEX idx_movements_organization ON inventory_movements(organization_id);
CREATE INDEX idx_movements_material ON inventory_movements(material_id);
CREATE INDEX idx_movements_type ON inventory_movements(movement_type);
CREATE INDEX idx_movements_date ON inventory_movements(created_at DESC);
CREATE INDEX idx_movements_reference ON inventory_movements(reference_type, reference_id);

COMMENT ON TABLE inventory_movements IS 'Kardex - Movimientos de entrada/salida/ajuste de inventario';
```

---

## 9️⃣ vg-ms-infrastructure

### V1__create_water_boxes_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V1__create_water_boxes_table.sql
-- DATABASE: vg_infrastructure (PostgreSQL)
-- DESCRIPTION: Cajas de agua
-- ══════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE water_boxes (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    updated_at              TIMESTAMP,
    updated_by              UUID,
    
    -- Campos específicos
    box_code                VARCHAR(50) UNIQUE NOT NULL,
    box_type                VARCHAR(30) NOT NULL,
    installation_date       TIMESTAMP NOT NULL,
    zone_id                 UUID NOT NULL,
    street_id               UUID,
    address                 TEXT NOT NULL,
    current_assignment_id   UUID,
    is_active               BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_box_type CHECK (box_type IN ('RESIDENTIAL', 'COMMERCIAL', 'COMMUNAL', 'INSTITUTIONAL'))
);

-- Índices
CREATE INDEX idx_water_boxes_organization ON water_boxes(organization_id);
CREATE INDEX idx_water_boxes_status ON water_boxes(record_status);
CREATE INDEX idx_water_boxes_zone ON water_boxes(zone_id);
CREATE INDEX idx_water_boxes_active ON water_boxes(is_active);
CREATE INDEX idx_water_boxes_assignment ON water_boxes(current_assignment_id);

COMMENT ON TABLE water_boxes IS 'Cajas de agua instaladas';
COMMENT ON COLUMN water_boxes.is_active IS 'Indica si está activo (con agua) o cortado';
```

### V2__create_water_box_assignments_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V2__create_water_box_assignments_table.sql
-- DATABASE: vg_infrastructure (PostgreSQL)
-- DESCRIPTION: Asignaciones de cajas de agua a usuarios
-- ══════════════════════════════════════════════════════════════

CREATE TABLE water_box_assignments (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    record_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    
    -- Campos específicos
    water_box_id            UUID NOT NULL,
    user_id                 UUID NOT NULL,
    assignment_date         TIMESTAMP NOT NULL,
    assignment_status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    end_date                TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_assignments_water_box FOREIGN KEY (water_box_id) 
        REFERENCES water_boxes(id),
    
    -- Constraints
    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT check_assignment_status CHECK (assignment_status IN ('ACTIVE', 'INACTIVE', 'TRANSFERRED'))
);

-- Índices
CREATE INDEX idx_assignments_organization ON water_box_assignments(organization_id);
CREATE INDEX idx_assignments_water_box ON water_box_assignments(water_box_id);
CREATE INDEX idx_assignments_user ON water_box_assignments(user_id);
CREATE INDEX idx_assignments_status ON water_box_assignments(assignment_status);
CREATE INDEX idx_assignments_date ON water_box_assignments(assignment_date DESC);

COMMENT ON TABLE water_box_assignments IS 'Historial de asignaciones de cajas a usuarios';
```

### V3__create_water_box_transfers_table.sql

```sql
-- ══════════════════════════════════════════════════════════════
-- MIGRATION: V3__create_water_box_transfers_table.sql
-- DATABASE: vg_infrastructure (PostgreSQL)
-- DESCRIPTION: Transferencias de cajas entre usuarios
-- ══════════════════════════════════════════════════════════════

CREATE TABLE water_box_transfers (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID,
    
    -- Campos específicos
    water_box_id            UUID NOT NULL,
    from_user_id            UUID NOT NULL,
    to_user_id              UUID NOT NULL,
    transfer_date           TIMESTAMP NOT NULL,
    transfer_fee            NUMERIC(10, 2) DEFAULT 0,
    notes                   TEXT,
    
    -- Foreign Keys
    CONSTRAINT fk_transfers_water_box FOREIGN KEY (water_box_id) 
        REFERENCES water_boxes(id),
    
    -- Constraints
    CONSTRAINT check_different_users CHECK (from_user_id != to_user_id),
    CONSTRAINT check_transfer_fee CHECK (transfer_fee >= 0)
);

-- Índices
CREATE INDEX idx_transfers_organization ON water_box_transfers(organization_id);
CREATE INDEX idx_transfers_water_box ON water_box_transfers(water_box_id);
CREATE INDEX idx_transfers_from_user ON water_box_transfers(from_user_id);
CREATE INDEX idx_transfers_to_user ON water_box_transfers(to_user_id);
CREATE INDEX idx_transfers_date ON water_box_transfers(transfer_date DESC);

COMMENT ON TABLE water_box_transfers IS 'Historial de transferencias de cajas entre usuarios';
```

---

# MONGODB SCRIPTS

## 3️⃣ vg-ms-organizations

### organizations_indexes.js

```javascript
// ══════════════════════════════════════════════════════════════
// MONGODB INDEXES: vg-ms-organizations
// DATABASE: JASS_DIGITAL
// ══════════════════════════════════════════════════════════════

use JASS_DIGITAL;

// ────────────────────────────────────────────────────────────
// COLLECTION: organizations
// ────────────────────────────────────────────────────────────
db.organizations.createIndex(
    { "organization_code": 1 }, 
    { unique: true, name: "idx_organizations_code" }
);

db.organizations.createIndex(
    { "record_status": 1, "created_at": -1 }, 
    { name: "idx_organizations_status_date" }
);

db.organizations.createIndex(
    { "department": 1, "province": 1, "district": 1 }, 
    { name: "idx_organizations_location" }
);

// Validación de schema
db.runCommand({
    collMod: "organizations",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_name", "record_status"],
            properties: {
                organization_name: { bsonType: "string" },
                record_status: { enum: ["ACTIVE", "INACTIVE"] },
                created_at: { bsonType: "date" }
            }
        }
    },
    validationLevel: "moderate"
});

// ────────────────────────────────────────────────────────────
// COLLECTION: zones
// ────────────────────────────────────────────────────────────
db.zones.createIndex(
    { "zone_code": 1 }, 
    { unique: true, name: "idx_zones_code" }
);

db.zones.createIndex(
    { "organization_id": 1, "record_status": 1 }, 
    { name: "idx_zones_org_status" }
);

db.zones.createIndex(
    { "zone_name": "text" }, 
    { name: "idx_zones_search" }
);

// Validación
db.runCommand({
    collMod: "zones",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "zone_code", "zone_name", "record_status"],
            properties: {
                organization_id: { bsonType: "string" },
                zone_code: { bsonType: "string" },
                zone_name: { bsonType: "string" },
                record_status: { enum: ["ACTIVE", "INACTIVE"] }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: streets
// ────────────────────────────────────────────────────────────
db.streets.createIndex(
    { "street_code": 1 }, 
    { unique: true, name: "idx_streets_code" }
);

db.streets.createIndex(
    { "organization_id": 1, "zone_id": 1, "record_status": 1 }, 
    { name: "idx_streets_org_zone_status" }
);

db.streets.createIndex(
    { "street_type": 1, "street_name": 1 }, 
    { name: "idx_streets_type_name" }
);

// Validación
db.runCommand({
    collMod: "streets",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "zone_id", "street_code", "street_type", "street_name", "record_status"],
            properties: {
                street_type: { enum: ["JR", "AV", "CALLE", "PASAJE"] },
                record_status: { enum: ["ACTIVE", "INACTIVE"] }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: fares
// ────────────────────────────────────────────────────────────
db.fares.createIndex(
    { "organization_id": 1, "fare_type": 1, "record_status": 1 }, 
    { name: "idx_fares_org_type_status" }
);

db.fares.createIndex(
    { "valid_from": 1, "valid_to": 1 }, 
    { name: "idx_fares_validity" }
);

// Validación
db.runCommand({
    collMod: "fares",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "fare_type", "amount", "valid_from"],
            properties: {
                fare_type: { 
                    enum: ["MONTHLY_FEE", "INSTALLATION_FEE", "RECONNECTION_FEE", "LATE_FEE", "TRANSFER_FEE"] 
                },
                amount: { bsonType: "double", minimum: 0 }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: parameters
// ────────────────────────────────────────────────────────────
db.parameters.createIndex(
    { "organization_id": 1, "parameter_type": 1 }, 
    { unique: true, name: "idx_parameters_org_type" }
);

db.parameters.createIndex(
    { "record_status": 1 }, 
    { name: "idx_parameters_status" }
);

// Validación
db.runCommand({
    collMod: "parameters",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "parameter_type", "parameter_value"],
            properties: {
                parameter_type: { 
                    enum: ["BILLING_DAY", "GRACE_PERIOD", "LATE_INTEREST_RATE", "MAX_DEBT_AMOUNT"] 
                }
            }
        }
    }
});

print("✅ Índices y validaciones creados para vg-ms-organizations");
```

---

## 5️⃣ vg-ms-water-quality

### water_quality_indexes.js

```javascript
// ══════════════════════════════════════════════════════════════
// MONGODB INDEXES: vg-ms-water-quality
// DATABASE: JASS_DIGITAL
// ══════════════════════════════════════════════════════════════

use JASS_DIGITAL;

// ────────────────────────────────────────────────────────────
// COLLECTION: testing_points
// ────────────────────────────────────────────────────────────
db.testing_points.createIndex(
    { "point_code": 1 }, 
    { unique: true, name: "idx_points_code" }
);

db.testing_points.createIndex(
    { "organization_id": 1, "record_status": 1 }, 
    { name: "idx_points_org_status" }
);

db.testing_points.createIndex(
    { "point_type": 1 }, 
    { name: "idx_points_type" }
);

db.testing_points.createIndex(
    { "zone_id": 1 }, 
    { name: "idx_points_zone" }
);

db.testing_points.createIndex(
    { "location": "2dsphere" }, 
    { name: "idx_points_geolocation" }
);

// Validación
db.runCommand({
    collMod: "testing_points",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "point_code", "point_name", "point_type"],
            properties: {
                point_type: { enum: ["RESERVOIR", "TAP", "WELL", "SOURCE"] },
                latitude: { bsonType: ["double", "null"], minimum: -90, maximum: 90 },
                longitude: { bsonType: ["double", "null"], minimum: -180, maximum: 180 }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: quality_tests
// ────────────────────────────────────────────────────────────
db.quality_tests.createIndex(
    { "test_code": 1 }, 
    { unique: true, name: "idx_tests_code" }
);

db.quality_tests.createIndex(
    { "organization_id": 1, "test_date": -1 }, 
    { name: "idx_tests_org_date" }
);

db.quality_tests.createIndex(
    { "testing_point_id": 1, "test_date": -1 }, 
    { name: "idx_tests_point_date" }
);

db.quality_tests.createIndex(
    { "test_type": 1, "test_result": 1 }, 
    { name: "idx_tests_type_result" }
);

db.quality_tests.createIndex(
    { "tested_by_user_id": 1 }, 
    { name: "idx_tests_operator" }
);

db.quality_tests.createIndex(
    { "test_result": 1 }, 
    { name: "idx_tests_result" }
);

// Validación
db.runCommand({
    collMod: "quality_tests",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "test_code", "testing_point_id", "test_date", "test_type", "test_result"],
            properties: {
                test_type: { enum: ["CHLORINE", "PH", "TURBIDITY", "BACTERIOLOGICAL", "CHEMICAL"] },
                test_result: { enum: ["APPROVED", "REJECTED", "REQUIRES_TREATMENT"] },
                chlorine_level: { bsonType: ["double", "null"], minimum: 0 },
                ph_level: { bsonType: ["double", "null"], minimum: 0, maximum: 14 },
                turbidity_level: { bsonType: ["double", "null"], minimum: 0 }
            }
        }
    }
});

print("✅ Índices y validaciones creados para vg-ms-water-quality");
```

---

## 6️⃣ vg-ms-distribution

### distribution_indexes.js

```javascript
// ══════════════════════════════════════════════════════════════
// MONGODB INDEXES: vg-ms-distribution
// DATABASE: JASS_DIGITAL
// ══════════════════════════════════════════════════════════════

use JASS_DIGITAL;

// ────────────────────────────────────────────────────────────
// COLLECTION: distribution_programs
// ────────────────────────────────────────────────────────────
db.distribution_programs.createIndex(
    { "program_code": 1 }, 
    { unique: true, name: "idx_programs_code" }
);

db.distribution_programs.createIndex(
    { "organization_id": 1, "distribution_status": 1 }, 
    { name: "idx_programs_org_status" }
);

db.distribution_programs.createIndex(
    { "start_date": 1, "end_date": 1 }, 
    { name: "idx_programs_dates" }
);

// Validación
db.runCommand({
    collMod: "distribution_programs",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "program_code", "program_name", "distribution_status", "start_date"],
            properties: {
                distribution_status: { enum: ["ACTIVE", "INACTIVE", "SUSPENDED"] }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: distribution_routes
// ────────────────────────────────────────────────────────────
db.distribution_routes.createIndex(
    { "route_code": 1 }, 
    { unique: true, name: "idx_routes_code" }
);

db.distribution_routes.createIndex(
    { "organization_id": 1, "distribution_status": 1 }, 
    { name: "idx_routes_org_status" }
);

db.distribution_routes.createIndex(
    { "zone_ids": 1 }, 
    { name: "idx_routes_zones" }
);

// Validación
db.runCommand({
    collMod: "distribution_routes",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "route_code", "route_name", "distribution_status"],
            properties: {
                zone_ids: { bsonType: "array" }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: distribution_schedules
// ────────────────────────────────────────────────────────────
db.distribution_schedules.createIndex(
    { "route_id": 1, "day_of_week": 1 }, 
    { name: "idx_schedules_route_day" }
);

db.distribution_schedules.createIndex(
    { "organization_id": 1, "record_status": 1 }, 
    { name: "idx_schedules_org_status" }
);

db.distribution_schedules.createIndex(
    { "day_of_week": 1 }, 
    { name: "idx_schedules_day" }
);

// Validación
db.runCommand({
    collMod: "distribution_schedules",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "route_id", "day_of_week", "start_time", "end_time"],
            properties: {
                day_of_week: { 
                    enum: ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"] 
                }
            }
        }
    }
});

print("✅ Índices y validaciones creados para vg-ms-distribution");
```

---

## 8️⃣ vg-ms-claims-incidents

### claims_incidents_indexes.js

```javascript
// ══════════════════════════════════════════════════════════════
// MONGODB INDEXES: vg-ms-claims-incidents
// DATABASE: JASS_DIGITAL
// ══════════════════════════════════════════════════════════════

use JASS_DIGITAL;

// ────────────────────────────────────────────────────────────
// COLLECTION: complaints
// ────────────────────────────────────────────────────────────
db.complaints.createIndex(
    { "complaint_code": 1 }, 
    { unique: true, name: "idx_complaints_code" }
);

db.complaints.createIndex(
    { "organization_id": 1, "complaint_status": 1, "complaint_date": -1 }, 
    { name: "idx_complaints_org_status_date" }
);

db.complaints.createIndex(
    { "user_id": 1, "complaint_date": -1 }, 
    { name: "idx_complaints_user" }
);

db.complaints.createIndex(
    { "assigned_to_user_id": 1, "complaint_status": 1 }, 
    { name: "idx_complaints_assigned" }
);

db.complaints.createIndex(
    { "complaint_priority": 1, "complaint_status": 1 }, 
    { name: "idx_complaints_priority_status" }
);

db.complaints.createIndex(
    { "category_id": 1 }, 
    { name: "idx_complaints_category" }
);

// Validación
db.runCommand({
    collMod: "complaints",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "complaint_code", "user_id", "complaint_date", "complaint_priority", "complaint_status"],
            properties: {
                complaint_priority: { enum: ["LOW", "MEDIUM", "HIGH", "URGENT"] },
                complaint_status: { enum: ["RECEIVED", "IN_PROGRESS", "RESOLVED", "CLOSED"] }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: complaint_categories
// ────────────────────────────────────────────────────────────
db.complaint_categories.createIndex(
    { "category_code": 1 }, 
    { unique: true, name: "idx_complaint_categories_code" }
);

db.complaint_categories.createIndex(
    { "organization_id": 1, "record_status": 1 }, 
    { name: "idx_complaint_categories_org_status" }
);

// ────────────────────────────────────────────────────────────
// COLLECTION: complaint_responses
// ────────────────────────────────────────────────────────────
db.complaint_responses.createIndex(
    { "complaint_id": 1, "response_date": -1 }, 
    { name: "idx_responses_complaint_date" }
);

db.complaint_responses.createIndex(
    { "responded_by_user_id": 1 }, 
    { name: "idx_responses_user" }
);

db.complaint_responses.createIndex(
    { "response_type": 1 }, 
    { name: "idx_responses_type" }
);

// Validación
db.runCommand({
    collMod: "complaint_responses",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["complaint_id", "response_date", "response_type", "responded_by_user_id"],
            properties: {
                response_type: { enum: ["INVESTIGACION", "SOLUCION", "SEGUIMIENTO"] }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: incidents
// ────────────────────────────────────────────────────────────
db.incidents.createIndex(
    { "incident_code": 1 }, 
    { unique: true, name: "idx_incidents_code" }
);

db.incidents.createIndex(
    { "organization_id": 1, "incident_status": 1, "incident_date": -1 }, 
    { name: "idx_incidents_org_status_date" }
);

db.incidents.createIndex(
    { "zone_id": 1, "street_id": 1 }, 
    { name: "idx_incidents_location" }
);

db.incidents.createIndex(
    { "assigned_to_user_id": 1, "incident_status": 1 }, 
    { name: "idx_incidents_assigned" }
);

db.incidents.createIndex(
    { "incident_severity": 1, "incident_status": 1 }, 
    { name: "idx_incidents_severity_status" }
);

db.incidents.createIndex(
    { "incident_type_id": 1 }, 
    { name: "idx_incidents_type" }
);

// Validación
db.runCommand({
    collMod: "incidents",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["organization_id", "incident_code", "incident_date", "incident_severity", "incident_status"],
            properties: {
                incident_severity: { enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL"] },
                incident_status: { enum: ["REPORTED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"] }
            }
        }
    }
});

// ────────────────────────────────────────────────────────────
// COLLECTION: incident_types
// ────────────────────────────────────────────────────────────
db.incident_types.createIndex(
    { "type_code": 1 }, 
    { unique: true, name: "idx_incident_types_code" }
);

db.incident_types.createIndex(
    { "organization_id": 1, "record_status": 1 }, 
    { name: "idx_incident_types_org_status" }
);

// ────────────────────────────────────────────────────────────
// COLLECTION: incident_resolutions
// ────────────────────────────────────────────────────────────
db.incident_resolutions.createIndex(
    { "incident_id": 1 }, 
    { unique: true, name: "idx_resolutions_incident" }
);

db.incident_resolutions.createIndex(
    { "resolved_by_user_id": 1, "resolution_date": -1 }, 
    { name: "idx_resolutions_user_date" }
);

db.incident_resolutions.createIndex(
    { "resolution_type": 1 }, 
    { name: "idx_resolutions_type" }
);

// Validación
db.runCommand({
    collMod: "incident_resolutions",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["incident_id", "resolution_date", "resolution_type", "resolved_by_user_id"],
            properties: {
                resolution_type: { enum: ["REPARACION_TEMPORAL", "REPARACION_COMPLETA", "REEMPLAZO"] },
                total_cost: { bsonType: ["double", "null"], minimum: 0 }
            }
        }
    }
});

print("✅ Índices y validaciones creados para vg-ms-claims-incidents");
```

---

## ✅ RESUMEN DE SCRIPTS

### PostgreSQL (Flyway Migrations)
- **vg-ms-users**: 1 migration
- **vg-ms-payments-billing**: 3 migrations
- **vg-ms-inventory-purchases**: 6 migrations
- **vg-ms-infrastructure**: 3 migrations
- **TOTAL**: 13 migrations SQL

### MongoDB (Índices y Validaciones)
- **vg-ms-organizations**: 5 collections
- **vg-ms-water-quality**: 2 collections
- **vg-ms-distribution**: 3 collections
- **vg-ms-claims-incidents**: 6 collections
- **TOTAL**: 16 collections con índices y validaciones

---

## 🚀 EJECUCIÓN DE SCRIPTS

### PostgreSQL (Automático con Flyway)
Los scripts se ejecutan automáticamente al iniciar cada microservicio si Flyway está configurado.

### MongoDB (Manual)
```bash
# Conectar a MongoDB
mongosh "mongodb+srv://sistemajass:password@cluster.mongodb.net/JASS_DIGITAL"

# Ejecutar scripts
load("/path/to/organizations_indexes.js")
load("/path/to/water_quality_indexes.js")
load("/path/to/distribution_indexes.js")
load("/path/to/claims_incidents_indexes.js")
```

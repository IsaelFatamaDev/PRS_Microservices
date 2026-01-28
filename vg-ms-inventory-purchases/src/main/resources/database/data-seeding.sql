-- =====================================================
-- SCRIPT DE INSERCIÓN DE DATOS INICIAL (SIMPLIFICADO PARA R2DBC)
-- Microservicio: Inventario y Compras
-- Organización: 6896b2ecf3e398570ffd99d3
-- Propósito: Datos de prueba con códigos pre-definidos
-- =====================================================

-- Habilitar extensión UUID si no está activada
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- LIMPIEZA DE DATOS EXISTENTES SOLO PARA LA ORGANIZACIÓN
-- =====================================================

-- Eliminar datos existentes solo para la organización específica

DELETE FROM inventory_movements WHERE organization_id = '6896b2ecf3e398570ffd99d3';
DELETE FROM purchase_details WHERE purchase_id IN (SELECT purchase_id FROM purchases WHERE organization_id = '6896b2ecf3e398570ffd99d3');
DELETE FROM purchases WHERE organization_id = '6896b2ecf3e398570ffd99d3';
DELETE FROM products WHERE organization_id = '6896b2ecf3e398570ffd99d3';
DELETE FROM suppliers WHERE organization_id = '6896b2ecf3e398570ffd99d3';
DELETE FROM product_categories WHERE organization_id = '6896b2ecf3e398570ffd99d3';
-- =====================================================
-- INSERCIÓN DE CATEGORÍAS DE PRODUCTOS
-- =====================================================

INSERT INTO product_categories (category_id, organization_id, category_code, category_name, description, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT001', 'Tubería y Conexiones PVC', 'Tuberías de PVC, codos, tees, reducciones y accesorios para redes de agua potable', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT002', 'Equipos de Bombeo y Presión', 'Bombas centrífugas, electrobombas, tanques hidroneumáticos y accesorios', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT003', 'Válvulas y Control de Flujo', 'Válvulas de compuerta, globo, check, reductoras de presión y accesorios', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT004', 'Químicos para Tratamiento', 'Cloro, sulfato de aluminio, cal, floculantes y desinfectantes', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT005', 'Medición y Micromedición', 'Medidores de agua, cajas de medidor, válvulas de paso y accesorios', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT006', 'Herramientas y Equipos', 'Herramientas para excavación, reparación e instalación de redes', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT007', 'Materiales de Construcción', 'Cemento, arena, grava, ladrillo y materiales para obras civiles', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'CAT008', 'Equipos de Seguridad', 'Cascos, chalecos, botas, guantes y equipos de protección personal', 'ACTIVO', CURRENT_TIMESTAMP);

-- =====================================================
-- INSERCIÓN DE PROVEEDORES
-- =====================================================

INSERT INTO suppliers (supplier_id, organization_id, supplier_code, supplier_name, contact_person, phone, email, address, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV001', 'Distribuidora Hidráulica del Centro SAC', 'Carlos Alberto Mendoza Rivera', '+51987654321', 'ventas@hidrocentro.pe', 'Av. Real 1425, Huancayo, Junín', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV002', 'Tubos y Sistemas Andinos EIRL', 'María Elena Rodríguez Quispe', '+51945123789', 'pedidos@tubosandinos.com', 'Jr. Huancavelica 567, Huancayo, Junín', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV003', 'Bombas y Equipos Industriales Junín', 'Roberto Silva Vargas', '+51912345678', 'contacto@bombasjunin.pe', 'Av. Ferrocarril 890, Huancayo, Junín', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV004', 'Química Andina para Tratamiento SAC', 'Ana Patricia González Flores', '+51923456789', 'ventas@quimicaandina.pe', 'Calle Real 234, El Tambo, Huancayo', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV005', 'Medidores y Válvulas del Centro', 'José Luis Morales Huamán', '+51934567890', 'info@medidorescentro.com', 'Av. Giráldez 456, Huancayo, Junín', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV006', 'Ferretería Saneamiento Rural Valle', 'Laura Beatriz Quispe Mamani', '+51956789012', 'ventas@ferreteriarural.pe', 'Jr. Ayacucho 789, Chilca, Huancayo', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV007', 'Constructora y Materiales Huanca', 'Pedro Antonio Vargas Sullca', '+51967890123', 'materiales@huancaconst.com', 'Av. Mantaro 321, Huancayo, Junín', 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROV008', 'Seguridad Industrial Andes', 'Carmen Rosa Huayllasco Torres', '+51978901234', 'seguridadandes@gmail.com', 'Jr. Puno 147, Huancayo, Junín', 'ACTIVO', CURRENT_TIMESTAMP);

-- =====================================================
-- INSERCIÓN DE PRODUCTOS
-- =====================================================

-- Productos para Tubería y Conexiones PVC
INSERT INTO products (product_id, organization_id, product_code, product_name, category_id, unit_of_measure, minimum_stock, maximum_stock, current_stock, unit_cost, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD001', 'Tubería PVC SAP 4" x 6m Clase 10', (SELECT category_id FROM product_categories WHERE category_code = 'CAT001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 20, 200, 45, 92.50, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD002', 'Tubería PVC SAP 2" x 6m Clase 10', (SELECT category_id FROM product_categories WHERE category_code = 'CAT001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 30, 300, 80, 48.75, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD003', 'Codo PVC SAP 4" x 90°', (SELECT category_id FROM product_categories WHERE category_code = 'CAT001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 50, 500, 120, 14.25, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD004', 'Tee PVC SAP 4"', (SELECT category_id FROM product_categories WHERE category_code = 'CAT001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 25, 150, 60, 18.90, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD005', 'Reducción PVC SAP 4" a 2"', (SELECT category_id FROM product_categories WHERE category_code = 'CAT001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 15, 100, 35, 22.50, 'ACTIVO', CURRENT_TIMESTAMP);

-- Productos para Equipos de Bombeo y Presión
INSERT INTO products (product_id, organization_id, product_code, product_name, category_id, unit_of_measure, minimum_stock, maximum_stock, current_stock, unit_cost, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD006', 'Electrobomba Centrífuga 2HP Monofásica', (SELECT category_id FROM product_categories WHERE category_code = 'CAT002' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 2, 10, 3, 950.00, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD007', 'Tanque Hidroneumático 80L', (SELECT category_id FROM product_categories WHERE category_code = 'CAT002' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 1, 5, 2, 420.00, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD008', 'Presostato para Bomba', (SELECT category_id FROM product_categories WHERE category_code = 'CAT002' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 5, 25, 8, 85.00, 'ACTIVO', CURRENT_TIMESTAMP);

-- Productos para Válvulas y Control de Flujo
INSERT INTO products (product_id, organization_id, product_code, product_name, category_id, unit_of_measure, minimum_stock, maximum_stock, current_stock, unit_cost, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD009', 'Válvula de Compuerta 4" Bronce', (SELECT category_id FROM product_categories WHERE category_code = 'CAT003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 10, 50, 15, 135.00, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD010', 'Válvula Check 2" Bronce', (SELECT category_id FROM product_categories WHERE category_code = 'CAT003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 8, 40, 12, 75.00, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD011', 'Válvula Reductora de Presión 2"', (SELECT category_id FROM product_categories WHERE category_code = 'CAT003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 3, 15, 5, 280.00, 'ACTIVO', CURRENT_TIMESTAMP);

-- Productos para Químicos de Tratamiento
INSERT INTO products (product_id, organization_id, product_code, product_name, category_id, unit_of_measure, minimum_stock, maximum_stock, current_stock, unit_cost, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD012', 'Cloro Granulado HTH 45kg', (SELECT category_id FROM product_categories WHERE category_code = 'CAT004' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'KG', 100, 1000, 275, 3.45, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD013', 'Sulfato de Aluminio 25kg', (SELECT category_id FROM product_categories WHERE category_code = 'CAT004' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'KG', 50, 500, 150, 2.80, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD014', 'Cal Hidratada 25kg', (SELECT category_id FROM product_categories WHERE category_code = 'CAT004' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'KG', 40, 400, 120, 1.95, 'ACTIVO', CURRENT_TIMESTAMP);

-- Productos para Medición y Micromedición
INSERT INTO products (product_id, organization_id, product_code, product_name, category_id, unit_of_measure, minimum_stock, maximum_stock, current_stock, unit_cost, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD015', 'Medidor de Agua 1/2" Clase B', (SELECT category_id FROM product_categories WHERE category_code = 'CAT005' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 25, 100, 35, 68.50, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD016', 'Caja de Medidor de Concreto', (SELECT category_id FROM product_categories WHERE category_code = 'CAT005' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 20, 80, 25, 45.00, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD017', 'Llave de Paso 1/2" Bronce', (SELECT category_id FROM product_categories WHERE category_code = 'CAT005' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 40, 200, 75, 19.50, 'ACTIVO', CURRENT_TIMESTAMP);

-- Productos para Herramientas y Equipos
INSERT INTO products (product_id, organization_id, product_code, product_name, category_id, unit_of_measure, minimum_stock, maximum_stock, current_stock, unit_cost, status, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD018', 'Pala Punta Acero Mango Madera', (SELECT category_id FROM product_categories WHERE category_code = 'CAT006' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 5, 20, 8, 38.50, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD019', 'Pico para Excavación', (SELECT category_id FROM product_categories WHERE category_code = 'CAT006' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 5, 15, 6, 42.00, 'ACTIVO', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PROD020', 'Llave Stillson 12"', (SELECT category_id FROM product_categories WHERE category_code = 'CAT006' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'UNIDAD', 3, 10, 4, 125.00, 'ACTIVO', CURRENT_TIMESTAMP);

-- =====================================================
-- INSERCIÓN DE COMPRAS INICIALES
-- =====================================================

INSERT INTO purchases (purchase_id, organization_id, purchase_code, supplier_id, purchase_date, delivery_date, total_amount, status, requested_by_user_id, approved_by_user_id, invoice_number, observations, created_at)
VALUES
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PUR001', (SELECT supplier_id FROM suppliers WHERE supplier_code = 'PROV001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), '2024-12-10', '2024-12-15', 2775.00, 'COMPLETADO', '68976d22a193bb4c13a00c90', '68976d22a193bb4c13a00c90', 'FAC-HC-001-2024', 'Tubería PVC para ampliación red matriz zona alta', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PUR002', (SELECT supplier_id FROM suppliers WHERE supplier_code = 'PROV002' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), '2024-12-12', '2024-12-12', 1710.00, 'COMPLETADO', '68976d22a193bb4c13a00c90', '68976d22a193bb4c13a00c90', 'FAC-TSA-002-2024', 'Codos y conexiones para reparaciones de emergencia', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PUR003', (SELECT supplier_id FROM suppliers WHERE supplier_code = 'PROV003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), '2024-12-18', '2024-12-25', 950.00, 'APROBADO', '68976d22a193bb4c13a00c90', '68976d22a193bb4c13a00c90', 'COT-BIJ-003-2024', 'Electrobomba de respaldo para estación de bombeo', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', 'PUR004', (SELECT supplier_id FROM suppliers WHERE supplier_code = 'PROV004' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), '2024-12-20', '2024-12-22', 948.75, 'COMPLETADO', '68976d22a193bb4c13a00c90', '68976d22a193bb4c13a00c90', 'FAC-QAT-004-2024', 'Químicos para tratamiento trimestral', CURRENT_TIMESTAMP);

-- =====================================================
-- INSERCIÓN DE DETALLES DE COMPRAS
-- =====================================================

INSERT INTO purchase_details (purchase_detail_id, purchase_id, product_id, quantity_ordered, quantity_received, unit_cost, subtotal, observations)
VALUES
    -- Detalles para PUR001 (Tubería PVC)
    (uuid_generate_v4(), (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), (SELECT product_id FROM products WHERE product_code = 'PROD001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 30, 30, 92.50, 2775.00, 'Tubería PVC 4" para red principal zona alta'),

    -- Detalles para PUR002 (Codos y conexiones)
    (uuid_generate_v4(), (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR002' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), (SELECT product_id FROM products WHERE product_code = 'PROD003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 120, 120, 14.25, 1710.00, 'Codos PVC 4" para reparaciones de red'),

    -- Detalles para PUR003 (Electrobomba)
    (uuid_generate_v4(), (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), (SELECT product_id FROM products WHERE product_code = 'PROD006' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 1, 0, 950.00, 950.00, 'Electrobomba centrífuga pendiente de entrega'),

    -- Detalles para PUR004 (Químicos)
    (uuid_generate_v4(), (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR004' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), (SELECT product_id FROM products WHERE product_code = 'PROD013' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 275, 275, 3.45, 948.75, 'Cloro granulado para desinfección trimestral');

-- =====================================================
-- INSERCIÓN DE MOVIMIENTOS DE INVENTARIO
-- =====================================================

INSERT INTO inventory_movements (movement_id, organization_id, product_id, movement_type, movement_reason, quantity, unit_cost, reference_document, reference_id, previous_stock, new_stock, movement_date, user_id, observations, created_at)
VALUES
    -- Movimientos de entrada por compras
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', (SELECT product_id FROM products WHERE product_code = 'PROD001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'ENTRADA', 'COMPRA', 30, 92.50, 'PUR001', (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 15, 45, '2024-12-15 09:30:00'::timestamp, '660e8400e29b41d4a716446655440001', 'Ingreso tubería PVC 4" para ampliación red matriz', CURRENT_TIMESTAMP),

    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', (SELECT product_id FROM products WHERE product_code = 'PROD003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'ENTRADA', 'COMPRA', 120, 14.25, 'PUR002', (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR002' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 0, 120, '2024-12-12 14:15:00'::timestamp, '660e8400e29b41d4a716446655440003', 'Ingreso codos PVC 4" para reparaciones', CURRENT_TIMESTAMP),

    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', (SELECT product_id FROM products WHERE product_code = 'PROD013' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'ENTRADA', 'COMPRA', 275, 3.45, 'PUR004', (SELECT purchase_id FROM purchases WHERE purchase_code = 'PUR004' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 0, 275, '2024-12-22 10:45:00'::timestamp, '660e8400e29b41d4a716446655440003', 'Ingreso cloro granulado para tratamiento', CURRENT_TIMESTAMP),

    -- Salidas por uso interno
    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', (SELECT product_id FROM products WHERE product_code = 'PROD001' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'SALIDA', 'USO_INTERNO', 5, 92.50, 'MANT-ZA-001', NULL, 45, 40, '2024-12-18 08:00:00'::timestamp, '660e8400e29b41d4a716446655440001', 'Uso en reparación de red principal zona alta', CURRENT_TIMESTAMP),

    (uuid_generate_v4(), '6896b2ecf3e398570ffd99d3', (SELECT product_id FROM products WHERE product_code = 'PROD003' AND organization_id = '6896b2ecf3e398570ffd99d3' LIMIT 1), 'SALIDA', 'USO_INTERNO', 15, 14.25, 'MANT-CON-002', NULL, 120, 105, '2024-12-19 13:30:00'::timestamp, '660e8400e29b41d4a716446655440003', 'Reparación conexiones domiciliarias sector 3', CURRENT_TIMESTAMP);

-- =====================================================
-- CONFIRMACIÓN DE DATOS INSERTADOS
-- =====================================================

-- Script completado exitosamente para organización 6896b2ecf3e398570ffd99d3
-- Códigos manuales implementados:
-- - 8 Categorías (CAT001 - CAT008)
-- - 8 Proveedores (PROV001 - PROV008)
-- - 20 Productos (PROD001 - PROD020)
-- - 4 Compras (PUR001 - PUR004)
-- - Movimientos de inventario registrados

# 📦 ENDPOINT PARA REGISTRAR CONSUMO DE PRODUCTOS - KARDEX

## 🎯 Propósito

Este endpoint permite registrar las **SALIDAS** de inventario cuando un producto es consumido desde otro microservicio. Es especialmente útil para mantener la trazabilidad completa en el KARDEX cuando el stock de productos ya ha sido actualizado.

## 🚀 Endpoint Principal

### POST `/api/v1/inventory-movements/consumption`

Registra un movimiento de inventario de tipo **SALIDA** por consumo de producto.

#### 📋 Request Body

```json
{
    "organizationId": "org-123",
    "productId": "prod-456",
    "quantity": 5,
    "unitCost": 45.50,
    "movementReason": "USO_INTERNO",
    "userId": "user-789",
    "referenceDocument": "REQ-001",
    "referenceId": "purchase-order-123",
    "observations": "Consumo para proyecto ABC",
    "previousStock": 100,
    "newStock": 95
}
```

#### ✅ Response Success

```json
{
    "status": true,
    "data": {
        "movementId": "mov-12345",
        "organizationId": "org-123",
        "productId": "prod-456",
        "movementType": "SALIDA",
        "movementReason": "USO_INTERNO",
        "quantity": 5,
        "unitCost": 45.50,
        "totalValue": 227.50,
        "previousStock": 100,
        "newStock": 95,
        "movementDate": "2025-09-13T10:30:00",
        "userId": "user-789",
        "observations": "Salida por consumo de producto - Consumo para proyecto ABC (Ref: REQ-001)",
        "referenceDocument": "REQ-001",
        "referenceId": "purchase-order-123",
        "createdAt": "2025-09-13T10:30:00",
        "success": true,
        "message": "Movimiento registrado correctamente"
    },
    "error": null
}
```

#### ❌ Response Error

```json
{
    "status": false,
    "data": null,
    "error": {
        "message": "Error de consistencia: El cálculo de stock no coincide",
        "errorCode": "BAD_REQUEST",
        "httpStatus": 400
    }
}
```

## 🔍 Endpoint de Consulta

### GET `/api/v1/inventory-movements/last-movement/{organizationId}/{productId}`

Obtiene el último movimiento de un producto (útil para validaciones).

#### ✅ Response Success

```json
{
    "status": true,
    "data": {
        "movementId": "mov-12345",
        "organizationId": "org-123",
        "productId": "prod-456",
        "movementType": "SALIDA",
        "movementReason": "USO_INTERNO",
        "quantity": 5,
        "unitCost": 45.50,
        "previousStock": 100,
        "newStock": 95,
        "movementDate": "2025-09-13T10:30:00",
        "userId": "user-789",
        "observations": "Salida por consumo de producto",
        "createdAt": "2025-09-13T10:30:00"
    },
    "error": null
}
```

## 📝 Campos del Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `organizationId` | String | ✅ | ID de la organización |
| `productId` | String | ✅ | ID del producto consumido |
| `quantity` | Integer | ✅ | Cantidad consumida (positiva) |
| `unitCost` | BigDecimal | ✅ | Costo unitario del producto |
| `movementReason` | Enum | ❌ | Motivo (por defecto: USO_INTERNO) |
| `userId` | String | ✅ | ID del usuario que realizó el consumo |
| `referenceDocument` | String | ❌ | Documento de referencia |
| `referenceId` | String | ❌ | ID de referencia (pedido, proyecto, etc.) |
| `observations` | String | ❌ | Observaciones adicionales |
| `previousStock` | Integer | ✅ | Stock antes del consumo |
| `newStock` | Integer | ✅ | Stock después del consumo |

## 📊 Motivos de Movimiento Disponibles

- `USO_INTERNO` (por defecto)
- `VENTA`
- `MERMA`
- `DONACION`
- `TRANSFERENCIA`
- `AJUSTE`

## 🔒 Validaciones

1. **Consistencia de Stock**: `previousStock - quantity = newStock`
2. **Cantidad Positiva**: La cantidad debe ser mayor a 0
3. **Campos Obligatorios**: organizationId, productId, quantity, unitCost, userId, previousStock, newStock

## 🌐 Ejemplo de Uso desde Otro Microservicio

### Java (WebClient)

```java
@Service
public class InventoryService {

    private final WebClient webClient;

    public Mono<Void> registerProductConsumption(String productId, int quantity, BigDecimal unitCost,
                                                int previousStock, int newStock, String userId) {

        InventoryMovementConsumptionRequest request = InventoryMovementConsumptionRequest.builder()
            .organizationId("org-123")
            .productId(productId)
            .quantity(quantity)
            .unitCost(unitCost)
            .movementReason(MovementReason.USO_INTERNO)
            .userId(userId)
            .previousStock(previousStock)
            .newStock(newStock)
            .observations("Consumo automático desde microservicio")
            .build();

        return webClient.post()
            .uri("/api/v1/inventory-movements/consumption")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ResponseDto.class)
            .doOnSuccess(response -> log.info("Movimiento registrado: {}", response))
            .then();
    }
}
```

### JavaScript (Axios)

```javascript
async function registerProductConsumption(productData) {
    try {
        const response = await axios.post('/api/v1/inventory-movements/consumption', {
            organizationId: 'org-123',
            productId: productData.productId,
            quantity: productData.quantity,
            unitCost: productData.unitCost,
            movementReason: 'USO_INTERNO',
            userId: productData.userId,
            previousStock: productData.previousStock,
            newStock: productData.newStock,
            observations: 'Consumo desde frontend'
        });

        console.log('Movimiento registrado:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error registrando movimiento:', error.response.data);
        throw error;
    }
}
```

## 🔄 Flujo de Integración Recomendado

1. **Tu compañero actualiza el stock** usando `PUT /api/v1/materials/{id}`
2. **Después del éxito**, llama a `POST /api/v1/inventory-movements/consumption`
3. **El sistema registra** automáticamente la salida en el KARDEX
4. **Mantiene la trazabilidad** completa de todos los movimientos

## ⚠️ Consideraciones Importantes

- Este endpoint debe ser llamado **DESPUÉS** de actualizar el stock del producto
- Los campos `previousStock` y `newStock` deben reflejar el estado real antes y después del consumo
- El sistema validará que `previousStock - quantity = newStock`
- El movimiento siempre será de tipo `SALIDA`

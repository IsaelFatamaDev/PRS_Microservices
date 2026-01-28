# Ejemplos de API - Microservicio de Pagos

## Estructura Correcta de Datos

### Crear un Pago con Detalles

**POST** `/api/admin/payments`

```json
{
  "organizationId": "org-123",
  "paymentCode": "PAG-2024-001",
  "userId": "user-456",
  "waterBoxId": "box-789",
  "paymentType": "SERVICIO_AGUA",
  "paymentMethod": "YAPE",
  "totalAmount": 85.50,
  "paymentDate": "2024-01-15",
  "paymentStatus": "PAID",
  "externalReference": "YAPE-789456123",
  "details": [
    {
      "concept": "SERVICIO_AGUA",
      "year": 2024,
      "month": 1,
      "amount": 45.50,
      "description": "Servicio de agua potable - Enero 2024",
      "periodStart": "2024-01-01",
      "periodEnd": "2024-01-31"
    },
    {
      "concept": "MANTENIMIENTO",
      "year": 2024,
      "month": 1,
      "amount": 40.00,
      "description": "Mantenimiento de infraestructura",
      "periodStart": "2024-01-01",
      "periodEnd": "2024-01-31"
    }
  ]
}
```

### Respuesta Esperada

```json
{
  "success": true,
  "data": {
    "paymentId": "pay-uuid-123",
    "organizationId": "org-123",
    "paymentCode": "PAG-2024-001",
    "userId": "user-456",
    "waterBoxId": "box-789",
    "paymentType": "SERVICIO_AGUA",
    "paymentMethod": "YAPE",
    "totalAmount": 85.50,
    "paymentDate": "2024-01-15",
    "paymentStatus": "PAID",
    "externalReference": "YAPE-789456123",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "details": [
      {
        "paymentDetailId": "detail-uuid-1",
        "paymentId": "pay-uuid-123",
        "concept": "SERVICIO_AGUA",
        "year": 2024,
        "month": 1,
        "amount": 45.50,
        "description": "Servicio de agua potable - Enero 2024",
        "periodStart": "2024-01-01",
        "periodEnd": "2024-01-31"
      },
      {
        "paymentDetailId": "detail-uuid-2",
        "paymentId": "pay-uuid-123",
        "concept": "MANTENIMIENTO",
        "year": 2024,
        "month": 1,
        "amount": 40.00,
        "description": "Mantenimiento de infraestructura",
        "periodStart": "2024-01-01",
        "periodEnd": "2024-01-31"
      }
    ]
  }
}
```

## Comandos cURL para Testing

### 1. Crear un Pago
```bash
curl -X POST http://localhost:8083/api/admin/payments \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": "org-123",
    "paymentCode": "PAG-2024-001",
    "userId": "user-456",
    "waterBoxId": "box-789",
    "paymentType": "SERVICIO_AGUA",
    "paymentMethod": "YAPE",
    "totalAmount": 85.50,
    "paymentDate": "2024-01-15",
    "paymentStatus": "PAID",
    "externalReference": "YAPE-789456123",
    "details": [
      {
        "concept": "SERVICIO_AGUA",
        "year": 2024,
        "month": 1,
        "amount": 45.50,
        "description": "Servicio de agua potable - Enero 2024",
        "periodStart": "2024-01-01",
        "periodEnd": "2024-01-31"
      }
    ]
  }'
```

### 2. Obtener Todos los Pagos Enriquecidos
```bash
curl -X GET http://localhost:8083/api/admin/payments/enriched
```

### 3. Obtener un Pago por ID
```bash
curl -X GET http://localhost:8083/api/admin/payments/{payment-id}/enriched
```

### 4. Listar Pagos Públicos
```bash
curl -X GET http://localhost:8083/api/client/payments
```

### 5. Health Check
```bash
curl -X GET http://localhost:8083/actuator/health
```

## Tipos de Datos Válidos

### Payment Types
- `SERVICIO_AGUA`
- `REPOSICION_CAJA`
- `MANTENIMIENTO`
- `MIXTO`

### Payment Methods
- `EFECTIVO`
- `YAPE`
- `PLIN`
- `TRANSFERENCIA`
- `TARJETA`

### Payment Status
- `PENDING`
- `PAID`
- `CANCELLED`
- `REFUNDED`

### Concepts (Ejemplos)
- `SERVICIO_AGUA`
- `REPOSICION_CAJA`
- `MANTENIMIENTO`
- `MULTA`
- `RECONEXION`
- `INSTALACION`

## Validaciones

### PaymentCreateRequest
- `organizationId`: Requerido
- `paymentCode`: Requerido, único
- `userId`: Requerido
- `totalAmount`: Requerido, mayor que 0
- `paymentDate`: Requerido
- `paymentStatus`: Requerido

### PaymentDRequest (Details)
- `concept`: Requerido
- `amount`: Requerido, mayor que 0
- `year`: Opcional, mayor que 2000
- `month`: Opcional, entre 1 y 12

## Errores Comunes

### 1. Error de Esquema de Base de Datos
```json
{
  "status": false,
  "error": {
    "errorCode": 500,
    "message": "Error retrieving payments",
    "details": "bad SQL grammar..."
  }
}
```
**Solución**: Ejecutar el script `database_migration.sql`

### 2. Error de Validación
```json
{
  "status": false,
  "error": {
    "errorCode": 400,
    "message": "Validation error",
    "details": "Amount must be positive"
  }
}
```
**Solución**: Verificar que todos los campos requeridos estén presentes y sean válidos

### 3. Error de Conexión a Microservicios
```json
{
  "status": false,
  "error": {
    "errorCode": 500,
    "message": "External service error",
    "details": "Unable to connect to ms-users"
  }
}
```
**Solución**: Verificar que los microservicios externos estén disponibles
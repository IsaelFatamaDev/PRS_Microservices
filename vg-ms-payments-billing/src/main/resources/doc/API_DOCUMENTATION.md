# API Documentation - Microservicio de Pagos

## Descripción
Microservicio para la gestión de pagos y recibos del Sistema JASS Digital.

## Endpoints Principales

### Endpoints Administrativos (`/api/admin`)

#### Pagos
- `GET /api/admin/payments/enriched` - Listar todos los pagos enriquecidos
- `GET /api/admin/payments/{id}/enriched` - Obtener pago enriquecido por ID
- `POST /api/admin/payments` - Crear un nuevo pago
- `PUT /api/admin/payments/{id}` - Actualizar un pago existente
- `DELETE /api/admin/payments/{id}` - Eliminar un pago por ID
- `DELETE /api/admin/payments` - Eliminar todos los pagos

### Endpoints Públicos (`/api/client`)

#### Pagos
- `GET /api/client/payments` - Listar pagos
- `GET /api/client/payments/{id}` - Obtener pago por ID

## Modelos de Datos

### PaymentCreateRequest
```json
{
  "organizationId": "string",
  "paymentCode": "string",
  "userId": "string",
  "waterBoxId": "string",
  "paymentType": "string",
  "paymentMethod": "string",
  "totalAmount": "number",
  "paymentDate": "date",
  "paymentStatus": "string",
  "externalReference": "string",
  "details": [
    {
      "concept": "string",
      "amount": "number",
      "quantity": "integer",
      "unitPrice": "number",
      "description": "string"
    }
  ]
}
```

### PaymentResponse
```json
{
  "paymentId": "string",
  "organizationId": "string",
  "paymentCode": "string",
  "userId": "string",
  "waterBoxId": "string",
  "paymentType": "string",
  "paymentMethod": "string",
  "totalAmount": "number",
  "paymentDate": "date",
  "paymentStatus": "string",
  "externalReference": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Códigos de Estado HTTP

- `200 OK` - Operación exitosa
- `201 Created` - Recurso creado exitosamente
- `204 No Content` - Operación exitosa sin contenido
- `400 Bad Request` - Error en la solicitud
- `404 Not Found` - Recurso no encontrado
- `500 Internal Server Error` - Error interno del servidor
# Documentación de Endpoints del Backend de Infraestructura

Este documento proporciona una visión general de los endpoints disponibles en el servicio de infraestructura, incluyendo sus funcionalidades, métodos HTTP, URLs y ejemplos de uso.

## Base URL

La URL base para todos los endpoints es: `http://localhost:8081/api/v1` (o la URL donde esté desplegado el servicio).

## Endpoints de `WaterBoxController`

Controlador para la gestión de cajas de agua.

### `GET /water-boxes/active`

Obtiene todas las cajas de agua activas.

- **Descripción:** Retorna una lista de todas las cajas de agua que se encuentran en estado activo.
- **Método:** `GET`
- **URL:** `/api/v1/water-boxes/active`
- **Respuestas:**
  - `200 OK`: Lista de `WaterBoxResponse`.

### `GET /water-boxes/inactive`

Obtiene todas las cajas de agua inactivas.

- **Descripción:** Retorna una lista de todas las cajas de agua que se encuentran en estado inactivo.
- **Método:** `GET`
- **URL:** `/api/v1/water-boxes/inactive`
- **Respuestas:**
  - `200 OK`: Lista de `WaterBoxResponse`.

### `GET /water-boxes/{id}`

Obtiene una caja de agua por su ID.

- **Descripción:** Retorna los detalles de una caja de agua específica utilizando su ID.
- **Método:** `GET`
- **URL:** `/api/v1/water-boxes/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la caja de agua.
- **Respuestas:**
  - `200 OK`: `WaterBoxResponse`.
  - `404 Not Found`: Si la caja de agua no existe.

### `POST /water-boxes`

Crea una nueva caja de agua.

- **Descripción:** Permite registrar una nueva caja de agua en el sistema.
- **Método:** `POST`
- **URL:** `/api/v1/water-boxes`
- **Cuerpo de la Solicitud (`WaterBoxRequest`):**
  ```json
  {
    "organizationId": 1,
    "boxCode": "BOX-001",
    "boxType": "RESIDENTIAL",
    "installationDate": "2023-01-15",
    "currentAssignmentId": null
  }
  ```
- **Respuestas:**
  - `201 Created`: `WaterBoxResponse` de la caja de agua creada.
  - `400 Bad Request`: Si la solicitud es inválida (ej. campos faltantes o incorrectos).

### `PUT /water-boxes/{id}`

Actualiza una caja de agua existente.

- **Descripción:** Modifica los detalles de una caja de agua existente por su ID.
- **Método:** `PUT`
- **URL:** `/api/v1/water-boxes/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la caja de agua a actualizar.
- **Cuerpo de la Solicitud (`WaterBoxRequest`):** (Mismos campos que `POST`)
- **Respuestas:**
  - `200 OK`: `WaterBoxResponse` de la caja de agua actualizada.
  - `400 Bad Request`: Si la solicitud es inválida.
  - `404 Not Found`: Si la caja de agua no existe.

### `DELETE /water-boxes/{id}`

Elimina (desactiva) una caja de agua.

- **Descripción:** Cambia el estado de una caja de agua a inactivo (eliminación lógica).
- **Método:** `DELETE`
- **URL:** `/api/v1/water-boxes/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la caja de agua a eliminar.
- **Respuestas:**
  - `204 No Content`: Si la eliminación fue exitosa.
  - `404 Not Found`: Si la caja de agua no existe.

### `PATCH /water-boxes/{id}/restore`

Restaura una caja de agua inactiva.

- **Descripción:** Cambia el estado de una caja de agua de inactivo a activo.
- **Método:** `PATCH`
- **URL:** `/api/v1/water-boxes/{id}/restore`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la caja de agua a restaurar.
- **Respuestas:**
  - `200 OK`: `WaterBoxResponse` de la caja de agua restaurada.
  - `404 Not Found`: Si la caja de agua no existe.

## Endpoints de `WaterBoxAssignmentController`

Controlador para la gestión de asignaciones de cajas de agua.

### `GET /water-box-assignments/active`

Obtiene todas las asignaciones de cajas de agua activas.

- **Descripción:** Retorna una lista de todas las asignaciones de cajas de agua que se encuentran en estado activo.
- **Método:** `GET`
- **URL:** `/api/v1/water-box-assignments/active`
- **Respuestas:**
  - `200 OK`: Lista de `WaterBoxAssignmentResponse`.

### `GET /water-box-assignments/inactive`

Obtiene todas las asignaciones de cajas de agua inactivas.

- **Descripción:** Retorna una lista de todas las asignaciones de cajas de agua que se encuentran en estado inactivo.
- **Método:** `GET`
- **URL:** `/api/v1/water-box-assignments/inactive`
- **Respuestas:**
  - `200 OK`: Lista de `WaterBoxAssignmentResponse`.

### `GET /water-box-assignments/{id}`

Obtiene una asignación de caja de agua por su ID.

- **Descripción:** Retorna los detalles de una asignación específica utilizando su ID.
- **Método:** `GET`
- **URL:** `/api/v1/water-box-assignments/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la asignación.
- **Respuestas:**
  - `200 OK`: `WaterBoxAssignmentResponse`.
  - `404 Not Found`: Si la asignación no existe.

### `POST /water-box-assignments`

Crea una nueva asignación de caja de agua.

- **Descripción:** Permite registrar una nueva asignación de caja de agua.
- **Método:** `POST`
- **URL:** `/api/v1/water-box-assignments`
- **Cuerpo de la Solicitud (`WaterBoxAssignmentRequest`):**
  ```json
  {
    "waterBoxId": 1,
    "userId": 101,
    "startDate": "2023-01-01T00:00:00",
    "endDate": "2024-01-01T00:00:00",
    "monthlyFee": 50.00
  }
  ```
- **Respuestas:**
  - `201 Created`: `WaterBoxAssignmentResponse` de la asignación creada.
  - `400 Bad Request`: Si la solicitud es inválida.

### `PUT /water-box-assignments/{id}`

Actualiza una asignación de caja de agua existente.

- **Descripción:** Modifica los detalles de una asignación existente por su ID.
- **Método:** `PUT`
- **URL:** `/api/v1/water-box-assignments/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la asignación a actualizar.
- **Cuerpo de la Solicitud (`WaterBoxAssignmentRequest`):** (Mismos campos que `POST`)
- **Respuestas:**
  - `200 OK`: `WaterBoxAssignmentResponse` de la asignación actualizada.
  - `400 Bad Request`: Si la solicitud es inválida.
  - `404 Not Found`: Si la asignación no existe.

### `DELETE /water-box-assignments/{id}`

Elimina (desactiva) una asignación de caja de agua.

- **Descripción:** Cambia el estado de una asignación a inactivo (eliminación lógica).
- **Método:** `DELETE`
- **URL:** `/api/v1/water-box-assignments/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la asignación a eliminar.
- **Respuestas:**
  - `204 No Content`: Si la eliminación fue exitosa.
  - `404 Not Found`: Si la asignación no existe.

### `PATCH /water-box-assignments/{id}/restore`

Restaura una asignación de caja de agua inactiva.

- **Descripción:** Cambia el estado de una asignación de inactivo a activo.
- **Método:** `PATCH`
- **URL:** `/api/v1/water-box-assignments/{id}/restore`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la asignación a restaurar.
- **Respuestas:**
  - `200 OK`: `WaterBoxAssignmentResponse` de la asignación restaurada.
  - `404 Not Found`: Si la asignación no existe.

## Endpoints de `WaterBoxTransferController`

Controlador para la gestión de transferencias de cajas de agua.

### `GET /water-box-transfers`

Obtiene todas las transferencias de cajas de agua.

- **Descripción:** Retorna una lista de todas las transferencias de cajas de agua registradas.
- **Método:** `GET`
- **URL:** `/api/v1/water-box-transfers`
- **Respuestas:**
  - `200 OK`: Lista de `WaterBoxTransferResponse`.

### `GET /water-box-transfers/{id}`

Obtiene una transferencia de caja de agua por su ID.

- **Descripción:** Retorna los detalles de una transferencia específica utilizando su ID.
- **Método:** `GET`
- **URL:** `/api/v1/water-box-transfers/{id}`
- **Parámetros de Ruta:**
  - `id` (Long): ID de la transferencia.
- **Respuestas:**
  - `200 OK`: `WaterBoxTransferResponse`.
  - `404 Not Found`: Si la transferencia no existe.

### `POST /water-box-transfers`

Crea una nueva transferencia de caja de agua.

- **Descripción:** Permite registrar una nueva transferencia de caja de agua.
- **Método:** `POST`
- **URL:** `/api/v1/water-box-transfers`
- **Cuerpo de la Solicitud (`WaterBoxTransferRequest`):**
  ```json
  {
    "waterBoxAssignmentId": 1,
    "previousUserId": 101,
    "newUserId": 102,
    "transferDate": "2023-06-01T10:00:00"
  }
  ```
- **Respuestas:**
  - `201 Created`: `WaterBoxTransferResponse` de la transferencia creada.
  - `400 Bad Request`: Si la solicitud es inválida.
# 🚀 Guía de Pruebas del Flujo Completo JASS

## 📋 Prerequisitos

Antes de ejecutar las pruebas, asegúrate de tener:

1. **Microservicios corriendo:**
   - `vg-ms-users` en puerto `8081`
   - `vg-ms-organizations` en puerto `8083`

2. **Bases de datos:**
   - PostgreSQL para `vg-ms-users`
   - MongoDB para `vg-ms-organizations`

3. **RabbitMQ** (opcional, para eventos)

## 🛠️ Iniciar los Microservicios

### Terminal 1 - Users

```bash
cd vg-ms-users
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Terminal 2 - Organizations

```bash
cd vg-ms-organizations
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📬 Importar Colección en Postman

1. Abre Postman
2. Click en **Import**
3. Selecciona el archivo: `POSTMAN_JASS_FLUJO_COMPLETO.json`
4. La colección se importará con todas las variables configuradas

## 🔄 Flujo de Pruebas (En Orden)

### Paso 1: Crear SUPER_ADMIN

```
POST http://localhost:8081/api/v1/users/setup/super-admin
```

- **No requiere autenticación** (endpoint público)
- Solo puede existir UN SUPER_ADMIN en el sistema
- Guardará el `super_admin_id` automáticamente

### Paso 2: Crear Organización

```
POST http://localhost:8083/api/v1/organizations
Headers:
  - X-User-Id: {super_admin_id}
  - X-User-Roles: SUPER_ADMIN
```

### Paso 3: Crear Zonas

```
POST http://localhost:8083/api/v1/zones
Headers:
  - X-User-Id: {super_admin_id}
  - X-User-Roles: SUPER_ADMIN
  - X-Organization-Id: {organization_id}
```

### Paso 4: Crear Calles

```
POST http://localhost:8083/api/v1/streets
Headers:
  - X-User-Id: {super_admin_id}
  - X-User-Roles: SUPER_ADMIN
  - X-Organization-Id: {organization_id}
```

### Paso 5: Crear ADMIN

```
POST http://localhost:8081/api/v1/users
Headers:
  - X-User-Id: {super_admin_id}
  - X-User-Roles: SUPER_ADMIN
Body:
  - role: "ADMIN"
  - organizationId, zoneId, streetId, etc.
```

### Paso 6: ADMIN crea usuarios

```
POST http://localhost:8081/api/v1/users
Headers:
  - X-User-Id: {admin_id}
  - X-User-Roles: ADMIN
  - X-Organization-Id: {organization_id}
Body:
  - role: "CLIENT" o "OPERATOR"
```

## 🔐 Headers de Seguridad

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| `X-User-Id` | ID del usuario autenticado | `uuid-123-456` |
| `X-User-Roles` | Roles del usuario (separados por coma) | `SUPER_ADMIN` o `ADMIN` |
| `X-Organization-Id` | ID de la organización del usuario | `uuid-org-123` |
| `X-User-Email` | Email del usuario (opcional) | `admin@jass.pe` |

## 👥 Matriz de Permisos

| Acción | SUPER_ADMIN | ADMIN | OPERATOR | CLIENT |
|--------|:-----------:|:-----:|:--------:|:------:|
| Crear Organización | ✅ | ❌ | ❌ | ❌ |
| Crear Zonas | ✅ | ✅ | ❌ | ❌ |
| Crear Calles | ✅ | ✅ | ❌ | ❌ |
| Crear ADMIN | ✅ | ❌ | ❌ | ❌ |
| Crear OPERATOR | ✅ | ✅ | ❌ | ❌ |
| Crear CLIENT | ✅ | ✅ | ❌ | ❌ |
| Ver usuarios org | ✅ | ✅ | ✅ | ❌ |

## 📝 Ejemplo de Requests

### 1. Crear SUPER_ADMIN (sin auth)

```json
POST /api/v1/users/setup/super-admin
{
    "firstName": "Super",
    "lastName": "Admin",
    "documentType": "DNI",
    "documentNumber": "12345678",
    "email": "superadmin@jass.pe",
    "phone": "987654321"
}
```

### 2. Crear Organización

```json
POST /api/v1/organizations
Headers: X-User-Id: {super_admin_id}, X-User-Roles: SUPER_ADMIN
{
    "organizationName": "JASS San Pedro de Lloc",
    "district": "San Pedro de Lloc",
    "province": "Pacasmayo",
    "department": "La Libertad",
    "address": "Calle Principal 123",
    "phone": "044123456",
    "email": "jass.sanpedro@gmail.com"
}
```

### 3. Crear Zona

```json
POST /api/v1/zones
Headers: X-User-Id: {super_admin_id}, X-User-Roles: SUPER_ADMIN
{
    "organizationId": "{organization_id}",
    "zoneName": "Zona Centro",
    "description": "Zona central del distrito"
}
```

### 4. Crear Calle

```json
POST /api/v1/streets
Headers: X-User-Id: {super_admin_id}, X-User-Roles: SUPER_ADMIN
{
    "organizationId": "{organization_id}",
    "zoneId": "{zone_id}",
    "streetType": "JR",
    "streetName": "Independencia"
}
```

### 5. Crear ADMIN

```json
POST /api/v1/users
Headers: X-User-Id: {super_admin_id}, X-User-Roles: SUPER_ADMIN
{
    "organizationId": "{organization_id}",
    "firstName": "Juan",
    "lastName": "Pérez",
    "documentType": "DNI",
    "documentNumber": "87654321",
    "email": "admin@jass.pe",
    "phone": "912345678",
    "address": "Jr. Independencia 456",
    "zoneId": "{zone_id}",
    "streetId": "{street_id}",
    "role": "ADMIN"
}
```

### 6. ADMIN crea CLIENT

```json
POST /api/v1/users
Headers: X-User-Id: {admin_id}, X-User-Roles: ADMIN, X-Organization-Id: {organization_id}
{
    "organizationId": "{organization_id}",
    "firstName": "María",
    "lastName": "López",
    "documentType": "DNI",
    "documentNumber": "11223344",
    "email": "maria@gmail.com",
    "phone": "923456789",
    "address": "Av. Grau 789",
    "zoneId": "{zone_id}",
    "streetId": "{street_id}",
    "role": "CLIENT"
}
```

## ⚠️ Notas Importantes

1. **SUPER_ADMIN único**: Solo puede existir un SUPER_ADMIN. Si intentas crear otro, recibirás un error.

2. **Variables automáticas**: La colección de Postman guarda automáticamente los IDs en variables para usarlos en requests posteriores.

3. **Orden de ejecución**: Debes seguir el orden de las carpetas (01, 02, 03...) para que las variables se llenen correctamente.

4. **Validación de jerarquía**: Para crear usuarios, la combinación organizationId/zoneId/streetId debe ser válida.

## 🐛 Troubleshooting

| Error | Causa | Solución |
|-------|-------|----------|
| `A SUPER_ADMIN already exists` | Ya existe un SUPER_ADMIN | Elimina el registro de la BD o usa el existente |
| `Missing X-User-Id header` | Falta header de autenticación | Agrega los headers requeridos |
| `Invalid organization/zone/street combination` | La jerarquía no existe | Verifica que la zona pertenece a la org y la calle a la zona |
| `User with role ADMIN cannot create ADMIN` | Sin permisos | Solo SUPER_ADMIN puede crear ADMIN |

## 📊 Puertos de los Microservicios

| Microservicio | Puerto | Base de Datos |
|--------------|--------|---------------|
| vg-ms-users | 8081 | PostgreSQL |
| vg-ms-organizations | 8083 | MongoDB |
| vg-ms-authentication | 8082 | Keycloak |
| vg-ms-gateway | 8080 | - |

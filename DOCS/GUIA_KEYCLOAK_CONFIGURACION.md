# 🔐 Guía de Configuración de Keycloak para JASS

## 📋 Prerrequisitos

1. Keycloak corriendo en `http://localhost:9090`
2. Realm: `sistema-jass` creado
3. Usuario admin de Keycloak

## 🛠️ Configuración del Cliente en Keycloak

### Paso 1: Acceder a Keycloak Admin Console

1. Abre: `http://localhost:9090/admin`
2. Login con:
   - Username: `admin`
   - Password: `admin`

### Paso 2: Crear el Cliente `jass-users-service`

1. En el menú lateral, selecciona **Clients**
2. Click en **Create client**
3. Configurar **General Settings**:
   - **Client type**: `OpenID Connect`
   - **Client ID**: `jass-users-service`
   - Click **Next**

4. Configurar **Capability config**:
   - ✅ **Client authentication**: ON (para habilitar client credentials)
   - ✅ **Authorization**: OFF
   - ✅ **Authentication flow**:
     - ✅ Standard flow
     - ✅ Direct access grants (Resource Owner Password Credentials)
     - ❌ Implicit flow
     - ❌ Service accounts roles
   - Click **Next**

5. Configurar **Login settings**:
   - **Root URL**: `http://localhost:8082`
   - **Home URL**: `http://localhost:8082`
   - **Valid redirect URIs**:
     - `http://localhost:8082/*`
     - `http://localhost:3000/*`
   - **Valid post logout redirect URIs**: `+`
   - **Web origins**: `*` (o específicamente `http://localhost:8082`)
   - Click **Save**

### Paso 3: Obtener el Client Secret

1. Ve a la pestaña **Credentials**
2. Copia el **Client secret**
3. Debería ser: `t4kyYflBZjLcYJBqEG4qf3JesF8dEsMB` (o el que generó Keycloak)

### Paso 4: Configurar Roles del Cliente

1. Ve a la pestaña **Roles**
2. Click en **Create role**
3. Crear los siguientes roles:
   - `SUPER_ADMIN`
   - `ADMIN`
   - `OPERATOR`
   - `CLIENT`

### Paso 5: Configurar Client Scopes

1. Ve a la pestaña **Client scopes**
2. Click en **Add client scope**
3. Selecciona los scopes necesarios:
   - `profile`
   - `email`
   - `roles`
   - `web-origins`

## 🔑 Flujo de Autenticación

### Datos de Login del SUPER_ADMIN

Cuando creas un SUPER_ADMIN con estos datos:

```json
{
  "firstName": "Super",
  "lastName": "Admin",
  "documentType": "DNI",
  "documentNumber": "12345678",
  "email": "superadmin@jass.pe",
  "phone": "987654321"
}
```

El sistema automáticamente crea el usuario en Keycloak con:

- **Username**: `super.admin@jass.gob.pe` (generado automáticamente)
- **Password**: `12345678` (el documentNumber)
- **Email**: `superadmin@jass.pe`

### Request de Login en Postman

```http
POST http://localhost:8082/api/v1/auth/login
Content-Type: application/json

{
  "username": "super.admin@jass.gob.pe",
  "password": "12345678",
  "clientId": "jass-users-service"
}
```

### Respuesta Esperada

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
    "expires_in": 3600,
    "refresh_expires_in": 1800,
    "token_type": "Bearer"
  },
  "timestamp": "2026-02-08T17:20:00"
}
```

## 🐛 Troubleshooting

### Error: "400 Bad Request"

**Causa**: El cliente `jass-users-service` no existe o no está configurado correctamente

**Solución**:

1. Verifica que el cliente existe en Keycloak Admin Console
2. Confirma que **Direct access grants** está habilitado
3. Verifica que el **client-secret** coincide con el de `application-dev.yml`

### Error: "401 Unauthorized" o "Invalid credentials"

**Causa**: Username o password incorrectos

**Solución**:

- Username debe ser: `{firstName}.{lastName}@jass.gob.pe` (en minúsculas, primer nombre + primer apellido)
- Password es el **documentNumber** del usuario

### Error: "User not found in Keycloak"

**Causa**: El usuario no fue sincronizado a Keycloak

**Solución**:

1. Verifica que RabbitMQ está corriendo
2. Revisa los logs de `vg-ms-authentication` para ver si recibió el evento
3. Si no, elimina el usuario de la BD y créalo nuevamente

## 📝 Ejemplo Completo de Flujo

### 1. Crear SUPER_ADMIN

```http
POST http://localhost:8081/api/v1/users/setup/super-admin
{
  "firstName": "Super",
  "lastName": "Admin",
  "documentType": "DNI",
  "documentNumber": "12345678",
  "email": "superadmin@jass.pe",
  "phone": "987654321"
}
```

### 2. Esperar Sincronización con Keycloak

Revisa los logs de `vg-ms-authentication` para confirmar:

```
User created in Keycloak with ID: xxx
Attributes updated successfully for user: xxx
```

### 3. Hacer Login

```http
POST http://localhost:8082/api/v1/auth/login
{
  "username": "super.admin@jass.gob.pe",
  "password": "12345678",
  "clientId": "jass-users-service"
}
```

### 4. Usar el Token

```http
GET http://localhost:8081/api/v1/users/me
Authorization: Bearer {access_token}
```

## 🔐 Cambiar Password del Usuario

Si necesitas cambiar la password temporal:

### Opción 1: Desde Keycloak Admin

1. Ve a **Users** → Buscar usuario por email
2. Click en **Credentials**
3. Click en **Reset password**
4. Establece nueva password y desmarca "Temporary"

### Opción 2: Desde la API (futuro endpoint)

```http
POST http://localhost:8082/api/v1/auth/change-password
{
  "oldPassword": "12345678",
  "newPassword": "NuevaPassword123!"
}
```

## 📊 Verificar Usuario en Keycloak

1. Admin Console → **Users**
2. Buscar por email: `superadmin@jass.pe`
3. Verificar:
   - ✅ Enabled: `true`
   - ✅ Email verified: `true`
   - ✅ Username: `super.admin@jass.gob.pe`
   - ✅ Role mappings: Debe tener el rol `SUPER_ADMIN`

## 🔄 Regenerar Client Secret (si es necesario)

1. Clients → `jass-users-service` → **Credentials**
2. Click en **Regenerate**
3. Copiar el nuevo secret
4. Actualizar en `application-dev.yml`:

   ```yaml
   keycloak:
     client-secret: {nuevo-secret}
   ```

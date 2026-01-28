# Debugging - Error 500 al Crear Incidentes

## Problema

Al intentar crear una incidencia desde el frontend, se recibe un error 500 (Internal Server Error).

## Pasos para Debuggear

### 1. Verificar que el servidor esté funcionando

```bash
# Probar el endpoint de prueba
curl http://localhost:8080/api/v1/incidents/test
```

### 2. Probar la creación de incidente con datos mínimos

```bash
# Probar el endpoint de creación de prueba
curl -X POST http://localhost:8080/api/v1/incidents/test-create
```

### 3. Probar la creación manual con curl

```bash
curl -X POST http://localhost:8080/api/v1/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "incident_code": "TEST002",
    "organization_id": "64abc123def4567890123456",
    "incident_type": "FUGA_TUBERIA",
    "incident_type_group": "INFRAESTRUCTURA",
    "incident_date": "2023-06-08T10:00:00Z",
    "description": "Prueba manual",
    "severity": "LOW",
    "zone_id": "64def123abc4567890123456",
    "affected_count": 1,
    "reported_by_user_id": "64abc789def1234567890123",
    "resolved": false,
    "status": "ACTIVE",
    "source_collection": "test_incidents"
  }'
```

### 4. Verificar logs del servidor

Los logs ahora incluyen información detallada sobre:

- Llamadas a endpoints
- Conversión de DTOs
- Operaciones de base de datos
- Errores específicos

### 5. Verificar conexión a MongoDB

```bash
# Verificar que la aplicación se conecte correctamente a MongoDB
# Los logs deberían mostrar conexión exitosa
```

## Posibles Causas del Error 500

### 1. Problema de Mapeo de Campos

- Verificar que todos los campos del DTO coincidan con el modelo
- Verificar anotaciones `@Field` en el modelo

### 2. Problema de Conversión de Fechas

- Verificar formato de fechas en el JSON
- Verificar conversión de `Instant`

### 3. Problema de Conexión a MongoDB

- Verificar URI de conexión
- Verificar credenciales
- Verificar red

### 4. Problema de Validación

- Verificar que todos los campos requeridos estén presentes
- Verificar tipos de datos

## Logs a Revisar

Buscar en los logs del servidor:

- `"Guardando nuevo incidente"`
- `"Error al guardar incidente"`
- `"Error al convertir DTO a Incident"`
- `"Error al convertir Incident a DTO"`

## Endpoints de Prueba Disponibles

1. **GET** `/api/v1/incidents/test` - Prueba básica de conectividad
2. **POST** `/api/v1/incidents/test-create` - Crear incidente con datos predefinidos
3. **GET** `/api/v1/incidents` - Listar todos los incidentes
4. **POST** `/api/v1/incidents` - Crear incidente con datos del frontend

## Configuración de Logging

El logging está configurado para mostrar:

- Nivel DEBUG para el paquete `pe.edu.vallegrande`
- Nivel DEBUG para Spring Web
- Nivel DEBUG para MongoDB
- Nivel DEBUG para WebFlux
- Nivel DEBUG para Reactor Netty

## Solución Temporal

Si el problema persiste, se puede:

1. Revisar los logs del servidor para identificar el error específico
2. Probar con el endpoint `/test-create` para verificar si el problema es con los datos del frontend
3. Verificar que el frontend esté enviando los datos en el formato correcto 
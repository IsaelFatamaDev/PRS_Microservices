# Explicación de Campos Null en Incidentes

## Campos que pueden ser null y por qué

### Campos de Fecha

- **`reportedAt`**: Es la única fecha en el modelo. Representa cuándo se reportó el incidente. Si es null, se establece
  automáticamente al momento de crear el incidente.

### Campos de Resolución

- **`resolvedByUserId`**: Solo se establece cuando el incidente es resuelto. Si el incidente aún no está resuelto, este
  campo será null.
- **`resolved`**: Indica si el incidente está resuelto (true) o no (false). Por defecto es false.

### Campos Opcionales

- **`linkedRecordId`**: Campo opcional para vincular el incidente con otro registro del sistema. Puede ser null si no
  hay vinculación.
- **`sourceCollection`**: Indica de qué colección proviene el incidente. Puede ser null si no se especifica.

### Campos con Valores por Defecto

- **`status`**: Por defecto es "ACTIVE"
- **`estimatedResolutionTime`**: Se calcula automáticamente basado en el tipo de incidente
- **`affectedCount`**: Puede ser null si no se especifica el número de afectados

## Endpoints Disponibles

### 1. Crear Incidente

```bash
POST /api/incidents
```

Crea un nuevo incidente con valores por defecto inteligentes.

### 2. Completar Incidente

```bash
PUT /api/incidents/{id}/complete
```

Completa campos faltantes en un incidente existente.

### 3. Resolver Incidente

```bash
PUT /api/incidents/{id}/resolve
```

Marca un incidente como resuelto y establece `resolvedByUserId`.

### 4. Endpoints de Prueba

```bash
POST /api/incidents/test/create-simple
POST /api/incidents/test/create-complete
```

Endpoints para crear incidentes de prueba con diferentes niveles de completitud.

## Ejemplo de JSON para Crear Incidente

```json
{
  "incidentCode": "INC001",
  "organizationId": "64abc123def4567890123456",
  "incidentType": "FUGA_TUBERIA",
  "incidentTypeGroup": "INFRAESTRUCTURA",
  "description": "Rotura de tubería principal",
  "severity": "HIGH",
  "zoneId": "64def123abc4567890123456",
  "reportedByUserId": "64abc789def1234567890123"
}
```

## Notas Importantes

1. **Solo una fecha**: El modelo ahora solo tiene `reportedAt` como fecha única.
2. **Campos null son normales**: Los campos null no indican errores, sino que el incidente aún no ha sido completado o
   resuelto.
3. **Valores automáticos**: El sistema establece automáticamente valores por defecto para campos requeridos.
4. **Resolución manual**: Para resolver un incidente, usar el endpoint específico de resolución.

## Flujo Recomendado

1. **Crear incidente** con datos mínimos
2. **Completar incidente** con datos adicionales (opcional)
3. **Resolver incidente** cuando se solucione el problema

Los campos null se irán completando a medida que se use cada endpoint. 
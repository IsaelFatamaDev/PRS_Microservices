# Configuración de Variables de Entorno en GitLab CI/CD

## ⚠️ IMPORTANTE

Para que el despliegue automático funcione, debes configurar las variables de entorno en GitLab CI/CD Settings.

## Cómo Configurar Variables en GitLab

### 1. Ir a Settings → CI/CD

1. Abre tu proyecto en GitLab
2. Ve a **Settings** (Configuración) en el menú lateral izquierdo
3. Click en **CI/CD**
4. Expande la sección **Variables**

### 2. Agregar Variables

Click en **Add Variable** y agrega cada una de las siguientes variables:

#### Variables de MongoDB

| Key | Value (Ejemplo) | Protected | Masked |
|-----|-----------------|-----------|--------|
| `MONGO_USERNAME` | `tu_usuario_mongodb` | ✅ | ✅ |
| `MONGO_PASSWORD` | `tu_password_mongodb` | ✅ | ✅ |
| `MONGO_HOST` | `tu_cluster.mongodb.net` | ✅ | ❌ |
| `MONGO_DATABASE` | `distribution_db` | ✅ | ❌ |

#### Variables de Keycloak

| Key | Value (Ejemplo) | Protected | Masked |
|-----|-----------------|-----------|--------|
| `KEYCLOAK_ISSUER_URI` | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass` | ✅ | ❌ |
| `KEYCLOAK_JWK_SET_URI` | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/certs` | ✅ | ❌ |

#### Variables de JWE Internal

| Key | Value (Ejemplo) | Protected | Masked |
|-----|-----------------|-----------|--------|
| `JWE_INTERNAL_SECRET` | `tu_secreto_minimo_32_caracteres` | ✅ | ✅ |
| `JWE_INTERNAL_EXPIRATION` | `86400` | ✅ | ❌ |
| `JWE_INTERNAL_ISSUER` | `ms-distribution-internal` | ✅ | ❌ |
| `JWE_INTERNAL_AUDIENCE` | `jass-microservices` | ✅ | ❌ |

#### Variables de Servicios Externos

| Key | Value (Ejemplo) | Protected | Masked |
|-----|-----------------|-----------|--------|
| `ORGANIZATION_SERVICE_URL` | `https://lab.vallegrande.edu.pe/jass/ms-organization/api/admin` | ✅ | ❌ |
| `ORGANIZATION_SERVICE_BASE_URL` | `https://lab.vallegrande.edu.pe/jass/ms-organization/api/admin` | ✅ | ❌ |
| `ORGANIZATION_SERVICE_TOKEN` | `tu_token_de_servicio` | ✅ | ✅ |

#### Variables de Spring

| Key | Value (Ejemplo) | Protected | Masked |
|-----|-----------------|-----------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` | ✅ | ❌ |
| `PORT` | `8086` | ✅ | ❌ |

#### Variables de DockerHub (ya configuradas)

| Key | Value | Protected | Masked |
|-----|-------|-----------|--------|
| `DOCKERHUB_USERNAME` | `isaelfatamadev` | ✅ | ❌ |
| `DOCKERHUB_TOKEN` | `tu_token_dockerhub` | ✅ | ✅ |

## Opciones de Variables

### Protected ✅
- **Activar** para variables sensibles
- Solo disponibles en branches protegidas (main, develop)

### Masked ✅
- **Activar** para passwords, tokens, secrets
- Oculta el valor en los logs de CI/CD

### Expand variable reference
- Dejar **desactivado** normalmente

## Cómo Funciona

### 1. GitLab CI/CD Pipeline

```yaml
# .gitlab-ci.yml
docker-build-push:
  script:
    - docker build \
        --build-arg MONGO_USERNAME="$MONGO_USERNAME" \
        --build-arg MONGO_PASSWORD="$MONGO_PASSWORD" \
        # ... más variables
        -t imagen:latest .
```

### 2. Dockerfile

```dockerfile
# Dockerfile
ARG MONGO_USERNAME
ARG MONGO_PASSWORD

ENV MONGO_USERNAME=${MONGO_USERNAME} \
    MONGO_PASSWORD=${MONGO_PASSWORD}
```

### 3. Spring Boot

```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb+srv://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}/${MONGO_DATABASE}
```

## Flujo Completo

```
1. Push a GitLab (develop/main)
   ↓
2. GitLab CI/CD se activa
   ↓
3. Lee variables de Settings → CI/CD → Variables
   ↓
4. Construye imagen Docker con --build-arg
   ↓
5. Variables se guardan en la imagen como ENV
   ↓
6. Sube imagen a DockerHub
   ↓
7. Contenedor usa las variables de entorno
   ↓
8. Spring Boot lee las variables
   ↓
9. ✅ Aplicación funciona
```

## Verificación

### Ver logs del pipeline

1. Ve a **CI/CD → Pipelines**
2. Click en el pipeline más reciente
3. Click en el job `docker-build-push`
4. Verifica que no haya errores

### Verificar que las variables NO se muestren en logs

Las variables marcadas como **Masked** deben aparecer como `[masked]` en los logs.

## Troubleshooting

### Error: "An SRV host name '${MONGO_HOST}' was provided..."

**Causa**: La variable `MONGO_HOST` no está configurada en GitLab CI/CD.

**Solución**: Agrega la variable en Settings → CI/CD → Variables.

### Error: "docker login failed"

**Causa**: `DOCKERHUB_TOKEN` o `DOCKERHUB_USERNAME` incorrectos.

**Solución**: Verifica las credenciales de DockerHub en GitLab Variables.

### Las variables no se cargan

**Causa**: Variables no están marcadas como disponibles para la branch.

**Solución**: 
1. Verifica que **Protected** esté activado
2. Verifica que estés en branch `develop` o `main`

## Seguridad

> [!IMPORTANT]
> - **NUNCA** pongas credenciales en el código
> - **SIEMPRE** usa GitLab CI/CD Variables para secretos
> - **ACTIVA** Masked para passwords y tokens
> - **ACTIVA** Protected para variables sensibles

> [!WARNING]
> Si cambias las credenciales, debes:
> 1. Actualizar las variables en GitLab CI/CD
> 2. Hacer push para que se reconstruya la imagen
> 3. Redesplegar el contenedor

## Desarrollo Local

Para desarrollo local, usa el archivo `.env`:

```bash
# 1. Copiar plantilla
cp .env.example .env

# 2. Editar con tus credenciales
nano .env

# 3. Ejecutar
docker-compose up -d
```

El `.env` local **NO se sube** a GitLab (está en `.gitignore`).

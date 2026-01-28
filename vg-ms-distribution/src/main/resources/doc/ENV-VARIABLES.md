# Configuración de Variables de Entorno

## ⚠️ IMPORTANTE

El archivo `.env` contiene credenciales sensibles y **NO debe subirse a GitLab**.

## Estructura

```
.env.example    → Plantilla con variables (SÍ se sube a GitLab)
.env            → Valores reales (NO se sube a GitLab, está en .gitignore)
```

## Configuración Inicial

### 1. Copiar el archivo de ejemplo

```bash
cp .env.example .env
```

### 2. Editar `.env` con tus credenciales reales

```bash
# Edita el archivo .env con tus valores
nano .env
```

## Variables Requeridas

El archivo `.env` debe contener:

```bash
# MongoDB
MONGO_USERNAME=tu_usuario
MONGO_PASSWORD=tu_password
MONGO_HOST=tu_host.mongodb.net
MONGO_DATABASE=distribution_db

# Keycloak
KEYCLOAK_ISSUER_URI=https://...
KEYCLOAK_JWK_SET_URI=https://...

# JWE Internal
JWE_INTERNAL_SECRET=tu_secreto_minimo_32_caracteres
JWE_INTERNAL_EXPIRATION=86400
JWE_INTERNAL_ISSUER=ms-distribution-internal
JWE_INTERNAL_AUDIENCE=jass-microservices

# Servicios Externos
ORGANIZATION_SERVICE_URL=https://...
ORGANIZATION_SERVICE_BASE_URL=https://...
ORGANIZATION_SERVICE_TOKEN=tu_token

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

## Uso

### Desarrollo Local con Docker Compose

```bash
# Las variables se cargan automáticamente desde .env
docker-compose up -d
```

### Desarrollo Local sin Docker

```bash
# Cargar variables en tu terminal (Linux/Mac)
export $(cat .env | xargs)

# O en Windows PowerShell
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}

# Ejecutar aplicación
mvn spring-boot:run
```

### Deployment en Cloud (Railway, Render, etc.)

1. **NO subas el archivo `.env`** a GitLab
2. Configura las variables de entorno en la plataforma cloud:
   - Railway: Settings → Variables
   - Render: Environment → Environment Variables
   - Fly.io: `flyctl secrets set VAR=value`

## Cómo Funciona

### application.yml

El archivo `application.yml` usa variables de entorno:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}/${MONGO_DATABASE}
```

### Docker Compose

El `docker-compose.yml` carga el `.env`:

```yaml
services:
  ms-distribution:
    env_file:
      - .env  # Carga todas las variables automáticamente
```

### Dockerfile

El Dockerfile **NO** incluye el `.env` (seguridad):
- Las variables se pasan en runtime
- No se guardan en la imagen Docker

## Seguridad

✅ **Buenas Prácticas:**
- `.env` está en `.gitignore`
- `.env.example` tiene valores de ejemplo (sin credenciales reales)
- Cambiar credenciales en producción
- Usar secrets manager en cloud

❌ **NO HACER:**
- Subir `.env` a GitLab
- Poner credenciales reales en `.env.example`
- Copiar `.env` dentro de la imagen Docker
- Compartir `.env` por email/chat

## Troubleshooting

### Error: "An SRV host name '${MONGO_HOST}' was provided..."

**Causa:** Las variables de entorno no se están cargando.

**Solución:**
1. Verifica que `.env` existe
2. Verifica que `docker-compose.yml` tiene `env_file: - .env`
3. Reinicia el contenedor: `docker-compose down && docker-compose up -d`

### Error: Variables no se cargan en desarrollo local

**Solución:**
```bash
# Verifica que las variables estén exportadas
echo $MONGO_USERNAME

# Si está vacío, exporta manualmente
export $(cat .env | xargs)
```

## Ejemplo Completo

```bash
# 1. Clonar repositorio
git clone https://gitlab.com/.../vg-ms-distribution.git
cd vg-ms-distribution

# 2. Copiar .env.example a .env
cp .env.example .env

# 3. Editar .env con tus credenciales
nano .env

# 4. Ejecutar con Docker Compose
docker-compose up -d

# 5. Verificar logs
docker-compose logs -f

# 6. Verificar que funciona
curl http://localhost:8086/jass/ms-distribution/actuator/health
```

## Notas Importantes

> [!IMPORTANT]
> El archivo `.env` contiene credenciales sensibles. Nunca lo subas a GitLab.

> [!WARNING]
> Si accidentalmente subes `.env` a GitLab:
> 1. Elimínalo del repositorio: `git rm --cached .env`
> 2. Cambia TODAS las credenciales inmediatamente
> 3. Haz commit: `git commit -m "Remove .env from git"`

> [!NOTE]
> En producción, usa el secrets manager de tu plataforma cloud en lugar de archivos `.env`.

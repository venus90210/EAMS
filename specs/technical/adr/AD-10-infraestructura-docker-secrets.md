# AD-10 — Infraestructura contenerizada con Docker y secretos en vault

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RNF04, RNF09                   |

---

## Contexto

El sistema maneja datos sensibles de menores de edad. Las credenciales de base de datos, claves JWT y API keys del servicio de email **no deben almacenarse en el repositorio ni en variables de entorno planas** en los archivos de configuración.

Adicionalmente, el entorno local de desarrollo debe ser reproducible y consistente para todos los miembros del equipo, eliminando el problema de "funciona en mi máquina".

## Decisión

Se adopta **Docker + Docker Compose para desarrollo local** y **secretos gestionados con vault** en todos los entornos.

### Entorno de desarrollo local

```yaml
# docker-compose.yml (fragmento)
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: eams_dev
      POSTGRES_USER: eams
      POSTGRES_PASSWORD: ${DB_PASSWORD}  # desde .env local, NO en el repo
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}

  backend:
    build: ./backend
    depends_on: [postgres, redis]
    environment:
      DATABASE_URL: ${DATABASE_URL}
      JWT_SECRET: ${JWT_SECRET}

  gateway:
    build: ./gateway
    ports: ["3000:3000"]
    depends_on: [backend]
```

### Gestión de secretos por entorno

| Entorno     | Herramienta              | Secretos gestionados                           |
|-------------|--------------------------|------------------------------------------------|
| Local       | `.env` local (git-ignored) | DB password, JWT secret, Redis password      |
| CI/CD       | GitHub Secrets           | Mismos secretos inyectados en el pipeline      |
| Producción  | Doppler / AWS Secrets Manager | DB URL, JWT secret, SMTP API key, Redis URL |

### Regla de oro

> **Ningún secreto entra al repositorio.** `.env` está en `.gitignore`.
> El repositorio contiene únicamente `.env.example` con claves vacías como referencia.

### Política de infraestructura en producción

| Componente        | Decisión                                      |
|-------------------|-----------------------------------------------|
| Despliegue        | Docker containers en Railway / Render / AWS   |
| HTTPS             | TLS obligatorio — sin HTTP en producción       |
| Backups PostgreSQL| Automáticos con retención mínima de 30 días   |
| Logs              | Centralizados, sin datos personales en texto claro |

### Estructura de archivos de configuración

```
eams/
  .env.example          ← En el repo (claves vacías, sin valores)
  .env                  ← Local únicamente (en .gitignore)
  .gitignore            ← Incluye: .env, *.pem, *.key, secrets/
  docker-compose.yml    ← Desarrollo local
  docker-compose.prod.yml ← Producción (sin credenciales hardcodeadas)
```

## Justificación

- **Reproducibilidad**: Docker Compose garantiza que todos los miembros del equipo usen las mismas versiones de PostgreSQL y Redis.
- **Seguridad de credenciales**: vault centralizado evita que las credenciales se filtren en el historial de Git.
- **Cumplimiento RNF04**: HTTPS/TLS obligatorio y secretos en vault cubren los estándares de seguridad para manejo de datos personales.
- **Portabilidad**: los contenedores pueden desplegarse en cualquier proveedor cloud sin cambiar la configuración de la aplicación.

## Consecuencias

- **Positivas**: entorno reproducible, secretos protegidos, despliegue portable.
- **Negativas**: requiere que todos los miembros del equipo tengan Docker instalado; overhead inicial de configuración del vault en producción.
- **Mitigación**: el `docker-compose.yml` incluido en el repositorio permite levantar el entorno completo con un solo comando (`docker compose up`). La documentación en el README describe el proceso de obtención de secretos desde Doppler.

## Alternativas descartadas

- **Variables de entorno planas en archivos `.env` en el repositorio**: descartadas porque exponen credenciales en el historial de Git y comprometen la seguridad de la plataforma.
- **Configuración hardcodeada en `application.properties`**: descartada por las mismas razones y por hacer imposible el despliegue en múltiples entornos sin modificar el código.
- **Kubernetes para el alcance actual**: descartado por sobredimensionar la infraestructura para la carga proyectada (5 instituciones, 5.000 usuarios). Queda como ruta de crecimiento si la escala lo exige.

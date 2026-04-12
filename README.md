# EAMS — Plataforma de Actividades Extracurriculares

Plataforma PWA para la gestión de actividades extracurriculares en instituciones educativas colombianas.

## Arquitectura

```
frontend/    → Next.js PWA (Service Worker, offline 48h)
gateway/     → API Gateway NestJS (JWT, RBAC, rate limiting)
backend/     → Spring Boot + Spring Modulith (lógica de negocio)
db/          → Migraciones PostgreSQL (Flyway)
specs/       → Especificaciones funcionales, técnicas y ADRs
```

## Requisitos

- Docker Desktop >= 24
- Java 21 (solo para desarrollo local del backend sin Docker)
- Node.js >= 20 (solo para desarrollo local de gateway/frontend sin Docker)

## Arranque rápido

```bash
# 1. Clonar el repositorio
git clone https://github.com/venus90210/EAMS.git
cd EAMS

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con los valores reales (ver sección Variables de entorno)

# 3. Levantar todos los servicios
docker compose up --build

# Servicios disponibles:
# Frontend:  http://localhost:3001
# Gateway:   http://localhost:3000
# Backend:   http://localhost:8080
# Swagger:   http://localhost:8080/swagger-ui.html
# PgAdmin:   conectar a localhost:5432
```

## Variables de entorno

Copiar `.env.example` → `.env` y completar:

| Variable              | Descripción                        | Requerida |
|-----------------------|------------------------------------|-----------|
| `POSTGRES_PASSWORD`   | Contraseña de PostgreSQL           | Sí        |
| `REDIS_PASSWORD`      | Contraseña de Redis                | Sí        |
| `JWT_SECRET`          | Clave secreta para firmar JWT      | Sí        |
| `SMTP_HOST`           | Host del servidor de correo        | Sí        |
| `SMTP_USER`           | Usuario SMTP                       | Sí        |
| `SMTP_PASSWORD`       | Contraseña SMTP                    | Sí        |

> **Nunca commitear el archivo `.env`** — está en `.gitignore`.

## Estructura de la base de datos

Las migraciones se ejecutan automáticamente al levantar el contenedor de PostgreSQL:

```
db/migrations/
  V1__create_institutions.sql       — Tabla de instituciones
  V2__create_users_and_students.sql — Usuarios, estudiantes, acudientes
  V3__create_activities.sql         — Actividades y horarios
  V4__create_enrollments.sql        — Inscripciones (con SELECT FOR UPDATE)
  V5__create_attendance.sql         — Sesiones y registros de asistencia
  V6__create_audit_log.sql          — Auditoría de cambios críticos
  V7__init_rls_config.sql           — Configuración Row-Level Security
```

## Ejecutar pruebas

```bash
# Pruebas unitarias (backend) — requiere Java 21
cd backend && mvn test jacoco:check

# Pruebas unitarias (gateway)
cd gateway && npm run test:cov

# Pruebas unitarias (frontend)
cd frontend && npm run test:cov

# Pruebas de integración (Testcontainers — requiere Docker)
cd backend && mvn test -Dgroups="integration"
```

## Especificaciones

| Documento                              | Descripción                              |
|----------------------------------------|------------------------------------------|
| `specs/PLAN.md`                        | Plan de implementación por fases         |
| `specs/functional/F1-F5.feature`       | Especificaciones funcionales en Gherkin  |
| `specs/technical/openapi/main.yaml`    | Contratos de API (OpenAPI 3.1)           |
| `specs/technical/adr/`                 | Decisiones arquitectónicas (AD-01~AD-10) |
| `specs/technical/testing-strategy.md` | Estrategia de pruebas unitarias e integración |

## Documentación de API

Con el backend corriendo:
```
http://localhost:8080/swagger-ui.html
```

O previsualizar la spec sin levantar el backend:
```bash
npx @redocly/cli preview-docs specs/technical/openapi/main.yaml
```

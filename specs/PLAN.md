# EAMS — Plan de Implementación

> **Plataforma de Gestión de Actividades Extracurriculares**
> Última actualización: 2026-04-11

## Leyenda de estados

| Símbolo | Estado       |
|---------|--------------|
| `[ ]`   | Pendiente    |
| `[~]`   | En progreso  |
| `[x]`   | Completado   |
| `[!]`   | Bloqueado    |

---

## Fase 0 — Infraestructura y scaffolding

> **Objetivo**: tener el entorno local funcionando con los tres contenedores levantados.
> **ADR de referencia**: AD-01, AD-10

### 0.1 Repositorio y estructura base
- [ ] Crear estructura de directorios del monorepo (`/backend`, `/gateway`, `/frontend`)
- [ ] Configurar `.gitignore` (incluir `.env`, `*.pem`, `*.key`, `node_modules`, `target/`)
- [ ] Crear `.env.example` con todas las variables requeridas sin valores
- [ ] Agregar `README.md` con instrucciones de arranque local

### 0.2 Docker Compose — entorno local
- [ ] Definir `docker-compose.yml` con servicios: `postgres`, `redis`, `backend`, `gateway`, `frontend`
- [ ] Configurar volúmenes persistentes para PostgreSQL
- [ ] Configurar healthchecks para `postgres` y `redis`
- [ ] Verificar que `docker compose up` levanta el entorno completo

### 0.3 Base de datos — schema inicial
> **ADR de referencia**: AD-08 (RLS), AD-07 (bloqueo pesimista)

- [ ] Crear migration inicial con tablas: `institutions`, `users`, `students`, `guardian_students`
- [ ] Crear tablas: `activities`, `schedules`
- [ ] Crear tablas: `enrollments`
- [ ] Crear tablas: `attendance_sessions`, `attendance_records`
- [ ] Crear tabla: `audit_log`
- [ ] Agregar campo `institution_id` en todas las tablas de dominio
- [ ] Habilitar Row-Level Security (RLS) en todas las tablas de dominio
- [ ] Crear políticas RLS por tabla (`tenant_isolation`)
- [ ] Crear índices: `(institution_id, status)` en `activities`, `(student_id, status)` en `enrollments`
- [ ] Agregar constraints de unicidad: `(student_id, activity_id)` en `enrollments`

---

## Fase 1 — Backend: Spring Boot Modulith

> **Objetivo**: implementar toda la lógica de negocio del monolito modular.
> **ADR de referencia**: AD-01, AD-02, AD-03

### 1.0 Setup del proyecto Spring Boot
- [ ] Crear proyecto con Spring Initializr: Spring Boot 3.x, Java 21
- [ ] Agregar dependencias: `spring-modulith`, `spring-data-jpa`, `spring-security`, `springdoc-openapi`
- [ ] Configurar conexión a PostgreSQL con pool HikariCP
- [ ] Configurar conexión a Redis (Lettuce)
- [ ] Configurar `TenantContextHolder` para propagar `institution_id` por petición
- [ ] Configurar `ApplicationEvent` publisher para eventos de dominio
- [ ] Escribir prueba de arquitectura Spring Modulith (verifica fronteras de módulos)

### 1.1 Módulo Auth & Security
> **Spec funcional**: F4-autenticacion.feature
> **Spec técnica**: specs/technical/openapi/auth.yaml
> **ADR**: AD-06

- [ ] Implementar entidad `User` con campos: `id`, `email`, `password_hash`, `role`, `institution_id`, `mfa_secret`
- [ ] Implementar `AuthService`: login con validación de credenciales
- [ ] Implementar generación de JWT (access token 15 min) con payload `{ sub, role, institutionId }`
- [ ] Implementar generación y almacenamiento de refresh token en Redis (TTL 7 días)
- [ ] Implementar `MfaService`: generación y verificación de código TOTP con `speakeasy` / `GoogleAuthenticator`
- [ ] Implementar flujo MFA: paso 1 (sesión temporal) → paso 2 (verificación TOTP) → emisión de tokens
- [ ] Implementar revocación de refresh token (`logout`, cambio de contraseña)
- [ ] Implementar `validatePermission(role, action)` para RBAC
- [ ] Escribir pruebas unitarias del módulo Auth (camino feliz + errores de F4)

### 1.2 Módulo Instituciones
> **ADR**: AD-08

- [ ] Implementar entidad `Institution` con campos: `id`, `name`, `email_domain`, `created_at`
- [ ] Implementar `InstitutionService`: CRUD, validación de dominio único
- [ ] Implementar `InstitutionContextProvider`: extrae y propaga `institution_id` al `TenantContextHolder`
- [ ] Restringir creación de instituciones a rol `SUPERADMIN`
- [ ] Escribir pruebas unitarias del módulo Instituciones

### 1.3 Módulo Usuarios
> **Spec técnica**: specs/technical/openapi/users.yaml

- [ ] Implementar entidad `Student` con campos: `id`, `first_name`, `last_name`, `grade`, `institution_id`, `guardian_id`
- [ ] Implementar `UserService`: registro, actualización, obtención de perfil
- [ ] Implementar `linkStudentToGuardian(guardianId, studentData)`
- [ ] Implementar `getStudentsByGuardian(guardianId)` con validación de institución
- [ ] Implementar carga masiva desde CSV (`bulkLoad`)
- [ ] Validar que todo usuario (excepto SUPERADMIN) tenga `institution_id` obligatorio
- [ ] Escribir pruebas unitarias del módulo Usuarios

### 1.4 Módulo Actividades
> **Spec funcional**: F5-estado-actividad.feature
> **Spec técnica**: specs/technical/openapi/activities.yaml
> **ADR**: AD-05 (caché Redis)

- [ ] Implementar entidad `Activity`: `id`, `name`, `description`, `status`, `total_spots`, `available_spots`, `institution_id`
- [ ] Implementar entidad `Schedule` (tabla informativa — no valida conflictos)
- [ ] Implementar `ActivityService`: crear (DRAFT), publicar (DRAFT→PUBLISHED), cambiar estado
- [ ] Implementar regla: `total_spots` inmutable; solo ADMIN puede modificarlo con registro en `audit_log`
- [ ] Implementar `getAvailableSpots(activityId)` con caché Redis (TTL 30s)
- [ ] Implementar invalidación de caché al cambiar estado o cupos
- [ ] Implementar filtro por rol: GUARDIAN ve solo PUBLISHED de su institución
- [ ] Publicar evento `ActivityStatusChanged` al deshabilitar/habilitar
- [ ] Escribir pruebas unitarias del módulo Actividades (incluyendo transiciones de estado inválidas)

### 1.5 Módulo Inscripciones
> **Spec funcional**: F1-inscripcion.feature
> **Spec técnica**: specs/technical/openapi/enrollment.yaml
> **ADR**: AD-07 (SELECT FOR UPDATE)

- [ ] Implementar entidad `Enrollment`: `id`, `student_id`, `activity_id`, `status`, `enrolled_at`, `cancelled_at`
- [ ] Implementar `EnrollmentService.enroll()` con:
  - [ ] Validación: acudiente es responsable del estudiante
  - [ ] Validación: actividad en estado PUBLISHED
  - [ ] `SELECT ... FOR UPDATE` sobre `available_spots` (AD-07)
  - [ ] Verificación: `available_spots > 0` → error 409 SPOT_EXHAUSTED
  - [ ] Verificación: sin enrollment ACTIVE del estudiante → error 409
  - [ ] INSERT enrollment + UPDATE `available_spots - 1` en la misma transacción `@Transactional`
  - [ ] Publicar evento `EnrollmentConfirmed`
- [ ] Implementar `cancelEnrollment()`: status → CANCELLED + `available_spots + 1` en misma transacción
- [ ] Implementar `getEnrollmentsByStudent()` con filtro por rol
- [ ] Implementar `getEnrollmentsByActivity()` (solo TEACHER asignado o ADMIN)
- [ ] Escribir pruebas de integración con concurrencia (simular race condition, verificar 0% sobrecupo)
- [ ] Escribir pruebas unitarias del módulo Inscripciones (todos los escenarios de F1)

### 1.6 Módulo Asistencia
> **Spec funcional**: F2-asistencia.feature
> **Spec técnica**: specs/technical/openapi/attendance.yaml

- [ ] Implementar entidad `AttendanceSession`: `id`, `activity_id`, `date`, `topics_covered`, `recorded_at`
- [ ] Implementar entidad `AttendanceRecord`: `id`, `session_id`, `student_id`, `present`, `observation`, `recorded_at`
- [ ] Implementar `AttendanceService.openSession()`: validar fecha = hoy (CalendarPort), validar docente asignado
- [ ] Implementar `recordAttendance()`: dentro de ventana 24h, máximo 3 toques por estudiante (RF13)
- [ ] Implementar `addObservation()`: dentro de ventana 24h → error 403 EDIT_WINDOW_EXPIRED si expiró
- [ ] Implementar `getAttendanceByStudent()` con filtro por acudiente (solo sus hijos)
- [ ] Publicar evento `ObservationPublished` al agregar observación
- [ ] Escribir pruebas unitarias del módulo Asistencia (ventana 24h, validación de fecha, permisos)

### 1.7 Módulo Notificaciones
> **ADR**: AD-09

- [ ] Implementar listener de `EnrollmentConfirmed` → encola email de confirmación
- [ ] Implementar listener de `SpotExhausted` → encola email de notificación
- [ ] Implementar listener de `ObservationPublished` → encola email al acudiente
- [ ] Implementar listener de `ActivityStatusChanged` → encola emails a acudientes afectados
- [ ] Implementar Worker de Notificaciones: consume cola Redis (BullMQ / Spring Scheduler)
- [ ] Implementar despacho de email vía SMTP / API transaccional (Resend / SendGrid)
- [ ] Garantizar idempotencia: deduplicación por ID de evento en Redis
- [ ] Configurar política de reintentos: 3 intentos con backoff exponencial
- [ ] Escribir prueba de integración: verificar que email se encola en <1s tras `EnrollmentConfirmed`

---

## Fase 2 — API Gateway: NestJS

> **Objetivo**: punto único de entrada con validación JWT, RBAC y rate limiting.
> **ADR de referencia**: AD-04, AD-06

### 2.0 Setup del proyecto NestJS
- [ ] Crear proyecto NestJS con TypeScript
- [ ] Instalar dependencias: `@nestjs/jwt`, `@nestjs/passport`, `passport-jwt`, `@nestjs/throttler`
- [ ] Configurar módulo de configuración con variables de entorno

### 2.1 Autenticación JWT
- [ ] Implementar `JwtStrategy` (Passport): extrae y valida token del header `Authorization: Bearer`
- [ ] Implementar `JwtAuthGuard`: guard aplicado globalmente
- [ ] Implementar extracción de `role` e `institutionId` del payload JWT

### 2.2 Control de acceso basado en roles (RBAC)
- [ ] Implementar decorador `@Roles(...roles)`
- [ ] Implementar `RolesGuard`: verifica que el rol del token tenga permiso para el endpoint
- [ ] Aplicar política de roles por módulo según tabla de AD-04
- [ ] Retornar HTTP 403 con `INSUFFICIENT_ROLE` ante acceso no autorizado

### 2.3 Rate limiting y CORS
- [ ] Configurar `ThrottlerModule`: límite por IP y por usuario (ej. 100 req/min)
- [ ] Configurar CORS con whitelist de orígenes permitidos
- [ ] Configurar headers de seguridad (Helmet)

### 2.4 Enrutamiento al backend
- [ ] Implementar proxy inverso hacia el backend Spring Boot
- [ ] Inyectar `institution_id` y `user_id` como headers internos hacia el backend
- [ ] Manejar errores de backend y normalizar respuestas de error

---

## Fase 3 — Frontend: Next.js PWA

> **Objetivo**: interfaz para los 4 roles con soporte offline de 48 horas.
> **ADR de referencia**: AD-05

### 3.0 Setup del proyecto Next.js
- [ ] Crear proyecto Next.js con TypeScript y App Router
- [ ] Instalar `next-pwa`, configurar Service Worker
- [ ] Crear `manifest.json` con iconos e información de la app
- [ ] Verificar instalación como PWA en Android Chrome

### 3.1 Autenticación y sesión
> **Spec funcional**: F4-autenticacion.feature

- [ ] Implementar pantalla de login con email y contraseña
- [ ] Implementar flujo MFA: pantalla de ingreso de código TOTP
- [ ] Implementar almacenamiento seguro de access token (memoria) y refresh token (httpOnly cookie)
- [ ] Implementar renovación silenciosa de access token antes de expiración
- [ ] Implementar logout con revocación de refresh token
- [ ] Implementar redirección por rol tras login exitoso

### 3.2 Módulo Actividades (GUARDIAN)
> **Spec funcional**: F5-estado-actividad.feature, F3-consulta-offline.feature

- [ ] Implementar listado de actividades publicadas con cupos disponibles
- [ ] Implementar detalle de actividad (horario, descripción, cupos)
- [ ] Cachear listado de actividades en Service Worker (48h offline)

### 3.3 Módulo Inscripción (GUARDIAN)
> **Spec funcional**: F1-inscripcion.feature

- [ ] Implementar formulario de inscripción: seleccionar hijo → seleccionar actividad → confirmar
- [ ] Mostrar disponibilidad de cupos en tiempo real
- [ ] Manejar errores: 409 SPOT_EXHAUSTED, 409 ALREADY_ENROLLED, 403 FORBIDDEN
- [ ] Mostrar confirmación tras inscripción exitosa
- [ ] Bloquear acción de inscripción en modo offline (F3)

### 3.4 Módulo Seguimiento (GUARDIAN)
> **Spec funcional**: F3-consulta-offline.feature

- [ ] Implementar vista de actividades inscritas por hijo
- [ ] Implementar historial de asistencia por actividad
- [ ] Cachear vistas de seguimiento en Service Worker (48h offline)

### 3.5 Módulo Asistencia (TEACHER)
> **Spec funcional**: F2-asistencia.feature

- [ ] Implementar apertura de sesión de asistencia (fecha = hoy)
- [ ] Implementar lista de inscritos con toggle presente/ausente (≤3 toques por estudiante)
- [ ] Implementar campo de observación con bloqueo al expirar ventana de 24h
- [ ] Cachear roster de la sesión en Service Worker (48h offline)

### 3.6 Módulo Administración (ADMIN)
> **Spec funcional**: F5-estado-actividad.feature

- [ ] Implementar CRUD de actividades (crear, editar, publicar, deshabilitar)
- [ ] Implementar gestión de usuarios (registrar docentes, cargar CSV de estudiantes)
- [ ] Implementar cambio de estado de actividades con confirmación

### 3.7 Indicador de modo offline
- [ ] Mostrar banner "Modo offline — datos de hace N horas" cuando no hay conexión
- [ ] Mostrar advertencia cuando el caché supera las 48 horas
- [ ] Ocultar acciones de escritura en modo offline

---

## Fase 4 — Pruebas e integración

> **Objetivo**: validar que la implementación cumple todas las specs funcionales y no funcionales.

### 4.1 Pruebas de contrato (OpenAPI)
- [ ] Instalar y configurar Dredd para validar el backend contra `main.yaml`
- [ ] Ejecutar Dredd en CI contra cada endpoint del backend
- [ ] Ejecutar `npx @redocly/cli lint specs/technical/openapi/main.yaml` en CI

### 4.2 Pruebas de comportamiento (Gherkin)
- [ ] Configurar Cucumber en el backend (Spring) para correr los `.feature`
- [ ] Implementar step definitions para F1 (inscripción)
- [ ] Implementar step definitions para F2 (asistencia)
- [ ] Implementar step definitions para F4 (autenticación)
- [ ] Implementar step definitions para F5 (estado de actividad)
- [ ] Configurar Playwright + Cucumber para F3 (consulta offline en navegador)

### 4.3 Pruebas de rendimiento
- [ ] Verificar RF04: disponibilidad de cupos en <1 segundo bajo carga
- [ ] Verificar RNF09: <3s en 95% de transacciones con 5.000 usuarios simulados (k6 / JMeter)
- [ ] Verificar RF07: email encolado en <1s, entregado en <60s

### 4.4 Pruebas de seguridad
- [ ] Verificar aislamiento RLS: usuario de inst-001 no puede ver datos de inst-002
- [ ] Verificar que refresh token revocado no permite renovación
- [ ] Verificar que roles sin permiso reciben 403 (tabla de AD-04)
- [ ] Verificar HTTPS en producción y cabeceras de seguridad

---

## Fase 5 — Despliegue

> **ADR de referencia**: AD-10

- [ ] Configurar secretos en Doppler (o AWS Secrets Manager)
- [ ] Crear pipeline CI/CD: lint → tests → build → deploy
- [ ] Desplegar PostgreSQL + Redis en proveedor cloud
- [ ] Desplegar backend, gateway y frontend en contenedores
- [ ] Configurar TLS / HTTPS
- [ ] Configurar backups automáticos de PostgreSQL (retención 30 días)
- [ ] Verificar logs centralizados sin datos personales en texto claro

---

## Matriz de trazabilidad: Tareas ↔ Specs

| Tarea clave                          | Feature / ADR                        | RF / RNF       |
|--------------------------------------|--------------------------------------|----------------|
| SELECT FOR UPDATE en inscripción     | F1-inscripcion · AD-07               | RF04, RF05     |
| Ventana de edición 24h (asistencia)  | F2-asistencia                        | RF13           |
| Service Worker caché 48h             | F3-consulta-offline · AD-05          | RNF08          |
| MFA para roles de escritura          | F4-autenticacion · AD-06             | RNF04          |
| RLS por institution_id               | AD-08                                | RNF06, RNF09   |
| Email asíncrono desacoplado          | AD-09                                | RF07           |
| RBAC en API Gateway                  | F4-autenticacion · AD-04             | RNF05          |
| Secretos en vault                    | AD-10                                | RNF04          |

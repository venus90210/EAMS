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

## Política de calidad — Pruebas unitarias

> **Cobertura mínima requerida: 95%** (líneas y ramas) en los tres contenedores.
> Referencia completa: [specs/technical/testing-strategy.md](technical/testing-strategy.md)

| Contenedor      | Framework       | Herramienta cobertura | Umbral  |
|-----------------|-----------------|-----------------------|---------|
| Backend         | JUnit 5 + Mockito | JaCoCo              | ≥ 95%   |
| API Gateway     | Jest + @nestjs/testing | Jest coverage  | ≥ 95%   |
| Frontend        | Jest + React Testing Library | Jest coverage | ≥ 95% |

**Regla de CI/CD**: ningún PR puede mergearse si alguno de los tres reportes de cobertura queda por debajo del 95%.

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
- [ ] Agregar dependencias de pruebas: `junit-jupiter`, `mockito-core`, `assertj-core`
- [ ] Configurar JaCoCo con umbral mínimo de 95% en líneas y ramas (falla el build si no se cumple)
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
- [ ] **Pruebas unitarias — Auth** (cobertura ≥ 95%)
  - [ ] `AuthService.login()`: credenciales correctas, incorrectas, usuario inactivo
  - [ ] `AuthService.refreshToken()`: token válido, revocado, expirado
  - [ ] `AuthService.logout()`: revocación exitosa en Redis
  - [ ] `MfaService.verifyTotp()`: código correcto, incorrecto, expirado
  - [ ] `JwtTokenProvider.generateToken()`: payload correcto, expiración correcta
  - [ ] `JwtTokenProvider.validateToken()`: token válido, malformado, expirado
  - [ ] `AuthService.validatePermission()`: cada combinación rol/acción de AD-04

### 1.2 Módulo Instituciones
> **ADR**: AD-08

- [ ] Implementar entidad `Institution` con campos: `id`, `name`, `email_domain`, `created_at`
- [ ] Implementar `InstitutionService`: CRUD, validación de dominio único
- [ ] Implementar `InstitutionContextProvider`: extrae y propaga `institution_id` al `TenantContextHolder`
- [ ] Restringir creación de instituciones a rol `SUPERADMIN`
- [ ] **Pruebas unitarias — Instituciones** (cobertura ≥ 95%)
  - [ ] `InstitutionService.create()`: dominio único, dominio duplicado (409)
  - [ ] `InstitutionService.update()`: solo SUPERADMIN, institución no encontrada
  - [ ] `InstitutionContextProvider.resolve()`: extracción correcta del `institution_id`

### 1.3 Módulo Usuarios
> **Spec técnica**: specs/technical/openapi/users.yaml

- [ ] Implementar entidad `Student` con campos: `id`, `first_name`, `last_name`, `grade`, `institution_id`, `guardian_id`
- [ ] Implementar `UserService`: registro, actualización, obtención de perfil
- [ ] Implementar `linkStudentToGuardian(guardianId, studentData)`
- [ ] Implementar `getStudentsByGuardian(guardianId)` con validación de institución
- [ ] Implementar carga masiva desde CSV (`bulkLoad`)
- [ ] Validar que todo usuario (excepto SUPERADMIN) tenga `institution_id` obligatorio
- [ ] **Pruebas unitarias — Usuarios** (cobertura ≥ 95%)
  - [ ] `UserService.register()`: email nuevo, email duplicado (409), rol no permitido
  - [ ] `UserService.linkStudentToGuardian()`: vinculación exitosa, acudiente inexistente, ya vinculado
  - [ ] `UserService.getStudentsByGuardian()`: padre ve solo sus hijos, filtro por institución
  - [ ] `UserService.bulkLoad()`: CSV válido, CSV con filas inválidas (reporte de errores parciales)

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
- [ ] **Pruebas unitarias — Actividades** (cobertura ≥ 95%)
  - [ ] `ActivityService.create()`: datos válidos → DRAFT, campos obligatorios faltantes (400)
  - [ ] `ActivityService.publish()`: DRAFT→PUBLISHED válido, PUBLISHED→PUBLISHED (409)
  - [ ] `ActivityService.updateStatus()`: PUBLISHED→DISABLED, DISABLED→PUBLISHED, institución incorrecta (403)
  - [ ] `ActivityService.update()`: ADMIN modifica `total_spots` + audit log, TEACHER intenta (403)
  - [ ] `ActivityService.listForRole()`: GUARDIAN ve solo PUBLISHED, TEACHER/ADMIN ven todos
  - [ ] `ActivityService.getAvailableSpots()`: retorna desde caché, retorna desde BD cuando caché vacío

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
- [ ] **Pruebas unitarias — Inscripciones** (cobertura ≥ 95%)
  - [ ] `EnrollmentService.enroll()`: cupo disponible → 201, cupo agotado → 409 SPOT_EXHAUSTED
  - [ ] `EnrollmentService.enroll()`: duplicado → 409 ALREADY_ENROLLED
  - [ ] `EnrollmentService.enroll()`: enrollment activo existente → 409 ACTIVE_ENROLLMENT_EXISTS
  - [ ] `EnrollmentService.enroll()`: padre no responsable del estudiante → 403
  - [ ] `EnrollmentService.enroll()`: actividad no publicada → 409
  - [ ] `EnrollmentService.cancelEnrollment()`: cancelación exitosa + cupo liberado
  - [ ] `EnrollmentService.cancelEnrollment()`: enrollment no encontrado → 404
  - [ ] `EnrollmentService.getByStudent()`: padre ve solo sus hijos, admin ve todos
  - [ ] `EnrollmentService.getByActivity()`: docente asignado, docente no asignado → 403
- [ ] Pruebas de integración con concurrencia: simular race condition, verificar 0% sobrecupo (SELECT FOR UPDATE)

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
- [ ] **Pruebas unitarias — Asistencia** (cobertura ≥ 95%)
  - [ ] `AttendanceService.openSession()`: fecha hoy → 201, fecha pasada → 422 INVALID_DATE
  - [ ] `AttendanceService.openSession()`: docente no asignado → 403, sesión duplicada → 409
  - [ ] `AttendanceService.recordAttendance()`: dentro de ventana → 200, fuera de ventana → 403
  - [ ] `AttendanceService.addObservation()`: dentro de ventana → 200, fuera de ventana → 403 EDIT_WINDOW_EXPIRED
  - [ ] `EditWindowPolicy.isEditable()`: exactamente en límite de 24h (boundary), 24h + 1s (expirado)
  - [ ] `AttendanceService.getByStudent()`: acudiente ve solo sus hijos, admin ve todos

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
- [ ] **Pruebas unitarias — Notificaciones** (cobertura ≥ 95%)
  - [ ] `NotificationListener.onEnrollmentConfirmed()`: evento encolado correctamente en Redis
  - [ ] `NotificationListener.onSpotExhausted()`: evento encolado correctamente
  - [ ] `NotificationListener.onObservationPublished()`: contiene email del acudiente correcto
  - [ ] `NotificationListener.onActivityStatusChanged()`: encola un email por cada acudiente afectado
  - [ ] `NotificationWorker.processEmail()`: envío exitoso, fallo con reintento, idempotencia (sin duplicados)
- [ ] Prueba de integración: email encolado en <1s tras publicación de `EnrollmentConfirmed`

---

## Fase 2 — API Gateway: NestJS

> **Objetivo**: punto único de entrada con validación JWT, RBAC y rate limiting.
> **ADR de referencia**: AD-04, AD-06

### 2.0 Setup del proyecto NestJS
- [ ] Crear proyecto NestJS con TypeScript
- [ ] Instalar dependencias: `@nestjs/jwt`, `@nestjs/passport`, `passport-jwt`, `@nestjs/throttler`
- [ ] Instalar dependencias de pruebas: `@nestjs/testing`, `jest`, `jest-mock-extended`
- [ ] Configurar Jest con umbral de cobertura ≥ 95% (líneas, ramas, funciones, sentencias)
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

### 2.5 Pruebas unitarias — API Gateway (cobertura ≥ 95%)
- [ ] `JwtAuthGuard.canActivate()`: token válido, expirado, malformado, ausente → 401
- [ ] `RolesGuard.canActivate()`: cada combinación rol/endpoint de la tabla AD-04
- [ ] `RolesGuard.canActivate()`: rol insuficiente → 403 INSUFFICIENT_ROLE
- [ ] `ThrottlerGuard`: request dentro del límite, request excedido → 429
- [ ] `ProxyService.forward()`: inyección correcta de headers `institution_id` y `user_id`
- [ ] `ErrorNormalizerInterceptor`: mapeo correcto de errores 4xx y 5xx del backend

---

## Fase 3 — Frontend: Next.js PWA

> **Objetivo**: interfaz para los 4 roles con soporte offline de 48 horas.
> **ADR de referencia**: AD-05

### 3.0 Setup del proyecto Next.js
- [ ] Crear proyecto Next.js con TypeScript y App Router
- [ ] Instalar `next-pwa`, configurar Service Worker
- [ ] Instalar dependencias de pruebas: `jest`, `@testing-library/react`, `@testing-library/user-event`, `msw`
- [ ] Configurar Jest con umbral de cobertura ≥ 95% (líneas, ramas, funciones, sentencias)
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

### 3.8 Pruebas unitarias — Frontend (cobertura ≥ 95%)

**Hooks**
- [ ] `useAuth()`: login exitoso, MFA requerido, error de credenciales, logout, renovación silenciosa de token
- [ ] `useEnrollment()`: inscripción exitosa, cupo agotado (409), duplicado (409), modo offline bloqueado
- [ ] `useOfflineStatus()`: online, offline, reconexión automática, caché expirado (>48h)
- [ ] `useActivities()`: carga desde API, carga desde caché cuando está offline

**Componentes**
- [ ] `<LoginForm />`: submit válido, validación de campos vacíos, flujo de pantalla MFA
- [ ] `<EnrollmentForm />`: selección hijo/actividad, confirmación, manejo de errores 409 y 403
- [ ] `<AttendanceList />`: toggle presente/ausente, límite de 3 toques por estudiante, campo observación
- [ ] `<OfflineBanner />`: visible en offline, oculto en online, advertencia cuando caché >48h
- [ ] `<ActivityCard />`: muestra cupos disponibles, estado deshabilitado, acción bloqueada offline

**Servicios / utils**
- [ ] `authService`: almacenamiento seguro de tokens, renovación antes de expiración, revocación al logout
- [ ] `apiClient`: inyección de Bearer token, manejo de 401 (dispara refresh), propagación de 403
- [ ] `cacheService`: lectura de caché, verificación de expiración 48h, escritura y limpieza

---

## Fase 4 — Pruebas e integración

> **Objetivo**: validar que la implementación cumple todas las specs funcionales y no funcionales.
> **Referencia completa**: [specs/technical/testing-strategy.md](technical/testing-strategy.md)

### 4.0 Setup de pruebas de integración
- [ ] Agregar dependencias: `testcontainers-postgresql`, `testcontainers-junit-jupiter`, `wiremock-jre8`
- [ ] Crear clase base `BaseIntegrationTest` con Testcontainers (PostgreSQL + Redis)
- [ ] Crear script `init-rls.sql` que aplica las políticas RLS en el contenedor de prueba
- [ ] Separar pruebas unitarias (`@Tag("unit")`) e integración (`@Tag("integration")`) en Maven Surefire
- [ ] Configurar CI para correr integration tests solo si las unitarias pasan

### 4.1 IT-01 — Inscripción concurrente sin sobrecupo
> **ADR**: AD-07 | **RF**: RF05 | **Clase**: `EnrollmentConcurrencyIT`

- [ ] Crear actividad con exactamente 1 cupo en PostgreSQL real (Testcontainers)
- [ ] Lanzar 10 hilos simultáneos con `ExecutorService` intentando inscribir
- [ ] Verificar que exactamente 1 inscripción es exitosa (HTTP 201)
- [ ] Verificar que `available_spots = 0` al final (nunca negativo)
- [ ] Verificar que 9 peticiones retornan HTTP 409 SPOT_EXHAUSTED

### 4.2 IT-02 — Aislamiento entre instituciones con RLS
> **ADR**: AD-08 | **RNF**: RNF06, RNF09 | **Clase**: `TenantIsolationIT`

- [ ] Crear inst-A e inst-B con datos propios en PostgreSQL real
- [ ] Ejecutar query sin filtro `WHERE institution_id` desde sesión de inst-A
- [ ] Verificar que RLS impide ver datos de inst-B (resultado vacío, no error)
- [ ] Repetir para tablas: `activities`, `enrollments`, `attendance_sessions`, `users`
- [ ] Verificar que Superadmin puede acceder a todas las instituciones

### 4.3 IT-03 — Revocación de refresh token en Redis
> **ADR**: AD-06 | **RNF**: RNF04, RNF06 | **Clase**: `TokenRevocationIT`

- [ ] Generar refresh token y almacenar en Redis real (Testcontainers)
- [ ] Llamar a POST /auth/logout → verificar DELETE en Redis
- [ ] Intentar POST /auth/refresh con el token revocado → verificar HTTP 401 TOKEN_REVOKED
- [ ] Verificar que el TTL natural del token no permite usarlo tras revocación

### 4.4 IT-04 — Flujo completo de notificación asíncrona
> **ADR**: AD-09 | **RF**: RF07 | **Clase**: `NotificationFlowIT`

- [ ] Configurar WireMock como servidor SMTP stub
- [ ] Ejecutar una inscripción completa en Spring Boot con Redis real
- [ ] Verificar que el evento `EnrollmentConfirmed` llega a la cola de Redis en <1s
- [ ] Verificar que el Worker consume el evento y llama al endpoint WireMock en <60s
- [ ] Verificar idempotencia: reencolar el mismo evento no genera un segundo email

### 4.5 Pruebas de contrato (OpenAPI)
- [ ] Instalar y configurar Dredd para validar el backend contra `main.yaml`
- [ ] Ejecutar Dredd en CI contra cada endpoint del backend
- [ ] Ejecutar `npx @redocly/cli lint specs/technical/openapi/main.yaml` en CI

### 4.6 Pruebas de comportamiento (Gherkin)
- [ ] Configurar Cucumber en el backend (Spring) para correr los `.feature`
- [ ] Implementar step definitions para F1 (inscripción)
- [ ] Implementar step definitions para F2 (asistencia)
- [ ] Implementar step definitions para F4 (autenticación)
- [ ] Implementar step definitions para F5 (estado de actividad)
- [ ] Configurar Playwright + Cucumber para F3 (consulta offline en navegador)

### 4.7 Pruebas de rendimiento
- [ ] Verificar RF04: disponibilidad de cupos en <1 segundo bajo carga
- [ ] Verificar RNF09: <3s en 95% de transacciones con 5.000 usuarios simulados (k6 / JMeter)
- [ ] Verificar RF07: email encolado en <1s, entregado en <60s

### 4.8 Pruebas de seguridad
- [ ] Verificar aislamiento RLS: usuario de inst-001 no puede ver datos de inst-002 (cubierto por IT-02)
- [ ] Verificar que refresh token revocado no permite renovación (cubierto por IT-03)
- [ ] Verificar que roles sin permiso reciben 403 (tabla de AD-04)
- [ ] Verificar HTTPS en producción y cabeceras de seguridad

---

## Fase 5 — Despliegue

> **ADR de referencia**: AD-10

- [ ] Configurar secretos en Doppler (o AWS Secrets Manager)
- [ ] Crear pipeline CI/CD con los siguientes jobs en orden:
  - [ ] `lint` — análisis estático de código
  - [ ] `test:backend` — JUnit + JaCoCo (bloquea si cobertura < 95%)
  - [ ] `test:gateway` — Jest (bloquea si cobertura < 95%)
  - [ ] `test:frontend` — Jest + RTL (bloquea si cobertura < 95%)
  - [ ] `build` — solo si los tres jobs de test pasan
  - [ ] `deploy` — solo si el build es exitoso
- [ ] Desplegar PostgreSQL + Redis en proveedor cloud
- [ ] Desplegar backend, gateway y frontend en contenedores
- [ ] Configurar TLS / HTTPS
- [ ] Configurar backups automáticos de PostgreSQL (retención 30 días)
- [ ] Verificar logs centralizados sin datos personales en texto claro

---

## Matriz de trazabilidad: Tareas ↔ Specs

| Tarea clave                          | Feature / ADR              | RF / RNF     | Prueba unitaria                              | Prueba integración |
|--------------------------------------|----------------------------|--------------|----------------------------------------------|--------------------|
| SELECT FOR UPDATE en inscripción     | F1-inscripcion · AD-07     | RF04, RF05   | `EnrollmentService.enroll()` todos los casos | IT-01 concurrencia |
| Ventana de edición 24h               | F2-asistencia              | RF13         | `EditWindowPolicy.isEditable()` boundary     | —                  |
| Service Worker caché 48h             | F3-consulta-offline · AD-05| RNF08        | `useOfflineStatus()`, `cacheService`         | —                  |
| MFA para roles de escritura          | F4-autenticacion · AD-06   | RNF04        | `MfaService.verifyTotp()`                    | IT-03 revocación   |
| RLS por institution_id               | AD-08                      | RNF06, RNF09 | —                                            | IT-02 aislamiento  |
| Email asíncrono desacoplado          | AD-09                      | RF07         | `NotificationWorker.processEmail()`          | IT-04 flujo <60s   |
| RBAC en API Gateway                  | F4-autenticacion · AD-04   | RNF05        | `RolesGuard.canActivate()` tabla AD-04       | —                  |
| Secretos en vault                    | AD-10                      | RNF04        | N/A (infraestructura)                        | —                  |
| Cobertura ≥ 95%                      | testing-strategy.md        | —            | Gate CI/CD: bloquea deploy si no se cumple   | Gate CI/CD: 4 IT obligatorias |

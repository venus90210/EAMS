# EAMS — Prompts Constructivos Consolidados

**Objetivo**: Documento que consolida los prompts más importantes y necesarios para replicar la construcción de EAMS desde cero, desde especificaciones hasta pruebas.

> **Nota**: Este documento es una guía de prompts estratégicos. Cada prompt debe ser refinado según el contexto específico de ejecución.

---

## Índice
1. [Fase 0 — Setup & Especificaciones](#fase-0)
2. [Fase 1 — Backend Spring Boot Modulith](#fase-1)
3. [Fase 2 — API Gateway NestJS](#fase-2)
4. [Fase 3 — Frontend Next.js PWA](#fase-3)
5. [Fase 4 — Testing (Backend & Frontend)](#fase-4)
6. [Checkpoints Críticos](#checkpoints)

---

## <a id="fase-0"></a>Fase 0 — Setup & Especificaciones

### 0.0 Definición de Requerimientos y Arquitectura

**Prompt**: 
```
Diseñar especificación ejecutiva para plataforma PWA de gestión de actividades 
extracurriculares en instituciones educativas colombianas. Incluir:

1. Problema: Gestión manual genera duplicidad, sobrecupos, falta de trazabilidad
2. Actores: Padre/Acudiente, Docente, Admin Institucional, Superadmin
3. Alcance: 
   - Múltiples instituciones simultáneas (mín 5)
   - Hasta 5,000 usuarios activos
   - Regulación colombiana (Ley 1581/2012 protección menores)
   - Soporte offline 48h (conectividad limitada)
4. Requerimientos funcionales clave:
   - RF04: Disponibilidad cupos <1 segundo
   - RF05: 0% registros duplicados
   - RF06: Detección conflicto horario 100% precisión
   - RF07: Email confirmación <60 segundos
5. Requerimientos no-funcionales:
   - RNF08: Offline 48h
   - RNF09: <3s respuesta 95% transacciones, 5000 usuarios

Proponer stack tecnológico con justificación por requerimiento.
Documentar en formato markdown con tabla de requerimientos.
```

**Resultado esperado**: Documento de especificación ejecutiva de 3-5 páginas con decisiones arquitectónicas justificadas.

---

### 0.1 Decisiones Arquitectónicas (ADRs)

**Prompt**:
```
Generar Architecture Decision Records (ADRs) para EAMS. Crear un ADR por cada 
decisión crítica siguiendo formato:

ADR-01: Monolito Modular (Spring Boot + Spring Modulith)
  - Contexto: Evaluar estilos arquitectónicos (monolito, modular, microservicios)
  - Decisión: Monolito Modular
  - Justificación: Tabla comparativa complejidad vs latencia vs costo
  - Mitigación: Patrón Strangler Fig para futura escala

ADR-02: Domain-Driven Design (DDD)
  - Contexto: Necesidad de mantener lógica de dominio clara
  - Decisión: Usar DDD con bounded contexts por módulo
  - Lenguaje ubicuo en español (Ley 1581, GDPR adaptado)

ADR-03: Arquitectura Hexagonal (Puertos & Adaptadores)
  - Contexto: Aislar lógica de negocio de infraestructura
  - Decisión: 3 capas (application, domain, infrastructure)
  - Inversión de dependencias hacia dominio

ADR-04: API Gateway (NestJS) para ruteo y validación
ADR-05: PWA con Service Worker (next-pwa) para offline 48h
ADR-06: JWT (15min) + Refresh Token (Redis) + MFA TOTP
ADR-07: SELECT FOR UPDATE (bloqueo pesimista) para 0% sobrecupo
ADR-08: Multi-tenancy con Row-Level Security (RLS) PostgreSQL
ADR-09: Notificaciones asíncronas via Redis/BullMQ (desacoplado)
ADR-10: Infraestructura Docker + Secrets via Doppler/AWS

Cada ADR debe tener:
- Estado (Aceptado, Propuesto, Descartado)
- Fecha y Autores
- Tabla de alternativas evaluadas
- Consecuencias (positivas/negativas/mitigaciones)
```

**Archivo esperado**: `/specs/technical/adr/AD-*.md` (uno por decisión)

---

### 0.2 Especificaciones Técnicas OpenAPI

**Prompt**:
```
Generar especificaciones OpenAPI 3.1 para los siguientes módulos:

1. Auth module: POST /auth/login, POST /auth/mfa/verify, POST /auth/refresh, POST /auth/logout
2. Users module: GET /api/users/{id}, PUT /api/users/{id}, GET /api/users/guardians/{id}/students
3. Activities module: GET, POST, PUT, DELETE /api/activities, GET /api/activities/{id}
4. Enrollments module: POST /api/enrollments, GET /api/enrollments/{id}, DELETE /api/enrollments/{id}
5. Attendance module: POST /api/attendance/sessions, POST /api/attendance/records

Para cada endpoint:
- Request/response schemas con validaciones
- Códigos HTTP (200, 401, 403, 404, 409, 422)
- Autenticación: Bearer JWT
- Ejemplos de request/response
- Rate limiting (si aplica)

Incluir definiciones compartidas: User, Activity, Enrollment, AttendanceSession, Error

Formato YAML organizado en carpeta /specs/technical/openapi/
```

**Archivos esperados**: `/specs/technical/openapi/*.yaml`

---

### 0.3 Especificaciones BDD (Gherkin)

**Prompt**:
```
Crear feature files Gherkin en español para flujos críticos:

F1-inscripcion.feature: 
  - Padre inscribe estudiante en actividad
  - Validaciones: cupos disponibles, horario conflictivo, duplicidad
  - Flujos: éxito, sobrecupo, error API

F2-asistencia.feature:
  - Docente abre sesión de asistencia
  - Registra presente/ausente con observaciones
  - Cierra sesión y sincroniza offline

F3-consulta-offline.feature:
  - Usuario visualiza actividades disponibles sin conexión
  - Sincronización cuando hay conexión
  - Conflictos de estado se resuelven servidor

F4-autenticacion.feature:
  - Login sin MFA, con MFA, cierre sesión
  - Refresh token automático
  - Sesión vencida

F5-estado-actividad.feature:
  - Admin crea actividad (DRAFT)
  - Publica (PUBLISHED)
  - Deshabilita (DISABLED)
  - Validaciones de estado

Formato: 3-5 scenarios por feature, con Given/When/Then en español
Incluir ejemplos de datos (tables de usuarios, actividades)
```

**Archivos esperados**: `/specs/functional/F*.feature`

---

### 0.4 Esquema de Base de Datos

**Prompt**:
```
Diseñar schema PostgreSQL 16 para EAMS. Incluir:

Tablas:
- institutions(id, name, email_domain, created_at, updated_at) - tenant
- users(id, email, password_hash, role, institution_id, mfa_secret, is_active, created_at, updated_at)
- students(id, student_number, guardian_id, institution_id, created_at, updated_at)
- guardian_students(guardian_id, student_id)
- activities(id, name, description, institution_id, status, total_spots, available_spots, schedule_json, created_at, updated_at)
- schedules(id, activity_id, day_of_week, start_time, end_time)
- enrollments(id, student_id, activity_id, institution_id, status, enrolled_at, created_at, updated_at)
  UNIQUE(student_id, activity_id, institution_id)
- attendance_sessions(id, activity_id, institution_id, date, opened_at, closed_at)
- attendance_records(id, session_id, enrollment_id, present, observations, recorded_at)
- audit_log(id, institution_id, user_id, action, target_entity, target_id, details, created_at)

Índices:
- (institution_id, status) en activities
- (student_id, status) en enrollments
- (activity_id, date) en attendance_sessions
- (session_id, enrollment_id) en attendance_records

Seguridad:
- Row-Level Security (RLS) en todas tablas de dominio
- Política: "usuarios solo ven datos de su institution_id"
- ENUM para status, roles (usando PostgreSQL ENUM tipo)

Constraints:
- CHECK enrollments.available_spots >= 0
- UNIQUE audit_log.id (para trazabilidad)

Optimizaciones:
- Índice GiST para timestamp ranges (audit_log)
- Particionamiento de audit_log por mes (RANGE partition)

Generar como migrations Flyway V001, V002, etc.
```

**Archivos esperados**: `/backend/src/main/resources/db/migration/V*.sql`

---

## <a id="fase-1"></a>Fase 1 — Backend Spring Boot Modulith

### 1.0 Setup del Proyecto Spring Boot

**Prompt**:
```
Configurar proyecto Spring Boot 3.x (Java 21) con Spring Modulith usando Maven.

Dependencias esenciales:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-modulith-starter-core
- spring-modulith-starter-jpa
- org.postgresql:postgresql (driver)
- org.springframework.data:spring-data-redis
- redis.clients:jedis (o lettuce)
- springdoc-openapi-starter-webmvc-ui (Swagger auto-generado)
- com.google.guava:guava (cache)
- de.taimos:totp (TOTP para MFA)

Testing:
- junit-jupiter
- mockito-core, mockito-inline
- assertj-core
- spring-boot-starter-test

Code quality:
- jacoco-maven-plugin (mín 95% cobertura)
- maven-enforcer-plugin
- spotbugs-maven-plugin
- dependency-check-maven

Estructura de módulos Spring Modulith:
src/main/java/com/eams/
  ├── auth/
  │   ├── application/
  │   ├── domain/
  │   └── infrastructure/
  ├── users/
  ├── institutions/
  ├── activities/
  ├── enrollments/
  ├── attendance/
  ├── notifications/
  └── shared/
      ├── init/
      ├── audit/
      └── config/

Cada módulo debe tener su propio DDD Bounded Context.

Configurar:
- TenantContextHolder para propagar institution_id por request
- ApplicationEvent publisher para eventos de dominio
- Spring Modulith module registry (verify en tests)
- HikariCP pool: max 20 connections
- Redis Lettuce config: mode=standalone
- Logging con SLF4J + Logback (profile-specific)
- SecurityConfig base (csrf=false, stateless JWT)

Verificación:
- mvn clean package -DskipTests
- docker build -t eams:latest .
- docker run -e SPRING_DATASOURCE_URL=... eams:latest
```

**Archivos esperados**:
- `backend/pom.xml`
- `backend/src/main/java/com/eams/**`
- `backend/src/main/resources/application.yml`, `application-prod.yml`
- `backend/Dockerfile`

---

### 1.1 Módulo Auth & Security (JWT + MFA)

**Prompt**:
```
Implementar módulo de autenticación con JWT 15min + Refresh Token Redis + MFA TOTP.

Domain Layer (DDD):
- Entidad User: {id, email, password_hash, role, institution_id, mfa_secret, is_active}
- Value Object Role: {GUARDIAN, TEACHER, ADMIN, SUPERADMIN}
- Value Object MfaStatus: {ENABLED, DISABLED, PENDING_SETUP}
- Service AuthDomainService: validar credenciales, generar JWT, validar MFA

Application Layer (Use Cases):
- LoginUserCommand: email, password → sessionToken (si MFA) o tokens (si no MFA)
- VerifyMfaCommand: sessionToken, mfa_code → accessToken, refreshToken
- RefreshTokenCommand: refreshToken → new accessToken, new refreshToken (rotación)
- LogoutCommand: refreshToken → revocación en Redis
- InitPasswordCommand: email → enviar email reset (async)

Infrastructure Layer (Adapters):
- JpaUserRepository: CRUD + find by email
- RedisSessionStore: almacenar sessionToken (5 min TTL), refreshToken (7 días TTL)
- JwtTokenProvider: generar/validar JWT con algoritmo HS256
- MfaService: generar secret TOTP con GoogleAuthenticator, validar código
- AuthController: endpoints /auth/login, /auth/mfa/verify, /auth/refresh, /auth/logout
- SecurityConfig: .csrf(disable), .sessionManagement(STATELESS), permitAll(/auth/**), denyAll(other)

Encryption:
- Password: BCrypt con salt 12 rounds
- JWT secret: 32 bytes aleatorios en .env
- MFA secret: base32 encoded

Validaciones:
- Email: formato válido, único por institution
- Password: mín 8 chars, al menos 1 mayúscula, 1 número, 1 símbolo
- TOTP code: 6 dígitos, ventana ±1 (30 segundos)

Pruebas Unitarias (JaCoCo ≥95%):
- AuthService.login(): GUARDIAN sin MFA, ADMIN con MFA, credenciales inválidas
- AuthService.mfaVerify(): código válido, inválido, sessionToken expirado
- AuthService.refreshToken(): token válido (con rotación), token revocado
- AuthService.logout(): revocación en Redis
- JwtTokenProvider: generar, validar, expirado, malformado
- MfaService: generar secret, validar código correcto/incorrecto

Seguridad:
- No loguear tokens, passwords, secrets
- Rate limiting en /auth/login (máx 5 intentos/5 min por IP)
- CORS solo desde gateway origin
```

**Archivos esperados**:
- `backend/src/main/java/com/eams/auth/domain/**`
- `backend/src/main/java/com/eams/auth/application/**`
- `backend/src/main/java/com/eams/auth/infrastructure/**`
- `backend/src/test/java/com/eams/auth/**Test.java` (cobertura ≥95%)

---

### 1.2 Módulo Activities (CRUD + Status)

**Prompt**:
```
Implementar módulo de actividades extracurriculares con DDD.

Domain Layer:
- Entidad Activity: {id, name, description, institution_id, status, total_spots, 
  available_spots, schedule, created_at, updated_at}
- Value Object Status: {DRAFT, PUBLISHED, DISABLED}
- Value Object Schedule: {dayOfWeek, startTime, endTime}
- Service ActivityDomainService: 
  - validateNameUniqueness()
  - validateScheduleConflict() (ej: no dos deportes viernes 4-6pm)
  - calculateAvailableSpots()

Application Layer:
- CreateActivityCommand: {name, description, totalSpots, schedule}
  → Activity (con available_spots = total_spots, status=DRAFT)
- UpdateActivityCommand: {id, ...fields}
  → Activity actualizada
- PublishActivityCommand: {id} → Activity con status=PUBLISHED
- DisableActivityCommand: {id} → Activity con status=DISABLED
- DeleteActivityCommand: {id}
- QueryActivitiesCommand: {institution_id, status?} → [Activity]

Infrastructure Layer:
- SpringDataActivityRepository: find*, save, delete
- ActivityController: GET /api/activities, POST, PUT, DELETE
- EventListener para audit_log (ACTIVITY_CREATED, ACTIVITY_PUBLISHED, etc)

Validaciones:
- name: required, max 100 chars
- totalSpots: min 1, max 500
- schedule: dayOfWeek válido (MONDAY-FRIDAY), startTime < endTime
- Conflicto horario: no dos actividades mismo schedule en misma institución
- Solo TEACHER o ADMIN pueden crear
- Solo ADMIN puede publicar
- Solo ADMIN de la institución puede modificar su actividad

RBAC: Rol-Based Access Control
- GUARDIAN: solo lectura PUBLISHED
- TEACHER: lectura + crear (status=DRAFT)
- ADMIN: CRUD + publicar/deshabilitar
- SUPERADMIN: CRUD todas las instituciones

Pruebas Unitarias (≥95%):
- ActivityService.createActivity(): validar nombre duplicado, cupos inválidos, schedule conflictivo
- ActivityService.publishActivity(): solo DRAFT → PUBLISHED, auditoria
- ActivityService.queryActivities(): filtrar por status, institution_id, permisos RBAC
- ScheduleConflictValidator: detectar horarios solapados
- Transaccionalidad: concurrent creates no generan race condition

Optimizaciones:
- Índice (institution_id, status) para queries frecuentes
- Cache en Redis de activities PUBLISHED por institución (TTL 1 hora)
```

**Archivos esperados**:
- `backend/src/main/java/com/eams/activities/domain/**`
- `backend/src/main/java/com/eams/activities/application/**`
- `backend/src/main/java/com/eams/activities/infrastructure/**`
- `backend/src/test/java/com/eams/activities/**Test.java`

---

### 1.3 Módulo Enrollments (0% Sobrecupo con SELECT FOR UPDATE)

**Prompt**:
```
Implementar módulo de inscripciones garantizando 0% sobrecupo usando 
bloqueo pesimista (SELECT FOR UPDATE).

Domain Layer:
- Entidad Enrollment: {id, student_id, activity_id, institution_id, status, enrolled_at}
- Value Object Status: {ACTIVE, CANCELLED}
- Service EnrollmentDomainService: 
  - validateStudentCapacity(studentId) [máx N actividades]
  - validateActivityCapacity(activityId) [mediante available_spots]

Application Layer:
- EnrollStudentCommand: {student_id, activity_id, institution_id}
  → Enrollment (status=ACTIVE)
  - Validaciones:
    * Activity existe y status=PUBLISHED
    * Student existe y pertenece a institution
    * Cupos disponibles > 0
    * No existe Enrollment previo (student_id, activity_id, institution_id)
  - Transacción:
    1. SELECT * FROM activities WHERE id=? FOR UPDATE (bloquea otros)
    2. Validar available_spots > 0
    3. Decrementar available_spots
    4. INSERT enrollment
    5. COMMIT (libera bloqueo)

- CancelEnrollmentCommand: {enrollment_id}
  - Validaciones: solo GUARDIAN puede cancelar su propia inscripción
  - Transacción: incrementar available_spots, marcar status=CANCELLED

- QueryEnrollmentsCommand: {student_id, institution_id} → [Enrollment]

Infrastructure Layer:
- SpringDataEnrollmentRepository: 
  @Query(value="SELECT * FROM enrollments WHERE activity_id=:id FOR UPDATE", nativeQuery=true)
- EnrollmentService con @Transactional(isolation=SERIALIZABLE)
- EnrollmentController: POST /api/enrollments, DELETE, GET
- EventListener: ENROLLMENT_CREATED → auditlog + notificación async

RBAC:
- GUARDIAN: crear/cancelar propias inscripciones
- TEACHER: ver inscripciones de actividades que dicta
- ADMIN: CRUD enrollments institución

Pruebas Unitarias (≥95%):
- EnrollmentService.enrollStudent(): 
  * Éxito: crea enrollment, decrementa cupos
  * Sin cupos: error
  * Duplicidad: error
  * Horario conflictivo: error
- ConcurrencyTest:
  * 100 hilos compiten por 10 cupos → 10 éxitos, 90 fallos (0% sobrecupo)
  * Validar available_spots correctamente decrementado
- Transactionality: rollback si error en INSERT

Seguridad:
- Bloqueo de lectura (SELECT FOR UPDATE) garantiza lectura-escritura atómica
- Timeout transacción: máx 5 segundos
- Rate limiting: máx 20 inscripciones/minuto por usuario
```

**Archivos esperados**:
- `backend/src/main/java/com/eams/enrollments/domain/**`
- `backend/src/main/java/com/eams/enrollments/application/**`
- `backend/src/main/java/com/eams/enrollments/infrastructure/**`
- `backend/src/test/java/com/eams/enrollments/**Test.java` (incluir ConcurrencyTest)

---

### 1.4 Módulo Attendance (Registro de Asistencia)

**Prompt**:
```
Implementar módulo de asistencia con sesiones de docente.

Domain Layer:
- Entidad AttendanceSession: {id, activity_id, institution_id, date, opened_at, closed_at}
- Entidad AttendanceRecord: {id, session_id, enrollment_id, present, observations, recorded_at}
- Service AttendanceDomainService: validateSessionActive()

Application Layer:
- OpenSessionCommand: {activity_id, date} → AttendanceSession (opened_at=NOW)
  Validaciones:
  * TEACHER solo puede abrir para actividades que dicta
  * No existe sesión abierta para mismo (activity_id, date)
  
- RecordAttendanceCommand: {session_id, enrollment_id, present, observations?}
  → AttendanceRecord
  Validaciones:
  * Enrollment debe estar ACTIVE
  * Session debe estar abierta (closed_at IS NULL)
  * Máximo 3 registros por enrollment por sesión (TOUCH LIMIT)
  
- CloseSessionCommand: {session_id} → session.closed_at=NOW
  Validaciones:
  * TEACHER que abrió sesión
  * Publica evento para sincronización offline

- QueryAttendanceCommand: {activity_id, date?} → [AttendanceRecord]

Infrastructure Layer:
- JpaAttendanceSessionRepository, JpaAttendanceRecordRepository
- AttendanceController: POST /api/attendance/sessions, POST /records, PUT /sessions/{id}
- EventListener: ATTENDANCE_RECORDED → sync offline + notify guardian

RBAC:
- TEACHER: crear/cerrar sesiones de sus actividades, registrar asistencia
- ADMIN: ver reportes asistencia
- GUARDIAN: ver asistencia de estudiantes (offline después de sync)

Pruebas Unitarias (≥95%):
- AttendanceService.openSession(): crear sesión, validar uniqueness
- AttendanceService.recordAttendance(): 
  * Registrar presente/ausente
  * Límite 3 toques (botón deshabilitado en UI)
  * Observaciones opcionales
- AttendanceService.closeSession(): session.closed_at actualizado
- TomuchTouchTest: más de 3 registros → rechazado

Offline Sync:
- AttendanceRecord se sincroniza cuando hay conexión (Service Worker)
- Datos locales sobrescriben servidor si timestamp local > servidor
```

**Archivos esperados**:
- `backend/src/main/java/com/eams/attendance/domain/**`
- `backend/src/main/java/com/eams/attendance/application/**`
- `backend/src/main/java/com/eams/attendance/infrastructure/**`
- `backend/src/test/java/com/eams/attendance/**Test.java`

---

### 1.5 Pruebas Unitarias Backend (JaCoCo ≥95%)

**Prompt**:
```
Implementar suite de pruebas unitarias backend con cobertura ≥95%.

Estructura:
src/test/java/com/eams/{module}/**Test.java

Por módulo:
1. Domain Layer Tests:
   - Entity tests: invariantes, value objects
   - Domain Service tests: lógica de dominio pura

2. Application Layer Tests:
   - Command handler tests: éxito, validaciones, errores
   - Event handler tests: side effects correctos
   
3. Infrastructure Layer Tests:
   - Repository tests: queries personalizadas
   - Controller tests: request/response mapping
   - Integration tests: flow completo con DB simulada

Testing Stack:
- JUnit 5 (@SpringBootTest, @DataJpaTest)
- Mockito: mock dependencies
- AssertJ: fluent assertions
- TestContainers: PostgreSQL real en tests (o H2 embedded)
- ArgumentCaptor: validar argumentos de mocks

Patrones:
- Arrange-Act-Assert (AAA)
- One assertion focus per test
- Fixtures en @BeforeEach o Factory methods
- @ParameterizedTest para múltiples casos

Configuración JaCoCo:
- pom.xml: 
  <plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <configuration>
      <rules>
        <rule>
          <element>PACKAGE</element>
          <excludes>
            <exclude>*Test</exclude>
            <exclude>*Configuration</exclude>
          </excludes>
          <limits>
            <limit>
              <counter>LINE</counter>
              <value>COVEREDRATIO</value>
              <minimum>0.95</minimum>
            </limit>
          </limits>
        </rule>
      </rules>
    </configuration>
  </plugin>

Ejecución:
mvn clean test jacoco:report
# Reporte en target/site/jacoco/index.html

Exclusiones:
- @Configuration classes
- Spring auto-generated code
- Main class
- Enums simples

Verificación:
mvn verify (falla si cobertura < 95%)
```

**Archivos esperados**:
- `backend/pom.xml` (JaCoCo plugin configurado)
- `backend/src/test/java/com/eams/**/*Test.java` (150+ tests)
- `backend/src/test/java/com/eams/ConcurrencyTest.java`
- `backend/src/test/resources/application-test.yml`

---

## <a id="fase-2"></a>Fase 2 — API Gateway NestJS

### 2.0 Setup del Proyecto NestJS

**Prompt**:
```
Configurar API Gateway NestJS que valida JWT, aplica RBAC, rate limiting y 
enruta a backend Spring Boot.

Stack:
- NestJS 10.x con TypeScript
- Express underlying
- @nestjs/jwt para validación JWT
- @nestjs/passport + passport-jwt
- @nestjs/throttler para rate limiting (5 req/seg por IP, 1000/día por user)
- axios para proxy HTTP a backend
- pino para logging
- helmet para seguridad HTTP headers
- @nestjs/cors for CORS whitelist

Estructura:
gateway/
├── src/
│   ├── main.ts
│   ├── app.module.ts
│   ├── auth/
│   │   ├── auth.service.ts
│   │   ├── jwt.strategy.ts
│   │   └── roles.guard.ts
│   ├── middleware/
│   │   ├── proxy.middleware.ts
│   │   └── error-handler.ts
│   ├── filters/
│   │   └── http-exception.filter.ts
│   └── config/
│       ├── configuration.ts
│       └── validate.env.ts

Funcionalidades:
1. JWT Validation:
   - Extraer token de Authorization: Bearer <token>
   - Validar firma con public key del backend (RS256)
   - Inyectar en req.user {sub, role, institutionId}

2. RBAC (Role-Based Access Control):
   - Guard @Roles(GUARDIAN, TEACHER, ADMIN)
   - Rechazar 403 si rol no autorizado

3. Rate Limiting:
   - Global: 5 req/seg por IP
   - Per-user: 1000/día
   - Excepto /health, /auth/* (sin límite)

4. Request Routing:
   - Proxy HTTP headers + body a backend:8080
   - Timeout 10 segundos
   - Retry logic (3 intentos con exponential backoff)
   - Preservar X-User-Role, X-Institution-ID headers

5. CORS:
   - Whitelist: frontend URL (ej http://localhost:3000)
   - Métodos: GET, POST, PUT, DELETE
   - Headers: Authorization, Content-Type
   - Credentials: true

6. Security Headers:
   - HSTS: max-age=31536000
   - X-Content-Type-Options: nosniff
   - X-Frame-Options: DENY
   - CSP: default-src 'self'
   - X-XSS-Protection: 1; mode=block

Validación de Configuración:
- Usar Joi para validar .env al startup
- Requeridas: JWT_SECRET, BACKEND_URL, JWT_ISSUER

Logging:
- Pino level: info (prod), debug (dev)
- Log every request: timestamp, method, path, status, duration
- NO loguear Authorization header, passwords

Tests:
- @nestjs/testing
- Mocks de axios para backend
- Validar JWT parsing, RBAC guards, rate limiting

Dockerfile:
- Multi-stage build
- Node 20-alpine
- Healthcheck: GET /health

Verificación:
npm run build
npm run start:prod
curl -H "Authorization: Bearer <token>" http://localhost:3001/api/activities
```

**Archivos esperados**:
- `gateway/package.json`
- `gateway/src/**/*.ts`
- `gateway/nest-cli.json`
- `gateway/.env.example`
- `gateway/Dockerfile`

---

## <a id="fase-3"></a>Fase 3 — Frontend Next.js PWA

### 3.0 Setup y Arquitectura

**Prompt**:
```
Configurar frontend Next.js 14 con App Router como PWA offline-first.

Stack:
- Next.js 14 + React 19
- next-pwa 5.x (Service Worker 48h offline)
- TypeScript
- Tailwind CSS + CSS Variables (tema LinkedIn)
- React Hook Form + Zod (validación)
- SWR / React Query para data fetching
- Zustand para estado global (auth, offline)
- next-intl para i18n (español)
- Playwright E2E tests
- Jest + React Testing Library para unit tests

Estructura:
frontend/
├── public/
│   └── manifest.json (PWA meta)
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── login/page.tsx
│   │   │   └── mfa/page.tsx
│   │   ├── guardian/
│   │   │   ├── activities/page.tsx
│   │   │   ├── enroll/page.tsx
│   │   │   └── tracking/page.tsx
│   │   ├── teacher/
│   │   │   └── attendance/page.tsx
│   │   ├── admin/
│   │   │   └── activities/page.tsx
│   │   └── layout.tsx
│   ├── components/
│   │   ├── auth/
│   │   ├── activities/
│   │   ├── attendance/
│   │   └── admin/
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useEnrollment.ts
│   │   ├── useAttendanceSessions.ts
│   │   └── useTracking.ts
│   ├── services/
│   │   ├── apiClient.ts (axios + interceptors)
│   │   └── authService.ts
│   ├── stores/
│   │   ├── authStore.ts (Zustand)
│   │   └── offlineStore.ts
│   ├── types/
│   │   └── index.ts (shared types con backend)
│   └── styles/
│       └── globals.css (CSS variables tema)

Tema LinkedIn (CSS Variables):
--primary: #0A66C2 (azul LinkedIn)
--accent: #D2D7DE (gris claro)
--surface: #FFFFFF
--muted: #6B7280
--text: #000000
--border: #D1D5DB
--background: #F3F2EF
--radius-lg: 12px
--radius-md: 8px

PWA Configuration (next-pwa):
- Register Service Worker en build time
- Cache strategies:
  * NetworkFirst para /api/* (revalidate cada 5 min)
  * StaleWhileRevalidate para static assets
  * CacheFirst para imágenes
- IndexedDB para offline state sync
- Periodic sync para background updates

Offline Mode (Zustand store):
- isOnline: boolean (detectado por navigator.onLine + ping)
- hasLocalChanges: boolean
- localQueue: Array<PendingRequest> (requests offline)
- syncQueue(): replay requests cuando conexión restaurada

Autenticación:
- JWT en localStorage (accessToken)
- Refresh token en localStorage con rotation
- Interceptor axios: inyectar Bearer <token>
- Auto-refresh en 401 (retry request)
- Logout si refresh falla

Validación (React Hook Form + Zod):
- Schema Zod para cada form
- Real-time validation on blur
- Error messages en español
- Custom hooks: useFormValidation(schema)

Tests:
- Jest: components, hooks, services (95% coverage)
- Playwright: E2E workflows (login, enroll, attendance)
- cypress: visual regression (opcional)

Verificación:
npm run dev
npm run build
npm run test -- --coverage
npm run e2e
```

**Archivos esperados**:
- `frontend/next.config.ts` (next-pwa configured)
- `frontend/public/manifest.json`
- `frontend/src/app/**/*.tsx`
- `frontend/src/components/**/*.tsx`
- `frontend/tailwind.config.ts`

---

### 3.1 Componentes de Autenticación

**Prompt**:
```
Implementar flujos de autenticación en frontend.

Páginas:
1. /login (LoginPage, LoginForm):
   - Email + Password form
   - Validación Zod: email, password 8+ chars
   - POST /auth/login → 
     * Sin MFA: guardar tokens, redirect /guardian/activities
     * Con MFA: guardar sessionToken, redirect /mfa
   - Error display: credenciales inválidas

2. /mfa (MfaPage, MfaForm):
   - Display QR para setup inicial (GoogleAuthenticator)
   - Input 6 dígitos TOTP
   - POST /auth/mfa/verify {sessionToken, code} → guardar tokens, redirect /guardian/activities
   - Validación: código 6 dígitos numéricos

3. AuthContext (React Context):
   - Provider: initializa sesión si hay refreshToken
   - Methods: login(), mfaVerify(), logout(), refreshSilently()
   - State: {user, isAuthenticated, loading, error}
   - useAuth hook para consumir contexto

4. ProtectedRoute (HOC):
   - Verifica isAuthenticated
   - Redirecta /login si no autenticado
   - Aplica @Roles guard en backend

Hooks:
- useAuth(): acceso a user, login, logout
- useAuthGuard(requiredRoles?): redirect si no autenticado/autorizado
- useMFA(): setup y verify MFA

Componentes:
- LoginForm: form con email/password
- MfaForm: input 6 dígitos + QR setup
- ProtectedLayout: layout si autenticado

Validaciones:
- Email: formato válido
- Password: 8+ chars, 1 mayús, 1 núm, 1 símbolo
- TOTP code: 6 dígitos

Offline:
- LoginForm deshabilitado en modo offline
- Ya autenticado: pueden acceder offline

Pruebas (Jest + RTL, ≥95%):
- LoginForm.test.tsx: render, submit, errors
- AuthContext.test.tsx: login, mfaVerify, logout, refreshSilently
- apiClient.test.ts: interceptors, 401 retry, token refresh
```

**Archivos esperados**:
- `frontend/src/app/(auth)/login/page.tsx`
- `frontend/src/app/(auth)/mfa/page.tsx`
- `frontend/src/components/auth/LoginForm.tsx`
- `frontend/src/components/auth/MfaForm.tsx`
- `frontend/src/contexts/AuthContext.tsx`
- `frontend/src/hooks/useAuth.ts`
- `frontend/src/__tests__/**Test.tsx`

---

### 3.2 Página de Actividades (GUARDIAN)

**Prompt**:
```
Implementar página /guardian/activities con catálogo de actividades.

Funcionalidad:
- Listar actividades PUBLISHED
- Filtros: estado, categoría, día de la semana
- Búsqueda: nombre / descripción
- Cards: nombre, descripción, cupos disponibles, horario, botón Inscribirse
- Modal inscripción: seleccionar estudiante + confirmar

Componentes:
1. ActivityCard:
   - Mostra: name, description, cupos (✓ 5/20), schedule (Mon 4-6pm)
   - Botón Inscribirse: habilita si cupos > 0 y online
   - Loading: spinner mientras procesa
   - Offline warning: botón deshabilitado
   - onClick → onEnroll callback

2. ActivityList:
   - Grilla responsive (1 col mobile, 2 tablet, 3 desktop)
   - Empty state: "No hay actividades disponibles"
   - Loading: skeleton cards
   - Filters sidebar: estado, día, categoría

3. EnrollmentModal:
   - Select: escoger estudiante (si padre tiene múltiples)
   - Validar: actividad, cupos, duplicidad
   - POST /api/enrollments {student_id, activity_id}
   - Success: toast, refresh lista
   - Error: modal error message

Página:
- Header: "Actividades Disponibles"
- useEffect: fetchActivities() al montar
- Estado: {activities, filters, isLoading, error}
- Offline: cached activities + "última actualización: 2 horas"

Hooks:
- useActivities({institutionId, status?: 'PUBLISHED'})
- useEnrollment({onSuccess?, onError?})

Validaciones:
- Inscribir solo si cupos > 0
- No inscribir si ya inscrito
- Máximo N actividades por estudiante

Responsive Design (LinkedIn style):
- Header con avatar + institution logo
- Cards con hover effect
- Modal centered con backdrop

Pruebas (≥95%):
- ActivityCard: render info, botón enroll, offline state
- ActivityList: filtros, búsqueda, empty state
- EnrollmentModal: seleccionar estudiante, validaciones

Offline:
- Service Worker cachea lista (StaleWhileRevalidate)
- IndexedDB: guardar actividades
- Indicador: "Online / Offline"
```

**Archivos esperados**:
- `frontend/src/app/guardian/activities/page.tsx`
- `frontend/src/components/activities/ActivityCard.tsx`
- `frontend/src/components/activities/ActivityList.tsx`
- `frontend/src/components/activities/EnrollmentModal.tsx`
- `frontend/src/hooks/useActivities.ts`
- `frontend/src/hooks/useEnrollment.ts`

---

### 3.3 Página de Asistencia (TEACHER)

**Prompt**:
```
Implementar página /teacher/attendance para registro de asistencia.

Funcionalidad:
- Docente abre sesión de asistencia
- Registra presente/ausente por estudiante
- Campo observaciones por estudiante
- Límite 3 toques por estudiante (seguridad)
- Cierra sesión cuando termina

Flujo:
1. Seleccionar actividad de hoy (datetime picker)
2. POST /api/attendance/sessions {activity_id, date} → session_id
3. Lista estudiantes inscritos en actividad
4. Por cada estudiante:
   - Toggle: Presente / Ausente (botón)
   - Contador de toques (máx 3)
   - Campo notas (opcional)
   - Guardar: POST /api/attendance/records {session_id, enrollment_id, present, observations}
5. Botón "Cerrar Sesión": PUT /api/attendance/sessions/{id} {closed_at: NOW}

Componentes:
1. AttendanceList:
   - Tabla: foto, nombre, estado (P/A), contador toques, notas
   - Expandible: click row abre observaciones
   - Botones: P (verde), A (rojo), deshabilitados después 3 toques
   - Loading state: deshabilitado durante sync

2. SessionHeader:
   - Mostrar: actividad, fecha, hora abierto
   - Botón "Cerrar Sesión": confirmación
   - Indicador online/offline

3. ObservationsPanel:
   - TextArea para notas por estudiante
   - Save button: guardar en attendanceRecords

Página:
- useEffect: fetchStudentsByActivity()
- useState: {session, students, records, isLoading}
- Validar: docente solo ve sus actividades

Offline:
- Service Worker cachea sesión abierta
- IndexedDB: guardar records localmente
- Sincronizar al cerrar sesión o reconectar
- Conflict: servidor timestamp > local → servidor gana

Hooks:
- useAttendanceSessions({activityId})
- useAttendanceRecords({sessionId, onSync?})

Validaciones:
- Solo docente que abrió puede cerrar
- No cerrar si no hay estudiantes registrados
- Máx 3 toques por estudiante (UI button disabled)

Pruebas (≥95%):
- AttendanceList: render estudiantes, toggle P/A, límite 3
- SessionHeader: mostrar info, cerrar sesión
- ObservationsPanel: guardar notas

Responsivo: mobile optimizado (touchable buttons)
```

**Archivos esperados**:
- `frontend/src/app/teacher/attendance/page.tsx`
- `frontend/src/components/attendance/AttendanceList.tsx`
- `frontend/src/components/attendance/SessionHeader.tsx`
- `frontend/src/components/attendance/ObservationsPanel.tsx`
- `frontend/src/hooks/useAttendanceSessions.ts`

---

## <a id="fase-4"></a>Fase 4 — Testing

### 4.1 Testing Backend (Spring Boot)

**Prompt**:
```
Estrategia de testing backend con JaCoCo ≥95% cobertura.

Pirámide de Tests:
- Unit (60%): Domain services, adapters, utilities
- Integration (30%): Repositories, controllers con base datos simulada
- Contract/API (10%): OpenAPI compliance (Fase 4.5)

Unit Tests (Domain Layer):
- ActivityDomainService.validateScheduleConflict()
- EnrollmentDomainService.validateStudentCapacity()
- AuthDomainService.validateCredentials()
- Value Objects: Schedule, Status, Role

Integration Tests (Application/Infrastructure):
- AuthService: login, mfaVerify, refreshToken, logout
- ActivityService: CRUD, publish, disable, queryByStatus
- EnrollmentService: enroll (validaciones + SELECTubmit FOR UPDATE), cancel
- AttendanceService: openSession, recordAttendance (límite 3), closeSession

Controller Tests (Request/Response):
- AuthController: /auth/login 200/401, /auth/mfa/verify 200/422
- ActivityController: GET /api/activities {status=PUBLISHED}, POST validaciones
- EnrollmentController: POST /api/enrollments (201, 409 dup, 412 sin cupos)
- AttendanceController: endpoints attendance_sessions y records

Concurrency Tests:
- 100 threads compiten por 10 cupos → 0% sobrecupo
- TransactionIsolation: SERIALIZABLE en enrollments

Setup:
- @SpringBootTest(webEnvironment = RANDOM_PORT)
- @DataJpaTest para repository tests
- TestContainers: PostgreSQL real (o H2 embedded)
- MockMvc: para controller tests
- Mockito: para external dependencies

Fixtures:
@BeforeEach setUp() {
  // crear user, institution, activity, student
}

Configuración JaCoCo:
pom.xml: <minimum>0.95</minimum>
Ejecutar: mvn clean test jacoco:report
Excluir: @Configuration, entities simples, enums

Coverage Report:
- target/site/jacoco/index.html
- Debe mostrar ≥95% lines, branches, methods

Ejecución:
mvn test -DskipIntegrationTests=false
mvn verify (falla si < 95%)
```

**Archivos esperados**:
- `backend/src/test/java/com/eams/**/*Test.java` (150+ tests)
- `backend/src/test/resources/application-test.yml`
- `backend/pom.xml` (JaCoCo configuration)

---

### 4.2 Testing Frontend (Jest + RTL)

**Prompt**:
```
Estrategia de testing frontend con Jest + React Testing Library ≥95% cobertura.

Stack:
- Jest 29.x + ts-jest
- React Testing Library (no Enzyme)
- @testing-library/user-event
- @testing-library/react
- MSW 2.x para mocking APIs (comentado, usar jest.mock())
- jest-axe para accesibilidad (optional)

Pirámide:
- Unit (50%): Components, hooks, utilities
- Integration (40%): Page flows, auth context, offline sync
- E2E (10%): Playwright (Fase 4.6)

Component Tests:
- LoginForm: render, submit, validaciones, errors
- ActivityCard: render info, botón, offline state
- AttendanceList: render estudiantes, toggle, límite 3 toques
- ActivityForm: create/edit modes, validaciones
- OfflineBanner: mostrar si offline

Hooks Tests (sin componentes):
- useAuth: login, logout, refreshSilently
- useEnrollment: enroll, cancel, retry
- useAttendanceSessions: openSession, recordAttendance
- useActivities: fetch, filter, cache
- useOfflineStore: sync queue, persist, restore

Service Tests:
- apiClient: interceptors, token injection, 401 retry
- authService: getAccessToken, storeTokens, clearTokens

Patrones:
- renderHookWithAuth: hook custom que proporciona AuthContext
- jest.mock('@/services/apiClient'): no hacer requests reales
- waitFor(() => expect(...)): async assertions
- act(async () => { ... }): state updates

Setup:
jest.config.js: moduleNameMapper, testEnvironment, setupFiles
setupFilesAfterEnv: jest-setup.ts (configure jest-dom, MSW)

Fixtures:
const mockActivities = [{ id: '1', name: 'Fútbol', ... }]
const mockUser = { id: 'u1', role: 'GUARDIAN', ... }

Ejecución:
npm test -- --coverage
npm test -- --watch (modo desarrollo)
npm test -- LoginForm.test.tsx (test específico)

Coverage Report:
coverage/index.html
Debe mostrar ≥95% lines, branches, functions, statements

Configuración:
jest.config.js:
{
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleNameMapper: { '@/(.*)': '<rootDir>/src/$1' },
  collectCoverageFrom: [ 'src/**/*.{ts,tsx}', '!src/**/*.d.ts', '!src/app/**' ],
  coverageThreshold: {
    'src/components/**': { branches: 95, functions: 95, lines: 95 },
    'src/hooks/**': { branches: 95, functions: 95, lines: 95 },
    'src/services/**': { branches: 95, functions: 95, lines: 95 },
  }
}
```

**Archivos esperados**:
- `frontend/jest.config.js`
- `frontend/src/**/__tests__/*.test.tsx` (100+ tests)
- `frontend/src/test-utils.tsx`
- `frontend/coverage/index.html`

---

### 4.3 Testing OpenAPI & Contract (Phase 4.5)

**Prompt**:
```
Validar que backend implementa especificación OpenAPI usando Dredd.

Setup:
- npm install -g dredd
- Especificación: /specs/technical/openapi/compiled.yaml
- Backend corriendo en localhost:8080

Dredd Configuration (dredd.yml):
endpoint: http://localhost:8080
apiDescriptionLocation: ./specs/technical/openapi/compiled.yaml
hookfiles: ./tests/contract/**/*.hook.js
reporter: [ json, html ]

Contract Tests:
- GET /api/activities: valida response schema
- POST /auth/login: valida request/response, códigos de error
- PUT /api/activities/{id}: validar que solo ADMIN puede
- POST /api/enrollments: validar enrollment schema, 409 duplicate

Hooks (fixtures):
- beforeAll: crear institution, user, activity
- beforeEach: inyectar tokens en headers
- afterEach: cleanup

Ejecución:
dredd ./specs/technical/openapi/compiled.yaml http://localhost:8080
# Genera reporte de compliance

Resultado:
- ✓ 50/50 transactions passed
- ✗ Schema mismatches debugged
- HTML report: dredd_reports/report.html
```

**Archivos esperados**:
- `/specs/technical/openapi/compiled.yaml`
- `/tests/contract/dredd.yml`
- `/tests/contract/**/*.hook.js`

---

### 4.4 Testing BDD (Phase 4.6)

**Prompt**:
```
Tests BDD con Cucumber + Playwright en español.

Feature Files (Gherkin):
/tests/bdd/features/*.feature

F1-inscripcion.feature:
Escenario: Padre inscribe estudiante en actividad disponible
  Dado que soy un padre autenticado
  Y tengo un estudiante "Juan" inscrito
  Y existe una actividad "Fútbol" publicada con 10 cupos
  Cuando selecciono la actividad "Fútbol"
  Y hago clic en "Inscribirse"
  Y confirmo la inscripción de "Juan"
  Entonces veo mensaje "Inscripción exitosa"
  Y Juan aparece en "Mis inscripciones"

Step Definitions (TypeScript):
Given('que soy un padre autenticado', async function() {
  await this.page.goto('http://localhost:3000/login')
  await this.page.fill('[name="email"]', 'padre@example.com')
  await this.page.fill('[name="password"]', 'password123')
  await this.page.click('button[type="submit"]')
  await this.page.waitForURL('**/activities')
})

Execution:
npm run test:bdd

Coverage:
- Flujos críticos: login, enroll, attendance, admin activities
- Validaciones: sobrecupo, duplicidad, horario
- Errores: credenciales inválidas, timeout
```

**Archivos esperados**:
- `/tests/bdd/features/*.feature`
- `/tests/bdd/steps/**/*.step.ts`
- `/tests/bdd/hooks.ts`

---

### 4.5 Testing Performance (Phase 4.7)

**Prompt**:
```
Tests de rendimiento con k6 / JMeter verificando RNF09.

RNF09: <3s respuesta 95% de transacciones, 5.000 usuarios
RNF04: Cupos disponibles <1 segundo

Script k6:
/tests/performance/load.js

Escenarios:
1. Ramp-up: 0 → 5000 usuarios en 10 min
2. Stress: 5000 usuarios por 5 min
3. Spike: +50% usuarios por 30 seg

Métricas:
- http_req_duration: p95 < 3000ms ✓
- http_req_failed: 0%
- iterations_completed: 5000
- data_sent / data_received: bandwidth

VU Stages:
stages: [
  { duration: '2m', target: 500 },  // ramp-up
  { duration: '5m', target: 5000 }, // full load
  { duration: '1m', target: 5000 }, // stress
  { duration: '30s', target: 6000 }, // spike
  { duration: '2m', target: 0 },    // ramp-down
]

Ejecución:
k6 run tests/performance/load.js

Resultado:
✓ p95 de latencia < 3s
✓ 0% errores bajo carga
✓ Cupos endpoint < 1s (RF04)
```

**Archivos esperados**:
- `/tests/performance/load.js` (k6 script)
- `/tests/performance/results/` (HTML report)

---

### 4.6 Security Testing (Phase 4.8 & 4.9)

**Prompt**:
```
Tests de seguridad validando OWASP Top 10.

Análisis:
1. SQL Injection: Verificar que queries usan parámetros (JPA)
2. XSS: Inputs sanitizados, CSP headers presentes
3. Broken Authentication: Tokens expirados rechazados
4. Broken Authorization: RLS aísla datos por institution_id
5. Sensitive Data Exposure: HTTPS, tokens no en logs
6. CORS: Solo orígenes whitelistados
7. Headers HTTP: HSTS, X-Content-Type-Options, etc.

Tests:
- Intentar bypass RLS: usuario inst-001 no ve inst-002
- Intentar refresh token revocado: 401
- Intentar escalada de privilegios: GUARDIAN no puede crear activity
- Intentar CORS desde origen no whitelist: 403
- Validar headers: curl -i http://localhost:8080/api/activities | grep HSTS

OWASP Dependency Check:
mvn dependency-check:check
# Detecta CVEs en dependencias

Ejecución:
npm run test:security
mvn dependency-check:check
```

**Archivos esperados**:
- `backend/src/test/java/com/eams/SecurityHeadersTest.java`
- `backend/src/test/java/com/eams/RlsTest.java`
- `backend/src/test/java/com/eams/AuthorizationTest.java`

---

## <a id="checkpoints"></a>Checkpoints Críticos

### Verificación Final

Antes de considerar cada fase completada:

**Fase 0** ✓
- [ ] Especificación ejecutiva aprobada
- [ ] 10+ ADRs documentados
- [ ] OpenAPI YAML compilado y validado
- [ ] 5+ feature files Gherkin
- [ ] Schema PostgreSQL con migrations

**Fase 1** ✓
- [ ] Backend compila sin warnings
- [ ] Todos los tests pasan
- [ ] JaCoCo ≥95% en líneas y ramas
- [ ] Docker image construida
- [ ] Endpoints accesibles en localhost:8080

**Fase 2** ✓
- [ ] Gateway compila y levanta
- [ ] JWT validation funciona
- [ ] Rate limiting activo
- [ ] CORS whitelist activo
- [ ] Proxy a backend funciona
- [ ] Docker image construida

**Fase 3** ✓
- [ ] Frontend compila sin warnings
- [ ] Dev server levanta en localhost:3000
- [ ] Todos los tests pasan
- [ ] Service Worker registrado y funcional
- [ ] Offline mode funciona (simular desconexión)
- [ ] PWA instalable en navegador
- [ ] LinkedIn styling aplicado

**Fase 4** ✓
- [ ] Backend: 150+ tests, cobertura ≥95%
- [ ] Frontend: 100+ tests, cobertura ≥95%
- [ ] OpenAPI contract tests pasan (Dredd)
- [ ] BDD scenarios ejecutan sin fallos (Cucumber)
- [ ] Performance: p95 latencia <3s bajo 5000 usuarios
- [ ] Security: RLS aisla datos, headers presentes, sin CVEs

---

## Notas de Implementación

1. **Orden de Ejecución**: Fases deben ejecutarse secuencialmente (0 → 1 → 2 → 3 → 4)

2. **Stack Decidido**: No proponer alternativas a menos que user explícitamente lo pida

3. **DDD Patterns**: Mantener separación domain/application/infrastructure en backend

4. **Testing First**: Escribir tests mientras se implementa (no after)

5. **Documentation**: Cada componente debe tener docstring (función, clase, archivo)

6. **Security**: 
   - No loguear tokens/passwords
   - Validar en boundaries (user input, external APIs)
   - RLS en PostgreSQL para multi-tenancy

7. **Offline**: Service Worker + IndexedDB para sync diferido

8. **Monitoring**: Logs estructurados (JSON) con correlation IDs

---

**Última Actualización**: 13 de Abril de 2026
**Generado para**: Replicación de EAMS desde cero

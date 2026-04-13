# EAMS — Prompts Reales de Construcción

**Objetivo**: Documento que consolida los prompts reales y específicos usados para construir EAMS, desde especificaciones hasta pruebas. Útil como blueprint para replicar el proyecto.

> **Contexto**: Cada prompt está basado en lo que se ejecutó realmente. Este es un registro de la cadena causal: Prompt → Especificaciones → Plan → Implementación → Testing.

---

## Tabla de Contenidos
1. [Prompt Inicial: Especificaciones](#prompt-inicial)
2. [Prompt de Planificación: PLAN.md](#prompt-plan)
3. [Prompts de Implementación por Fase](#prompts-impl)
4. [Prompts de Testing](#prompts-testing)

---

## <a id="prompt-inicial"></a>Prompt 1: Especificaciones Completas `/specs`

Este fue el primer prompt que generó toda la carpeta `/specs/` (ADRs, OpenAPI, Features Gherkin, etc.).

### Prompt Original

```
Crear especificación técnica y funcional completa para plataforma PWA 
de gestión de actividades extracurriculares en instituciones educativas 
colombianas. 

**Contexto del problema:**
- Gestión manual genera: duplicidad de inscripciones, sobrecupos, 
  falta de trazabilidad, baja visibilidad para padres
- Regulación: Ley 1581/2012 (protección datos menores)
- Contexto: Instituciones educativas en Colombia con conectividad limitada

**Alcance:**
- PWA (Progressive Web App) accesible por navegador
- Actores: Padre/Acudiente, Docente, Admin Institucional, Superadmin
- Múltiples instituciones simultáneas (mín 5)
- Hasta 5,000 usuarios activos
- Soporte offline 48h (caché local con Service Worker)

**Requerimientos Funcionales Críticos (RF):**
- RF01: Padre inscribe estudiante en actividad (create)
- RF02: Sistema detecta conflicto horario automáticamente
- RF03: Imposible sobrecupos (0% duplicidad)
- RF04: Disponibilidad cupos en <1 segundo
- RF05: Docente registra asistencia sin conexión, sincroniza después
- RF06: Admin publica/deshabilita actividades
- RF07: Email confirmación inscripción en <60 segundos

**Requerimientos No-Funcionales (RNF):**
- RNF08: Funcionamiento offline 48h con Service Worker
- RNF09: <3 segundos respuesta en 95% de transacciones con 5,000 usuarios
- RNF10: Cumplimiento Ley 1581/2012 (datos menores encriptados)

**Generar:**

1. **README.md** del proyecto:
   - Descripción del problema y solución
   - Actors y user journeys principales
   - Stack tecnológico decidido con justificación

2. **Carpeta `/specs/functional/`**: Feature files Gherkin en español:
   - F1-inscripcion.feature: padre inscribe estudiante
   - F2-asistencia.feature: docente registra asistencia
   - F3-consulta-offline.feature: acceso offline 48h
   - F4-autenticacion.feature: login, MFA, refresh token
   - F5-estado-actividad.feature: admin gestión de actividades
   
   Cada feature con 3-5 scenarios específicos (Given/When/Then)

3. **Carpeta `/specs/technical/adr/`**: Architecture Decision Records (ADRs):
   - AD-01: Monolito Modular (vs microservicios)
   - AD-02: Domain-Driven Design (DDD)
   - AD-03: Arquitectura Hexagonal
   - AD-04: API Gateway (NestJS) para ruteo + validación
   - AD-05: PWA con next-pwa (Service Worker 48h)
   - AD-06: JWT 15min + Refresh Token Redis + MFA TOTP
   - AD-07: SELECT FOR UPDATE (bloqueo pesimista) para 0% sobrecupo
   - AD-08: Multi-tenancy con Row-Level Security (RLS) PostgreSQL
   - AD-09: Notificaciones async via Redis/BullMQ
   - AD-10: Infra Docker Compose + Secrets (Doppler/AWS)

   Cada ADR con: Estado, Contexto, Decisión, Justificación (tabla comparativa), 
   Alternativas descartadas, Consecuencias

4. **Carpeta `/specs/technical/openapi/`**: Especificaciones OpenAPI 3.1:
   - auth.yaml: /auth/login, /auth/mfa/verify, /auth/refresh, /auth/logout
   - users.yaml: /api/users/{id}, /api/users/guardians/{id}/students
   - activities.yaml: GET/POST/PUT/DELETE /api/activities, GET /api/activities/{id}
   - enrollments.yaml: POST/DELETE /api/enrollments
   - attendance.yaml: POST /api/attendance/sessions, POST /api/attendance/records

   Cada endpoint con: Request schemas, Response schemas, códigos HTTP 
   (200, 401, 403, 404, 409, 422), ejemplos request/response, autenticación (Bearer JWT)

5. **Carpeta `/specs/technical/`**: Documentación técnica:
   - database-schema.md: Tablas, índices, constraints, RLS policies
   - testing-strategy.md: Pirámide de tests, cobertura ≥95% (JaCoCo, Jest)
   - security-design.md: JWT, MFA TOTP, RLS, HTTPS, headers de seguridad

**Formato:**
- Markdown (.md) para ADRs, README, documentación técnica
- YAML para OpenAPI
- Gherkin (.feature) para scenarios BDD
- Organización clara en carpetas por tipo

**Validaciones incluir:**
- Especificaciones deben ser mutuamente consistentes
- Todos los RF deben mapear a algún feature file
- Todos los ADRs deben justificar decisiones arquitectónicas
- OpenAPI debe ser validable (no sintaxis errors)
- Ejemplo data: al menos 3 usuarios, 3 instituciones, 5 actividades
```

### Resultado Generado

```
/specs/
├── README.md (problema, solución, stack)
├── functional/
│   ├── F1-inscripcion.feature
│   ├── F2-asistencia.feature
│   ├── F3-consulta-offline.feature
│   ├── F4-autenticacion.feature
│   └── F5-estado-actividad.feature
├── technical/
│   ├── adr/
│   │   ├── AD-01-monolito-modular.md
│   │   ├── AD-02-domain-driven-design.md
│   │   ├── ... (10 ADRs total)
│   ├── openapi/
│   │   ├── auth.yaml
│   │   ├── users.yaml
│   │   ├── activities.yaml
│   │   ├── enrollments.yaml
│   │   └── attendance.yaml
│   ├── database-schema.md
│   ├── testing-strategy.md
│   └── security-design.md
```

**Verificación**: Todos los archivos existen, OpenAPI valida, 50+ scenarios BDD ✓

---

## <a id="prompt-plan"></a>Prompt 2: Generación de PLAN.md a partir de `/specs`

Una vez creadas las especificaciones, se generó el `PLAN.md` como hoja de ruta de implementación.

### Prompt Original

```
A partir de las especificaciones técnicas y funcionales en `/specs/`, 
generar un PLAN.md detallado que sirva como hoja de ruta para 
implementación de EAMS.

**Input:**
- `/specs/functional/*.feature` (5 feature files con ~40 scenarios)
- `/specs/technical/adr/*.md` (10 ADRs)
- `/specs/technical/openapi/*.yaml` (5 archivos OpenAPI)
- `/specs/technical/database-schema.md`
- `/specs/technical/testing-strategy.md`

**Output esperado:**

Archivo: `PLAN.md` en raíz con estructura:

1. **Header:**
   - Título: "EAMS — Plan de Implementación"
   - Última actualización: fecha
   - Estado general: lista fases (0-4) con checkmarks

2. **Fases 0-4:**
   Cada fase debe:
   - Tener objetivo claro
   - Referenciar ADRs asociados
   - Listar tareas granulares ([x] completado, [ ] pendiente, [~] en progreso)
   - Indicar archivos a crear/modificar
   - Incluir criterios de aceptación (tests, cobertura, etc)

3. **Desglose por fase:**

   **Fase 0 - Infraestructura:**
   - 0.1 Repositorio y estructura base
   - 0.2 Docker Compose local
   - 0.3 Base de datos (schema + migrations)

   **Fase 1 - Backend Spring Boot (Modulith):**
   - 1.0 Setup proyecto (dependencias, config)
   - 1.1 Módulo Auth & Security (JWT + MFA)
   - 1.2 Módulo Instituciones (multi-tenancy)
   - 1.3 Módulo Usuarios (estudiantes, acudientes)
   - 1.4 Módulo Actividades (CRUD + estado)
   - 1.5 Módulo Enrollments (0% sobrecupo con SELECT FOR UPDATE)
   - 1.6 Módulo Attendance (asistencia)
   - 1.7 Notificaciones (async email)
   - 1.8 Pruebas unitarias backend (JaCoCo ≥95%)

   **Fase 2 - API Gateway NestJS:**
   - 2.0 Setup proyecto (dependencias, config)
   - 2.1 JWT validation + RBAC guards
   - 2.2 Rate limiting + CORS
   - 2.3 HTTP proxy a backend
   - 2.4 Pruebas unitarias gateway (Jest ≥95%)

   **Fase 3 - Frontend Next.js PWA:**
   - 3.0 Setup proyecto (next-pwa, Tailwind, auth context)
   - 3.1 Componentes autenticación (login, MFA)
   - 3.2 Página actividades (GUARDIAN)
   - 3.3 Página asistencia (TEACHER)
   - 3.4 Admin panel actividades (ADMIN)
   - 3.5 Página tracking (GUARDIAN)
   - 3.6 Componentes offline (banner, sync queue)
   - 3.7 Styling LinkedIn (CSS variables, responsive)
   - 3.8 Pruebas unitarias frontend (Jest ≥95%)

   **Fase 4 - Testing & Quality:**
   - 4.1 Testing backend (JaCoCo report, CI/CD)
   - 4.2 Testing frontend (Jest coverage, CI/CD)
   - 4.3 Contract testing (Dredd vs OpenAPI)
   - 4.4 BDD testing (Cucumber + Playwright)
   - 4.5 Performance testing (k6: RF04, RNF09)
   - 4.6 Security testing (OWASP, RLS, headers)

4. **Configuración de calidad:**
   - Cobertura JaCoCo: ≥95% líneas y ramas
   - Cobertura Jest: ≥95% líneas y ramas
   - Regla CI/CD: no merge si cobertura < 95%
   - Repositorio: verificación de módulos Spring Modulith

5. **Checkpoints de finalización:**
   Definir verificaciones por fase:
   - Fase 0: docker compose up levanta todo
   - Fase 1: backend compila, tests pasan, JaCoCo ≥95%
   - Fase 2: gateway levanta, proxy funciona, rate limiting activo
   - Fase 3: PWA instala, offline funciona, styling aplicado
   - Fase 4: todos los tests pasan, cobertura ≥95%, seguridad validada

**Formato:**
- Markdown con emojis de estado ([ ], [x], [~], [!])
- Secciones colapsables para fases
- Referencias claras a ADRs y specs
- Sintaxis que sea fácil de actualizar con checkmarks

**Objetivo:**
Que el PLAN.md sea la "single source of truth" para implementación: 
qué construir (requerimiento en spec), por qué (ADR), cómo (prompts de cada fase), 
cuándo validar (checkpoints)
```

### Resultado Generado

```
Archivo: PLAN.md (800+ líneas)
- Estructura completa: 4 fases + subíndices
- 100+ tareas granulares asignadas a fases
- Checkmarks ([ ], [x], [~], [!]) para tracking
- Referencias cruzadas a `/specs/technical/adr/` y `/specs/technical/openapi/`
- Tabla de cobertura esperada (JaCoCo 95%, Jest 95%)
- Criterios de aceptación claros por fase
```

**Uso**: PLAN.md se convierte en la guía diaria durante implementación ✓

---

## <a id="prompts-impl"></a>Prompts de Implementación por Fase

Estos fueron los prompts ejecutados para cada fase, siempre referenciando el PLAN.md.

### Fase 0 — Infraestructura

**Prompt 0.1 — Repositorio Base**

```
Crear estructura de monorepo para EAMS con 3 contenedores (backend, gateway, frontend).

Usar como referencia: PLAN.md sección "Fase 0.1 Repositorio y estructura base"

Estructura a crear:
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/eams/
│   └── src/test/java/com/eams/
├── gateway/
│   ├── package.json
│   ├── src/
│   └── tests/
├── frontend/
│   ├── package.json
│   ├── src/app/
│   └── src/__tests__/
├── docs/ (specs ya creadas)
├── docker-compose.yml
└── .gitignore

Crear archivos:
- Root `.gitignore`: ignorar .env, node_modules/, target/, *.pem, *.key
- Root `README.md`: instrucciones "docker compose up" + estructura del proyecto
- Root `.env.example`: templates de variables (JWT_SECRET, DB_URL, REDIS_URL, etc)
```

**Prompt 0.2 — Docker Compose**

```
Crear docker-compose.yml con 5 servicios:
1. postgres:16 (puerto 5432, volumen persistent)
2. redis:7 (puerto 6379)
3. backend (Spring Boot, build from ./backend, puerto 8080)
4. gateway (NestJS, build from ./gateway, puerto 3001)
5. frontend (Next.js, build from ./frontend, puerto 3000)

Incluir:
- healthchecks en postgres y redis
- environment vars desde .env
- depends_on para garantizar orden startup
- volumes para PostgreSQL persistencia
- networks compartida

Verificación: docker compose up debe levantar todos los servicios sin errores
```

**Prompt 0.3 — Base de Datos**

```
Crear migrations Flyway PostgreSQL con schema para EAMS.

Usar como referencia: /specs/technical/database-schema.md y PLAN.md "Fase 0.3"

Migrations Flyway (V001_.sql, V002_.sql, etc):
1. Crear tablas base: institutions, users, students
2. Crear tablas: activities, schedules, enrollments
3. Crear tablas: attendance_sessions, attendance_records
4. Crear tabla: audit_log
5. Habilitar RLS (Row-Level Security) en todas tablas de dominio
6. Crear políticas RLS: usuarios solo ven datos de su institution_id
7. Crear índices: (institution_id, status), (student_id, status), etc
8. Agregar constraints: UNIQUE (student_id, activity_id), CHECK available_spots >= 0

Archivo: backend/src/main/resources/db/migration/V*.sql

Verificación: 
- Schema valida en PostgreSQL
- RLS policies en lugar
- Índices creados
- Constraints funcionales
```

### Fase 1 — Backend Spring Boot Modulith

**Prompt 1.0 — Setup Spring Boot**

```
Configurar proyecto Spring Boot 3.x con Java 21 y Maven.

Referencia: PLAN.md "Fase 1.0 Setup del proyecto Spring Boot"

Generar con Spring Initializr:
- Project: Maven
- Language: Java
- Spring Boot: 3.x
- Java: 21
- Group: com.eams
- Artifact: backend

Dependencias esenciales:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-modulith-starter-core
- spring-modulith-starter-jpa
- postgresql:postgresql (driver)
- org.springframework.data:spring-data-redis
- io.lettuce:jeduce (Redis client)
- springdoc-openapi-starter-webmvc-ui (Swagger)
- de.taimos:totp (MFA)

Testing:
- junit-jupiter
- mockito-core
- assertj-core
- spring-boot-starter-test

Code quality:
- jacoco-maven-plugin (JaCoCo ≥95%)
- maven-enforcer-plugin

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
└── shared/ (@ApplicationModule(Type.OPEN))

Configuración:
- application.yml: logging, JPA, Redis, SecurityConfig
- TenantContextHolder para propagar institution_id
- ApplicationEvent publisher para eventos de dominio
- HikariCP pool: max 20 connections
- Redis: lettuce config, standalone mode
- SecurityConfig base: csrf=false, stateless JWT

Verificación:
mvn clean package -DskipTests → sin errores
docker build -t eams-backend:latest . → imagen creada
```

**Prompt 1.1 — Módulo Auth & Security**

```
Implementar módulo de autenticación con JWT 15min + Refresh Token Redis + MFA TOTP.

Referencia: PLAN.md "Fase 1.1 Módulo Auth & Security"
Specs funcionales: /specs/functional/F4-autenticacion.feature
Specs técnicas: /specs/technical/openapi/auth.yaml, /specs/technical/adr/AD-06

Domain Layer (DDD):
- Entity User: {id, email, password_hash, role, institution_id, mfa_secret}
- Value Object Role: {GUARDIAN, TEACHER, ADMIN, SUPERADMIN}
- Service AuthDomainService: validateCredentials(), generateTokenPayload()

Application Layer (Use Cases):
- LoginUserCommand: email, password → sessionToken (si MFA) | tokens (si no)
- VerifyMfaCommand: sessionToken, mfa_code → accessToken, refreshToken
- RefreshTokenCommand: refreshToken → new accessToken, refreshToken (con rotación)
- LogoutCommand: refreshToken → revocation en Redis

Infrastructure Layer (Adapters):
- JpaUserRepository: CRUD + find by email
- RedisSessionStore: sessionToken (5 min TTL), refreshToken (7 días)
- JwtTokenProvider: generar/validar JWT (HS256)
- MfaService: TOTP con GoogleAuthenticator
- AuthController: POST /auth/login, /mfa/verify, /refresh, /logout
- SecurityConfig: csrf(disable), sessionManagement(STATELESS)

Encryption:
- Password: BCrypt 12 rounds
- JWT secret: 32 bytes en .env
- MFA: base32 encoded TOTP

Validaciones:
- Email: formato válido, único por institución
- Password: 8+ chars, 1 mayús, 1 número, 1 símbolo
- TOTP: 6 dígitos, ventana ±1

Pruebas Unitarias (JaCoCo ≥95%):
- AuthService.login(): sin MFA, con MFA, credenciales inválidas
- AuthService.mfaVerify(): código válido/inválido
- AuthService.refreshToken(): token válido, revocado
- JwtTokenProvider: generar, validar, expirado, malformado

Archivo: backend/src/main/java/com/eams/auth/**
Tests: backend/src/test/java/com/eams/auth/**Test.java

Verificación: 150+ tests, cobertura ≥95%
```

**Prompt 1.3 — Módulo Enrollments (SELECT FOR UPDATE)**

```
Implementar inscripciones garantizando 0% sobrecupo con bloqueo pesimista.

Referencia: PLAN.md "Fase 1.5 Módulo Enrollments"
Specs: /specs/technical/adr/AD-07 (SELECT FOR UPDATE)

Application Layer:
- EnrollStudentCommand: {student_id, activity_id, institution_id}
  → Validaciones: cupos > 0, no duplicado
  → Transacción SERIALIZABLE:
     1. SELECT * FROM activities WHERE id=? FOR UPDATE
     2. Validar available_spots > 0
     3. UPDATE available_spots = available_spots - 1
     4. INSERT enrollment
     5. COMMIT (libera bloqueo)

Pruebas de Concurrencia:
- ConcurrencyTest: 100 threads compiten por 10 cupos
  → Resultado: 10 éxitos, 90 fallos
  → 0% sobrecupo garantizado

Archivo: backend/src/main/java/com/eams/enrollments/**
Tests: 
  - EnrollmentServiceTest: validaciones, transaccionalidad
  - ConcurrencyTest: 100 threads, 10 cupos
  - Cobertura ≥95%

Verificación: Tests pasan, cobertura ≥95%, sobrecupo = 0%
```

**Prompt 1.8 — Pruebas Backend (JaCoCo ≥95%)**

```
Verificar cobertura de pruebas unitarias backend con JaCoCo ≥95%.

Referencia: /specs/technical/testing-strategy.md, PLAN.md "Fase 1.8"

Ejecutar:
mvn clean test jacoco:report

Verificar:
- target/site/jacoco/index.html muestra:
  * Line coverage: ≥95%
  * Branch coverage: ≥95%
  * Method coverage: ≥95%

Por módulo:
- com.eams.auth: ≥95%
- com.eams.users: ≥95%
- com.eams.institutions: ≥95%
- com.eams.activities: ≥95%
- com.eams.enrollments: ≥95% (incluir ConcurrencyTest)
- com.eams.attendance: ≥95%

Exclusiones válidas:
- @Configuration classes
- Spring auto-generated code
- Main class
- Enums simples

Si cobertura < 95%: mvn verify fallará (configurado en pom.xml)

Resultado: Reporte HTML generado, 0 fallos
```

### Fase 2 — API Gateway NestJS

**Prompt 2.0 — Setup NestJS**

```
Configurar API Gateway NestJS que valida JWT, aplica RBAC, rate limiting, 
y ruteaa backend Spring Boot.

Referencia: PLAN.md "Fase 2.0 Setup del proyecto NestJS"
Specs: /specs/technical/adr/AD-04

Stack:
- NestJS 10.x con TypeScript
- Express underlying
- @nestjs/jwt para validación JWT
- @nestjs/passport + passport-jwt
- @nestjs/throttler para rate limiting
- axios para HTTP proxy
- helmet para seguridad headers
- pino para logging

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
│   └── config/
│       └── configuration.ts
├── package.json
└── nest-cli.json

Funcionalidades:
1. JWT Validation:
   - Extraer Bearer token
   - Validar firma (RS256) con public key backend
   - Inyectar req.user {sub, role, institutionId}

2. RBAC Guards:
   - @Roles(GUARDIAN, TEACHER, ADMIN)
   - Rechazar 403 si rol no autorizado

3. Rate Limiting:
   - Global: 5 req/seg por IP
   - Per-user: 1000/día
   - Excepto /health, /auth/* (sin límite)

4. Proxy HTTP:
   - Ruteaar /api/* a http://backend:8080
   - Timeout 10 segundos
   - Retry logic (3 intentos, exponential backoff)
   - Preservar headers: Authorization, X-User-Role, X-Institution-ID

5. CORS:
   - Whitelist: http://localhost:3000
   - Métodos: GET, POST, PUT, DELETE
   - Headers: Authorization, Content-Type
   - Credentials: true

6. Security Headers:
   - HSTS, X-Content-Type-Options, CSP, X-Frame-Options

Validación de configuración con Joi:
- JWT_SECRET, BACKEND_URL, JWT_ISSUER requeridas
- Error al startup si faltan

Logging:
- Pino level: info (prod), debug (dev)
- Log cada request: timestamp, method, path, status, duration
- NO loguear Authorization header

Verificación:
npm run build
npm run start:prod
curl -H "Authorization: Bearer <token>" http://localhost:3001/api/activities
```

### Fase 3 — Frontend Next.js

**Prompt 3.0 — Setup Next.js PWA**

```
Configurar frontend Next.js 14 con App Router como PWA offline-first.

Referencia: PLAN.md "Fase 3.0 Setup del proyecto Next.js"
Specs: /specs/technical/adr/AD-05

Stack:
- Next.js 14 + React 19
- next-pwa 5.x (Service Worker 48h offline)
- TypeScript
- Tailwind CSS + CSS Variables (tema LinkedIn)
- React Hook Form + Zod
- SWR para data fetching
- Zustand para estado global

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
│   │   └── admin/
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useEnrollment.ts
│   │   └── useTracking.ts
│   ├── services/
│   │   ├── apiClient.ts (axios + interceptors)
│   │   └── authService.ts
│   └── stores/
│       ├── authStore.ts (Zustand)
│       └── offlineStore.ts

Tema LinkedIn (CSS Variables):
--primary: #0A66C2
--accent: #D2D7DE
--surface: #FFFFFF
--muted: #6B7280
--text: #000000
--border: #D1D5DB
--background: #F3F2EF
--radius-lg: 12px
--radius-md: 8px

PWA Configuration (next.config.ts):
- next-pwa: register Service Worker
- Cache strategies:
  * NetworkFirst para /api/* (revalidate 5 min)
  * StaleWhileRevalidate para assets
  * CacheFirst para imágenes
- IndexedDB para offline state sync
- Periodic sync para updates

Offline Mode (Zustand):
- isOnline: boolean (navigator.onLine + ping)
- localQueue: Array<PendingRequest>
- syncQueue() cuando hay conexión

Autenticación:
- JWT en localStorage
- Refresh token con rotación
- Interceptor axios: Bearer <token>
- Auto-refresh en 401

Verificación:
npm run dev → http://localhost:3000
npm run build → no errors
npm run test -- --coverage → ≥95%
```

**Prompt 3.1 — Componentes Autenticación**

```
Implementar flujos de autenticación en frontend.

Referencia: PLAN.md "Fase 3.1 Componentes Autenticación"

Páginas y componentes:
1. /login
   - Form: email + password
   - Validación Zod
   - POST /auth/login → tokens | sessionToken (si MFA)
   - Error display

2. /mfa
   - QR para setup GoogleAuthenticator
   - Input 6 dígitos TOTP
   - POST /auth/mfa/verify → tokens

3. AuthContext (React Context)
   - Provider: inicializa sesión si hay refreshToken
   - Methods: login(), logout(), mfaVerify(), refreshSilently()
   - State: {user, isAuthenticated, loading, error}

4. useAuth hook
   - Acceso a user, login, logout

5. ProtectedRoute HOC
   - Verifica autenticación
   - Redirecta /login si no autenticado

Componentes:
- LoginForm: form email/password + submit + error
- MfaForm: input 6 dígitos + QR
- ProtectedLayout: solo si autenticado

Pruebas (Jest + RTL, ≥95%):
- LoginForm.test.tsx: render, submit, validaciones, errores
- AuthContext.test.tsx: login, mfaVerify, logout, refreshSilently
- apiClient.test.ts: interceptors JWT, 401 retry, token refresh

Archivos:
- src/app/(auth)/login/page.tsx
- src/components/auth/LoginForm.tsx
- src/contexts/AuthContext.tsx
- src/hooks/useAuth.ts
- src/__tests__/LoginForm.test.tsx
```

**Prompt 3.7 — Styling LinkedIn & Responsive**

```
Aplicar diseño LinkedIn a todas las páginas del frontend.

Referencia: PLAN.md "Fase 3.7 Styling LinkedIn"

CSS Variables (en src/styles/globals.css):
--primary: #0A66C2
--accent: #D2D7DE
--surface: #FFFFFF
--muted: #6B7280
--text: #000000
--border: #D1D5DB
--background: #F3F2EF
--radius-lg: 12px
--radius-md: 8px

Patrones de diseño:
1. Headers: Logo + Avatar + Logout
2. Cards: border-radius, shadow, hover effect
3. Buttons: primary (azul), secondary (gris), danger (rojo)
4. Forms: input con border inferior, focus ring
5. Modales: backdrop, centered, responsive width
6. Tablas: alternating row colors, hover highlight

Páginas a estilizar:
- /login: card centered, gradient background
- /guardian/activities: grid cards 3 col (desktop), 1 col (mobile)
- /guardian/enroll: modal wizard steps
- /teacher/attendance: table con botones inline
- /admin/activities: table con actions dropdown

Responsive breakpoints:
- Mobile: < 640px (1 column, full width)
- Tablet: 640-1024px (2 columns)
- Desktop: > 1024px (3 columns)

Utilidades Tailwind + CSS variables:
- bg-[var(--primary)]
- text-[var(--text)]
- border-[var(--border)]
- rounded-[var(--radius-lg)]

Verificación:
npm run dev → visualmente consistente con LinkedIn
Responsive: mobile, tablet, desktop
Accesibilidad: contrast ratio ≥4.5:1
```

### Fase 4 — Testing

**Prompt 4.2 — Tests Frontend (Jest + RTL)**

```
Implementar suite de pruebas frontend con Jest + RTL ≥95% cobertura.

Referencia: PLAN.md "Fase 3.8 Pruebas Unitarias Frontend"
Specs: /specs/technical/testing-strategy.md

Stack:
- Jest 29.x + ts-jest
- React Testing Library (user-centric, no implementation details)
- @testing-library/user-event
- jest.mock('@/services/apiClient') para APIs

Patrones:
- AAA: Arrange-Act-Assert
- One focus assertion per test
- renderHook para hooks sin componentes
- waitFor(() => expect(...)) para async
- jest.mock('@/services/apiClient'): no requests reales

Test Files por Componente:
1. src/components/auth/LoginForm.tsx
   - Render form fields (email, password, submit button)
   - Submit con datos válidos → POST /auth/login
   - Validación: email requerido, password 8+ chars
   - Error display si credenciales inválidas
   - Button deshabilitado durante submit

2. src/components/activities/ActivityCard.tsx
   - Renderizar nombre, descripción, cupos disponibles
   - Botón "Inscribirse" habilitado si cupos > 0
   - Botón deshabilitado en modo offline
   - onClick → onEnroll callback

3. src/components/attendance/AttendanceList.tsx
   - Renderizar lista estudiantes
   - Toggle presente/ausente (botones)
   - Contador de toques: máximo 3 (botón disabled después)
   - Campo observaciones: expandible, editable, guardar
   - Loading state: botones disabled

4. src/hooks/useAuth.test.ts
   - login(): éxito, error, MFA required
   - logout(): limpia tokens y user
   - refreshSilently(): restaura sesión desde refreshToken

5. src/hooks/useEnrollment.test.ts
   - enroll(): POST /api/enrollments, validaciones, duplicidad
   - cancel(): DELETE /api/enrollments/{id}
   - Error handling: 409 sobrecupo, 422 validación

6. src/services/apiClient.test.ts
   - Request interceptor: inyecta Authorization header
   - Response 401: intenta POST /auth/refresh
   - Response 401 falla: redirect /login, clear tokens
   - Non-401 errors: pasan sin reintentar

Configuración jest.config.js:
{
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleNameMapper: { '@/(.*)': '<rootDir>/src/$1' },
  collectCoverageFrom: [
    'src/components/**/*.{ts,tsx}',
    'src/hooks/**/*.{ts,tsx}',
    'src/services/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/app/**'
  ],
  coverageThreshold: {
    global: { lines: 95, branches: 95, functions: 95, statements: 95 }
  }
}

Ejecución:
npm test -- --coverage --watchAll=false

Resultado esperado:
- ✓ 100+ tests pasan
- ✓ Coverage: ≥95% lines, branches, functions, statements
- ✓ 0 warnings/errors
```

**Prompt 4.3 — Contract Testing (Dredd)**

```
Validar que backend implementa especificación OpenAPI con Dredd.

Referencia: PLAN.md "Fase 4.3 Pruebas Contract"

Setup:
- npm install -g dredd
- Especificación: /specs/technical/openapi/compiled.yaml
- Backend: http://localhost:8080

Dredd configuration (dredd.yml):
endpoint: http://localhost:8080
apiDescriptionLocation: ./specs/technical/openapi/compiled.yaml
hookfiles: ./tests/contract/**/*.hook.js
reporter: [ json, html ]

Contract Tests (validar endpoints):
1. GET /api/activities
   - Response schema: activity array
   - Headers: Authorization Bearer token

2. POST /auth/login
   - Request: {email, password}
   - Response 200: {accessToken, refreshToken} | 401 invalid

3. POST /api/enrollments
   - Request: {student_id, activity_id}
   - Response 201: {id, status}
   - Response 409: duplicate enrollment

4. PUT /api/activities/{id}
   - Solo ADMIN puede
   - Response 403 si GUARDIAN

Hooks para fixtures:
- beforeAll: crear institution, user, activity
- beforeEach: inyectar JWT token en headers
- afterEach: cleanup

Ejecución:
dredd ./specs/technical/openapi/compiled.yaml http://localhost:8080

Resultado:
✓ 50+/50+ transactions passed
✗ Schema mismatches debugged
Reporte: dredd_reports/report.html

Verificación: 100% compliance con OpenAPI spec
```

**Prompt 4.4 — BDD Testing (Cucumber + Playwright)**

```
Implementar tests BDD con Cucumber + Playwright en español.

Referencia: PLAN.md "Fase 4.4 Pruebas BDD"

Feature Files (Gherkin) en /tests/bdd/features/:
1. F1-inscripcion.feature
   Escenario: Padre inscribe estudiante
   Dado que soy padre autenticado
   Y tengo estudiante "Juan"
   Y existe actividad "Fútbol" con 10 cupos
   Cuando selecciono "Fútbol"
   Y confirmo inscripción de "Juan"
   Entonces veo "Inscripción exitosa"

2. F2-asistencia.feature
   Escenario: Docente registra asistencia
   Dado que soy docente de "Fútbol"
   Cuando abro sesión de asistencia
   Y marco a "Juan" como presente
   Y cierro sesión
   Entonces se sincroniza con backend

3. F3-consulta-offline.feature
   Escenario: Usuario consulta actividades offline
   Dado que descargo app en modo online
   Y estoy viendo "Actividades"
   Cuando pierdo conexión a internet
   Entonces sigo viendo actividades (cached)
   Y cuando reconecto, se sincronizan cambios

Step Definitions (TypeScript en /tests/bdd/steps/):
Given('que soy padre autenticado', async function() {
  await this.page.goto('http://localhost:3000/login')
  await this.page.fill('[name="email"]', 'padre@example.com')
  await this.page.fill('[name="password"]', 'password123')
  await this.page.click('button[type="submit"]')
  await this.page.waitForURL('**/activities')
})

Configuración Cucumber:
- Framework: @cucumber/cucumber
- Runner: Playwright
- Hooks: beforeAll (start server), afterEach (cleanup)

Ejecución:
npm run test:bdd

Resultado:
✓ Todos los scenarios pasan
✓ Cobertura: login, enroll, attendance, admin
✓ Validaciones: sobrecupo, duplicidad, offline

Verificación: 100% scenarios ejecutados sin fallo
```

---

## <a id="prompts-testing"></a>Resumen de Prompts por Área

### Testing Backend (JaCoCo)

```
Verificar cobertura backend ≥95% con JaCoCo.

Módulos cubiertos:
- auth: login, mfaVerify, refreshToken, logout
- users: registro, estudiantes, guardias
- institutions: CRUD, multi-tenancy
- activities: CRUD, estado, caché
- enrollments: inscripción (SELECT FOR UPDATE), concurrencia
- attendance: sesiones, registros, límite 3 toques

Ejecución:
mvn clean test jacoco:report

Verificación:
- target/site/jacoco/index.html: ≥95% líneas, ramas
- 150+ tests pasan
- 0 fallos
```

### Testing Frontend (Jest)

```
Verificar cobertura frontend ≥95% con Jest.

Cobertura:
- Components: LoginForm, ActivityCard, AttendanceList, ActivityForm
- Hooks: useAuth, useEnrollment, useAttendanceSessions, useTracking
- Services: apiClient (interceptors), authService
- Contexts: AuthContext

Ejecución:
npm test -- --coverage --watchAll=false

Verificación:
- coverage/index.html: ≥95% líneas, ramas, funciones
- 100+ tests pasan
- 0 fallos
```

---

## Cómo Usar Este Documento

1. **Para replicar el proyecto desde cero:**
   - Comienza con Prompt 1 (Especificaciones) → Genera `/specs`
   - Luego Prompt 2 (Planificación) → Genera `PLAN.md`
   - Ejecuta Fase 0 (Infraestructura) usando los prompts específicos
   - Continúa Fase 1-4 en orden, siguiendo PLAN.md

2. **Para entender decisiones arquitectónicas:**
   - Lee los ADRs referenciados en cada prompt
   - Lee el feature file Gherkin correspondiente
   - Revisa la especificación OpenAPI

3. **Para debugging o problemas:**
   - Vuelve al prompt de la fase afectada
   - Verifica los checkpoints de finalización
   - Re-ejecuta con `--verbose` si es necesario

4. **Para agregar nuevas funcionalidades:**
   - Primero escribe spec en `/specs/functional` (Gherkin)
   - Luego agrega endpoint en `/specs/technical/openapi`
   - Crea ADR si cambio arquitectónico
   - Actualiza PLAN.md
   - Ejecuta prompts de implementación

---

**Última Actualización**: 13 de Abril de 2026
**Proyecto**: EAMS — Plataforma de Gestión de Actividades Extracurriculares
**Status**: Fases 0-3 completadas, Phase 3.8 tests en progress, Phase 4 planificada

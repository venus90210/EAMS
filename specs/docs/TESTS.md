# Resumen de Tests — EAMS

## Backend Tests

### 1. Unit Tests (`@Tag("unit")`)

Aislados con **JUnit 5 + Mockito**, sin contexto Spring ni base de datos.

| Módulo | Archivo | Qué cubre |
|--------|---------|-----------|
| **Auth** | `AuthServiceTest.java` | Login (guardián vs admin), MFA, refresh token, revocación, logout |
| | `JwtTokenProviderTest.java` | Generación JWT, claims (role, institutionId), tamper detection, expiración, MFA-pending token (13 casos) |
| | `MfaServiceTest.java` | Generación de secretos TOTP (uniqueness), formato OTP URL, verificación contra librería TOTP en vivo |
| | `UserManagementServiceTest.java` | Registro (guardian, teacher, superadmin), enforcement de roles, email duplicado, get/update perfil |
| | `UserResponseSecurityTest.java` | Inspección por reflexión que DTO no filtra `passwordHash` ni `mfaSecret` |
| **Institutions** | `InstitutionServiceTest.java` | CRUD, normalización de dominio a minúsculas, conflicto de dominio duplicado |
| | `InstitutionContextProviderTest.java` | Comportamiento de lookup de contexto institucional |
| **Students** | `StudentServiceTest.java` | Vinculación guardián-estudiante, carga masiva desde CSV (filas válidas, inválidas, columnas insuficientes) |
| **Activities** | `ActivityServiceTest.java` | CRUD, transiciones de estado (DRAFT→PUBLISHED, PUBLISHED↔DISABLED), control por rol (admin vs guardian vs teacher), cache hit/miss para cupos disponibles |
| **Attendance** | `AttendanceServiceTest.java` | Apertura de sesión (duplicada, fecha inválida, role guard), registro de asistencia (ventana de tiempo, duplicada), actualización de observaciones, recuperación por rol |
| **Enrollments** | `EnrollmentServiceTest.java` | Validación (student no encontrado, activity no encontrada, no publicada, ya inscrito, cupos agotados), decremento de cupos, cancelación con liberación de cupos, control por rol |
| **Architecture** | `ArchitectureTest.java` | Verificación de límites de módulos (Spring Modulith), generación de documentación PlantUML; verificación de compliance está deshabilitada (Fase 1.8 pending) |

### 2. Integration Tests (`@Tag("integration")`, `*IT.java`)

**Spring Boot Test + Testcontainers** (PostgreSQL 16 + Redis 7 reales). Cada test hereda de `BaseIntegrationTest`.

| Nombre | Archivo | Qué cubre | Validación |
|--------|---------|-----------|-----------|
| **IT-01: Concurrencia de Enrollments** | `EnrollmentConcurrencyIT.java` | 10 threads concurrentes contra activity con 1 cupo; verifica que pesimistic locking (`SELECT FOR UPDATE`) permite exactamente 1 inscripción exitosa | ADR AD-07 |
| **IT-02: Tenant Isolation** | `TenantIsolationIT.java` | Row-Level Security (RLS) aísla datos por tenant; institución A no puede leer actividades de institución B | ADR AD-08 |
| **IT-03: Token Revocation** | `TokenRevocationIT.java` | Login, revocación de refresh token en Redis, luego uso fallido retorna HTTP 401 | — |
| **IT-04: Notification Flow** | `NotificationFlowIT.java` | Inscripción de estudiante dispara evento asíncrono (Spring Modulith) → procesamiento de notificación por email | ADR AD-09 |

**Base Compartida**: `BaseIntegrationTest.java`
- Setup de Testcontainers para Postgres + Redis
- `@DynamicPropertySource` inyecta URLs de contenedores en properties
- `@ActiveProfiles("test")`

### 3. Security Headers Test

| Archivo | Qué cubre |
|---------|-----------|
| `SecurityHeadersTest.java` | `@SpringBootTest` con contexto Spring real; GET `/auth/login` (permitAll) verifica presencia de headers: `X-Content-Type-Options`, `X-Frame-Options`, `X-XSS-Protection`, `Content-Security-Policy` |

---

## Frontend Tests

### 1. Component Tests

**Jest 30 + @testing-library/react 16 + @testing-library/user-event 14**  
Mocked via `jest.mock()` o **MSW 2** (`/frontend/src/mocks/server.ts`).

| Componente | Archivo | Qué cubre |
|------------|---------|-----------|
| **LoginForm** | `components/__tests__/LoginForm.test.tsx` | Renderizado, submit de login, branching MFA (onSuccess vs onMfaRequired), display de errores, estado loading/disabled |
| **ActivityForm** | `components/__tests__/ActivityForm.test.tsx` | Modos crear vs editar, validación de campos requeridos, cupos > 0, submit con datos correctos, cancel, limpiar errores al re-tipear, pre-populate en edit |
| **ActivityCard** | `components/__tests__/ActivityCard.test.tsx` | Renderizado de info, display de horario, estado botón enroll (online/offline/sin cupos), aviso offline, loading state, feedback de color de cupos |
| **ActivityManagementList** | `components/__tests__/ActivityManagementList.test.tsx` | Lista admin: badges de status, cupos, acciones (edit/publish/disable/delete), visibilidad botones DRAFT vs PUBLISHED, estado vacío, loading |
| **AttendanceList** | `components/__tests__/AttendanceList.test.tsx` | Toggle asistencia por estudiante, límite 3-toggle por estudiante, campo observaciones, save observaciones, disable durante loading, params de callback correctos |
| **EnrollmentForm** | `components/__tests__/EnrollmentForm.test.tsx` | Renderizado, select de estudiante, disable cuando sin estudiantes, info de cupos, cancel, botón next, múltiples estudiantes, nombre actividad en título |
| **OfflineBanner** | `components/__tests__/OfflineBanner.test.tsx` | Oculto cuando online + cache fresco; banner amarillo cuando offline con edad de cache (minutos/horas); banner rojo cuando cache expirado |

### 2. Hook Tests

| Hook | Archivo | Qué cubre |
|------|---------|-----------|
| `useAuth` | `hooks/__tests__/useAuth.test.ts` | Forma básica (funciones, null user, isAuthenticated, loading, error) |
| `useActivities` | `hooks/__tests__/useActivities.test.ts` | Fetch on mount cuando online, refetch, serve desde cache cuando offline, fallback a cache on error API, estado error cuando sin cache |
| `useAdminActivities` | `hooks/__tests__/useAdminActivities.test.ts` | Operaciones CRUD admin: fetch, create, update, publish, disable, delete; error handling en create |
| `useEnrollment` | `hooks/__tests__/useEnrollment.test.ts` | Enroll éxito, 409 conflict variants (cupo agotado, ya inscrito, inscripción activa existe), estado default, error clearing on retry |
| `useAttendanceSessions` | `hooks/__tests__/useAttendanceSessions.test.ts` | Abrir sesión, error on open, registrar asistencia con/sin observaciones, error on record |
| `useStudents` | `hooks/__tests__/useStudents.test.ts` | Fetch estudiantes por usuario, null-user guard, loading state, lista vacía, error handling |
| `useTracking` | `hooks/__tests__/useTracking.test.ts` | Fetch y grouping de enrollments + attendance por studentId, grouping multi-estudiante, asistencia vacía, null-user guard, endpoints API correctos |
| `useOfflineStatus` | `hooks/__tests__/useOfflineStatus.test.ts` | Estado online/offline, cálculo edad cache, detección cache expirado (>48h), limpiar event listeners on unmount |

### 3. Context Tests

| Context | Archivo | Qué cubre |
|---------|---------|-----------|
| **AuthContext** | `contexts/__tests__/AuthContext.test.tsx` | Estado inicial unauthenticated, restore sesión desde refresh token on mount, login sin MFA, login con MFA required, login failure, logout (token clear), MFA verification, invalid MFA, loading state durante ops async |

### 4. Service Tests

| Servicio | Archivo | Qué cubre |
|----------|---------|-----------|
| **authService** | `services/__tests__/authService.test.ts` | Token storage/retrieval (in-memory + localStorage), clearTokens, persistence entre calls |
| **apiClient** | `services/__tests__/apiClient.test.ts` | Lógica de interceptor Axios: inyección de Authorization header, manejo de tokens |
| **cacheService** | `services/__tests__/cacheService.test.ts` | Cache localStorage con TTL: set/get, TTL custom, expiración, `has`, `getAge`, `isExpired`, `remove`, `clear` |

---

## Frameworks & Herramientas

| Capa | Frameworks |
|------|-----------|
| **Backend Unit** | JUnit 5, Mockito, Spring Modulith Test |
| **Backend Integration** | JUnit 5, Spring Boot Test, Testcontainers (Postgres 16, Redis 7), MockMvc |
| **Backend Security** | Spring Boot Test, MockMvc |
| **Frontend** | Jest 30, @testing-library/react 16, @testing-library/user-event 14, jest-environment-jsdom, ts-jest, MSW 2 |

---

## Patrones Destacados

### Backend
- **Tagging con `@Tag`**: Permite ejecución selectiva (ej. `mvn test -Dtest=ArchitectureTest`)
- **Shared Base Class**: `BaseIntegrationTest` centraliza lifecycle de Testcontainers; todos `*IT.java` heredan
- **Concurrency Testing**: `EnrollmentConcurrencyIT` usa `CountDownLatch` + `ExecutorService` para reproducir race conditions bajo pesimistic locking
- **Security by Reflection**: `UserResponseSecurityTest` inspecciona campos DTO reflectivamente para asegurar que no filtra datos sensibles vía serialización
- **ADR Traceability**: Los tests de integración referencian explícitamente Architecture Decision Records (AD-06 a AD-09) y requisitos (RF-series)

### Frontend
- **Offline-First Coverage**: Múltiples tests cubren explícitamente path offline/cache fallback (`useActivities`, `useOfflineStatus`, `OfflineBanner`) — refleja PWA design
- **Shared Test Utils**: `test-utils.tsx` exporta `renderHookWithAuth` que wrappea hooks en `AuthProvider` real, evitando boilerplate de context setup
- **MSW Server Stub**: `/frontend/src/mocks/server.ts` configurado para override de handlers por test suite, aunque mayoría de tests usan `jest.mock()` directo en API client

---

## Cobertura Resumida

- **Backend**: 13 tests unitarios aislados + 4 integration tests (concurrencia, tenant isolation, revocación, notificaciones) + 1 security headers test
- **Frontend**: 8 componentes + 8 hooks + 1 context + 3 servicios = ~20 test suites con Jest

El proyecto demuestra cobertura de paths críticos (offline, concurrencia, seguridad, compliance), incluyendo testing de race conditions y aislamiento multi-tenant en bases de datos reales.

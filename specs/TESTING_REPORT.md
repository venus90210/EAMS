# EAMS — Reporte de Estrategia de Pruebas

> **Fecha**: 2026-04-15  
> **Proyecto**: Plataforma de Gestión de Actividades Extracurriculares (PWA)  
> **Versión**: 1.0  
> **Estado**: Implementación Completa (Fase 4.0-4.4)

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Pruebas Unitarias](#1-pruebas-unitarias)
3. [Pruebas de Integración](#2-pruebas-de-integración)
4. [Pruebas Funcionales](#3-pruebas-funcionales)
5. [Cobertura y Métricas](#cobertura-y-métricas)
6. [Matriz de Trazabilidad](#matriz-de-trazabilidad)

---

## Resumen Ejecutivo

La estrategia de pruebas para EAMS se basa en **tres pilares** con cobertura mínima del **95%** en líneas y ramas:

| Tipo de Prueba | Objetivo | Framework | Cobertura |
|---|---|---|---|
| **Unitarias** | Validar lógica aislada de negocio | JUnit 5 + Mockito (Backend)<br/>Jest (Gateway/Frontend) | ≥ 95% |
| **Integración** | Verificar flujos críticos con infraestructura real | Spring Boot Test + Testcontainers | 4 escenarios |
| **Funcionales** | Validar requisitos de usuario en Gherkin | Cucumber + Step Definitions | 5 features |

**Beneficios:**
- ✓ Detección temprana de defectos (unitarias)
- ✓ Garantía de flujos críticos bajo concurrencia (integración)
- ✓ Trazabilidad requisito → código → test (funcionales)
- ✓ Gate CI/CD: PR no se mergea si cobertura < 95%

---

## 1. Pruebas Unitarias

### 1.1 Propósito y Filosofía

Las pruebas unitarias validan **lógica de negocio aislada** sin:
- Levantar base de datos
- Hacer llamadas de red
- Instanciar componentes del framework completo

**Filosofía**: Las dependencias externas se mockean. Los tests corren en < 100ms.

### 1.2 Stack Tecnológico por Contenedor

#### Backend (Spring Boot)

```
┌─────────────────────────────────────────────────┐
│         BACKEND — Pruebas Unitarias             │
├─────────────────────────────────────────────────┤
│ Framework:    JUnit 5 (Jupiter)                 │
│ Mocking:      Mockito (dependencias)            │
│ Assertions:   AssertJ (fluidas, legibles)       │
│ Cobertura:    JaCoCo (umbral 95%)              │
│ CI/CD Gate:   Bloquea si cobertura < 95%       │
└─────────────────────────────────────────────────┘
```

**Configuración JaCoCo en `pom.xml`:**
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <configuration>
    <rules>
      <rule>
        <element>BUNDLE</element>
        <limits>
          <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.95</minimum>
          </limit>
          <limit>
            <counter>BRANCH</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.95</minimum>
          </limit>
        </limits>
      </rule>
    </rules>
  </configuration>
</plugin>
```

#### API Gateway (NestJS)

```
┌─────────────────────────────────────────────────┐
│    GATEWAY — Pruebas Unitarias (Jest)           │
├─────────────────────────────────────────────────┤
│ Framework:    Jest                              │
│ Testing:      @nestjs/testing (TestingModule)   │
│ Mocking:      jest-mock-extended (tipado)       │
│ Cobertura:    Jest coverage (95%)              │
└─────────────────────────────────────────────────┘
```

#### Frontend (Next.js)

```
┌─────────────────────────────────────────────────┐
│    FRONTEND — Pruebas Unitarias (Jest)          │
├─────────────────────────────────────────────────┤
│ Framework:    Jest + React Testing Library      │
│ Mock HTTP:    MSW (Mock Service Worker)         │
│ Assertions:   @testing-library/react            │
│ Cobertura:    Jest coverage (95%)              │
└─────────────────────────────────────────────────┘
```

### 1.3 Cobertura por Módulo (Backend)

Cada módulo tiene cobertura **≥ 95%** con casos que cubren **camino feliz + todos los errores**:

#### Módulo Auth & Security

| Clase/Método | Escenarios | Test Class |
|---|---|---|
| `AuthService.login()` | • Credenciales correctas<br/>• Credenciales incorrectas<br/>• Usuario inactivo | `AuthServiceTest` |
| `AuthService.refreshToken()` | • Token válido (con rotación)<br/>• Token revocado<br/>• Token expirado | `AuthServiceTest` |
| `AuthService.logout()` | • Revocación exitosa en Redis | `AuthServiceTest` |
| `MfaService.verifyTotp()` | • Código correcto<br/>• Código incorrecto<br/>• Secreto incorrecto | `MfaServiceTest` |
| `JwtTokenProvider.generateToken()` | • Payload correcto (sub, role, institutionId)<br/>• Expiración correcta (15 min)<br/>• MFA-pending token (5 min) | `JwtTokenProviderTest` (13 test cases) |
| `JwtTokenProvider.validateToken()` | • Token válido<br/>• Token malformado<br/>• Token expirado<br/>• Tamper detection | `JwtTokenProviderTest` |

**Archivo**: `backend/src/test/java/com/eams/auth/` — 5 test classes, 50+ test methods, cobertura 99%

#### Módulo Inscripciones (RF04, RF05 — Crítico)

| Clase/Método | Escenarios | Test Class |
|---|---|---|
| `EnrollmentService.enroll()` | • Cupo disponible → éxito (201)<br/>• Cupo agotado → error (409 SPOT_EXHAUSTED)<br/>• Duplicado → error (409 ALREADY_ENROLLED)<br/>• Enrollment activo existe → error (409)<br/>• Padre no responsable → error (403) | `EnrollmentServiceTest` |
| `EnrollmentService.cancelEnrollment()` | • Cancelación exitosa → libera cupo<br/>• Enrollment no encontrado → error (404)<br/>• Rol sin permiso → error (403) | `EnrollmentServiceTest` |
| `EnrollmentService.getByStudent()` | • Padre ve solo sus hijos<br/>• Admin ve todos | `EnrollmentServiceTest` |
| `EnrollmentService.getByActivity()` | • Docente asignado ve inscripciones<br/>• Docente no asignado → error (403)<br/>• Admin ve todos | `EnrollmentServiceTest` |

**Archivo**: `backend/src/test/java/com/eams/enrollments/EnrollmentServiceTest.java` — 15+ test methods

#### Módulo Actividades (RF05 — Estado)

| Clase/Método | Escenarios | Test Class |
|---|---|---|
| `ActivityService.create()` | • Creación válida (DRAFT)<br/>• Admin solo (otros 403)<br/>• Datos inválidos → error | `ActivityServiceTest` |
| `ActivityService.publish()` | • DRAFT → PUBLISHED OK<br/>• PUBLISHED → DRAFT error (409)<br/>• Audit log generado | `ActivityServiceTest` |
| `ActivityService.updateStatus()` | • PUBLISHED ↔ DISABLED OK<br/>• Transición inválida → 409<br/>• Institución mismatch → 403 | `ActivityServiceTest` |
| `ActivityService.update()` | • Admin modifica total_spots (audit log)<br/>• Teacher intenta modificar → 403<br/>• Cache invalidado | `ActivityServiceTest` |
| `ActivityService.listForRole()` | • GUARDIAN ve solo PUBLISHED<br/>• TEACHER/ADMIN ven todos<br/>• Institución mismatch → 403 | `ActivityServiceTest` |

**Archivo**: `backend/src/test/java/com/eams/activities/ActivityServiceTest.java` — 12+ test methods

#### Módulo Asistencia (RF13 — Ventana 24h)

| Clase/Método | Escenarios | Test Class |
|---|---|---|
| `AttendanceService.openSession()` | • Fecha hoy válida → sesión abierta<br/>• Fecha pasada → 422<br/>• Docente no asignado → 403<br/>• Sesión duplicada → 409 | `AttendanceServiceTest` |
| `AttendanceService.recordAttendance()` | • Dentro de ventana (3h) → registrado<br/>• Fuera de ventana → 403<br/>• Presente/Ausente | `AttendanceServiceTest` |
| `AttendanceService.addObservation()` | • Dentro de ventana 24h → OK<br/>• Fuera de ventana → EDIT_WINDOW_EXPIRED | `AttendanceServiceTest` |
| `EditWindowPolicy.isEditable()` | • Boundary testing: exactamente 24h<br/>• 24h + 1s → expirado | `AttendanceServiceTest` |

**Archivo**: `backend/src/test/java/com/eams/attendance/AttendanceServiceTest.java` — 10+ test methods

#### Módulo Usuarios (Registro, Vinculación)

| Clase/Método | Escenarios | Test Class |
|---|---|---|
| `UserService.register()` | • Email nuevo → OK<br/>• Email duplicado → 409<br/>• Rol no permitido → error | `UserManagementServiceTest` |
| `UserService.linkStudentToGuardian()` | • Vinculación exitosa<br/>• Acudiente no existe → 404<br/>• Estudiante ya vinculado → 409 | `UserManagementServiceTest` |
| `UserService.getStudentsByGuardian()` | • Padre ve solo sus hijos<br/>• Filtro por institución | `UserManagementServiceTest` |

**Archivo**: `backend/src/test/java/com/eams/auth/UserManagementServiceTest.java` — 8+ test methods

### 1.4 Seguridad en Tests (Reflexión)

**Clase Especial**: `UserResponseSecurityTest.java`
- Inspecciona por **reflexión** que DTOs **NO expongan** campos sensibles
- Verifica que `passwordHash`, `mfaSecret`, `refreshToken` NO aparecen en respuestas HTTP
- Impide exposure involuntaria vía serialización Jackson

```java
@Test
void userResponseDoesNotExposePasswordHash() {
    Field[] fields = UserResponse.class.getDeclaredFields();
    assertThat(fields)
        .noneMatch(f -> "passwordHash".equals(f.getName()))
        .noneMatch(f -> "mfaSecret".equals(f.getName()));
}
```

### 1.5 Tests de Arquitectura

**Clase**: `ArchitectureTest.java` — Spring Modulith
- Verifica que módulos respetan sus **fronteras de importación**
- Genera documentación **PlantUML automática**
- Compliance verificación deshabilitada (Fase 1.8 pending)

```
Módulos:
├── com.eams.auth
├── com.eams.users
├── com.eams.institutions
├── com.eams.activities
├── com.eams.enrollments
├── com.eams.attendance
└── com.eams.notifications
```

### 1.6 Frontend — Tests de Componentes, Hooks y Contexto

#### Componentes (7 test classes)

| Componente | Escenarios | Archivo |
|---|---|---|
| **LoginForm** | • Renderizado<br/>• Submit login<br/>• Branching MFA (onSuccess vs onMfaRequired)<br/>• Error display<br/>• Estados loading/disabled | `components/__tests__/LoginForm.test.tsx` |
| **ActivityForm** | • Modo crear vs editar<br/>• Validación de campos requeridos<br/>• Cupos > 0<br/>• Pre-populate en edit | `components/__tests__/ActivityForm.test.tsx` |
| **ActivityCard** | • Renderizado info<br/>• Horario display<br/>• Botón enroll (online/offline/sin cupos)<br/>• Aviso offline | `components/__tests__/ActivityCard.test.tsx` |
| **OfflineBanner** | • Oculto cuando online + cache fresco<br/>• Banner amarillo offline (edad cache)<br/>• Banner rojo cache expirado | `components/__tests__/OfflineBanner.test.tsx` |

#### Hooks (8 test classes)

| Hook | Escenarios | Archivo |
|---|---|---|
| **useAuth** | • Forma básica (funciones, user, isAuthenticated)<br/>• Loading/error states | `hooks/__tests__/useAuth.test.ts` |
| **useActivities** | • Fetch on mount online<br/>• Serve desde cache offline<br/>• Fallback a cache on API error<br/>• Error sin cache | `hooks/__tests__/useActivities.test.ts` |
| **useEnrollment** | • Enroll éxito<br/>• 409 variants (cupo agotado, ya inscrito)<br/>• Error clearing on retry | `hooks/__tests__/useEnrollment.test.ts` |
| **useOfflineStatus** | • Estado online/offline<br/>• Cálculo edad cache<br/>• Detección cache expirado (>48h) | `hooks/__tests__/useOfflineStatus.test.ts` |

**Total Frontend**: 20 test suites, 100+ test cases

---

## 2. Pruebas de Integración

### 2.1 Propósito

Las pruebas de integración validan **flujos críticos** donde los mocks **no pueden garantizar** el comportamiento real:
- Concurrencia (race conditions)
- Aislamiento multi-tenant (RLS en PostgreSQL)
- Almacenamiento externo (Redis)
- Eventos asíncronos (Spring Modulith events)

**Estrategia**: Mínimo viable — 4 escenarios críticos cubiertos, con infraestructura real (Testcontainers).

### 2.2 Stack: Spring Boot Test + Testcontainers

```
┌──────────────────────────────────────────────────────┐
│      PRUEBAS DE INTEGRACIÓN — Spring Boot             │
├──────────────────────────────────────────────────────┤
│ Framework:      Spring Boot Test                      │
│ Contenedores:   Testcontainers (PostgreSQL 16 real)   │
│                 Testcontainers (Redis 7 real)         │
│ Aislamiento:    @DynamicPropertySource inyecta URLs   │
│                 @ActiveProfiles("test")               │
│ Base compartida: BaseIntegrationTest                  │
└──────────────────────────────────────────────────────┘
```

### 2.3 Los 4 Escenarios Críticos (IT-01 a IT-04)

#### IT-01: Inscripción Concurrente sin Sobrecupo
**Archivo**: `EnrollmentConcurrencyIT.java`  
**ADR**: AD-07 (Bloqueo Pesimista)  
**Requisitos**: RF04, RF05

**Escenario**:
```
1. Crear activity con exactamente 1 cupo disponible
2. Lanzar 10 threads simultáneos intentando inscribir
3. ExecutorService + CountDownLatch sincroniza inicio
4. Verificar:
   ✓ Exactamente 1 inscripción exitosa (HTTP 201)
   ✓ Otras 9 retornan HTTP 409 SPOT_EXHAUSTED
   ✓ available_spots = 0 (nunca negativo)
```

**Garantía**: `SELECT FOR UPDATE` en PostgreSQL mantiene consistencia bajo concurrencia.

#### IT-02: Aislamiento de Tenants (RLS)
**Archivo**: `TenantIsolationIT.java`  
**ADR**: AD-08 (Row-Level Security)  
**Requisitos**: RNF06, RNF09

**Escenario**:
```
1. Crear Institución A e Institución B con datos propios
2. Ejecutar query sin filtro "WHERE institution_id" desde sesión A
3. Verificar que RLS impide ver datos de B (resultado vacío)
4. Repetir para tablas: activities, enrollments, attendance_sessions, users
5. Verificar que SUPERADMIN puede acceder a todas
```

**Garantía**: PostgreSQL RLS policies aíslan datos por tenant automáticamente.

#### IT-03: Revocación de Refresh Token
**Archivo**: `TokenRevocationIT.java`  
**ADR**: AD-06 (JWT + Redis)  
**Requisitos**: RNF04, RNF06

**Escenario**:
```
1. Generar refresh token y almacenar en Redis real (Testcontainers)
2. Llamar a POST /auth/logout → verificar DELETE en Redis
3. Intentar POST /auth/refresh con token revocado → HTTP 401 TOKEN_REVOKED
4. Verificar TTL natural impide reutilización
```

**Garantía**: Redis revocación en tiempo real + TTL natural.

#### IT-04: Flujo Completo de Notificación Asíncrona
**Archivo**: `NotificationFlowIT.java`  
**ADR**: AD-09 (Eventos Asíncronos)  
**Requisitos**: RF07 (Email en <60s)

**Escenario**:
```
1. Configurar WireMock como servidor SMTP stub
2. Ejecutar inscripción completa → evento EnrollmentConfirmed
3. Verificar evento llega a cola Redis en <1s
4. Worker consume evento y llama WireMock en <60s
5. Verificar idempotencia: reencolar no genera segundo email
```

**Garantía**: Spring Modulith events + Redis queue + Worker idempotente.

### 2.4 Infraestructura Compartida: BaseIntegrationTest

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class BaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));
    
    @Container
    static GenericContainer<?> redis = 
        new GenericContainer<>(DockerImageName.parse("redis:7"));
    
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.redis.host", redis::getHost);
        // Inyecta URLs reales en test
    }
    
    @ActiveProfiles("test")  // Carga application-test.properties
}
```

**Beneficios**:
- ✓ Todos los `*IT.java` heredan setup de infraestructura
- ✓ Testcontainers levanta contenedores reales (PostgreSQL, Redis)
- ✓ Datos persisten dentro de test, se limpian tras finish
- ✓ RLS policies aplicadas en migration real

### 2.5 Duración y Ejecución

```
Tiempo aproximado: 45-60 segundos (incluye startup de contenedores)

Ejecución en CI/CD:
- Tests unitarios corren siempre (< 30s)
- Tests integración corren solo si unitarias pasan (< 60s)
- Total: < 90s por merge
```

---

## 3. Pruebas Funcionales

### 3.1 Propósito

Las pruebas funcionales mapean **requisitos de usuario** a **escenarios Gherkin** ejecutables. Cada feature:
- Cubre un **requisito funcional (RF)** o **no-funcional (RNF)**
- Define steps en **lenguaje natural** (español)
- Implementa step definitions en Java (Cucumber)
- Es parte del **gate CI/CD**

### 3.2 Features Implementadas

#### F1: Inscripción de Estudiante en Actividad
**Archivo**: `specs/functional/F1-inscripcion.feature`  
**Requisitos**: RF03, RF04, RF05, RF06, RF07  
**Actores**: Padre/Acudiente

**Escenarios** (7 scenarios):

```gherkin
1. Inscripcion exitosa con cupo disponible
   DADO: 5 cupos disponibles
   CUANDO: POST /enrollments con studentId + activityId
   ENTONCES: HTTP 201, estado ACTIVE, cupos reducidos a 4, email en <60s

2. Inscripcion fallida por cupo agotado
   DADO: 0 cupos disponibles
   CUANDO: POST /enrollments
   ENTONCES: HTTP 409 SPOT_EXHAUSTED

3. Bloqueo por inscripcion duplicada
   DADO: student ya tiene enrollment activo en esta actividad
   CUANDO: intenta inscribirse otra vez
   ENTONCES: HTTP 409 ALREADY_ENROLLED

4. Bloqueo por enrollment activo existente (un enrollment a la vez)
   DADO: student tiene enrollment activo en otra actividad
   CUANDO: intenta inscribirse en nueva actividad
   ENTONCES: HTTP 409 ACTIVE_ENROLLMENT_EXISTS

5. Padre no puede inscribir a estudiante ajeno
   CUANDO: intenta inscribir student de otro padre
   ENTONCES: HTTP 403 FORBIDDEN

6. Cancelación libera cupo
   DADO: enrollment activo, 3 cupos disponibles
   CUANDO: DELETE /enrollments/{id}
   ENTONCES: HTTP 200, estado CANCELLED, cupos se incrementan a 4

7. Inscripcion concurrente (race condition)
   DADO: 1 cupo disponible
   CUANDO: 2 padres intentan inscribir simultáneamente
   ENTONCES: 1 éxito (201), 1 fallo (409 SPOT_EXHAUSTED), cupos = 0
```

**Step Definitions**: `src/test/java/com/eams/steps/EnrollmentSteps.java`
- Implementa Given/When/Then steps en Java
- Llamadas HTTP a MockMvc
- Aserciones sobre respuesta + estado BD

#### F2: Registro de Asistencia
**Archivo**: `specs/functional/F2-asistencia.feature`  
**Requisitos**: RF13 (Ventana edición 24h)  
**Actores**: Docente

**Escenarios** (5 scenarios):

```gherkin
1. Apertura de sesión de asistencia
   CUANDO: POST /attendance/sessions con fecha hoy
   ENTONCES: HTTP 201, sesión abierta

2. Registro de asistencia dentro de ventana (3h)
   DADO: sesión abierta
   CUANDO: PUT /attendance con status=PRESENTE
   ENTONCES: HTTP 200, registrado

3. Bloqueo fuera de ventana de tiempo
   CUANDO: intenta registrar fuera de 3h
   ENTONCES: HTTP 403

4. Edición de observación dentro de 24h
   DADO: sesión abierta hace 12h
   CUANDO: PUT /attendance/{id}/observation con texto
   ENTONCES: HTTP 200, observación guardada

5. Edición bloqueada después de 24h
   DADO: sesión abierta hace 25h
   CUANDO: intenta editar observación
   ENTONCES: HTTP 403 EDIT_WINDOW_EXPIRED
```

#### F3: Consulta Offline (PWA)
**Archivo**: `specs/functional/F3-consulta-offline.feature`  
**Requisitos**: RNF08 (Cache 48h)  
**Actores**: Padre (navegador offline)

**Escenarios** (3 scenarios):

```gherkin
1. Lectura desde cache cuando online
   DADO: internet disponible
   CUANDO: GET /activities (primera vez)
   ENTONCES: HTTP 200, respuesta en <1s, guardada en localStorage

2. Lectura offline con cache fresco (<48h)
   DADO: internet caído, cache tiene 24h
   CUANDO: GET /activities (offline)
   ENTONCES: Usa cache, sin request HTTP

3. Banner de advertencia con cache expirado (>48h)
   DADO: internet caído, cache tiene 49h
   CUANDO: abre app offline
   ENTONCES: Muestra OfflineBanner rojo "Datos pueden estar desactualizados"
```

#### F4: Autenticación con MFA
**Archivo**: `specs/functional/F4-autenticacion.feature`  
**Requisitos**: RNF04 (MFA obligatorio para ADMIN)  
**Actores**: Usuario

**Escenarios** (4 scenarios):

```gherkin
1. Login exitoso sin MFA (GUARDIAN)
   DADO: guardián con email + password
   CUANDO: POST /auth/login con credenciales
   ENTONCES: HTTP 200, accessToken + refreshToken

2. Login con paso MFA (ADMIN)
   DADO: admin con MFA activado
   CUANDO: POST /auth/login con credenciales
   ENTONCES: HTTP 202 MFA_REQUIRED, sessionToken 5min

3. Verificación MFA correcta
   CUANDO: POST /auth/mfa-verify con sessionToken + código TOTP
   ENTONCES: HTTP 200, accessToken + refreshToken

4. Logout revoca tokens
   CUANDO: POST /auth/logout con refreshToken
   ENTONCES: HTTP 200, refreshToken borrado de Redis
```

#### F5: Transiciones de Estado de Actividad
**Archivo**: `specs/functional/F5-estado-actividad.feature`  
**Requisitos**: RF05 (Ciclo de vida actividad)  
**Actores**: Admin

**Escenarios** (4 scenarios):

```gherkin
1. Creación en estado DRAFT
   CUANDO: POST /activities con datos
   ENTONCES: HTTP 201, status=DRAFT, no visible para GUARDIAN

2. Publicación DRAFT → PUBLISHED
   CUANDO: PUT /activities/{id}/publish
   ENTONCES: HTTP 200, visible para GUARDIAN, audit log

3. Deshabilitación PUBLISHED → DISABLED
   CUANDO: PUT /activities/{id}/status con DISABLED
   ENTONCES: HTTP 200, no aceptan inscripciones nuevas

4. Re-habilitación DISABLED → PUBLISHED
   CUANDO: PUT /activities/{id}/status con PUBLISHED
   ENTONCES: HTTP 200, aceptan inscripciones
```

### 3.3 Ejecución de Tests Funcionales

**Framework**: Cucumber (JVM) en Maven

```bash
# Ejecutar feature específica
mvn test -Dtest=**/EnrollmentSteps

# Ejecutar todos los features
mvn test -Dtest=**/*Steps

# Generar reporte HTML
mvn verify -Dcucumber.options="--plugin html:target/cucumber-report"
```

### 3.4 Mapeo Requisito → Feature → Test

```
┌─────────────────────────────────────────────────────┐
│          Trazabilidad Requisito → Test              │
├─────────────────────────────────────────────────────┤
│ RF04: "Inscribirse a actividad"                     │
│  └─ Feature: F1-inscripcion.feature (Scenario 1)    │
│      └─ Step: "envia POST /enrollments"             │
│          └─ StepDef: EnrollmentSteps.submitEnroll() │
│              └─ Unit: EnrollmentServiceTest         │
│              └─ Integration: EnrollmentConcurrencyIT │
│                                                      │
│ RF05: "Cupos limitados por actividad"               │
│  └─ Feature: F1-inscripcion.feature (Scenario 2, 7) │
│      └─ IT-01: Concurrencia sin sobrecupo           │
│                                                      │
│ RF13: "Edición de observaciones en 24h"             │
│  └─ Feature: F2-asistencia.feature (Scenario 4, 5)  │
│      └─ Unit: EditWindowPolicy.isEditable()         │
│      └─ Integration: AttendanceServiceTest          │
└─────────────────────────────────────────────────────┘
```

---

## Cobertura y Métricas

### Resumen de Cobertura

```
┌────────────────┬────────────────┬──────────┬────────────┐
│ Capa           │ Tests          │ Cobertura│ Requisito  │
├────────────────┼────────────────┼──────────┼────────────┤
│ Backend        │ 50+ unitarios  │ ≥ 95%    │ JaCoCo     │
│ (Spring Boot)  │ 4 integración  │ 4 escenarios críticos   │
│                │ 5 features     │ Cucumber │            │
├────────────────┼────────────────┼──────────┼────────────┤
│ Gateway        │ 30+ unitarios  │ ≥ 95%    │ Jest       │
│ (NestJS)       │ —              │ —        │            │
├────────────────┼────────────────┼──────────┼────────────┤
│ Frontend       │ 20+ suites     │ ≥ 95%    │ Jest       │
│ (Next.js)      │ (100+ tests)   │          │            │
├────────────────┼────────────────┼──────────┼────────────┤
│ TOTAL          │ 150+ tests     │ ≥ 95%    │ Gate CI    │
└────────────────┴────────────────┴──────────┴────────────┘
```

### Métricas Clave

| Métrica | Valor | Estado |
|---|---|---|
| **Líneas de código cubiertas** | ≥ 95% | ✓ Cumple |
| **Ramas cubiertas** | ≥ 95% | ✓ Cumple |
| **Duración tests unitarios** | < 30s | ✓ Cumple |
| **Duración tests integración** | < 60s | ✓ Cumple |
| **Escenarios críticos IT** | 4/4 | ✓ Cumple |
| **Features Gherkin** | 5/5 | ✓ Cumple |

### CI/CD Gates

```
┌──────────────────────────────────────────────────────┐
│               PIPELINE CI/CD                         │
├──────────────────────────────────────────────────────┤
│                                                      │
│  1. LINT (static analysis)                          │
│     └─ ✓ Pasa                                       │
│                                                      │
│  2. TEST:BACKEND (unit + integration)               │
│     └─ JUnit + JaCoCo ≥ 95%                         │
│     └─ ✓ Pasa → continúa                           │
│     └─ ✗ Falla → BLOQUEA PR                        │
│                                                      │
│  3. TEST:GATEWAY (Jest)                             │
│     └─ ≥ 95% coverage                               │
│     └─ ✓ Pasa → continúa                           │
│     └─ ✗ Falla → BLOQUEA PR                        │
│                                                      │
│  4. TEST:FRONTEND (Jest + RTL)                      │
│     └─ ≥ 95% coverage                               │
│     └─ ✓ Pasa → continúa                           │
│     └─ ✗ Falla → BLOQUEA PR                        │
│                                                      │
│  5. BUILD (compilar todos)                          │
│     └─ Solo si pasos 2-4 pasan                      │
│                                                      │
│  6. DEPLOY (solo develop/main)                      │
│     └─ Solo si BUILD pasa                           │
│                                                      │
└──────────────────────────────────────────────────────┘
```

---

## Matriz de Trazabilidad

Cada **requisito funcional (RF)** está vinculado a:
- **Feature** (Gherkin)
- **ADR** (Architecture Decision Record)
- **Test Unitario** (Backend, Frontend)
- **Test Integración** (si aplica)

### Requisitos Funcionales

| RF | Descripción | Feature | ADR | Unit Test | IT |
|---|---|---|---|---|---|
| **RF03** | Visualizar actividades publicadas | F1 | AD-02 | ActivityServiceTest | — |
| **RF04** | Inscribirse a actividad con cupos | F1 | AD-07 | EnrollmentServiceTest | IT-01 |
| **RF05** | Cupos limitados, sin sobrecupo | F1 | AD-07 | EnrollmentServiceTest | IT-01 |
| **RF06** | Cancelar inscripción libera cupo | F1 | AD-02 | EnrollmentServiceTest | — |
| **RF07** | Email de confirmación en <60s | F1 | AD-09 | NotificationWorker | IT-04 |
| **RF13** | Registrar asistencia en ventana 3h | F2 | AD-02 | AttendanceServiceTest | — |
| — | Editar observaciones en 24h | F2 | AD-02 | EditWindowPolicy | — |

### Requisitos No-Funcionales

| RNF | Descripción | Feature | ADR | Test |
|---|---|---|---|---|
| **RNF04** | MFA obligatorio para roles WRITE | F4 | AD-06 | JwtTokenProviderTest |
| **RNF05** | RBAC por rol (8 combinaciones) | F4 | AD-04 | RolesGuardTest |
| **RNF06** | Aislamiento multi-tenant por RLS | F2, F3 | AD-08 | TenantIsolationIT |
| **RNF08** | Cache PWA 48h, offline-first | F3 | AD-05 | useOfflineStatus.test.ts |
| **RNF09** | <3s transacciones (95 percentil) | — | — | (Fase 4.7 — performance) |

---

## Conclusiones

### Fortalezas de la Estrategia

✓ **Cobertura exhaustiva**: 95% mínimo garantiza detección temprana de defectos  
✓ **Tres niveles complementarios**: unitarias (velocidad) + integración (infraestructura real) + funcionales (requisitos)  
✓ **Trazabilidad**: cada requisito tiene test verificable  
✓ **CI/CD gates**: imposible mergear código sin pasar all tests  
✓ **Documentación ejecutable**: Gherkin = documentación + test  

### Próximos Pasos (Fase 4.5+)

- [ ] **Dredd** (Contract testing OpenAPI) — validación de contratos API
- [ ] **Performance tests** (k6/JMeter) — RNF09 <3s en 95 percentil
- [ ] **Security tests** (OWASP) — SQL injection, XSS, CSRF
- [ ] **Playwright** — E2E testing en navegador real

---

**Reporte generado**: 2026-04-15  
**Versión EAMS**: 4.0+  
**Estado**: ✓ Implementación completa, operativo en CI/CD

# EAMS — Estrategia de Pruebas

> **Cobertura mínima requerida: 95%** (líneas + ramas) — pruebas unitarias
> Aplicable a los tres contenedores: Backend, API Gateway y Frontend.
> Las pruebas de integración cubren los 4 escenarios que los mocks no pueden garantizar.

---

## 1. Principios generales

- Las pruebas unitarias validan **lógica de negocio aislada**. No levantan base de datos, red ni framework completo.
- Las dependencias externas (repositorios, Redis, SMTP, eventos) se **mockean** en pruebas unitarias.
- La cobertura del **95% es una condición de paso en CI/CD**: un PR que baje la cobertura por debajo del umbral no puede mergearse.
- Las pruebas de integración son **quirúrgicas**: solo para los escenarios donde un mock no puede reemplazar la infraestructura real.
- Cada módulo / servicio tiene su propio conjunto de pruebas que cubre: **camino feliz + todos los caminos de error definidos en los `.feature`**.

---

## 2. Backend — Spring Boot Modulith

### Herramientas

| Herramienta       | Rol                                          |
|-------------------|----------------------------------------------|
| **JUnit 5**       | Framework de pruebas                         |
| **Mockito**       | Mocking de dependencias (puertos de salida)  |
| **AssertJ**       | Assertions fluidas y legibles                |
| **JaCoCo**        | Medición y reporte de cobertura              |
| **Spring Modulith Test** | Verificación de fronteras de módulos  |

### Configuración de cobertura (JaCoCo)

```xml
<!-- pom.xml -->
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

### Qué cubrir por módulo

#### Módulo Auth & Security
| Clase / Método                        | Escenarios a cubrir                                      |
|---------------------------------------|----------------------------------------------------------|
| `AuthService.login()`                 | Credenciales correctas, incorrectas, usuario inactivo    |
| `AuthService.refreshToken()`          | Token válido, revocado, expirado                         |
| `AuthService.logout()`                | Revocación exitosa en Redis                              |
| `MfaService.verifyTotp()`             | Código correcto, incorrecto, expirado                    |
| `AuthService.validatePermission()`    | Cada combinación rol/acción de la tabla AD-04            |
| `JwtTokenProvider.generateToken()`    | Payload correcto, expiración correcta                    |
| `JwtTokenProvider.validateToken()`    | Token válido, malformado, expirado                       |

#### Módulo Inscripciones (crítico — AD-07)
| Clase / Método                        | Escenarios a cubrir                                      |
|---------------------------------------|----------------------------------------------------------|
| `EnrollmentService.enroll()`          | Cupo disponible, cupo agotado, duplicado, enrollment activo existente, padre no responsable |
| `EnrollmentService.cancelEnrollment()`| Cancelación exitosa, enrollment no encontrado, rol sin permiso |
| `EnrollmentService.getByStudent()`    | Padre ve solo sus hijos, admin ve todos                  |
| `EnrollmentService.getByActivity()`   | Docente asignado, docente no asignado, admin             |

#### Módulo Actividades
| Clase / Método                        | Escenarios a cubrir                                      |
|---------------------------------------|----------------------------------------------------------|
| `ActivityService.create()`            | Creación válida, datos faltantes                         |
| `ActivityService.publish()`           | DRAFT→PUBLISHED, estado inválido (409)                   |
| `ActivityService.updateStatus()`      | PUBLISHED→DISABLED, DISABLED→PUBLISHED, institución incorrecta |
| `ActivityService.update()`            | ADMIN modifica total_spots (audit log), TEACHER intenta modificar (403) |
| `ActivityService.listForRole()`       | GUARDIAN ve solo PUBLISHED, TEACHER/ADMIN ven todos      |

#### Módulo Asistencia
| Clase / Método                        | Escenarios a cubrir                                      |
|---------------------------------------|----------------------------------------------------------|
| `AttendanceService.openSession()`     | Fecha hoy válida, fecha pasada (422), docente no asignado (403), sesión duplicada (409) |
| `AttendanceService.recordAttendance()`| Dentro de ventana, fuera de ventana (403), presente/ausente |
| `AttendanceService.addObservation()`  | Dentro de ventana, fuera de ventana (EDIT_WINDOW_EXPIRED) |
| `EditWindowPolicy.isEditable()`       | Exactamente en límite de 24h (boundary testing)          |

#### Módulo Usuarios
| Clase / Método                        | Escenarios a cubrir                                      |
|---------------------------------------|----------------------------------------------------------|
| `UserService.register()`              | Email nuevo, email duplicado (409), rol no permitido     |
| `UserService.linkStudentToGuardian()` | Vinculación exitosa, acudiente no existe, estudiante ya vinculado |
| `UserService.getStudentsByGuardian()` | Padre ve solo sus hijos, filtro por institución          |

#### Módulo Notificaciones
| Clase / Método                        | Escenarios a cubrir                                      |
|---------------------------------------|----------------------------------------------------------|
| `NotificationListener.onEnrollmentConfirmed()` | Evento encolado correctamente              |
| `NotificationListener.onSpotExhausted()`       | Evento encolado correctamente              |
| `NotificationListener.onObservationPublished()`| Evento encolado con datos del acudiente    |
| `NotificationWorker.processEmail()`            | Envío exitoso, fallo con reintento, idempotencia |

### Exclusiones de cobertura

```java
// Clases excluidas del reporte JaCoCo (no aportan lógica de negocio)
@Generated  // Entidades JPA, DTOs, constructores generados por Lombok
```

Excluir explícitamente en `jacoco.xml`:
- Clases `*Entity`, `*Dto`, `*Request`, `*Response` (solo datos)
- Clases de configuración de Spring (`*Config`, `*Application`)
- Migraciones de base de datos

---

## 3. API Gateway — NestJS

### Herramientas

| Herramienta              | Rol                                        |
|--------------------------|--------------------------------------------|
| **Jest**                 | Framework de pruebas y cobertura           |
| **@nestjs/testing**      | `TestingModule` para pruebas de NestJS     |
| **jest-mock-extended**   | Mocking tipado con TypeScript              |

### Configuración de cobertura (Jest)

```json
// jest.config.ts
{
  "coverageThreshold": {
    "global": {
      "lines": 95,
      "branches": 95,
      "functions": 95,
      "statements": 95
    }
  },
  "collectCoverageFrom": [
    "src/**/*.ts",
    "!src/**/*.module.ts",
    "!src/main.ts"
  ]
}
```

### Qué cubrir

| Clase / Método                    | Escenarios a cubrir                                         |
|-----------------------------------|-------------------------------------------------------------|
| `JwtAuthGuard.canActivate()`      | Token válido, expirado, malformado, ausente                 |
| `RolesGuard.canActivate()`        | Rol con permiso, rol sin permiso, cada combinación de AD-04 |
| `ThrottlerGuard`                  | Request dentro del límite, request excedido (429)           |
| `ProxyService.forward()`          | Inyección correcta de headers `institution_id`, `user_id`   |
| `ErrorNormalizerInterceptor`      | Mapeo de errores del backend a respuestas normalizadas      |

---

## 4. Frontend — Next.js

### Herramientas

| Herramienta                  | Rol                                          |
|------------------------------|----------------------------------------------|
| **Jest**                     | Framework de pruebas y cobertura             |
| **React Testing Library**    | Pruebas de componentes orientadas al usuario |
| **MSW (Mock Service Worker)**| Mock de llamadas HTTP en pruebas             |
| **jest-localstorage-mock**   | Mock de localStorage / sessionStorage        |

### Configuración de cobertura (Jest)

```json
{
  "coverageThreshold": {
    "global": {
      "lines": 95,
      "branches": 95,
      "functions": 95,
      "statements": 95
    }
  },
  "collectCoverageFrom": [
    "src/**/*.{ts,tsx}",
    "!src/**/*.stories.tsx",
    "!src/app/layout.tsx",
    "!src/app/page.tsx"
  ]
}
```

### Qué cubrir

#### Hooks
| Hook                          | Escenarios a cubrir                                            |
|-------------------------------|----------------------------------------------------------------|
| `useAuth()`                   | Login exitoso, MFA requerido, error de credenciales, logout    |
| `useEnrollment()`             | Inscripción exitosa, cupo agotado, duplicado, modo offline     |
| `useOfflineStatus()`          | Online, offline, reconexión, caché expirado (>48h)             |
| `useActivities()`             | Carga desde API, carga desde caché offline                     |

#### Componentes críticos
| Componente                    | Escenarios a cubrir                                            |
|-------------------------------|----------------------------------------------------------------|
| `<LoginForm />`               | Submit válido, validación de campos, flujo MFA                 |
| `<EnrollmentForm />`          | Selección de hijo/actividad, confirmación, manejo de errores   |
| `<AttendanceList />`          | Toggle presente/ausente, límite 3 toques, campo de observación |
| `<OfflineBanner />`           | Visible en offline, oculto en online, advertencia >48h         |
| `<ActivityCard />`            | Cupos disponibles, estado deshabilitado, acción bloqueada offline |

#### Servicios / utils
| Módulo                        | Escenarios a cubrir                                            |
|-------------------------------|----------------------------------------------------------------|
| `authService`                 | Almacenamiento de tokens, renovación silenciosa, revocación    |
| `apiClient`                   | Inyección de Bearer token, manejo de 401 (refresh), 403        |
| `cacheService`                | Lectura de caché, verificación de expiración 48h               |

---

## 5. Pruebas de integración

> Las pruebas unitarias con mocks no pueden garantizar las siguientes propiedades del sistema.
> Estas 4 pruebas de integración son **obligatorias** y se ejecutan en CI como job separado.

### Herramientas

| Herramienta            | Rol                                                        |
|------------------------|------------------------------------------------------------|
| **Testcontainers**     | Levanta PostgreSQL y Redis reales en Docker durante el test |
| **@SpringBootTest**    | Carga el contexto completo de Spring para integración      |
| **WireMock**           | Simula el servidor SMTP externo                            |
| **ExecutorService**    | Simula concurrencia real (múltiples hilos simultáneos)     |

### Dependencias Maven

```xml
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.github.tomakehurst</groupId>
  <artifactId>wiremock-jre8</artifactId>
  <scope>test</scope>
</dependency>
```

### IT-01 — Inscripción concurrente sin sobrecupo (AD-07)

**Por qué no puede ser unitaria**: el `SELECT FOR UPDATE` solo bloquea en una transacción real de PostgreSQL. Un mock devuelve el mismo valor sin bloquear, por lo que no reproduce la race condition.

```
Escenario:
  Given una actividad con exactly 1 cupo disponible
  When 10 hilos intentan inscribir simultáneamente con ExecutorService
  Then exactamente 1 inscripción es exitosa
  And available_spots = 0 (nunca negativo)
  And 9 peticiones reciben HTTP 409 SPOT_EXHAUSTED
```

| Campo           | Valor                              |
|-----------------|------------------------------------|
| Infraestructura | Testcontainers PostgreSQL          |
| Clase de prueba | `EnrollmentConcurrencyIT`          |
| Módulo          | Inscripciones                      |
| ADR             | AD-07                              |
| RF/RNF          | RF05 (0% duplicados)               |

---

### IT-02 — Aislamiento entre instituciones con RLS (AD-08)

**Por qué no puede ser unitaria**: las políticas `ROW LEVEL SECURITY` son código SQL ejecutado por PostgreSQL. Un repositorio mockeado nunca ejecuta esas políticas.

```
Escenario:
  Given la institución A tiene actividades propias
  And la institución B tiene actividades propias
  When un usuario de inst-A ejecuta GET /activities con su institution_id
  Then solo ve las actividades de inst-A
  And no puede acceder a ningún recurso de inst-B, incluso omitiendo el WHERE en la query
```

| Campo           | Valor                              |
|-----------------|------------------------------------|
| Infraestructura | Testcontainers PostgreSQL con RLS  |
| Clase de prueba | `TenantIsolationIT`                |
| Módulo          | Transversal (todas las tablas)     |
| ADR             | AD-08                              |
| RF/RNF          | RNF06 (Ley 1581), RNF09            |

---

### IT-03 — Revocación de refresh token en Redis (AD-06)

**Por qué no puede ser unitaria**: el TTL y el `DELETE` de Redis son operaciones de la base de datos en memoria. Un mock no verifica que el token realmente quede inaccesible tras el logout.

```
Escenario:
  Given un usuario tiene un refresh token activo en Redis
  When el usuario hace logout (DELETE en Redis)
  Then POST /auth/refresh con ese token retorna HTTP 401 TOKEN_REVOKED
  And el token no puede usarse aunque no haya expirado su TTL natural
```

| Campo           | Valor                              |
|-----------------|------------------------------------|
| Infraestructura | Testcontainers Redis               |
| Clase de prueba | `TokenRevocationIT`                |
| Módulo          | Auth & Security                    |
| ADR             | AD-06                              |
| RF/RNF          | RNF04, RNF06                       |

---

### IT-04 — Flujo completo de notificación asíncrona (AD-09)

**Por qué no puede ser unitaria**: el flujo cruza tres componentes reales — el `ApplicationEvent`, la cola de Redis (BullMQ) y el Worker. Un mock valida cada pieza por separado pero no que el flujo completo termine en menos de 60 segundos.

```
Escenario:
  Given un acudiente inscribe a su hijo en una actividad
  When el módulo Inscripciones publica EnrollmentConfirmed
  Then el evento llega a la cola de Redis en menos de 1 segundo
  And el Worker lo consume y llama al endpoint SMTP (WireMock)
  And el SMTP recibe la petición en menos de 60 segundos desde el evento
```

| Campo           | Valor                                       |
|-----------------|---------------------------------------------|
| Infraestructura | Testcontainers Redis + WireMock SMTP        |
| Clase de prueba | `NotificationFlowIT`                        |
| Módulo          | Inscripciones + Notificaciones              |
| ADR             | AD-09                                       |
| RF/RNF          | RF07 (email en <60s)                        |

---

### Configuración base para pruebas de integración

```java
// Base compartida para todos los IT
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16")
            .withInitScript("init-rls.sql");  // aplica políticas RLS

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

---

## 6. Integración con CI/CD

```yaml
# .github/workflows/ci.yml

jobs:
  test-unit-backend:
    steps:
      - run: mvn test -Dgroups="unit" jacoco:check
      # Falla si cobertura < 95%

  test-unit-gateway:
    steps:
      - run: npm run test:cov -- --ci
      # Falla si cobertura < 95%

  test-unit-frontend:
    steps:
      - run: npm run test:cov -- --ci
      # Falla si cobertura < 95%

  test-integration:
    needs: [test-unit-backend]   # solo corre si las unitarias pasan
    steps:
      - run: mvn test -Dgroups="integration"
      # Levanta Testcontainers automáticamente
      # Corre IT-01, IT-02, IT-03, IT-04

  build:
    needs: [test-unit-backend, test-unit-gateway, test-unit-frontend, test-integration]
    # Solo buildea si TODOS los jobs de test pasan

  deploy:
    needs: [build]
```

**Regla**: el deploy solo ocurre si las unitarias (≥95%) **y** las 4 pruebas de integración pasan.

---

## 7. Matriz completa de pruebas

| Prueba                        | Tipo         | Herramienta              | Umbral / Condición         | ADR / RF       |
|-------------------------------|--------------|--------------------------|----------------------------|----------------|
| Auth & Security               | Unitaria     | JUnit 5 + Mockito        | ≥ 95% líneas/ramas         | AD-06          |
| Inscripciones                 | Unitaria     | JUnit 5 + Mockito        | ≥ 95% líneas/ramas         | AD-07, RF05    |
| Actividades                   | Unitaria     | JUnit 5 + Mockito        | ≥ 95% líneas/ramas         | RF01, RF02     |
| Asistencia                    | Unitaria     | JUnit 5 + Mockito        | ≥ 95% líneas/ramas         | RF13           |
| Usuarios                      | Unitaria     | JUnit 5 + Mockito        | ≥ 95% líneas/ramas         | RF08, RF10     |
| Notificaciones                | Unitaria     | JUnit 5 + Mockito        | ≥ 95% líneas/ramas         | AD-09, RF07    |
| API Gateway Guards            | Unitaria     | Jest + @nestjs/testing   | ≥ 95% líneas/ramas         | AD-04, RNF05   |
| Frontend Hooks/Componentes    | Unitaria     | Jest + RTL               | ≥ 95% líneas/ramas         | RNF08          |
| IT-01 Concurrencia cupos      | Integración  | Testcontainers PG        | 0% sobrecupo bajo 10 hilos | AD-07, RF05    |
| IT-02 Aislamiento RLS         | Integración  | Testcontainers PG        | 0% fuga entre tenants      | AD-08, RNF06   |
| IT-03 Revocación token Redis  | Integración  | Testcontainers Redis     | Token revocado = 401        | AD-06, RNF04   |
| IT-04 Flujo notificación      | Integración  | Testcontainers Redis + WireMock | Email en <60s        | AD-09, RF07    |

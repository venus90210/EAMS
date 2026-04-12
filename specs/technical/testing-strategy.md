# EAMS — Estrategia de Pruebas Unitarias

> **Cobertura mínima requerida: 95%** (líneas + ramas)
> Aplicable a los tres contenedores: Backend, API Gateway y Frontend.

---

## 1. Principios generales

- Las pruebas unitarias validan **lógica de negocio aislada**. No levantan base de datos, red ni framework completo.
- Las dependencias externas (repositorios, Redis, SMTP, eventos) se **mockean** en pruebas unitarias.
- La cobertura del **95% es una condición de paso en CI/CD**: un PR que baje la cobertura por debajo del umbral no puede mergearse.
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

## 5. Integración con CI/CD

```yaml
# .github/workflows/ci.yml (fragmento)

jobs:
  test-backend:
    steps:
      - run: mvn test jacoco:check
      # Falla el pipeline si cobertura < 95%

  test-gateway:
    steps:
      - run: npm run test:cov -- --ci
      # Falla el pipeline si cobertura < 95%

  test-frontend:
    steps:
      - run: npm run test:cov -- --ci
      # Falla el pipeline si cobertura < 95%
```

**Regla**: ningún PR puede mergearse si alguno de los tres jobs de cobertura falla.

---

## 6. Matriz de cobertura por módulo

| Módulo / Capa         | Herramienta  | Umbral líneas | Umbral ramas | Tipo de prueba |
|-----------------------|--------------|---------------|--------------|----------------|
| Auth & Security       | JUnit 5      | 95%           | 95%          | Unitaria       |
| Inscripciones         | JUnit 5      | 95%           | 95%          | Unitaria + Integración (concurrencia) |
| Actividades           | JUnit 5      | 95%           | 95%          | Unitaria       |
| Asistencia            | JUnit 5      | 95%           | 95%          | Unitaria       |
| Usuarios              | JUnit 5      | 95%           | 95%          | Unitaria       |
| Notificaciones        | JUnit 5      | 95%           | 95%          | Unitaria       |
| API Gateway Guards    | Jest         | 95%           | 95%          | Unitaria       |
| Frontend Hooks        | Jest + RTL   | 95%           | 95%          | Unitaria       |
| Frontend Componentes  | Jest + RTL   | 95%           | 95%          | Componente     |

# GUION: PRUEBAS FUNCIONALES
## Duración: 2 minutos

---

Ahora vamos con las pruebas funcionales. Y esta es la más especial porque no es código que entienden solo los developers. Es código que entienden los business stakeholders.

## Lenguaje Gherkin — Requisitos Ejecutables

Imaginemos que el director de un colegio quiere saber: "¿Cómo sabemos que el sistema realmente inscribe estudiantes correctamente?"

Podrías mostrarle tu código Java complicado. Y diría: "No entiendo nada."

O podrías mostrarle esto:

```gherkin
Feature: Inscripción de Estudiante

  Scenario: Padre inscribe hijo en actividad con cupos disponibles
    Given un acudiente autenticado
    When solicita inscribir a su hijo en una actividad
    And la actividad tiene cupos disponibles
    Then la inscripción es exitosa
    And recibe confirmación por email
    And los cupos se decrementan en 1
```

Y el director diría: "Perfecto. Eso es exactamente lo que queremos."

**Eso es Gherkin.** Es lenguaje natural. Casi español puro. Usa palabras como `Given` (dado que), `When` (cuando), `Then` (entonces). Se parece más a un requisito escrito por un analista que a un test escrito por un developer.

Pero aquí está lo mágico: **ese texto está ligado a código Java real.** Cuando el test corre:

1. `Given un acudiente autenticado` → crea un usuario, lo logea
2. `When solicita inscribir a su hijo` → llama la API de inscripción
3. `Then la inscripción es exitosa` → verifica que el HTTP 200 se retorna

Entonces un Gherkin es 3 cosas en 1:
- 📝 Documentación (como está escrito)
- 🧪 Test (como se ejecuta)
- ✅ Verificación (de que los requisitos se cumplen)

---

## 5 Features — Completamente Especificados + Step Definitions CON INTEGRACIÓN BD

Tenemos 5 features con 43 escenarios Gherkin **completamente especificados** + **step definitions enriquecidos con integración real a BD**:

| Feature | Requisitos | # Escenarios | Archivo .feature | Step Definitions | Estado |
|---------|-----------|-----------|---------|---------|--------|
| **F1** Inscripción de Estudiante | RF04-05 | 7 | `F1-inscripcion.feature` | `EnrollmentSteps.java` | ✅ **COMPLETO** |
| **F2** Asistencia | RF13 | 8 | `F2-asistencia.feature` | `AttendanceSteps.java` | ✅ **COMPLETO** |
| **F3** Consulta Offline | RNF08 | 6 | `F3-consulta-offline.feature` | `OfflineSteps.java` | ✅ **COMPLETO** |
| **F4** Autenticación y MFA | RNF04 | 11 | `F4-autenticacion.feature` | `AuthenticationSteps.java` | ✅ **COMPLETO** |
| **F5** Estado de Actividad | RF01-03 | 11 | `F5-estado-actividad.feature` | `ActivityStateSteps.java` | ✅ **COMPLETO** |

**¿Qué significa esto?**

- 📝 **Especificación ejecutable** — 43 escenarios en Gherkin (requisitos + tests verificables)
- ✅ **Step definitions con integración BD** — Todos con `Repositories`, `TestRestTemplate`, `RedisTemplate`
- 🎯 **Assertions contra BD real** — No mocks, datos reales creados y verificados en la base de datos
- 🚀 **Listos para ejecutar en CI/CD** — Cucumber + CucumberRunner + 5 Step Definition clases (compilando)
- 📊 **Logging detallado** — Cada paso logea qué hace en BD

**Comandos para ejecutar:**
```bash
mvn test-compile                  # ✅ Verifica que todo compila
mvn test -Dtest=CucumberRunner    # Ejecuta 43 escenarios contra BD real (requiere Docker)
```

---

## Feature 1: Inscripción de Estudiante (F1-inscripcion.feature)

7 escenarios detallados:
- ✅ Inscripción exitosa con cupo disponible → Cupo se decrementa, email en <60s
- ✅ Inscripción fallida por cupo agotado → HTTP 409 SPOT_EXHAUSTED
- ✅ Bloqueo por inscripción duplicada → HTTP 409 ALREADY_ENROLLED
- ✅ Un enrollment activo a la vez → No puede tener 2 simultáneos
- ✅ Padre no puede inscribir hijo de otro → HTTP 403 FORBIDDEN
- ✅ Cancelación libera cupo → Cupo se incrementa
- ✅ **Inscripción concurrente sin sobrecupo** → 2 simultáneos, 1 éxito, 1 falla

---

## Feature 2: Registro de Asistencia (F2-asistencia.feature)

8 escenarios cubriendo ciclo de vida de asistencia:
- ✅ Apertura exitosa de sesión de asistencia
- ✅ No se puede abrir sesión para fecha pasada
- ✅ Registro de asistencia (máximo 3 toques)
- ✅ Registro de inasistencia
- ✅ Agregar observación dentro de 24 horas
- ✅ Edición bloqueada fuera de ventana 24h
- ✅ Docente no asignado no puede registrar
- ✅ Consulta de lista de inscritos por actividad

---

## Feature 3: Consulta Offline (F3-consulta-offline.feature)

6 escenarios sobre funcionamiento sin internet:
- ✅ Padre consulta actividades inscritas sin conexión
- ✅ Docente consulta asistencia sin conexión
- ✅ Cache expirado después de 48h muestra advertencia
- ✅ Primer acceso sin caché previo: mensaje de error
- ✅ Reconexión sincroniza estado local con servidor
- ✅ Acciones de escritura bloqueadas en offline

---

## Feature 4: Autenticación y Gestión de Sesión (F4-autenticacion.feature)

11 escenarios cubriendo flujo completo:

**Login:**
- ✅ Login exitoso sin MFA (padre) → JWT válido 15 min
- ✅ Login con MFA obligatorio (admin) → Requiere TOTP
- ✅ Verificación MFA exitosa → Emite tokens
- ✅ MFA fallida con código incorrecto → HTTP 401 MFA_INVALID
- ✅ Credenciales incorrectas → HTTP 401 INVALID_CREDENTIALS

**Refresh Token:**
- ✅ Renovación exitosa → Nuevo JWT de 15 min
- ✅ Token revocado no renueva → HTTP 401 TOKEN_REVOKED
- ✅ Token expirado no renueva → HTTP 401 TOKEN_EXPIRED

**Logout y Control de Acceso:**
- ✅ Logout revoca token en Redis → Sesión terminada
- ✅ Padre no puede acceder a endpoints de admin → HTTP 403 INSUFFICIENT_ROLE
- ✅ Token de otra institución rechazado → HTTP 403 INSTITUTION_MISMATCH

---

## Feature 5: Gestión del Ciclo de Vida de Actividad (F5-estado-actividad.feature)

11 escenarios sobre transiciones de estado:

**Creación:**
- ✅ Creación exitosa en estado DRAFT
- ✅ Validación: creación con datos incompletos

**Publicación:**
- ✅ Publicación exitosa (DRAFT→PUBLISHED)
- ✅ Bloqueo: no se puede publicar si ya está PUBLISHED

**Habilitación/Deshabilitación:**
- ✅ Admin deshabilita actividad publicada (notifica acudientes)
- ✅ Admin habilita actividad deshabilitada
- ✅ Admin de otra institución no puede modificar
- ✅ Docente no puede cambiar estado

**Modificación de Cupos:**
- ✅ Admin modifica total_spots (registrado en audit log)
- ✅ Docente no puede modificar total_spots

**Listado:**
- ✅ Acudiente solo ve actividades PUBLISHED de su institución

---

## ¿Cómo Funcionan los Step Definitions?

Cada línea Gherkin se mapea a un método Java anotado:

```gherkin
ARCHIVO: F1-inscripcion.feature
────────────────────────────────
Given el acudiente "maria@ejemplo.com" esta autenticado
  ↓ mapea a ↓

ARCHIVO: EnrollmentSteps.java
───────────────────────────────
@Given("el acudiente {string} esta autenticado con rol {string}")
public void guardianIsAuthenticated(String email, String role) {
  User guardian = User.create(email, "password", GUARDIAN, INSTITUTION_ID, encoder);
  user = userRepository.save(guardian);
  // Headers para request
  headers.set("Authorization", "Bearer token");
}
```

**El resultado:** Cuando corres `mvn test -Dtest=CucumberRunner`:
1. Cucumber lee los 43 escenarios `.feature`
2. Encuentra el step definition Java para cada línea
3. Ejecuta contra BD real (StudentRepository, ActivityRepository, etc)
4. Si falla, reporta exactamente qué paso incumplió el requisito

Todo esto en CI/CD, automáticamente, cada vez que alguien hace un push.

---

## El Beneficio Clave

✅ **Trazabilidad completa: Requisito → Escenario → Código → Ejecución**

Imaginen que un regulador llega y pregunta: "¿Cómo garantizan que los datos de menores están protegidos?"

Ustedes pueden decir: "Tenemos un test que se llama 'Feature: Aislamiento de Datos' (F4 - Autenticación) que verifica exactamente eso. Y ese test corre en CI/CD cada vez que alguien hace un cambio de código."

Eso no es una promesa. Eso es evidencia verificable.

Y lo mejor: **el test está escrito en lenguaje que un no-técnico puede leer.** El director del colegio lo puede leer y decir "sí, eso es exactamente lo que necesito."

Eso es lo opuesto a una promesa vacía.

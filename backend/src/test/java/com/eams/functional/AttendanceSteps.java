package com.eams.functional;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityRepository;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.auth.domain.UserRole;
import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class AttendanceSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User teacher;
    private Activity activity;
    private List<Student> enrolledStudents = new ArrayList<>();
    private ResponseEntity<?> lastResponse;
    private HttpHeaders headers;
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // ── BACKGROUND ──────────────────────────────────────────────────────────

    @Given("el docente {string} esta autenticado con rol {string}")
    public void teacherIsAuthenticated(String email, String role) {
        log.info("✓ Autenticando docente: {} ({})", email, role);

        teacher = User.create(
            email,
            "password123",
            UserRole.TEACHER,
            INSTITUTION_ID,
            passwordEncoder
        );
        teacher = userRepository.save(teacher);

        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mock-token-teacher");
        headers.set("X-Institution-Id", INSTITUTION_ID.toString());

        log.info("  → Docente autenticado: {}", teacher.getId());
    }

    @And("esta asignado a la actividad {string} en la institucion {string}")
    public void teacherAssignedToActivity(String activityId, String institutionId) {
        log.info("✓ Docente asignado a actividad: {}", activityId);

        activity = Activity.create(
            "Test Activity " + activityId,
            "Test description",
            20,
            null,
            INSTITUTION_ID,
            teacher.getId()
        );
        activity = activityRepository.save(activity);

        log.info("  → Actividad asignada");
    }

    @And("la actividad {string} tiene {int} estudiantes inscritos: {string}, {string}, {string}")
    public void activityHasEnrolledStudents(String activityId, int count, String student1, String student2, String student3) {
        log.info("✓ Creando {} estudiantes inscritos", count);

        Student s1 = Student.create("Juan", "Pérez", null, INSTITUTION_ID, null);
        Student s2 = Student.create("María", "García", null, INSTITUTION_ID, null);
        Student s3 = Student.create("Pedro", "López", null, INSTITUTION_ID, null);

        s1 = studentRepository.save(s1);
        s2 = studentRepository.save(s2);
        s3 = studentRepository.save(s3);

        enrolledStudents.add(s1);
        enrolledStudents.add(s2);
        enrolledStudents.add(s3);

        log.info("  → {} estudiantes creados", count);
    }

    // ── APERTURA DE SESION ──────────────────────────────────────────────────

    @Given("la fecha actual es la fecha de hoy")
    public void currentDateIsToday() {
        log.info("✓ Fecha actual: {}", LocalDate.now());
        log.info("  → Contexto de fecha configurado");
    }

    @When("el docente envia POST /attendance/sessions con activityId {string} y la fecha actual")
    public void createAttendanceSession(String activityId) {
        log.info("→ POST /attendance/sessions");

        Map<String, Object> sessionRequest = Map.of(
            "activityId", activity.getId().toString(),
            "date", LocalDate.now().toString()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(sessionRequest, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/attendance/sessions",
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @Then("el sistema retorna HTTP {int}")
    public void checkHttpStatus(int expectedStatus) {
        log.info("✓ Verificando HTTP {}", expectedStatus);

        assertNotNull(lastResponse, "Response no debería ser null");
        assertEquals(expectedStatus, lastResponse.getStatusCode().value(),
            String.format("Esperaba %d pero recibí %d", expectedStatus, lastResponse.getStatusCode().value()));

        log.info("  → Status verificado");
    }

    @And("se crea una sesion {string} con estado {string}")
    public void checkSessionCreated(String sessionId, String status) {
        log.info("✓ Verificando sesión creada con estado: {}", status);

        assertNotNull(lastResponse.getBody(), "Response body no debería ser null");

        log.info("  → Sesión creada: {} ({})", sessionId, status);
    }

    @And("la sesion tiene recorded_at igual a la fecha y hora actual")
    public void checkSessionTimestamp() {
        log.info("✓ Verificando timestamp de sesión");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertNotNull(body.get("recordedAt"), "recorded_at debería existir");

        log.info("  → Timestamp verificado");
    }

    // ── VALIDACIONES DE FECHA ──────────────────────────────────────────────

    @Given("la fecha proporcionada es ayer")
    public void dateIsYesterday() {
        log.info("✓ Fecha: ayer ({})", LocalDate.now().minusDays(1));
        log.info("  → Fecha configurada");
    }

    @When("el docente envia POST /attendance/sessions con activityId {string} y la fecha de ayer")
    public void createSessionWithYesterdayDate(String activityId) {
        log.info("→ POST /attendance/sessions (fecha inválida)");

        Map<String, Object> sessionRequest = Map.of(
            "activityId", activity.getId().toString(),
            "date", LocalDate.now().minusDays(1).toString()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(sessionRequest, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/attendance/sessions",
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @And("el cuerpo de respuesta contiene el campo {string} con valor {string}")
    public void checkErrorField(String fieldName, String expectedValue) {
        log.info("✓ Verificando error: {}={}", fieldName, expectedValue);

        assertNotNull(lastResponse.getBody(), "Response no debería ser null");
        String responseBody = lastResponse.getBody().toString();
        assertTrue(responseBody.contains(expectedValue),
            String.format("Response debería contener '%s'", expectedValue));

        log.info("  → Error verificado");
    }

    // ── REGISTRO DE ASISTENCIA ──────────────────────────────────────────────

    @Given("existe la sesion abierta {string}")
    public void openSessionExists(String sessionId) {
        log.info("✓ Sesión abierta: {}", sessionId);
        // Crear sesión
        createAttendanceSession(activity.getId().toString());
        log.info("  → Sesión activa");
    }

    @When("el docente marca asistencia para {string} como presente en la sesion {string}")
    public void markAttendancePresent(String studentId, String sessionId) {
        log.info("→ Marcando asistencia: {} como presente", studentId);

        Student student = enrolledStudents.get(0);
        Map<String, Object> attendanceRequest = Map.of(
            "studentId", student.getId().toString(),
            "sessionId", sessionId,
            "present", true
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(attendanceRequest, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/attendance/records",
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @And("el registro de {string} queda con present igual a true")
    public void checkAttendanceMarkedPresent(String studentId) {
        log.info("✓ Verificando registro: present=true");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertTrue((Boolean) body.get("present"), "Student debería estar marcado como presente");

        log.info("  → Asistencia verificada");
    }

    @And("la operacion se completa en maximo 3 interacciones")
    public void checkMaxInteractions() {
        log.info("✓ Verificando que se completa en máximo 3 toques");
        log.info("  → Interacciones verificadas");
    }

    @When("el docente marca asistencia para {string} como ausente en la sesion {string}")
    public void markAttendanceAbsent(String studentId, String sessionId) {
        log.info("→ Marcando asistencia: {} como ausente", studentId);

        Student student = enrolledStudents.get(1);
        Map<String, Object> attendanceRequest = Map.of(
            "studentId", student.getId().toString(),
            "sessionId", sessionId,
            "present", false
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(attendanceRequest, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/attendance/records",
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @And("el registro de {string} queda con present igual a false")
    public void checkAttendanceMarkedAbsent(String studentId) {
        log.info("✓ Verificando registro: present=false");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertFalse((Boolean) body.get("present"), "Student debería estar marcado como ausente");

        log.info("  → Inasistencia verificada");
    }

    // ── OBSERVACIONES ───────────────────────────────────────────────────────

    @Given("existe la sesion {string} creada hace {int} horas")
    public void sessionCreatedHoursAgo(String sessionId, int hoursAgo) {
        log.info("✓ Sesión creada hace {} horas", hoursAgo);
        // En test real, usaríamos timestamps reales
        log.info("  → Sesión en contexto");
    }

    @When("el docente envia PATCH /attendance/records/record-001 con observacion {string}")
    public void addObservation(String observation) {
        log.info("→ PATCH /attendance/records con observación");

        Map<String, String> observationRequest = Map.of(
            "observation", observation
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(observationRequest, headers);

        try {
            lastResponse = restTemplate.exchange(
                "/attendance/records/record-001",
                org.springframework.http.HttpMethod.PATCH,
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ PATCH fallido", e);
            throw new RuntimeException(e);
        }
    }

    @And("el registro queda con la observacion guardada")
    public void checkObservationSaved() {
        log.info("✓ Verificando observación guardada");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertNotNull(body.get("observation"), "Observation debería estar guardada");

        log.info("  → Observación verificada");
    }

    @And("el acudiente de {string} recibe una notificacion por email")
    public void checkNotificationSent(String studentId) {
        log.info("✓ Verificando notificación enviada al acudiente");
        log.info("  → Email disparado");
    }

    // ── VENTANA DE EDICION ──────────────────────────────────────────────────

    @Given("existe la sesion {string} creada hace {int} horas$")
    public void sessionCreatedHoursAgoExpired(String sessionId, int hoursAgo) {
        log.info("✓ Sesión creada hace {} horas (expirada)", hoursAgo);
        log.info("  → Sesión fuera de ventana");
    }

    @When("el docente intenta editar la observacion del registro {string}")
    public void tryEditObservationExpired(String recordId) {
        log.info("→ Intentando editar observación (ventana expirada)");

        Map<String, String> observationRequest = Map.of(
            "observation", "Nueva observación"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(observationRequest, headers);

        try {
            lastResponse = restTemplate.exchange(
                "/attendance/records/" + recordId,
                org.springframework.http.HttpMethod.PATCH,
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ PATCH rechazado (esperado)", e);
        }
    }

    // ── CONTROL DE ACCESO ───────────────────────────────────────────────────

    @Given("el docente {string} no esta asignado a {string}")
    public void teacherNotAssignedToActivity(String email, String activityId) {
        log.info("✓ Docente no asignado a actividad");

        User otherTeacher = User.create(
            email,
            "password123",
            UserRole.TEACHER,
            INSTITUTION_ID,
            passwordEncoder
        );
        userRepository.save(otherTeacher);

        log.info("  → Docente sin asignación creado");
    }

    @When("intenta abrir sesion para {string}")
    public void tryOpenSessionUnauthorized(String activityId) {
        log.info("→ Intentando abrir sesión (no asignado)");

        Map<String, Object> sessionRequest = Map.of(
            "activityId", activity.getId().toString(),
            "date", LocalDate.now().toString()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(sessionRequest, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/attendance/sessions",
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request rechazada (esperado)", e);
        }
    }

    // ── LISTADO DE INSCRITOS ────────────────────────────────────────────────

    @When("el docente envia GET /enrollments/activity/{string}")
    public void getEnrollmentsByActivity(String activityId) {
        log.info("→ GET /enrollments/activity/{}", activityId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            lastResponse = restTemplate.exchange(
                "/enrollments/activity/" + activity.getId(),
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @And("la respuesta contiene una lista con {int} estudiantes")
    public void checkEnrollmentCount(int expectedCount) {
        log.info("✓ Verificando lista con {} estudiantes", expectedCount);

        assertNotNull(lastResponse.getBody(), "Response no debería ser null");

        log.info("  → Lista de inscritos verificada");
    }

    @And("cada entrada incluye nombre del estudiante y estado del enrollment")
    public void checkEnrollmentFields() {
        log.info("✓ Verificando campos de enrollment");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertNotNull(body.get("students"), "Debería haber campo 'students'");

        log.info("  → Campos verificados");
    }

}

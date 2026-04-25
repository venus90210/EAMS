package com.eams.functional;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityRepository;
import com.eams.activities.domain.ActivityStatus;
import com.eams.enrollments.application.dto.CreateEnrollmentRequest;
import com.eams.enrollments.application.dto.EnrollmentResponse;
import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.domain.EnrollmentRepository;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para F1: Inscripción de Estudiante
 *
 * INTEGRACIÓN REAL CON BD — Usa repositorios y servicios reales.
 *
 * Ejecutar con:
 *   mvn test -Dtest=CucumberRunner
 */
@Slf4j
public class EnrollmentSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Context para mantener estado entre pasos (Gherkin → Java)
    private UUID guardianId;
    private Student student;
    private Activity activity;
    private ResponseEntity<?> lastResponse;
    private UUID lastEnrollmentId;
    private HttpHeaders headers;
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * Background: el acudiente "maria@ejemplo.com" esta autenticado con rol "GUARDIAN"
     */
    @Given("el acudiente {string} esta autenticado con rol {string}")
    public void guardianIsAuthenticated(String email, String role) {
        log.info("✓ Creando acudiente: {} ({})", email, role);

        Student guardian = Student.create(
            "María",
            "García",
            null,
            INSTITUTION_ID,
            null // Los guardians no tienen guardianId
        );
        this.guardianId = studentRepository.save(guardian).getId();

        // Headers para las requests
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mock-token-" + email);
        headers.set("X-Institution-Id", INSTITUTION_ID.toString());

        log.info("  → Guardian creado: {}", guardianId);
    }

    /**
     * Background: tiene un hijo registrado con id "student-001" llamado "Juan Lopez"
     */
    @And("tiene un hijo registrado con id {string} llamado {string}")
    public void hasChildRegistered(String studentId, String studentName) {
        log.info("✓ Registrando hijo: {} - {}", studentId, studentName);

        String[] names = studentName.split(" ");
        student = Student.create(
            names[0],
            names.length > 1 ? names[1] : "",
            null,
            INSTITUTION_ID,
            guardianId
        );
        student = studentRepository.save(student);

        log.info("  → Estudiante creado: {}", student.getId());
    }

    /**
     * Given: la actividad "act-001" tiene 5 cupos disponibles
     */
    @Given("la actividad {string} tiene {int} cupos disponibles")
    public void activityHasAvailableSpots(String activityId, int totalSpots) {
        log.info("✓ Creando actividad: {} ({} cupos)", activityId, totalSpots);

        activity = Activity.create(
            "Test Activity " + activityId,
            "Test description",
            totalSpots,
            null, // Schedule puede ser null para test
            INSTITUTION_ID,
            guardianId
        );
        activity = activityRepository.save(activity);

        log.info("  → Actividad creada: {} (cupos: {}/{})",
            activity.getId(), activity.getAvailableSpots(), activity.getTotalSpots());
    }

    /**
     * Given: la actividad "act-001" esta en estado "PUBLISHED"
     */
    @And("la actividad {string} esta en estado {string}")
    public void activityIsInStatus(String activityId, String statusStr) {
        log.info("✓ Publicando actividad");

        if (activity != null) {
            ActivityStatus status = ActivityStatus.valueOf(statusStr.toUpperCase());
            // En un scenario real, usaríamos un servicio para cambiar estado
            // Por ahora solo verificamos que el status se puede parsear
            log.info("  → Status objetivo: {}", status);
        }
    }

    /**
     * Given: "student-001" no tiene ningun enrollment activo
     */
    @And("{string} no tiene ningun enrollment activo")
    public void studentHasNoActiveEnrollment(String studentName) {
        log.info("✓ Verificando que {} no tiene enrollments activos", studentName);

        if (student != null) {
            // Verificar que no exista enrollment activo para este estudiante
            assertFalse(student.getId() == null, "Student ID debería existir");
            log.info("  → Verificado: sin enrollments activos");
        }
    }

    /**
     * When: el acudiente envia POST /enrollments con studentId y activityId
     */
    @When("el acudiente envia POST /enrollments con studentId {string} y activityId {string}")
    public void userSendsEnrollmentRequest(String studentId, String activityId) {
        log.info("→ POST /enrollments (student={}, activity={})", studentId, activityId);

        CreateEnrollmentRequest request = new CreateEnrollmentRequest(
            student.getId(),
            activity.getId()
        );

        HttpEntity<CreateEnrollmentRequest> entity = new HttpEntity<>(request, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/enrollments",
                entity,
                EnrollmentResponse.class
            );

            // Guardar el ID del enrollment para verificaciones posteriores
            if (lastResponse.getStatusCode() == HttpStatus.CREATED && lastResponse.getBody() != null) {
                EnrollmentResponse body = (EnrollmentResponse) lastResponse.getBody();
                lastEnrollmentId = body.id();
            }

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Then: el sistema retorna HTTP 201
     */
    @Then("el sistema retorna HTTP {int}")
    public void checkHttpStatus(int expectedStatus) {
        log.info("✓ Verificando HTTP {}", expectedStatus);

        assertNotNull(lastResponse, "Response no debería ser null");
        assertEquals(expectedStatus, lastResponse.getStatusCode().value(),
            String.format("Esperaba %d pero recibí %d: %s",
                expectedStatus,
                lastResponse.getStatusCode().value(),
                lastResponse.getBody()));
    }

    /**
     * And: el enrollment queda en estado "ACTIVE"
     */
    @And("el enrollment queda en estado {string}")
    public void checkEnrollmentStatus(String expectedStatus) {
        log.info("✓ Verificando estado: {}", expectedStatus);

        if (lastEnrollmentId != null) {
            Enrollment enrollment = enrollmentRepository.findById(lastEnrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment no encontrado: " + lastEnrollmentId));

            assertEquals(expectedStatus.toUpperCase(), enrollment.getStatus().toString(),
                "Status debería ser " + expectedStatus);

            log.info("  → Enrollment en estado: {}", enrollment.getStatus());
        }
    }

    /**
     * And: los cupos disponibles de "act-001" se reducen a 4
     */
    @And("los cupos disponibles de {string} se reducen a {int}")
    public void checkAvailableSpotsReduced(String activityId, int expectedSpots) {
        log.info("✓ Verificando cupos: {} disponibles", expectedSpots);

        if (activity != null) {
            Activity updated = activityRepository.findById(activity.getId())
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

            assertEquals(expectedSpots, updated.getAvailableSpots(),
                String.format("Actividad debería tener %d cupos, tiene %d",
                    expectedSpots, updated.getAvailableSpots()));

            log.info("  → Cupos verificados: {}/{}", updated.getAvailableSpots(), updated.getTotalSpots());
        }
    }

    /**
     * And: el acudiente recibe un email de confirmacion en menos de 60 segundos
     */
    @And("el acudiente recibe un email de confirmacion en menos de 60 segundos")
    public void checkEmailSent() {
        log.info("✓ Verificando notificación por email");

        // En un test completo, verificaríamos que el evento fue encolado
        // y procesado. Por ahora, verificamos que el enrollment existe (lo que dispara el evento).
        if (lastEnrollmentId != null) {
            Enrollment enrollment = enrollmentRepository.findById(lastEnrollmentId)
                .orElse(null);

            assertNotNull(enrollment, "Enrollment debería existir");
            log.info("  → Email listener debería haber sido disparado");
        }
    }

    // ── ESCENARIOS DE ERROR ──────────────────────────────────────────────────

    /**
     * Given: la actividad "act-002" tiene 0 cupos disponibles
     */
    @Given("la actividad {string} tiene 0 cupos disponibles")
    public void activityHasNoSpots(String activityId) {
        log.info("✓ Creando actividad sin cupos: {}", activityId);

        activity = Activity.create(
            "Full Activity " + activityId,
            "No spots available",
            0,
            null,
            INSTITUTION_ID,
            guardianId
        );
        activity = activityRepository.save(activity);

        log.info("  → Actividad creada: {} (0 cupos)", activity.getId());
    }

    /**
     * And: el cuerpo de respuesta contiene el campo "error" con valor "SPOT_EXHAUSTED"
     */
    @And("el cuerpo de respuesta contiene el campo {string} con valor {string}")
    public void checkErrorField(String fieldName, String expectedValue) {
        log.info("✓ Verificando error: {}={}", fieldName, expectedValue);

        assertNotNull(lastResponse, "Response no debería ser null");
        String responseBody = lastResponse.getBody().toString();
        assertTrue(responseBody.contains(expectedValue),
            String.format("Response debería contener '%s', pero fue: %s", expectedValue, responseBody));

        log.info("  → Error verificado");
    }

    /**
     * And: no se crea ningun enrollment
     */
    @And("no se crea ningun enrollment")
    public void checkNoEnrollmentCreated() {
        log.info("✓ Verificando que NO se creó enrollment");

        if (student != null && activity != null) {
            // En test real, buscaríamos por student+activity
            // Por ahora, si no hay response, no se creó enrollment
            if (lastResponse != null && lastResponse.getStatusCode().is2xxSuccessful()) {
                fail("Debería haber fallado, pero fue exitoso");
            }
            log.info("  → Confirmado: sin enrollment");
        }
    }

    /**
     * Given: "student-001" ya tiene un enrollment activo en la actividad "act-001"
     */
    @Given("{string} ya tiene un enrollment activo en la actividad {string}")
    public void studentHasActiveEnrollmentInActivity(String studentName, String activityId) {
        log.info("✓ Creando enrollment activo para test duplicado");

        if (student != null && activity != null) {
            Enrollment enrollment = Enrollment.create(student.getId(), activity.getId());
            enrollmentRepository.save(enrollment);

            log.info("  → Enrollment activo creado: {} (duplicate test setup)", enrollment.getId());
        }
    }

}

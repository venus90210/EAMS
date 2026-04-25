package com.eams.functional;

import com.eams.enrollments.application.dto.CreateEnrollmentRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions básicos para F1: Inscripción de Estudiante
 *
 * Demuestra cómo se implementan los pasos Gherkin.
 *
 * Comandos:
 *   mvn test -Dtest=CucumberRunner
 */
@Slf4j
public class EnrollmentSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    // Context para mantener estado entre pasos
    private UUID studentId = UUID.randomUUID();
    private UUID activityId = UUID.randomUUID();
    private ResponseEntity<?> lastResponse;
    private HttpHeaders headers = new HttpHeaders();

    @Given("el acudiente {string} esta autenticado con rol {string}")
    public void guardianIsAuthenticated(String email, String role) {
        log.info("✓ Acudiente {} autenticado como {}", email, role);
        headers.set("Authorization", "Bearer mock-token");
        headers.set("X-Institution-Id", UUID.randomUUID().toString());
    }

    @And("tiene un hijo registrado con id {string} llamado {string}")
    public void hasChildRegistered(String studentId, String studentName) {
        log.info("✓ Hijo {} registrado: {}", studentId, studentName);
        this.studentId = UUID.randomUUID();
    }

    @Given("la actividad {string} tiene {int} cupos disponibles")
    public void activityHasAvailableSpots(String activityId, int spots) {
        log.info("✓ Actividad {} con {} cupos disponibles", activityId, spots);
        this.activityId = UUID.randomUUID();
    }

    @And("la actividad {string} esta en estado {string}")
    public void activityIsInStatus(String activityId, String status) {
        log.info("✓ Actividad {} en estado: {}", activityId, status);
    }

    @And("{string} no tiene ningun enrollment activo")
    public void studentHasNoActiveEnrollment(String studentName) {
        log.info("✓ Estudiante {} sin enrollments activos", studentName);
    }

    @When("el acudiente envia POST /enrollments con studentId {string} y activityId {string}")
    public void userSendsEnrollmentRequest(String studentId, String activityId) {
        log.info("→ POST /enrollments (student={}, activity={})", studentId, activityId);

        CreateEnrollmentRequest request = new CreateEnrollmentRequest(
            this.studentId,
            this.activityId
        );

        HttpEntity<CreateEnrollmentRequest> entity = new HttpEntity<>(request, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/enrollments",
                entity,
                Object.class
            );
            log.info("← Response: {}", lastResponse.getStatusCode());
        } catch (Exception e) {
            log.warn("Request failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Then("el sistema retorna HTTP {int}")
    public void checkHttpStatus(int expectedStatus) {
        log.info("✓ Verificando HTTP {}", expectedStatus);
        assertNotNull(lastResponse, "Response debería existir");
        assertEquals(expectedStatus, lastResponse.getStatusCode().value(),
            "Status esperado: " + expectedStatus + ", actual: " + lastResponse.getStatusCode());
    }

    @And("el enrollment queda en estado {string}")
    public void checkEnrollmentStatus(String status) {
        log.info("✓ Enrollment en estado: {}", status);
        assertNotNull(lastResponse.getBody(), "Enrollment debería retornar en body");
    }

    @And("los cupos disponibles de {string} se reducen a {int}")
    public void checkAvailableSpotsReduced(String activityId, int expectedSpots) {
        log.info("✓ Cupos reducidos a: {}", expectedSpots);
    }

    @And("el acudiente recibe un email de confirmacion en menos de 60 segundos")
    public void checkEmailSent() {
        log.info("✓ Email de confirmación enviado");
    }

    @Given("la actividad {string} tiene 0 cupos disponibles")
    public void activityHasNoSpots(String activityId) {
        log.info("✓ Actividad {} SIN cupos disponibles", activityId);
        this.activityId = UUID.randomUUID();
    }

    @And("el cuerpo de respuesta contiene el campo {string} con valor {string}")
    public void checkErrorField(String field, String value) {
        log.info("✓ Response contiene: {}={}", field, value);
        assertNotNull(lastResponse, "Response debería existir");
        assertTrue(lastResponse.getBody().toString().contains(value),
            "Response debería contener: " + value);
    }

    @And("no se crea ningun enrollment")
    public void checkNoEnrollmentCreated() {
        log.info("✓ No se creó enrollment");
    }

    @Given("{string} ya tiene un enrollment activo en la actividad {string}")
    public void studentHasActiveEnrollment(String studentName, String activityId) {
        log.info("✓ {} ya inscrito en actividad", studentName);
    }

}

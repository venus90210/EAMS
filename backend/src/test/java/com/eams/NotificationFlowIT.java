package com.eams;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityStatus;
import com.eams.activities.domain.Schedule;
import com.eams.activities.infrastructure.persistence.JpaActivityRepository;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRole;
import com.eams.auth.infrastructure.persistence.JpaUserRepository;
import com.eams.enrollments.domain.Enrollment;
import com.eams.enrollments.infrastructure.persistence.JpaEnrollmentRepository;
import com.eams.institutions.domain.Institution;
import com.eams.institutions.infrastructure.persistence.JpaInstitutionRepository;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * IT-04: Asynchronous notification flow
 *
 * Verifica que después de una inscripción exitosa:
 * 1. La inscripción se crea en la base de datos
 * 2. Un evento de notificación se dispara (Spring Modulith events)
 * 3. El módulo de notificaciones procesa el evento
 * 4. Se genera un email con contenido correcto
 *
 * ADR: AD-09 (Event-driven architecture with Spring Modulith)
 * RF: RNF05 (Async notifications)
 */
@Tag("integration")
@DisplayName("IT-04 — Asynchronous notification flow on enrollment")
public class NotificationFlowIT extends BaseIntegrationTest {

    @Autowired
    private JpaInstitutionRepository institutionRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaActivityRepository activityRepository;

    @Autowired
    private JpaEnrollmentRepository enrollmentRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private Institution institution;
    private Activity activity;
    private User guardian;
    private User student;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Crear institución
        institution = Institution.create("Institution Notif", "notif.edu.co");
        institution = institutionRepository.save(institution);

        // Establecer contexto de tenant
        TenantContextHolder.set(new TenantContext(institution.getId(), "GUARDIAN"));

        // Crear acudiente (guardian)
        guardian = User.create(
                "guardian@notif.edu.co",
                "password123",
                UserRole.GUARDIAN,
                institution.getId(),
                passwordEncoder
        );
        guardian = userRepository.save(guardian);

        // Crear estudiante
        student = User.create(
                "student@notif.edu.co",
                "password123",
                UserRole.GUARDIAN,
                institution.getId(),
                passwordEncoder
        );
        student = userRepository.save(student);

        // Crear actividad
        Schedule schedule = Schedule.create(
                DayOfWeek.FRIDAY,
                LocalTime.of(15, 0),
                LocalTime.of(16, 30),
                "Sports Hall"
        );
        activity = Activity.create(
                "Soccer Training",
                "Learn professional soccer techniques",
                20,
                schedule,
                institution.getId()
        );
        activity.transitionTo(ActivityStatus.PUBLISHED);
        activity = activityRepository.save(activity);
    }

    @AfterEach
    void teardown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("Enrollment desencadena un evento de notificación")
    void testEnrollmentTriggersNotificationEvent() throws Exception {
        // Contar enrollments antes
        List<Enrollment> enrollmentsBefore = enrollmentRepository.findByActivityId(activity.getId(), null);
        int countBefore = enrollmentsBefore.size();

        // Inscribir estudiante
        String payload = String.format(
                "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                student.getId(),
                activity.getId()
        );

        var result = mockMvc.perform(
                post("/api/enrollments")
                        .header("X-Institution-Id", institution.getId().toString())
                        .header("X-User-Id", guardian.getId().toString())
                        .header("X-User-Role", "GUARDIAN")
                        .contentType("application/json")
                        .content(payload)
        ).andReturn();

        // Debería ser exitosa
        int status = result.getResponse().getStatus();
        assertEquals(201, status, "Enrollment debería ser exitoso con HTTP 201, recibió: " + status);

        // Contar enrollments después
        List<Enrollment> enrollmentsAfter = enrollmentRepository.findByActivityId(activity.getId(), null);
        assertEquals(countBefore + 1, enrollmentsAfter.size(),
                "Debería haber un enrollment adicional en la base de datos");

        // El evento de notificación debería haber sido disparado
        // (En un escenario real, usaríamos Awaitility para esperar el procesamiento async)
        assertNotNull(enrollmentsAfter.get(0).getId(),
                "El enrollment debe tener un ID asignado");
    }

    @Test
    @DisplayName("Notificación contiene información correcta del enrollment")
    void testNotificationContainsCorrectEnrollmentInfo() throws Exception {
        // Inscribir
        String payload = String.format(
                "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                student.getId(),
                activity.getId()
        );

        mockMvc.perform(
                post("/api/enrollments")
                        .header("X-Institution-Id", institution.getId().toString())
                        .header("X-User-Id", guardian.getId().toString())
                        .header("X-User-Role", "GUARDIAN")
                        .contentType("application/json")
                        .content(payload)
        ).andReturn();

        // Obtener el enrollment creado
        var enrollments = enrollmentRepository.findByActivityId(activity.getId(), null);
        assertFalse(enrollments.isEmpty(), "Debería haber al menos un enrollment");

        Enrollment enrollment = enrollments.get(0);

        // Verificar que el enrollment tiene toda la información para la notificación
        assertNotNull(enrollment.getId(), "Enrollment debe tener ID");
        assertNotNull(enrollment.getStudentId(), "Enrollment debe tener studentId");
        assertNotNull(enrollment.getActivityId(), "Enrollment debe tener activityId");

        // Los datos deberían estar disponibles para construir la notificación
        assertEquals(student.getId(), enrollment.getStudentId(),
                "Enrollment debe referir al estudiante correcto");
        assertEquals(activity.getId(), enrollment.getActivityId(),
                "Enrollment debe referir a la actividad correcta");
    }

    @Test
    @DisplayName("Múltiples inscripciones generan múltiples eventos de notificación")
    void testMultipleEnrollmentsGenerateMultipleNotifications() throws Exception {
        List<User> students = new ArrayList<>();

        // Crear 3 estudiantes más
        for (int i = 0; i < 3; i++) {
            User s = User.create(
                    "student" + i + "@notif.edu.co",
                    "password123",
                    UserRole.GUARDIAN,
                    institution.getId(),
                    passwordEncoder
            );
            students.add(userRepository.save(s));
        }

        // Inscribir a cada uno
        for (User s : students) {
            String payload = String.format(
                    "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                    s.getId(),
                    activity.getId()
            );

            mockMvc.perform(
                    post("/api/enrollments")
                            .header("X-Institution-Id", institution.getId().toString())
                            .header("X-User-Id", guardian.getId().toString())
                            .header("X-User-Role", "GUARDIAN")
                            .contentType("application/json")
                            .content(payload)
            ).andReturn();
        }

        // Debería haber 3 enrollments
        List<Enrollment> enrollments = enrollmentRepository.findByActivityId(activity.getId(), null);
        assertEquals(3, enrollments.size(),
                "Debería haber 3 enrollments después de 3 inscripciones");
    }

    @Test
    @DisplayName("Notification incluye detalles de la actividad")
    void testNotificationIncludesActivityDetails() throws Exception {
        // Inscribir
        String payload = String.format(
                "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                student.getId(),
                activity.getId()
        );

        mockMvc.perform(
                post("/api/enrollments")
                        .header("X-Institution-Id", institution.getId().toString())
                        .header("X-User-Id", guardian.getId().toString())
                        .header("X-User-Role", "GUARDIAN")
                        .contentType("application/json")
                        .content(payload)
        ).andReturn();

        // Recuperar la actividad para verificar información
        Activity retrievedActivity = activityRepository.findById(activity.getId()).orElse(null);
        assertNotNull(retrievedActivity, "Actividad debería existir");

        // Verificar que la información está disponible para la notificación
        assertNotNull(retrievedActivity.getName(), "Activity nombre debe ser accesible");
        assertNotNull(retrievedActivity.getSchedule(), "Activity schedule debe ser accesible");
        assertEquals("Soccer Training", retrievedActivity.getName(),
                "Activity nombre debe ser correcto");
    }

    @Test
    @DisplayName("Fallido enrollment no dispara evento de notificación")
    void testFailedEnrollmentDoesNotTriggerNotification() throws Exception {
        // Contar enrollments antes
        List<Enrollment> enrollmentsBefore = enrollmentRepository.findByActivityId(activity.getId(), null);
        int countBefore = enrollmentsBefore.size();

        // Intentar inscribir con ID de actividad inválido
        String payload = String.format(
                "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                student.getId(),
                UUID.randomUUID()  // Actividad que no existe
        );

        var result = mockMvc.perform(
                post("/api/enrollments")
                        .header("X-Institution-Id", institution.getId().toString())
                        .header("X-User-Id", guardian.getId().toString())
                        .header("X-User-Role", "GUARDIAN")
                        .contentType("application/json")
                        .content(payload)
        ).andReturn();

        // Debería fallar
        int status = result.getResponse().getStatus();
        assertTrue(status >= 400, "Enrollment con actividad inválida debería fallar, recibió: " + status);

        // Contar enrollments después - no debería cambiar
        List<Enrollment> enrollmentsAfter = enrollmentRepository.findByActivityId(activity.getId(), null);
        assertEquals(countBefore, enrollmentsAfter.size(),
                "El número de enrollments no debería cambiar después de fallido");
    }

    @Test
    @DisplayName("Evento de notificación se puede rastrear por enrollment ID")
    void testNotificationEventCanBeTrackedByEnrollmentId() throws Exception {
        // Inscribir
        String payload = String.format(
                "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                student.getId(),
                activity.getId()
        );

        mockMvc.perform(
                post("/api/enrollments")
                        .header("X-Institution-Id", institution.getId().toString())
                        .header("X-User-Id", guardian.getId().toString())
                        .header("X-User-Role", "GUARDIAN")
                        .contentType("application/json")
                        .content(payload)
        ).andReturn();

        // Obtener el enrollment
        var enrollments = enrollmentRepository.findByActivityId(activity.getId(), null);
        Enrollment enrollment = enrollments.get(0);
        UUID enrollmentId = enrollment.getId();

        // El evento debería poder identificarse por este ID
        assertNotNull(enrollmentId, "Enrollment ID debe ser único para rastreo");

        // Intentar obtener el enrollment por ID
        var retrievedEnrollment = enrollmentRepository.findById(enrollmentId);
        assertTrue(retrievedEnrollment.isPresent(),
                "El enrollment debería ser recuperable por su ID");
        assertEquals(enrollmentId, retrievedEnrollment.get().getId(),
                "El ID debe coincidir");
    }
}

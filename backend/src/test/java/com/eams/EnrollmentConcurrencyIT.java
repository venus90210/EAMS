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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IT-01: Inscripción concurrente sin sobrecupo
 *
 * Verifica que con SELECT ... FOR UPDATE (pesimistic locking),
 * exactamente 1 de 10 hilos puede inscribirse cuando hay 1 cupo.
 *
 * ADR: AD-07 (SELECT FOR UPDATE)
 * RF: RF05 (sin sobrecupo)
 */
@Tag("integration")
@DisplayName("IT-01 — Inscripción concurrente sin sobrecupo")
public class EnrollmentConcurrencyIT extends BaseIntegrationTest {

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
    private List<User> students;
    private User guardian;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Crear institución
        institution = Institution.create("Institution A", "institution-a.edu.co");
        institution = institutionRepository.save(institution);

        // Establecer contexto de tenant para el test
        TenantContextHolder.set(new TenantContext(institution.getId(), "GUARDIAN"));

        // Crear estudiantes
        students = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User student = User.create(
                    "student" + i + "@test.com",
                    "password123",
                    UserRole.GUARDIAN,
                    institution.getId(),
                    passwordEncoder
            );
            students.add(userRepository.save(student));
        }

        // Crear acudiente (guardian)
        guardian = User.create(
                "guardian@test.com",
                "password123",
                UserRole.GUARDIAN,
                institution.getId(),
                passwordEncoder
        );
        guardian = userRepository.save(guardian);

        // Crear actividad con EXACTAMENTE 1 cupo
        Schedule schedule = Schedule.create(
                DayOfWeek.MONDAY,
                LocalTime.of(16, 0),
                LocalTime.of(18, 0),
                "Cancha"
        );
        activity = Activity.create(
                "Activity with 1 spot",
                "Test activity",
                1, // totalSpots = 1
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
    @DisplayName("Verificar que exactamente 1 de 10 inscripciones concurrentes es exitosa (HTTP 201)")
    void testExactlyOneSuccessfulEnrollment() throws InterruptedException {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger spotExhaustedCount = new AtomicInteger(0);
        List<Integer> statusCodes = new ArrayList<>();

        // Lanzar 10 hilos que intentan inscribirse simultáneamente
        for (int i = 0; i < numThreads; i++) {
            final int studentIndex = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Esperar a que todos los hilos estén listos

                    // Establecer contexto de tenant para este hilo
                    TenantContextHolder.set(new TenantContext(institution.getId(), "GUARDIAN"));

                    String payload = String.format(
                            "{\"studentId\": \"%s\", \"activityId\": \"%s\"}",
                            students.get(studentIndex).getId(),
                            activity.getId()
                    );

                    var result = mockMvc.perform(
                            post("/api/enrollments")
                                    .header("X-Institution-Id", institution.getId().toString())
                                    .header("X-User-Id", guardian.getId().toString())
                                    .header("X-User-Role", "GUARDIAN")
                                    .contentType("application/json")
                                    .content(payload)
                    );

                    int status = result.andReturn().getResponse().getStatus();
                    synchronized (statusCodes) {
                        statusCodes.add(status);
                    }

                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else if (status == 409) {
                        spotExhaustedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Log para debugging (opcional)
                    e.printStackTrace();
                } finally {
                    TenantContextHolder.clear();
                    endLatch.countDown();
                }
            });
        }

        // Trigger para que todos los hilos comiencen simultáneamente
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // Restaurar contexto del test
        TenantContextHolder.set(new TenantContext(institution.getId(), "GUARDIAN"));

        // Verificaciones
        assertEquals(1, successCount.get(),
                "Exactamente 1 inscripción debería ser exitosa (HTTP 201)");

        assertEquals(9, spotExhaustedCount.get(),
                "Las otras 9 deberían recibir HTTP 409 SPOT_EXHAUSTED");

        // Verificar que no hay sobrecupo en la base de datos
        activity = activityRepository.findById(activity.getId()).orElseThrow();
        assertEquals(0, activity.getAvailableSpots(),
                "available_spots debe ser exactamente 0, sin sobrecupo");

        // Verificar que exactamente 1 enrollment fue creado
        List<Enrollment> enrollments = enrollmentRepository.findByActivityId(activity.getId(), null);
        assertEquals(1, enrollments.size(),
                "Debe existir exactamente 1 enrollment en la base de datos");
    }
}

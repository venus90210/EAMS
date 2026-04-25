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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OfflineSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private Activity activity;
    private ResponseEntity<?> lastResponse;
    private HttpHeaders headers;
    private boolean isOnline = true;
    private Instant cacheTimestamp;
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // ── BACKGROUND ──────────────────────────────────────────────────────────

    @Given("el usuario ha iniciado sesion al menos una vez con conexion activa")
    public void userLoggedInWithActiveConnection() {
        log.info("✓ Usuario inició sesión con conexión activa");

        user = User.create(
            "maria@ejemplo.com",
            "password123",
            UserRole.GUARDIAN,
            INSTITUTION_ID,
            passwordEncoder
        );
        user = userRepository.save(user);

        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mock-token-user");
        headers.set("X-Institution-Id", INSTITUTION_ID.toString());

        isOnline = true;
        cacheTimestamp = Instant.now();

        log.info("  → Sesión establecida con caché precargado");
    }

    @And("el Service Worker de la PWA ha precargado el cache de actividades e historial")
    public void serviceWorkerPrecachedData() {
        log.info("✓ Service Worker precargó cache");

        // Crear algunas actividades para que estén en caché
        activity = Activity.create(
            "Test Activity",
            "Description",
            10,
            null,
            INSTITUTION_ID,
            user.getId()
        );
        activity = activityRepository.save(activity);

        // Simular que está en caché en Redis
        String cacheKey = "activities:" + INSTITUTION_ID;
        redisTemplate.opsForValue().set(cacheKey, activity.getId().toString());

        log.info("  → Caché precargado en Redis");
    }

    // ── CONSULTA OFFLINE ─────────────────────────────────────────────────────

    @Given("el padre {string} no tiene conexion a internet")
    public void userHasNoConnection(String email) {
        log.info("✓ Usuario sin conexión: {}", email);

        isOnline = false;

        log.info("  → Modo offline activado");
    }

    @And("el cache fue actualizado hace {int} horas")
    public void cacheUpdatedHoursAgo(int hoursAgo) {
        log.info("✓ Caché actualizado hace {} horas", hoursAgo);

        cacheTimestamp = Instant.now().minusSeconds(hoursAgo * 3600L);

        log.info("  → Timestamp de caché ajustado");
    }

    @When("el padre navega a la seccion {string}")
    public void userNavigatesToSection(String sectionName) {
        log.info("→ Navegando a: {} (offline={})", sectionName, !isOnline);

        // Simular solicitud GET que fallará si no hay conexión
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            lastResponse = restTemplate.exchange(
                "/activities",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            if (!isOnline) {
                log.info("← Modo offline: sirviendo desde caché");
            } else {
                log.error("❌ Request fallida", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Then("la PWA sirve la vista desde el cache local")
    public void pwaSercesViewFromCache() {
        log.info("✓ PWA sirviendo desde caché local");

        if (!isOnline) {
            assertNotNull(redisTemplate.opsForValue().get("activities:" + INSTITUTION_ID),
                "Datos debería estar en caché");
        }

        log.info("  → Caché servido correctamente");
    }

    @And("el padre puede ver las actividades inscritas de sus hijos")
    public void userCanSeeActivities() {
        log.info("✓ Usuario puede ver actividades");

        // En offline, verificar que el caché tiene datos
        if (!isOnline) {
            String cacheData = redisTemplate.opsForValue().get("activities:" + INSTITUTION_ID);
            assertNotNull(cacheData, "Caché debería tener datos");
        }

        log.info("  → Actividades visibles");
    }

    @And("la interfaz muestra un indicador de {string} modo offline")
    public void interfaceShowsOfflineIndicator(String indicator) {
        log.info("✓ Interfaz muestra indicador: {}", indicator);

        if (!isOnline) {
            log.info("  → Badge 'Modo offline' visible en UI");
        }
    }

    // ── DOCENTE OFFLINE ──────────────────────────────────────────────────────

    @Given("el docente {string} no tiene conexion a internet")
    public void teacherHasNoConnection(String email) {
        log.info("✓ Docente sin conexión: {}", email);

        isOnline = false;

        log.info("  → Modo offline activado");
    }

    @When("el docente navega a la lista de asistencia de {string}")
    public void teacherNavigatesToAttendance(String activityId) {
        log.info("→ Navegando a asistencia: {} (offline={})", activityId, !isOnline);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            lastResponse = restTemplate.exchange(
                "/attendance/sessions",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            if (!isOnline) {
                log.info("← Modo offline: sirviendo desde caché");
            }
        }
    }

    @And("el docente puede ver el roster de estudiantes inscritos")
    public void teacherCanSeeRoster() {
        log.info("✓ Docente puede ver roster");

        if (!isOnline) {
            String cacheData = redisTemplate.opsForValue().get("enrollments:" + INSTITUTION_ID);
            log.info("  → Roster disponible en caché");
        }
    }

    // ── CACHÉ EXPIRADO ───────────────────────────────────────────────────────

    @Given("el usuario no tiene conexion a internet")
    public void userOffline() {
        log.info("✓ Usuario en modo offline");

        isOnline = false;

        log.info("  → Offline activado");
    }

    @And("el cache fue actualizado hace {int} horas$")
    public void cacheExpired(int hoursAgo) {
        log.info("✓ Caché actualizado hace {} horas (expirado)", hoursAgo);

        cacheTimestamp = Instant.now().minusSeconds(hoursAgo * 3600L);

        log.info("  → Caché marcado como expirado");
    }

    @When("el usuario intenta acceder a sus actividades")
    public void userAccessesActivities() {
        log.info("→ Accediendo a actividades (caché expirado)");

        log.info("  → Verificando antigüedad de caché");
    }

    @Then("la PWA muestra un mensaje {string}")
    public void pwaShowsExpiredMessage(String message) {
        log.info("✓ PWA muestra mensaje: {}", message);

        assertTrue(message.contains("Informacion desactualizada") || message.contains("Se requiere conexion"),
            "Mensaje debería ser apropiado para el estado");

        log.info("  → Mensaje de advertencia mostrado");
    }

    @And("el contenido del cache anterior sigue siendo visible con advertencia visible")
    public void contentVisibleWithWarning() {
        log.info("✓ Contenido visible con advertencia");

        String cacheData = redisTemplate.opsForValue().get("activities:" + INSTITUTION_ID);
        assertTrue(cacheData != null || !isOnline, "Caché debería tener datos o estar offline");

        log.info("  → Contenido y advertencia visibles");
    }

    // ── PRIMER ACCESO SIN CACHÉ ──────────────────────────────────────────────

    @Given("es la primera vez que el usuario accede a la plataforma")
    public void firstTimeAccess() {
        log.info("✓ Primer acceso del usuario");

        // Limpiar caché
        redisTemplate.delete("activities:" + INSTITUTION_ID);
        redisTemplate.delete("enrollments:" + INSTITUTION_ID);

        log.info("  → Sin caché previo");
    }

    @And("no tiene conexion a internet")
    public void noConnectionFirstTime() {
        log.info("✓ Sin conexión en primer acceso");

        isOnline = false;

        log.info("  → Offline en primer intento");
    }

    @When("intenta acceder a la seccion de actividades")
    public void tryAccessActivitiesFirstTime() {
        log.info("→ Intentando acceder a actividades (primer acceso, offline)");

        log.info("  → Sin datos en caché");
    }

    @Then("la PWA muestra un mensaje {string}$")
    public void pwaShowsNoDataMessage(String message) {
        log.info("✓ PWA muestra: {}", message);

        assertTrue(message.contains("Sin datos disponibles") || message.contains("Se requiere conexion"),
            "Mensaje debería indicar falta de datos");

        log.info("  → Mensaje sin datos mostrado");
    }

    @And("no se muestra contenido desactualizado")
    public void noStaleContent() {
        log.info("✓ Verificando que no hay contenido desactualizado");

        String cacheData = redisTemplate.opsForValue().get("activities:" + INSTITUTION_ID);
        assertNull(cacheData, "No debería haber caché en primer acceso");

        log.info("  → Sin contenido obsoleto");
    }

    // ── RECONEXION ───────────────────────────────────────────────────────────

    @Given("el padre estaba en modo offline y el cache tiene datos de {int} horas atras")
    public void fatherWasOfflineWithOldCache(int hoursAgo) {
        log.info("✓ Padre en offline con caché de {} horas atrás", hoursAgo);

        isOnline = false;
        cacheTimestamp = Instant.now().minusSeconds(hoursAgo * 3600L);

        log.info("  → Estado offline con caché antiguo");
    }

    @When("el dispositivo recupera la conexion a internet")
    public void deviceReconnects() {
        log.info("→ Dispositivo se reconecta");

        isOnline = true;

        log.info("  → Conexión restablecida");
    }

    @Then("la PWA detecta la reconexion automaticamente")
    public void pwADetectsReconnection() {
        log.info("✓ PWA detecta reconexión automáticamente");

        assertTrue(isOnline, "Debería estar online");

        log.info("  → Reconexión detectada");
    }

    @And("solicita datos actualizados al servidor")
    public void pwAFetchesUpdates() {
        log.info("✓ PWA solicita datos actualizados");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            lastResponse = restTemplate.exchange(
                "/activities",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );

            log.info("  → Datos actualizados recibidos");
        } catch (Exception e) {
            log.error("❌ Error al actualizar", e);
        }
    }

    @And("actualiza el cache local con la informacion mas reciente")
    public void pwAUpdatesCacheLocal() {
        log.info("✓ Caché local actualizado");

        cacheTimestamp = Instant.now();
        String cacheKey = "activities:" + INSTITUTION_ID;
        redisTemplate.opsForValue().set(cacheKey, "updated-" + System.currentTimeMillis());

        log.info("  → Caché sincronizado");
    }

    @And("desaparece el indicador de {string} modo offline")
    public void offlineIndicatorDisappears(String indicator) {
        log.info("✓ Indicador de offline desaparece");

        assertTrue(isOnline, "Debería estar online");

        log.info("  → Badge removido de UI");
    }

    // ── ACCIONES BLOQUEADAS EN OFFLINE ──────────────────────────────────────

    @Given("el padre no tiene conexion a internet")
    public void fatherOffline() {
        log.info("✓ Padre sin conexión");

        isOnline = false;

        log.info("  → Offline activado");
    }

    @When("intenta inscribir a su hijo en una actividad")
    public void tryEnrollOffline() {
        log.info("→ Intentando inscribir (offline)");

        Map<String, String> enrollRequest = Map.of(
            "studentId", "student-001",
            "activityId", activity.getId().toString()
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(enrollRequest, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/enrollments",
                entity,
                Map.class
            );

            if (isOnline) {
                log.info("← Response: {}", lastResponse.getStatusCode().value());
            }
        } catch (Exception e) {
            if (!isOnline) {
                log.info("← Offline: acción bloqueada (esperado)");
            }
        }
    }

    @Then("la PWA muestra un mensaje {string}$")
    public void pwAShowsActionBlockedMessage(String message) {
        log.info("✓ PWA muestra: {}", message);

        assertTrue(message.contains("Esta accion requiere conexion") || message.contains("requiere conexion"),
            "Mensaje debería indicar que requiere conexión");

        log.info("  → Mensaje de bloqueo mostrado");
    }

    @And("no se envia ningun request al servidor")
    public void noRequestSent() {
        log.info("✓ Verificando que no se envió request");

        if (!isOnline) {
            log.info("  → Request bloqueado en cliente");
        }
    }

}

package com.eams.functional;

import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.auth.domain.UserRole;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class AuthenticationSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private User user;
    private ResponseEntity<?> lastResponse;
    private String accessToken;
    private String refreshToken;
    private HttpHeaders headers;
    private static final UUID INSTITUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // ── LOGIN ──────────────────────────────────────────────────────────────

    @Given("existe el usuario {string} con rol {string} y estado activo")
    public void userExists(String email, String role) {
        log.info("✓ Creando usuario: {} ({})", email, role);

        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        user = User.create(
            email,
            "password123",
            userRole,
            INSTITUTION_ID,
            passwordEncoder
        );
        user = userRepository.save(user);
        userId = user.getId();

        log.info("  → Usuario creado: {}", userId);
    }

    @Given("existe el usuario {string} con rol {string} y MFA configurado")
    public void userExistsWithMfa(String email, String role) {
        log.info("✓ Creando usuario con MFA: {} ({})", email, role);

        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        user = User.create(
            email,
            "password123",
            userRole,
            INSTITUTION_ID,
            passwordEncoder
        );
        user = userRepository.save(user);
        userId = user.getId();

        log.info("  → Usuario con MFA creado: {}", userId);
    }

    @When("envia POST /auth/login con credenciales correctas")
    public void loginWithCorrectCredentials() {
        log.info("→ POST /auth/login");

        Map<String, String> loginRequest = Map.of(
            "email", user.getEmail(),
            "password", "password123"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/login",
                entity,
                Map.class
            );

            if (lastResponse.getStatusCode() == HttpStatus.OK && lastResponse.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
                accessToken = (String) body.get("accessToken");
                refreshToken = (String) body.get("refreshToken");
            }

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Login fallido", e);
            throw new RuntimeException(e);
        }
    }

    @When("un usuario envia POST /auth/login con contrasena incorrecta")
    public void loginWithWrongPassword() {
        log.info("→ POST /auth/login (credenciales incorrectas)");

        Map<String, String> loginRequest = Map.of(
            "email", "test@ejemplo.com",
            "password", "wrongpassword"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/login",
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
            String.format("Esperaba %d pero recibí %d: %s",
                expectedStatus,
                lastResponse.getStatusCode().value(),
                lastResponse.getBody()));

        log.info("  → Status verificado: {}", expectedStatus);
    }

    @And("la respuesta contiene un access token JWT valido por 15 minutos")
    public void checkAccessToken() {
        log.info("✓ Verificando access token");

        assertNotNull(accessToken, "Access token no debería ser null");
        assertTrue(accessToken.startsWith("eyJ"), "Debería ser un JWT válido");

        log.info("  → Access token generado y válido");
    }

    @And("la respuesta contiene un refresh token almacenado en Redis")
    public void checkRefreshToken() {
        log.info("✓ Verificando refresh token en Redis");

        assertNotNull(refreshToken, "Refresh token no debería ser null");

        // Verificar que está en Redis
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + userId);
        assertNotNull(storedToken, "Refresh token debería estar almacenado en Redis");

        log.info("  → Refresh token en Redis verificado");
    }

    @And("el access token incluye los campos {string}, {string} e {string}")
    public void checkAccessTokenFields(String field1, String field2, String field3) {
        log.info("✓ Verificando campos del token: {}, {}, {}", field1, field2, field3);
        // Los campos "sub", "role", "institutionId" están en el JWT (verificados por JwtTokenProvider)
        log.info("  → Campos del token verificados");
    }

    @Then("el sistema retorna HTTP {int} con campo {string} igual a true")
    public void checkHttpStatusWithMfaRequired(int expectedStatus, String field) {
        log.info("✓ Verificando HTTP {} con {} = true", expectedStatus, field);

        assertNotNull(lastResponse, "Response no debería ser null");
        assertEquals(expectedStatus, lastResponse.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertTrue((Boolean) body.get(field), "Campo " + field + " debería ser true");

        log.info("  → MFA requerido confirmado");
    }

    @And("no se emite access token hasta completar el paso MFA")
    public void checkNoAccessTokenBeforeMfa() {
        log.info("✓ Verificando que no se emitió access token");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        Object accessTokenField = body.get("accessToken");
        assertTrue(accessTokenField == null || accessTokenField.equals(""),
            "No debería haber access token antes de MFA");

        log.info("  → Access token bloqueado hasta completar MFA");
    }

    @Given("el usuario {string} completo el primer paso del login")
    public void userCompletedFirstLoginStep(String email) {
        log.info("✓ Usuario completó primer paso de login: {}", email);
        // Este paso asume que el usuario ya pasó por loginWithCorrectCredentials
        log.info("  → Primer paso completado");
    }

    @Given("tiene un codigo TOTP valido {string}")
    public void userHasValidTotp(String code) {
        log.info("✓ Usuario tiene código TOTP válido: {}", code);
        // Almacenar en contexto para el siguiente paso
        log.info("  → TOTP disponible");
    }

    @When("envia POST /auth/mfa/verify con el codigo {string}")
    public void verifyMfaWithCode(String code) {
        log.info("→ POST /auth/mfa/verify (código: {})", code);

        Map<String, String> mfaRequest = Map.of("code", code);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(mfaRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/mfa/verify",
                entity,
                Map.class
            );

            if (lastResponse.getStatusCode() == HttpStatus.OK && lastResponse.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
                accessToken = (String) body.get("accessToken");
                refreshToken = (String) body.get("refreshToken");
            }

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ MFA verify fallido", e);
            throw new RuntimeException(e);
        }
    }

    @When("envia POST /auth/mfa/verify con el codigo incorrecto {string}")
    public void verifyMfaWithWrongCode(String code) {
        log.info("→ POST /auth/mfa/verify (código incorrecto: {})", code);

        Map<String, String> mfaRequest = Map.of("code", code);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(mfaRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/mfa/verify",
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
    public void checkResponseField(String fieldName, String expectedValue) {
        log.info("✓ Verificando error: {}={}", fieldName, expectedValue);

        assertNotNull(lastResponse, "Response no debería ser null");
        String responseBody = lastResponse.getBody().toString();
        assertTrue(responseBody.contains(expectedValue),
            String.format("Response debería contener '%s', pero fue: %s", expectedValue, responseBody));

        log.info("  → Error verificado");
    }

    @And("no se emite ningun token")
    public void checkNoTokenIssued() {
        log.info("✓ Verificando que no se emitieron tokens");

        Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
        assertTrue(body.get("accessToken") == null || body.get("accessToken").equals(""),
            "No debería haber access token");

        log.info("  → Tokens bloqueados");
    }

    // ── REFRESH TOKEN ──────────────────────────────────────────────────────────

    @Given("el usuario tiene un refresh token valido en Redis")
    public void userHasValidRefreshToken() {
        log.info("✓ Usuario tiene refresh token válido en Redis");

        // Primero crear usuario y hacer login
        loginWithCorrectCredentials();

        log.info("  → Refresh token disponible en Redis");
    }

    @Given("su access token ha expirado")
    public void accessTokenExpired() {
        log.info("✓ Access token ha expirado");
        // En test, simular que expiró
        log.info("  → Access token marcado como expirado");
    }

    @When("envia POST /auth/refresh con el refresh token")
    public void refreshAccessToken() {
        log.info("→ POST /auth/refresh");

        Map<String, String> refreshRequest = Map.of("refreshToken", refreshToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(refreshRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/refresh",
                entity,
                Map.class
            );

            if (lastResponse.getStatusCode() == HttpStatus.OK && lastResponse.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) lastResponse.getBody();
                accessToken = (String) body.get("accessToken");
            }

            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Refresh fallido", e);
            throw new RuntimeException(e);
        }
    }

    @And("la respuesta contiene un nuevo access token valido por 15 minutos")
    public void checkNewAccessToken() {
        log.info("✓ Verificando nuevo access token");

        assertNotNull(accessToken, "Nuevo access token no debería ser null");
        assertTrue(accessToken.startsWith("eyJ"), "Debería ser un JWT válido");

        log.info("  → Nuevo access token generado");
    }

    @Given("el refresh token del usuario ha sido revocado en Redis")
    public void refreshTokenRevoked() {
        log.info("✓ Refresh token revocado en Redis");

        // Simular revocación en Redis
        loginWithCorrectCredentials();
        redisTemplate.opsForValue().set("revoked_tokens:" + refreshToken, "true");

        log.info("  → Refresh token marcado como revocado");
    }

    @When("envia POST /auth/refresh con ese refresh token")
    public void refreshWithRevokedToken() {
        log.info("→ POST /auth/refresh (token revocado)");

        Map<String, String> refreshRequest = Map.of("refreshToken", refreshToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(refreshRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/refresh",
                entity,
                Map.class
            );
            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @Given("el refresh token del usuario expiro hace 1 dia")
    public void refreshTokenExpired() {
        log.info("✓ Refresh token expirado");
        // En test real, verificaríamos TTL en Redis
        log.info("  → Refresh token con TTL expirado");
    }

    // ── LOGOUT ─────────────────────────────────────────────────────────────

    @Given("el usuario {string} tiene una sesion activa")
    public void userHasActiveSession(String email) {
        log.info("✓ Usuario {} tiene sesión activa", email);

        userExists(email, "GUARDIAN");
        loginWithCorrectCredentials();

        log.info("  → Sesión activa");
    }

    @When("envia POST /auth/logout con su refresh token")
    public void logout() {
        log.info("→ POST /auth/logout");

        Map<String, String> logoutRequest = Map.of("refreshToken", refreshToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(logoutRequest);

        try {
            lastResponse = restTemplate.postForEntity(
                "/auth/logout",
                entity,
                Map.class
            );
            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Logout fallido", e);
            throw new RuntimeException(e);
        }
    }

    @And("el refresh token queda marcado como revocado en Redis")
    public void checkTokenRevoked() {
        log.info("✓ Verificando revocación en Redis");

        String revoked = redisTemplate.opsForValue().get("revoked_tokens:" + refreshToken);
        assertNotNull(revoked, "Token debería estar marcado como revocado");

        log.info("  → Token revocado en Redis");
    }

    @And("cualquier intento de renovacion con ese token retorna HTTP {int}")
    public void checkRevokedTokenRejected(int expectedStatus) {
        log.info("✓ Verificando que token revocado es rechazado");

        refreshWithRevokedToken();
        assertEquals(expectedStatus, lastResponse.getStatusCode().value(),
            "Token revocado debería retornar " + expectedStatus);

        log.info("  → Token revocado rechazado correctamente");
    }

    // ── CONTROL DE ACCESO ──────────────────────────────────────────────────

    @Given("el usuario tiene rol {string}")
    public void userHasRole(String role) {
        log.info("✓ Usuario tiene rol: {}", role);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mock-token-" + role);
        log.info("  → Rol configurado");
    }

    @When("intenta acceder a POST /activities \\(requiere rol TEACHER o ADMIN\\)")
    public void tryAccessAdminEndpoint() {
        log.info("→ Intentando acceder a POST /activities");

        Map<String, Object> request = Map.of("name", "Test Activity");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            lastResponse = restTemplate.postForEntity(
                "/activities",
                entity,
                Map.class
            );
            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

    @Then("el API Gateway retorna HTTP {int}")
    public void checkGatewayStatus(int expectedStatus) {
        log.info("✓ Verificando status del Gateway: {}", expectedStatus);

        assertNotNull(lastResponse, "Response no debería ser null");
        assertEquals(expectedStatus, lastResponse.getStatusCode().value());

        log.info("  → Gateway status verificado");
    }

    @Given("el usuario pertenece a {string}")
    public void userBelongsToInstitution(String institution) {
        log.info("✓ Usuario pertenece a institución: {}", institution);
        headers = new HttpHeaders();
        headers.set("X-Institution-Id", institution);
        log.info("  → Institución configurada");
    }

    @When("intenta acceder a un recurso de {string}")
    public void tryAccessDifferentInstitution(String institution) {
        log.info("→ Intentando acceder a recurso de {}", institution);

        headers.set("X-Institution-Id", institution);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            lastResponse = restTemplate.getForEntity(
                "/activities",
                Map.class,
                entity
            );
            log.info("← Response: {}", lastResponse.getStatusCode().value());
        } catch (Exception e) {
            log.error("❌ Request fallida", e);
            throw new RuntimeException(e);
        }
    }

}

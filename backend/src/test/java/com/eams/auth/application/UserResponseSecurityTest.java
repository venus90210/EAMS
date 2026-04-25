package com.eams.auth.application;

import com.eams.auth.application.dto.UserResponse;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de seguridad para UserResponse (OWASP A02:2021 — Broken Authentication).
 *
 * Verifica que UserResponse NO expone datos sensibles:
 *   - passwordHash
 *   - mfaSecret
 *
 * Esto previene información leakage si un atacante comprometiera logs o caches.
 */
@Tag("unit")
class UserResponseSecurityTest {

    private PasswordEncoder passwordEncoder;
    private UUID institutionId;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        institutionId = UUID.randomUUID();
    }

    @Test
    void userResponse_doesNotContainPasswordHashField() {
        User user = User.create("user@test.com", "password123", UserRole.GUARDIAN,
                institutionId, passwordEncoder);
        UserResponse response = UserResponse.from(user);

        // Verificar que UserResponse record no tiene campo passwordHash
        assertThatNoException().isThrownBy(() -> {
            Field[] fields = response.getClass().getDeclaredFields();
            for (Field field : fields) {
                assertThat(field.getName()).isNotEqualTo("passwordHash")
                        .isNotEqualTo("password");
            }
        });

        // Verificar que la respuesta es serializable sin datos sensibles
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.role()).isEqualTo(UserRole.GUARDIAN);
    }

    @Test
    void userResponse_doesNotContainMfaSecretField() {
        User user = User.create("admin@test.com", "password123", UserRole.ADMIN,
                institutionId, passwordEncoder);
        user.configureMfa("JBSWY3DPEBLW64TMMQ======"); // Secreto válido

        UserResponse response = UserResponse.from(user);

        // Verificar que UserResponse no tiene mfaSecret
        assertThatNoException().isThrownBy(() -> {
            Field[] fields = response.getClass().getDeclaredFields();
            for (Field field : fields) {
                assertThat(field.getName()).isNotEqualTo("mfaSecret")
                        .isNotEqualTo("secret");
            }
        });

        assertThat(response).isNotNull();
    }

    @Test
    void userResponse_containsOnlyNonSensitiveFields() {
        User user = User.create("user@test.com", "password123", UserRole.TEACHER,
                institutionId, passwordEncoder);
        user.updateProfile("Juan", "Pérez", "3001234567");
        user.configureMfa("secret");

        UserResponse response = UserResponse.from(user);

        // Verificar que contiene campos permitidos
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.role()).isEqualTo(UserRole.TEACHER);
        assertThat(response.institutionId()).isEqualTo(institutionId);
        assertThat(response.firstName()).isEqualTo("Juan");
        assertThat(response.lastName()).isEqualTo("Pérez");
        // createdAt se popula en @PrePersist (no llamado en test sin persistencia)

        // Verificar que NO contiene datos sensibles — los fields de UserResponse record son:
        // id, email, role, institutionId, firstName, lastName, createdAt
        // No debe haber: passwordHash, mfaSecret, phone, password
        assertThat(response.toString())
                .doesNotContain("passwordHash")
                .doesNotContain("mfaSecret")
                .doesNotContain("password");
    }
}

package com.eams.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio del módulo Auth.
 * Representa un usuario autenticable de la plataforma.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "institution_id")
    private UUID institutionId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ── Factory method ──────────────────────────────────────────────────────

    public static User create(String email,
                              String rawPassword,
                              UserRole role,
                              UUID institutionId,
                              PasswordEncoder encoder) {
        User user = new User();
        user.id = UUID.randomUUID(); // generado en dominio; JPA lo respeta si ya no es null
        user.email = email.toLowerCase().strip();
        user.passwordHash = encoder.encode(rawPassword);
        user.role = role;
        user.institutionId = institutionId;
        return user;
    }

    // ── Actualización de perfil ─────────────────────────────────────────────

    public void updateProfile(String firstName, String lastName, String phone) {
        if (firstName != null && !firstName.isBlank()) this.firstName = firstName.strip();
        if (lastName  != null && !lastName.isBlank())  this.lastName  = lastName.strip();
        if (phone     != null && !phone.isBlank())     this.phone     = phone.strip();
    }

    // ── Comportamiento de dominio ───────────────────────────────────────────

    public boolean matchesPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, passwordHash);
    }

    public boolean requiresMfa() {
        return role.requiresMfa();
    }

    public void configureMfa(String secret) {
        this.mfaSecret = secret;
    }

    public boolean hasMfaConfigured() {
        return mfaSecret != null && !mfaSecret.isBlank();
    }
}

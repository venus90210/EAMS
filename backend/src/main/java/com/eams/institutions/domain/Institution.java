package com.eams.institutions.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Institución educativa registrada en la plataforma.
 * Raíz de agregado del módulo Instituciones (AD-02, AD-08).
 *
 * Cada institución actúa como tenant; su {@code id} se propaga
 * como {@code institution_id} en todas las tablas de dominio (AD-08).
 */
@Entity
@Table(name = "institutions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "email_domain", nullable = false, unique = true, length = 100)
    private String emailDomain;

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

    public static Institution create(String name, String emailDomain) {
        Institution inst = new Institution();
        inst.id          = UUID.randomUUID();
        inst.name        = name.strip();
        inst.emailDomain = emailDomain.toLowerCase().strip();
        return inst;
    }

    // ── Comportamiento de dominio ───────────────────────────────────────────

    public void update(String name, String emailDomain) {
        if (name != null && !name.isBlank()) {
            this.name = name.strip();
        }
        if (emailDomain != null && !emailDomain.isBlank()) {
            this.emailDomain = emailDomain.toLowerCase().strip();
        }
    }
}

package com.eams.auth.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del módulo Auth — abstracción de persistencia.
 * La implementación concreta (JPA) vive en infrastructure/persistence.
 * El dominio solo conoce esta interfaz (AD-03 — Arquitectura Hexagonal).
 */
public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
    boolean existsByEmail(String email);
}

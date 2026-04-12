package com.eams.auth.infrastructure.persistence;

import com.eams.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno.
 * No debe ser accedido directamente fuera del paquete persistence.
 * El dominio interactúa únicamente con UserRepository (AD-03).
 */
interface SpringDataUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

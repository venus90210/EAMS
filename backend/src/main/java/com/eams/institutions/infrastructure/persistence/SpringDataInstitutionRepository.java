package com.eams.institutions.infrastructure.persistence;

import com.eams.institutions.domain.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno.
 * No debe ser accedido directamente fuera del paquete persistence (AD-03).
 */
interface SpringDataInstitutionRepository extends JpaRepository<Institution, UUID> {
    Optional<Institution> findByEmailDomain(String emailDomain);
    boolean existsByEmailDomain(String emailDomain);
}

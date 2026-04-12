package com.eams.institutions.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del módulo Instituciones — abstracción de persistencia (AD-03).
 */
public interface InstitutionRepository {
    Institution save(Institution institution);
    Optional<Institution> findById(UUID id);
    Optional<Institution> findByEmailDomain(String emailDomain);
    List<Institution> findAll();
    boolean existsByEmailDomain(String emailDomain);
}

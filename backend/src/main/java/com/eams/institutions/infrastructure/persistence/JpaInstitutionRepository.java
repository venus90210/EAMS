package com.eams.institutions.infrastructure.persistence;

import com.eams.institutions.domain.Institution;
import com.eams.institutions.domain.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida InstitutionRepository (AD-03).
 */
@Repository
@RequiredArgsConstructor
public class JpaInstitutionRepository implements InstitutionRepository {

    private final SpringDataInstitutionRepository spring;

    @Override
    public Institution save(Institution institution) {
        return spring.save(institution);
    }

    @Override
    public Optional<Institution> findById(UUID id) {
        return spring.findById(id);
    }

    @Override
    public Optional<Institution> findByEmailDomain(String emailDomain) {
        return spring.findByEmailDomain(emailDomain);
    }

    @Override
    public List<Institution> findAll() {
        return spring.findAll();
    }

    @Override
    public boolean existsByEmailDomain(String emailDomain) {
        return spring.existsByEmailDomain(emailDomain);
    }
}

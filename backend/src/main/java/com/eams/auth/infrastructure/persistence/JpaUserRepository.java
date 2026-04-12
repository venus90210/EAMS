package com.eams.auth.infrastructure.persistence;

import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida UserRepository (AD-03).
 * Delega en SpringDataUserRepository y expone solo la interfaz del dominio.
 */
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository spring;

    @Override
    public Optional<User> findByEmail(String email) {
        return spring.findByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return spring.findById(id);
    }

    @Override
    public User save(User user) {
        return spring.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return spring.existsByEmail(email);
    }
}

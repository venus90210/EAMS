package com.eams.auth.infrastructure.persistence;

import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.shared.user.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida UserRepository (AD-03).
 * También implementa UserLookupPort (shared) para que el módulo 'users'
 * pueda verificar la existencia de un guardianId sin depender de auth.domain.
 */
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository, UserLookupPort {

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

    @Override
    public boolean existsById(UUID userId) {
        return spring.existsById(userId);
    }
}

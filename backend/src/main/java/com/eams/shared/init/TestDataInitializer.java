package com.eams.shared.init;

import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRole;
import com.eams.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Initializes test users on application startup.
 * This ensures test credentials have properly bcrypt-hashed passwords.
 *
 * Solo se ejecuta en perfiles 'dev' y 'test'.
 * NO se carga en producción (@Profile({"dev", "test"})).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Institution UUID from V8__insert_test_data.sql
    private static final UUID INSTITUTION_ID = UUID.fromString("b716fa11-ea40-468a-9dc8-ae131402c7ff");

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing test users with hashed passwords...");

        // Create GUARDIAN user if not exists
        if (!userRepository.existsByEmail("guardian@example.com")) {
            User guardian = User.create(
                "guardian@example.com",
                "password123",
                UserRole.GUARDIAN,
                INSTITUTION_ID,
                passwordEncoder
            );
            guardian.updateProfile("Ana", "Martínez", "+57 300 1234567");
            userRepository.save(guardian);
            log.info("Created GUARDIAN user: guardian@example.com");
        }

        // Create TEACHER user if not exists
        if (!userRepository.existsByEmail("teacher@example.com")) {
            User teacher = User.create(
                "teacher@example.com",
                "password123",
                UserRole.TEACHER,
                INSTITUTION_ID,
                passwordEncoder
            );
            teacher.updateProfile("Pedro", "Rodríguez", "+57 300 2345678");
            userRepository.save(teacher);
            log.info("Created TEACHER user: teacher@example.com");
        }

        // Create ADMIN user if not exists
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = User.create(
                "admin@example.com",
                "password123",
                UserRole.ADMIN,
                INSTITUTION_ID,
                passwordEncoder
            );
            admin.updateProfile("Clara", "Sánchez", "+57 300 3456789");
            userRepository.save(admin);
            log.info("Created ADMIN user: admin@example.com");
        }

        log.info("Test users initialization completed");
    }
}

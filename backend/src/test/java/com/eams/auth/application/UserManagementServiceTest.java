package com.eams.auth.application;

import com.eams.auth.application.dto.RegisterUserRequest;
import com.eams.auth.application.dto.UpdateUserRequest;
import com.eams.auth.application.dto.UserResponse;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.auth.domain.UserRole;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserManagementService service;

    private final UUID institutionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_guardian_noContext_succeeds() {
        // GUARDIAN puede auto-registrarse sin contexto de tenant
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.existsByEmail("guardian@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        var request = new RegisterUserRequest(
                "guardian@test.com", "password123", UserRole.GUARDIAN,
                institutionId, "Ana", "López", null);

        UserResponse response = service.register(request);

        assertThat(response.email()).isEqualTo("guardian@test.com");
        assertThat(response.role()).isEqualTo(UserRole.GUARDIAN);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        var request = new RegisterUserRequest(
                "dup@test.com", "password123", UserRole.GUARDIAN,
                institutionId, null, null, null);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void register_teacher_withAdminContext_succeeds() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        when(userRepository.existsByEmail("teacher@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        var request = new RegisterUserRequest(
                "teacher@test.com", "password123", UserRole.TEACHER,
                institutionId, "Carlos", "Ruiz", null);

        UserResponse response = service.register(request);

        assertThat(response.role()).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void register_teacher_withoutAdminContext_throwsForbidden() {
        // TEACHER no puede auto-registrarse — requiere ADMIN en contexto
        var request = new RegisterUserRequest(
                "teacher@test.com", "password123", UserRole.TEACHER,
                institutionId, null, null, null);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void register_superadmin_byAdmin_throwsForbidden() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        when(userRepository.existsByEmail("sa@test.com")).thenReturn(false);

        var request = new RegisterUserRequest(
                "sa@test.com", "password123", UserRole.SUPERADMIN,
                null, null, null, null);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void register_teacher_missingInstitutionId_throwsUnprocessable() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        when(userRepository.existsByEmail("t@test.com")).thenReturn(false);

        var request = new RegisterUserRequest(
                "t@test.com", "password123", UserRole.TEACHER,
                null, null, null, null); // institutionId = null

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("INSTITUTION_REQUIRED");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                });
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    void getProfile_existingUser_returnsResponse() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        UUID userId = UUID.randomUUID();
        User user = User.create("u@test.com", "pass", UserRole.GUARDIAN, institutionId, passwordEncoder);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = service.getProfile(userId);

        assertThat(response.email()).isEqualTo("u@test.com");
    }

    @Test
    void getProfile_notFound_throwsNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(userId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_asAdmin_updatesProfile() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        UUID userId = UUID.randomUUID();
        User user = User.create("u@test.com", "pass", UserRole.GUARDIAN, institutionId, passwordEncoder);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = service.updateUser(userId,
                new UpdateUserRequest("Nuevo", "Apellido", "3001234567"));

        assertThat(response.firstName()).isEqualTo("Nuevo");
        assertThat(response.lastName()).isEqualTo("Apellido");
    }

    @Test
    void updateUser_withoutAdminContext_throwsForbidden() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        TenantContextHolder.set(new TenantContext(institutionId, "GUARDIAN"));
        UUID userId = UUID.randomUUID();
        User user = User.create("u@test.com", "pass", UserRole.GUARDIAN, institutionId, passwordEncoder);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.updateUser(userId,
                new UpdateUserRequest("X", "Y", null)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void updateUser_notFound_throwsNotFound() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(userId,
                new UpdateUserRequest("X", "Y", null)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}

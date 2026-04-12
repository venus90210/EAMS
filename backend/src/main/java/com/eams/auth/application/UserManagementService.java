package com.eams.auth.application;

import com.eams.auth.application.dto.RegisterUserRequest;
import com.eams.auth.application.dto.UpdateUserRequest;
import com.eams.auth.application.dto.UserResponse;
import com.eams.auth.domain.User;
import com.eams.auth.domain.UserRepository;
import com.eams.auth.domain.UserRole;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Casos de uso de gestión de usuarios (Phase 1.3).
 *
 * Reglas de negocio:
 *   - GUARDIAN puede auto-registrarse (sin contexto de tenant activo).
 *   - TEACHER / ADMIN solo pueden ser creados por ADMIN o SUPERADMIN.
 *   - SUPERADMIN solo puede ser creado por otro SUPERADMIN.
 *   - Un usuario solo puede editar su propio perfil; ADMIN/SUPERADMIN pueden editar cualquiera.
 *   - SUPERADMIN no requiere institutionId.
 */
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Registrar ────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo usuario.
     *
     * @throws DomainException EMAIL_ALREADY_EXISTS si el email ya está registrado (409)
     * @throws DomainException INSUFFICIENT_ROLE si el rol solicitado no está permitido (403)
     */
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw DomainException.conflict("EMAIL_ALREADY_EXISTS",
                    "El email '%s' ya está registrado".formatted(request.email()));
        }

        validateRoleCreation(request.role());
        validateInstitutionId(request.role(), request.institutionId());

        User user = User.create(
                request.email(),
                request.password(),
                request.role(),
                request.institutionId(),
                passwordEncoder);

        if (request.firstName() != null || request.lastName() != null || request.phone() != null) {
            user.updateProfile(request.firstName(), request.lastName(), request.phone());
        }

        return UserResponse.from(userRepository.save(user));
    }

    // ── Obtener perfil ───────────────────────────────────────────────────────

    public UserResponse getProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> DomainException.notFound("Usuario no encontrado: " + userId));
    }

    // ── Actualizar perfil ────────────────────────────────────────────────────

    /**
     * Actualiza firstName, lastName y phone de un usuario.
     *
     * @throws DomainException NOT_FOUND si el usuario no existe (404)
     * @throws DomainException INSUFFICIENT_ROLE si el solicitante no puede editar este usuario (403)
     */
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> DomainException.notFound("Usuario no encontrado: " + userId));

        assertCanEdit(userId);

        user.updateProfile(request.firstName(), request.lastName(), request.phone());
        return UserResponse.from(userRepository.save(user));
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    /**
     * Valida que el rol solicitado pueda ser creado por el solicitante actual.
     * GUARDIAN puede auto-registrarse sin contexto.
     * TEACHER/ADMIN/SUPERADMIN requieren ADMIN o SUPERADMIN en contexto.
     */
    private void validateRoleCreation(UserRole requestedRole) {
        if (requestedRole == UserRole.GUARDIAN) return; // auto-registro permitido

        var ctx = TenantContextHolder.get();
        if (ctx.isEmpty()) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Se requiere autenticación para crear usuarios con rol " + requestedRole);
        }

        String callerRole = ctx.get().role();
        boolean callerIsAdminOrSuperAdmin =
                "ADMIN".equals(callerRole) || "SUPERADMIN".equals(callerRole);

        if (!callerIsAdminOrSuperAdmin) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN o SUPERADMIN pueden crear usuarios con rol " + requestedRole);
        }

        if (requestedRole == UserRole.SUPERADMIN && !"SUPERADMIN".equals(callerRole)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo SUPERADMIN puede crear otro SUPERADMIN");
        }
    }

    /** SUPERADMIN no requiere institutionId. Los demás roles sí. */
    private void validateInstitutionId(UserRole role, UUID institutionId) {
        if (role != UserRole.SUPERADMIN && institutionId == null) {
            throw DomainException.unprocessable("INSTITUTION_REQUIRED",
                    "institution_id es obligatorio para el rol " + role);
        }
    }

    /**
     * El solicitante puede editar su propio perfil; ADMIN/SUPERADMIN pueden editar cualquiera.
     * La validación de "es el mismo usuario" se delega al controlador (el gateway inyecta
     * el userId del token en un header interno separado del institutionId).
     */
    private void assertCanEdit(UUID targetUserId) {
        var ctx = TenantContextHolder.get();
        if (ctx.isEmpty()) {
            throw DomainException.forbidden("FORBIDDEN", "No hay contexto de autenticación");
        }
        String role = ctx.get().role();
        boolean isAdmin = "ADMIN".equals(role) || "SUPERADMIN".equals(role);
        if (!isAdmin) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "No tienes permisos para editar este perfil");
        }
    }
}

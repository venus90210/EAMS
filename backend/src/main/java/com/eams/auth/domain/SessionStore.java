package com.eams.auth.domain;

import java.util.UUID;

/**
 * Puerto de salida — almacén de refresh tokens (Redis).
 * Permite revocar sesiones activas de forma inmediata (AD-06).
 */
public interface SessionStore {

    /**
     * Almacena un refresh token asociado al usuario con TTL en segundos.
     */
    void save(String refreshToken, UUID userId, long ttlSeconds);

    /**
     * Retorna el userId asociado al token, o vacío si fue revocado o expiró.
     */
    java.util.Optional<UUID> findUserIdByToken(String refreshToken);

    /**
     * Revoca el token eliminándolo del store (logout, cambio de contraseña).
     */
    void revoke(String refreshToken);

    /**
     * Revoca todos los tokens activos de un usuario.
     */
    void revokeAllForUser(UUID userId);
}

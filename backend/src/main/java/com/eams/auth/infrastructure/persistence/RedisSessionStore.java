package com.eams.auth.infrastructure.persistence;

import com.eams.auth.domain.SessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Adaptador Redis del puerto de salida SessionStore (AD-06).
 *
 * Esquema de claves:
 *   session:{refreshToken}       → userId (UUID string)   TTL = 7 días
 *   user_sessions:{userId}       → Set de refreshTokens activos
 *
 * El Set de sesiones por usuario permite revocarlas todas (revokeAllForUser).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionStore implements SessionStore {

    private static final String SESSION_PREFIX      = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";

    private final StringRedisTemplate redis;

    @Override
    public void save(String refreshToken, UUID userId, long ttlSeconds) {
        String sessionKey     = SESSION_PREFIX + refreshToken;
        String userSessionKey = USER_SESSIONS_PREFIX + userId;

        redis.opsForValue().set(sessionKey, userId.toString(), Duration.ofSeconds(ttlSeconds));
        redis.opsForSet().add(userSessionKey, refreshToken);
        redis.expire(userSessionKey, Duration.ofSeconds(ttlSeconds));

        log.debug("Sesión guardada para usuario {} con TTL {}s", userId, ttlSeconds);
    }

    @Override
    public Optional<UUID> findUserIdByToken(String refreshToken) {
        String value = redis.opsForValue().get(SESSION_PREFIX + refreshToken);
        return Optional.ofNullable(value).map(UUID::fromString);
    }

    @Override
    public void revoke(String refreshToken) {
        // Obtener userId antes de borrar la sesión para limpiar el índice inverso
        String value = redis.opsForValue().get(SESSION_PREFIX + refreshToken);
        redis.delete(SESSION_PREFIX + refreshToken);

        if (value != null) {
            redis.opsForSet().remove(USER_SESSIONS_PREFIX + value, refreshToken);
        }

        log.debug("Refresh token revocado");
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        String userSessionKey = USER_SESSIONS_PREFIX + userId;
        Set<String> tokens = redis.opsForSet().members(userSessionKey);

        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(token -> redis.delete(SESSION_PREFIX + token));
        }

        redis.delete(userSessionKey);
        log.debug("Todas las sesiones revocadas para usuario {}", userId);
    }
}

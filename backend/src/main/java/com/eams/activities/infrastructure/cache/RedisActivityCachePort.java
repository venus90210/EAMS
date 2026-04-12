package com.eams.activities.infrastructure.cache;

import com.eams.activities.domain.ActivityCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementación de ActivityCachePort usando Redis (AD-05).
 *
 * Política de caché:
 *   - Key: "activity:available_spots:{activityId}"
 *   - TTL: 30 segundos
 *   - Valor: número de cupos disponibles (Integer)
 *
 * Invalidación: cuando cambia estado o se modifica total_spots
 */
@Component
@RequiredArgsConstructor
public class RedisActivityCachePort implements ActivityCachePort {

    private static final String CACHE_KEY_PREFIX = "activity:available_spots:";
    private static final long CACHE_TTL_SECONDS = 30;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<Integer> getAvailableSpots(UUID activityId) {
        String key = buildKey(activityId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Integer) {
            return Optional.of((Integer) value);
        }
        return Optional.empty();
    }

    @Override
    public void setAvailableSpots(UUID activityId, Integer spots) {
        String key = buildKey(activityId);
        redisTemplate.opsForValue().set(key, spots, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void invalidate(UUID activityId) {
        String key = buildKey(activityId);
        redisTemplate.delete(key);
    }

    @Override
    public void invalidateByInstitution(UUID institutionId) {
        // Nota: Esta es una simplificación — en producción usaría Sets o Sorted Sets
        // para trackear qué actividades pertenecen a qué institución.
        // Por ahora, esta es una operación costosa que barrería todas las keys.
        // Para Phase 1.4 es suficiente hacer invalidate() por activity.
    }

    private String buildKey(UUID activityId) {
        return CACHE_KEY_PREFIX + activityId;
    }
}

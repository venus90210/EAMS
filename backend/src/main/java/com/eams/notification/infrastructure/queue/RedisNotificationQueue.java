package com.eams.notification.infrastructure.queue;

import com.eams.notification.domain.NotificationJob;
import com.eams.notification.domain.NotificationQueuePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementación Redis del puerto NotificationQueuePort.
 *
 * Usa Redis List para la cola (LPUSH para encolar, RPOP para desencolar)
 * y Redis String con TTL para el registro de idempotencia.
 *
 * Topología:
 * - Key: "notifications:queue" → List de JSON serializados
 * - Key: "notifications:processed:{idempotencyKey}" → String (valor "1"), TTL 7 días
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationQueue implements NotificationQueuePort {

    private static final String QUEUE_KEY = "notifications:queue";
    private static final String PROCESSED_KEY_PREFIX = "notifications:processed:";
    private static final long PROCESSED_TTL_DAYS = 7;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void push(NotificationJob job) {
        try {
            String json = objectMapper.writeValueAsString(job);
            Long position = redisTemplate.opsForList().leftPush(QUEUE_KEY, json);
            log.debug("Job {} encolado en posición {}", job.id(), position);
        } catch (Exception e) {
            log.error("Error serializando NotificationJob {}: {}", job.id(), e.getMessage(), e);
            throw new RuntimeException("Error enqueueing notification", e);
        }
    }

    @Override
    public Optional<NotificationJob> pollNext() {
        try {
            String json = redisTemplate.opsForList().rightPop(QUEUE_KEY);
            if (json == null) {
                return Optional.empty();
            }
            NotificationJob job = objectMapper.readValue(json, NotificationJob.class);
            log.debug("Job {} desencolado", job.id());
            return Optional.of(job);
        } catch (Exception e) {
            log.error("Error deserializando NotificationJob: {}", e.getMessage(), e);
            throw new RuntimeException("Error dequeueing notification", e);
        }
    }

    @Override
    public void markProcessed(String idempotencyKey) {
        String key = PROCESSED_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, "1", PROCESSED_TTL_DAYS, TimeUnit.DAYS);
        log.debug("Marcado como procesado: {}", idempotencyKey);
    }

    @Override
    public boolean isProcessed(String idempotencyKey) {
        String key = PROCESSED_KEY_PREFIX + idempotencyKey;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}

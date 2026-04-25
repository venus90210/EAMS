package com.eams.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Configuración de Redis para la plataforma EAMS.
 *
 * Proporciona el bean RedisTemplate para:
 * - Almacenamiento de refresh tokens (TTL 7 días)
 * - Caché de actividades y cupos disponibles (TTL 30s)
 * - Revocación de tokens en logout
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}

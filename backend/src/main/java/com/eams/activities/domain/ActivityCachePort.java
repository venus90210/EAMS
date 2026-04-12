package com.eams.activities.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de caché para consultas de available_spots (AD-05 — Redis).
 *
 * Permite que el servicio de dominio reste acoplamiento a la tecnología
 * de caché (Redis, Memcached, etc.).
 *
 * TTL: 30 segundos (RF04 — disponibilidad en <1s)
 */
public interface ActivityCachePort {

    /**
     * Obtiene cupos disponibles desde caché si existen.
     *
     * @return Optional.empty() si no hay entrada en caché
     */
    Optional<Integer> getAvailableSpots(UUID activityId);

    /**
     * Almacena cupos disponibles en caché con TTL 30s.
     */
    void setAvailableSpots(UUID activityId, Integer spots);

    /**
     * Invalida la entrada de caché para una actividad.
     * Llamado cuando cambia el estado o se modifica total_spots.
     */
    void invalidate(UUID activityId);

    /**
     * Invalida todas las entradas de caché para una institución.
     * Llamado cuando cambia el estado de múltiples actividades.
     */
    void invalidateByInstitution(UUID institutionId);
}

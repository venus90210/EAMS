package com.eams.activities.application.dto;

import java.util.UUID;

/**
 * Respuesta para GET /activities/{id}/spots.
 * Contiene la disponibilidad actual de cupos (cacheada en Redis).
 */
public record SpotsResponse(
        UUID activityId,
        Integer totalSpots,
        Integer availableSpots
) {
    public int enrolledSpots() {
        return totalSpots - availableSpots;
    }
}

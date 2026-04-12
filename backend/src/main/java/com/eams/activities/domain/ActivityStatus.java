package com.eams.activities.domain;

/**
 * Estados del ciclo de vida de una actividad (F5-estado-actividad.feature).
 *
 * DRAFT       → creada, no visible a GUARDIAN
 * PUBLISHED   → lista para inscripciones, visible a todos
 * DISABLED    → cerrada, no acepta nuevas inscripciones
 */
public enum ActivityStatus {
    DRAFT,
    PUBLISHED,
    DISABLED;

    /**
     * Valida transiciones de estado permitidas.
     *
     * Permitidas:
     *   DRAFT    → PUBLISHED
     *   PUBLISHED → DISABLED
     *   DISABLED → PUBLISHED
     *
     * @throws IllegalArgumentException si la transición no es válida
     */
    public void validateTransitionTo(ActivityStatus newStatus) {
        if (newStatus == this) {
            throw new IllegalArgumentException(
                    "No se puede transicionar a " + newStatus + " desde " + this);
        }

        switch (this) {
            case DRAFT:
                if (newStatus != PUBLISHED) {
                    throw new IllegalArgumentException(
                            "Desde DRAFT solo se puede transicionar a PUBLISHED, intento: " + newStatus);
                }
                break;
            case PUBLISHED:
                if (newStatus != DISABLED) {
                    throw new IllegalArgumentException(
                            "Desde PUBLISHED solo se puede transicionar a DISABLED, intento: " + newStatus);
                }
                break;
            case DISABLED:
                if (newStatus != PUBLISHED) {
                    throw new IllegalArgumentException(
                            "Desde DISABLED solo se puede transicionar a PUBLISHED, intento: " + newStatus);
                }
                break;
        }
    }
}

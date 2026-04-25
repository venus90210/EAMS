package com.eams.activities.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del módulo Activities — abstracción de persistencia.
 */
public interface ActivityRepository {

    Activity save(Activity activity);

    Optional<Activity> findById(UUID activityId);

    /**
     * Lista actividades de una institución, opcionalmente filtradas por estado.
     */
    List<Activity> findByInstitutionId(UUID institutionId, ActivityStatus status);

    /**
     * Lista todas las actividades de una institución (sin filtro de estado).
     */
    List<Activity> findByInstitutionId(UUID institutionId);
}

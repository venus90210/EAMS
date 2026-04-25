package com.eams.activities.infrastructure.persistence;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityRepository;
import com.eams.activities.domain.ActivityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida ActivityRepository (AD-03).
 */
@Repository
@RequiredArgsConstructor
public class JpaActivityRepository implements ActivityRepository {

    private final SpringDataActivityRepository spring;

    @Override
    public Activity save(Activity activity) {
        return spring.save(activity);
    }

    @Override
    public Optional<Activity> findById(UUID activityId) {
        return spring.findById(activityId);
    }

    @Override
    public List<Activity> findByInstitutionId(UUID institutionId, ActivityStatus status) {
        return spring.findByInstitutionIdAndStatus(institutionId, status);
    }

    @Override
    public List<Activity> findByInstitutionId(UUID institutionId) {
        return spring.findByInstitutionId(institutionId);
    }
}

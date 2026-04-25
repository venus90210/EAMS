package com.eams.activities.api;

import com.eams.activities.domain.ActivityCachePort;

import java.util.Optional;
import java.util.UUID;

/**
 * Public re-export of ActivityCachePort.
 * Allows other modules to depend on this interface.
 */
public interface ActivityCachePortApi extends ActivityCachePort {
    @Override
    Optional<Integer> getAvailableSpots(UUID activityId);

    @Override
    void setAvailableSpots(UUID activityId, Integer spots);

    @Override
    void invalidate(UUID activityId);

    @Override
    void invalidateByInstitution(UUID institutionId);
}

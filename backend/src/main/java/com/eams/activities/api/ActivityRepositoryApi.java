package com.eams.activities.api;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Public re-export of ActivityRepository port.
 * Allows other modules to depend on this interface.
 */
public interface ActivityRepositoryApi extends ActivityRepository {
    @Override
    Activity save(Activity activity);

    @Override
    Optional<Activity> findById(UUID activityId);
}

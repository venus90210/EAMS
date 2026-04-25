package com.eams.activities.application.dto;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityStatus;
import com.eams.activities.domain.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Instant;
import java.util.UUID;

/**
 * Representación pública de una actividad.
 */
public record ActivityResponse(
        UUID id,
        String name,
        String description,
        ActivityStatus status,
        Integer totalSpots,
        Integer availableSpots,
        ScheduleDTO schedule,
        UUID institutionId,
        Instant createdAt,
        Instant updatedAt
) {

    public record ScheduleDTO(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String location
    ) {}

    public static ActivityResponse from(Activity activity) {
        Schedule sched = activity.getSchedule();
        ScheduleDTO schedDTO = sched != null
                ? new ScheduleDTO(sched.getDayOfWeek(), sched.getStartTime(),
                                 sched.getEndTime(), sched.getLocation())
                : null;

        return new ActivityResponse(
                activity.getId(),
                activity.getName(),
                activity.getDescription(),
                activity.getStatus(),
                activity.getTotalSpots(),
                activity.getAvailableSpots(),
                schedDTO,
                activity.getInstitutionId(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }
}

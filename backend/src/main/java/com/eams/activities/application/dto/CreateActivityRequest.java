package com.eams.activities.application.dto;

import com.eams.activities.domain.Schedule;
import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Payload para crear una actividad (POST /activities).
 */
public record CreateActivityRequest(
        @NotBlank @Size(min = 3, max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull @Min(1)
        Integer totalSpots,

        ScheduleDTO schedule
) {

    public Schedule toSchedule() {
        if (schedule == null) return null;
        return Schedule.create(schedule.dayOfWeek(), schedule.startTime(),
                              schedule.endTime(), schedule.location());
    }

    public record ScheduleDTO(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String location
    ) {}
}

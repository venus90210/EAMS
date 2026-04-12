package com.eams.activities.application.dto;

import com.eams.activities.domain.Schedule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Payload para actualizar una actividad (PATCH /activities/{id}).
 * Todos los campos son opcionales (actualización parcial).
 */
public record UpdateActivityRequest(
        @Size(min = 3, max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Min(1)
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

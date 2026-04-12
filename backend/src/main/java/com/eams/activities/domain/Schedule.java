package com.eams.activities.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Horario de una actividad (entidad anidada, informativa — no valida conflictos).
 *
 * Campos:
 *   - dayOfWeek: día de la semana (MONDAY, TUESDAY, etc.)
 *   - startTime: hora de inicio (ej. 14:30)
 *   - endTime: hora de fin (ej. 16:00)
 *   - location: lugar (cancha, aula, etc.)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Schedule {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "location")
    private String location;

    // ── Factory method ──────────────────────────────────────────────────────

    public static Schedule create(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String location) {
        Schedule s = new Schedule();
        s.dayOfWeek = dayOfWeek;
        s.startTime = startTime;
        s.endTime    = endTime;
        s.location   = location != null ? location.strip() : null;
        return s;
    }
}

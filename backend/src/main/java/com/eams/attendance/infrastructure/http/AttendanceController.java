package com.eams.attendance.infrastructure.http;

import com.eams.attendance.application.AttendanceService;
import com.eams.attendance.application.dto.AttendanceRecordResponse;
import com.eams.attendance.application.dto.AttendanceSessionResponse;
import com.eams.attendance.application.dto.OpenSessionRequest;
import com.eams.attendance.application.dto.RecordAttendanceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── Sesiones ─────────────────────────────────────────────────────────────

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceSessionResponse openSession(
            @Valid @RequestBody OpenSessionRequest request) {
        return AttendanceSessionResponse.from(
                attendanceService.openSession(
                        request.activityId(),
                        request.date(),
                        request.topicsCovered()
                )
        );
    }

    // ── Marcación de asistencia ──────────────────────────────────────────────

    @PostMapping("/records")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceRecordResponse recordAttendance(
            @Valid @RequestBody RecordAttendanceRequest request) {
        return AttendanceRecordResponse.from(
                attendanceService.recordAttendance(
                        request.sessionId(),
                        request.studentId(),
                        request.present(),
                        request.observation()
                )
        );
    }

    @GetMapping("/sessions/{sessionId}/records")
    public List<AttendanceRecordResponse> getSessionAttendance(
            @PathVariable UUID sessionId) {
        return attendanceService.getAttendanceBySession(sessionId)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }

    // ── Observaciones ────────────────────────────────────────────────────────

    @PatchMapping("/records/{recordId}/observation")
    public AttendanceRecordResponse addObservation(
            @PathVariable UUID recordId,
            @RequestParam String observation) {
        return AttendanceRecordResponse.from(
                attendanceService.addObservation(recordId, observation)
        );
    }

    // ── Consultas ────────────────────────────────────────────────────────────

    @GetMapping("/student/{studentId}")
    public List<AttendanceRecordResponse> getStudentAttendance(
            @PathVariable UUID studentId) {
        return attendanceService.getAttendanceByStudent(studentId)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }

    @GetMapping("/guardians/{guardianId}")
    public List<AttendanceRecordResponse> getGuardianAttendance(
            @PathVariable UUID guardianId) {
        return attendanceService.getAttendanceByGuardian(guardianId)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }
}

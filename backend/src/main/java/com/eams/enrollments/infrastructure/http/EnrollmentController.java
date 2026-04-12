package com.eams.enrollments.infrastructure.http;

import com.eams.enrollments.application.EnrollmentService;
import com.eams.enrollments.application.dto.CreateEnrollmentRequest;
import com.eams.enrollments.application.dto.EnrollmentResponse;
import com.eams.enrollments.domain.EnrollmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse createEnrollment(
            @Valid @RequestBody CreateEnrollmentRequest request) {
        return EnrollmentResponse.from(
                enrollmentService.enroll(request.studentId(), request.activityId()));
    }

    @DeleteMapping("/{enrollmentId}")
    public EnrollmentResponse cancelEnrollment(@PathVariable UUID enrollmentId) {
        return EnrollmentResponse.from(enrollmentService.cancel(enrollmentId));
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> getEnrollmentsByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) EnrollmentStatus status) {
        return enrollmentService.getEnrollmentsByStudent(studentId, status)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @GetMapping("/activity/{activityId}")
    public List<EnrollmentResponse> getEnrollmentsByActivity(
            @PathVariable UUID activityId,
            @RequestParam(required = false) EnrollmentStatus status) {
        return enrollmentService.getEnrollmentsByActivity(activityId, status)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }
}

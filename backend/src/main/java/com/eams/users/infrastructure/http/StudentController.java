package com.eams.users.infrastructure.http;

import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContextHolder;
import com.eams.users.application.StudentService;
import com.eams.users.application.dto.BulkLoadResult;
import com.eams.users.application.dto.LinkStudentRequest;
import com.eams.users.application.dto.StudentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador HTTP del módulo Users.
 *
 * Endpoints:
 *   POST  /users/students/link                     — vincular estudiante (solo ADMIN)
 *   GET   /users/guardians/{guardianId}/students   — listar hijos de un acudiente
 *   POST  /users/bulk                              — carga masiva CSV (ADMIN/SUPERADMIN)
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/students/link")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse linkStudentToGuardian(@Valid @RequestBody LinkStudentRequest request) {
        requireAdmin();
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        return studentService.linkStudentToGuardian(request, institutionId);
    }

    @GetMapping("/guardians/{guardianId}/students")
    public List<StudentResponse> getStudentsByGuardian(@PathVariable UUID guardianId) {
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        return studentService.getStudentsByGuardian(guardianId, institutionId);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BulkLoadResult bulkLoad(@RequestParam("file") MultipartFile file) throws IOException {
        requireAdminOrSuperAdmin();
        UUID institutionId = TenantContextHolder.requireContext().institutionId();
        return studentService.bulkLoad(file.getInputStream(), institutionId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void requireAdmin() {
        String role = TenantContextHolder.requireContext().role();
        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN puede vincular estudiantes");
        }
    }

    private void requireAdminOrSuperAdmin() {
        String role = TenantContextHolder.requireContext().role();
        if (!"ADMIN".equals(role) && !"SUPERADMIN".equals(role)) {
            throw DomainException.forbidden("INSUFFICIENT_ROLE",
                    "Solo ADMIN o SUPERADMIN pueden realizar carga masiva");
        }
    }
}

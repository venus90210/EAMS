package com.eams.users.application;

import com.eams.shared.exception.DomainException;
import com.eams.shared.user.UserLookupPort;
import com.eams.users.application.dto.BulkLoadResult;
import com.eams.users.application.dto.LinkStudentRequest;
import com.eams.users.application.dto.StudentResponse;
import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso del módulo Users: gestión de estudiantes y asociaciones acudiente-estudiante.
 *
 * Reglas de negocio:
 *   - linkStudentToGuardian: el guardian debe existir; el par (guardianId, nombre) no debe repetirse.
 *   - getStudentsByGuardian: GUARDIAN ve solo sus propios hijos (filtrado por institutionId).
 *   - bulkLoad: CSV con columnas guardianId,firstName,lastName,grade; errores parciales se reportan.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserLookupPort userLookupPort;

    // ── Vincular estudiante ──────────────────────────────────────────────────

    /**
     * Crea un estudiante y lo vincula a su acudiente.
     *
     * @throws DomainException NOT_FOUND si el guardian no existe (404)
     * @throws DomainException STUDENT_ALREADY_LINKED si la combinación ya existe (409)
     */
    public StudentResponse linkStudentToGuardian(LinkStudentRequest request, UUID institutionId) {
        if (!userLookupPort.existsById(request.guardianId())) {
            throw DomainException.notFound(
                    "Acudiente no encontrado: " + request.guardianId());
        }

        if (studentRepository.existsByGuardianIdAndFirstNameAndLastName(
                request.guardianId(),
                request.studentFirstName(),
                request.studentLastName())) {
            throw DomainException.conflict("STUDENT_ALREADY_LINKED",
                    "El estudiante '%s %s' ya está vinculado a este acudiente"
                            .formatted(request.studentFirstName(), request.studentLastName()));
        }

        Student student = Student.create(
                request.studentFirstName(),
                request.studentLastName(),
                request.grade(),
                institutionId,
                request.guardianId());

        return StudentResponse.from(studentRepository.save(student));
    }

    // ── Listar estudiantes de un acudiente ───────────────────────────────────

    /**
     * Retorna los estudiantes de un acudiente filtrados por institución.
     *
     * @throws DomainException NOT_FOUND si el guardian no existe (404)
     */
    public List<StudentResponse> getStudentsByGuardian(UUID guardianId, UUID institutionId) {
        if (!userLookupPort.existsById(guardianId)) {
            throw DomainException.notFound("Acudiente no encontrado: " + guardianId);
        }

        return studentRepository.findByGuardianIdAndInstitutionId(guardianId, institutionId)
                .stream()
                .map(StudentResponse::from)
                .toList();
    }

    // ── Carga masiva desde CSV ───────────────────────────────────────────────

    /**
     * Procesa un CSV con columnas: guardianId,firstName,lastName,grade
     * Retorna un reporte con los registros exitosos y los errores por fila.
     */
    public BulkLoadResult bulkLoad(InputStream csvStream, UUID institutionId) {
        List<BulkLoadResult.RowError> errors = new ArrayList<>();
        int successCount = 0;
        int rowNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (firstLine) {       // omitir encabezado
                    firstLine = false;
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 3) {
                    errors.add(new BulkLoadResult.RowError(rowNumber,
                            "Formato inválido — se esperan al menos 3 columnas"));
                    continue;
                }

                try {
                    UUID guardianId   = UUID.fromString(cols[0].strip());
                    String firstName  = cols[1].strip();
                    String lastName   = cols[2].strip();
                    String grade      = cols.length > 3 ? cols[3].strip() : null;

                    if (firstName.isBlank() || lastName.isBlank()) {
                        errors.add(new BulkLoadResult.RowError(rowNumber,
                                "firstName y lastName son obligatorios"));
                        continue;
                    }

                    linkStudentToGuardian(
                            new LinkStudentRequest(guardianId, firstName, lastName, grade),
                            institutionId);
                    successCount++;

                } catch (IllegalArgumentException e) {
                    errors.add(new BulkLoadResult.RowError(rowNumber,
                            "guardianId no es un UUID válido"));
                } catch (DomainException e) {
                    errors.add(new BulkLoadResult.RowError(rowNumber, e.getMessage()));
                }
            }

        } catch (Exception e) {
            errors.add(new BulkLoadResult.RowError(rowNumber,
                    "Error leyendo el archivo: " + e.getMessage()));
        }

        return new BulkLoadResult(rowNumber > 0 ? rowNumber - 1 : 0, successCount, errors);
    }
}

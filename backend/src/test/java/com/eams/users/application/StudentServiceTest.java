package com.eams.users.application;

import com.eams.shared.exception.DomainException;
import com.eams.shared.user.UserLookupPort;
import com.eams.users.application.dto.BulkLoadResult;
import com.eams.users.application.dto.LinkStudentRequest;
import com.eams.users.application.dto.StudentResponse;
import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserLookupPort userLookupPort;

    private StudentService service;

    private final UUID institutionId = UUID.randomUUID();
    private final UUID guardianId    = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new StudentService(studentRepository, userLookupPort);
    }

    // ── linkStudentToGuardian ─────────────────────────────────────────────────

    @Test
    void linkStudentToGuardian_validRequest_returnsStudentResponse() {
        when(userLookupPort.existsById(guardianId)).thenReturn(true);
        when(studentRepository.existsByGuardianIdAndFirstNameAndLastName(
                guardianId, "Juan", "Pérez")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        var request = new LinkStudentRequest(guardianId, "Juan", "Pérez", "5A");
        StudentResponse response = service.linkStudentToGuardian(request, institutionId);

        assertThat(response.firstName()).isEqualTo("Juan");
        assertThat(response.lastName()).isEqualTo("Pérez");
        assertThat(response.grade()).isEqualTo("5A");
        assertThat(response.guardianId()).isEqualTo(guardianId);
        assertThat(response.institutionId()).isEqualTo(institutionId);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void linkStudentToGuardian_guardianNotFound_throwsNotFound() {
        when(userLookupPort.existsById(guardianId)).thenReturn(false);

        var request = new LinkStudentRequest(guardianId, "Juan", "Pérez", null);

        assertThatThrownBy(() -> service.linkStudentToGuardian(request, institutionId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void linkStudentToGuardian_alreadyLinked_throwsConflict() {
        when(userLookupPort.existsById(guardianId)).thenReturn(true);
        when(studentRepository.existsByGuardianIdAndFirstNameAndLastName(
                guardianId, "Juan", "Pérez")).thenReturn(true);

        var request = new LinkStudentRequest(guardianId, "Juan", "Pérez", "5A");

        assertThatThrownBy(() -> service.linkStudentToGuardian(request, institutionId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("STUDENT_ALREADY_LINKED");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    // ── getStudentsByGuardian ────────────────────────────────────────────────

    @Test
    void getStudentsByGuardian_guardianWithStudents_returnsFiltered() {
        when(userLookupPort.existsById(guardianId)).thenReturn(true);
        Student s1 = Student.create("Ana", "García", "4B", institutionId, guardianId);
        Student s2 = Student.create("Luis", "García", "2A", institutionId, guardianId);
        when(studentRepository.findByGuardianIdAndInstitutionId(guardianId, institutionId))
                .thenReturn(List.of(s1, s2));

        List<StudentResponse> result = service.getStudentsByGuardian(guardianId, institutionId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudentResponse::firstName)
                .containsExactlyInAnyOrder("Ana", "Luis");
    }

    @Test
    void getStudentsByGuardian_guardianNotFound_throwsNotFound() {
        when(userLookupPort.existsById(guardianId)).thenReturn(false);

        assertThatThrownBy(() -> service.getStudentsByGuardian(guardianId, institutionId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getStudentsByGuardian_otherInstitution_returnsEmpty() {
        UUID otherInstitution = UUID.randomUUID();
        when(userLookupPort.existsById(guardianId)).thenReturn(true);
        when(studentRepository.findByGuardianIdAndInstitutionId(guardianId, otherInstitution))
                .thenReturn(List.of());

        List<StudentResponse> result = service.getStudentsByGuardian(guardianId, otherInstitution);

        assertThat(result).isEmpty();
    }

    // ── bulkLoad ──────────────────────────────────────────────────────────────

    @Test
    void bulkLoad_validCsv_processesAllRows() {
        when(userLookupPort.existsById(any(UUID.class))).thenReturn(true);
        when(studentRepository.existsByGuardianIdAndFirstNameAndLastName(
                any(), anyString(), anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        String csv = "guardianId,firstName,lastName,grade\n"
                + guardianId + ",María,López,3A\n"
                + guardianId + ",Pedro,López,1B\n";

        BulkLoadResult result = service.bulkLoad(toStream(csv), institutionId);

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.errors()).isEmpty();
        assertThat(result.totalRows()).isEqualTo(2);
    }

    @Test
    void bulkLoad_csvWithInvalidRows_returnsPartialErrors() {
        when(userLookupPort.existsById(any(UUID.class))).thenReturn(true);
        when(studentRepository.existsByGuardianIdAndFirstNameAndLastName(
                any(), anyString(), anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        String csv = "guardianId,firstName,lastName,grade\n"
                + guardianId + ",Valida,Fila,2C\n"         // fila 2 — válida
                + "not-a-uuid,Inválida,Fila,3A\n"           // fila 3 — UUID inválido
                + guardianId + ",,SinNombre,1A\n";          // fila 4 — firstName vacío

        BulkLoadResult result = service.bulkLoad(toStream(csv), institutionId);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors()).extracting(BulkLoadResult.RowError::rowNumber)
                .containsExactly(3, 4);
    }

    @Test
    void bulkLoad_csvWithInsufficientColumns_reportsError() {
        String csv = "guardianId,firstName,lastName\n"
                + "solo-una-columna\n";

        BulkLoadResult result = service.bulkLoad(toStream(csv), institutionId);

        assertThat(result.successCount()).isEqualTo(0);
        assertThat(result.errors()).hasSize(1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}

package com.eams.institutions.application;

import com.eams.institutions.application.dto.CreateInstitutionRequest;
import com.eams.institutions.application.dto.InstitutionResponse;
import com.eams.institutions.application.dto.UpdateInstitutionRequest;
import com.eams.institutions.domain.Institution;
import com.eams.institutions.domain.InstitutionRepository;
import com.eams.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class InstitutionServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    private InstitutionService institutionService;

    @BeforeEach
    void setUp() {
        institutionService = new InstitutionService(institutionRepository);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_newDomain_returnsCreatedInstitution() {
        when(institutionRepository.existsByEmailDomain("colegio.edu.co")).thenReturn(false);
        when(institutionRepository.save(any(Institution.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InstitutionResponse response = institutionService.create(
                new CreateInstitutionRequest("Colegio Nacional", "colegio.edu.co"));

        assertThat(response.name()).isEqualTo("Colegio Nacional");
        assertThat(response.emailDomain()).isEqualTo("colegio.edu.co");
        verify(institutionRepository).save(any(Institution.class));
    }

    @Test
    void create_duplicateDomain_throwsConflict() {
        when(institutionRepository.existsByEmailDomain("colegio.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> institutionService.create(
                new CreateInstitutionRequest("Otro Colegio", "colegio.edu.co")))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("EMAIL_DOMAIN_TAKEN");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        verify(institutionRepository, never()).save(any());
    }

    @Test
    void create_normalizesEmailDomainToLowerCase() {
        when(institutionRepository.existsByEmailDomain("colegio.edu.co")).thenReturn(false);
        when(institutionRepository.save(any(Institution.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InstitutionResponse response = institutionService.create(
                new CreateInstitutionRequest("Colegio", "COLEGIO.EDU.CO"));

        assertThat(response.emailDomain()).isEqualTo("colegio.edu.co");
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_existingInstitution_updatesFields() {
        Institution existing = Institution.create("Colegio Viejo", "viejo.edu.co");
        UUID id = UUID.randomUUID();

        when(institutionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(institutionRepository.existsByEmailDomain("nuevo.edu.co")).thenReturn(false);
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InstitutionResponse response = institutionService.update(id,
                new UpdateInstitutionRequest("Colegio Nuevo", "nuevo.edu.co"));

        assertThat(response.name()).isEqualTo("Colegio Nuevo");
        assertThat(response.emailDomain()).isEqualTo("nuevo.edu.co");
    }

    @Test
    void update_notFound_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(institutionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> institutionService.update(id,
                new UpdateInstitutionRequest("Nombre", null)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void update_duplicateEmailDomain_throwsConflict() {
        Institution existing = Institution.create("Colegio A", "a.edu.co");
        UUID id = UUID.randomUUID();

        when(institutionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(institutionRepository.existsByEmailDomain("b.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> institutionService.update(id,
                new UpdateInstitutionRequest(null, "b.edu.co")))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo("EMAIL_DOMAIN_TAKEN"));
    }

    @Test
    void update_sameDomain_doesNotCheckDuplicate() {
        Institution existing = Institution.create("Colegio A", "a.edu.co");
        UUID id = UUID.randomUUID();

        when(institutionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(institutionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Mismo dominio, solo actualiza el nombre — no debe lanzar EMAIL_DOMAIN_TAKEN
        assertThatCode(() -> institutionService.update(id,
                new UpdateInstitutionRequest("Nuevo Nombre", "a.edu.co")))
                .doesNotThrowAnyException();

        verify(institutionRepository, never()).existsByEmailDomain("a.edu.co");
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_existing_returnsResponse() {
        Institution inst = Institution.create("Colegio", "col.edu.co");
        UUID id = UUID.randomUUID();
        when(institutionRepository.findById(id)).thenReturn(Optional.of(inst));

        InstitutionResponse response = institutionService.findById(id);

        assertThat(response.name()).isEqualTo("Colegio");
        assertThat(response.emailDomain()).isEqualTo("col.edu.co");
    }

    @Test
    void findById_notFound_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(institutionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> institutionService.findById(id))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllInstitutions() {
        when(institutionRepository.findAll()).thenReturn(List.of(
                Institution.create("Inst A", "a.edu.co"),
                Institution.create("Inst B", "b.edu.co")
        ));

        List<InstitutionResponse> result = institutionService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InstitutionResponse::name)
                .containsExactlyInAnyOrder("Inst A", "Inst B");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(institutionRepository.findAll()).thenReturn(List.of());

        assertThat(institutionService.findAll()).isEmpty();
    }
}

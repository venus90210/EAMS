package com.eams.institutions.application;

import com.eams.institutions.domain.Institution;
import com.eams.institutions.domain.InstitutionRepository;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class InstitutionContextProviderTest {

    @Mock
    private InstitutionRepository institutionRepository;

    private InstitutionContextProvider provider;

    private final UUID institutionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        provider = new InstitutionContextProvider(institutionRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ── requireCurrentInstitution ────────────────────────────────────────────

    @Test
    void requireCurrentInstitution_validContext_returnsInstitution() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Institution inst = Institution.create("Colegio", "col.edu.co");
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(inst));

        Institution result = provider.requireCurrentInstitution();

        assertThat(result.getName()).isEqualTo("Colegio");
    }

    @Test
    void requireCurrentInstitution_noContext_throwsForbidden() {
        // TenantContextHolder vacío
        assertThatThrownBy(() -> provider.requireCurrentInstitution())
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void requireCurrentInstitution_institutionNotInDb_throwsNotFound() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.requireCurrentInstitution())
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── assertAccessTo ────────────────────────────────────────────────────────

    @Test
    void assertAccessTo_sameInstitution_doesNotThrow() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));

        assertThatCode(() -> provider.assertAccessTo(institutionId))
                .doesNotThrowAnyException();
    }

    @Test
    void assertAccessTo_superAdmin_canAccessAnyInstitution() {
        UUID otherId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(null, "SUPERADMIN"));

        assertThatCode(() -> provider.assertAccessTo(otherId))
                .doesNotThrowAnyException();
    }

    @Test
    void assertAccessTo_differentInstitution_throwsForbidden() {
        UUID otherId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));

        assertThatThrownBy(() -> provider.assertAccessTo(otherId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("INSTITUTION_MISMATCH");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }
}

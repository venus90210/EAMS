package com.eams.institutions.application;

import com.eams.institutions.application.dto.CreateInstitutionRequest;
import com.eams.institutions.application.dto.InstitutionResponse;
import com.eams.institutions.application.dto.UpdateInstitutionRequest;
import com.eams.institutions.domain.Institution;
import com.eams.institutions.domain.InstitutionRepository;
import com.eams.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso del módulo Instituciones (AD-08).
 *
 * Restricciones de acceso:
 *   - create / update: solo SUPERADMIN (validado en el controlador vía TenantContext)
 *   - findById / findAll: cualquier rol autenticado
 *
 * Invariantes de dominio:
 *   - emailDomain debe ser único en toda la plataforma (constraint DB + validación en capa application)
 */
@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    // ── Crear ────────────────────────────────────────────────────────────────

    /**
     * Crea una nueva institución educativa.
     *
     * @throws DomainException EMAIL_DOMAIN_TAKEN si el dominio ya está registrado
     */
    public InstitutionResponse create(CreateInstitutionRequest request) {
        if (institutionRepository.existsByEmailDomain(request.emailDomain().toLowerCase())) {
            throw DomainException.conflict("EMAIL_DOMAIN_TAKEN",
                    "El dominio '%s' ya está registrado".formatted(request.emailDomain()));
        }

        Institution saved = institutionRepository.save(
                Institution.create(request.name(), request.emailDomain()));

        return InstitutionResponse.from(saved);
    }

    // ── Actualizar ───────────────────────────────────────────────────────────

    /**
     * Actualiza nombre y/o dominio de correo de una institución.
     *
     * @throws DomainException NOT_FOUND si el id no existe
     * @throws DomainException EMAIL_DOMAIN_TAKEN si el nuevo dominio ya está tomado
     */
    public InstitutionResponse update(UUID id, UpdateInstitutionRequest request) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> DomainException.notFound("Institución no encontrada: " + id));

        if (request.emailDomain() != null && !request.emailDomain().isBlank()) {
            String newDomain = request.emailDomain().toLowerCase();
            if (!newDomain.equals(institution.getEmailDomain())
                    && institutionRepository.existsByEmailDomain(newDomain)) {
                throw DomainException.conflict("EMAIL_DOMAIN_TAKEN",
                        "El dominio '%s' ya está registrado".formatted(request.emailDomain()));
            }
        }

        institution.update(request.name(), request.emailDomain());
        return InstitutionResponse.from(institutionRepository.save(institution));
    }

    // ── Consultar ────────────────────────────────────────────────────────────

    public InstitutionResponse findById(UUID id) {
        return institutionRepository.findById(id)
                .map(InstitutionResponse::from)
                .orElseThrow(() -> DomainException.notFound("Institución no encontrada: " + id));
    }

    public List<InstitutionResponse> findAll() {
        return institutionRepository.findAll().stream()
                .map(InstitutionResponse::from)
                .toList();
    }
}

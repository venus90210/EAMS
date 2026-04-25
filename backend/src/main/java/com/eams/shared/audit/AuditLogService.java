package com.eams.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio para generar entradas de auditoría.
 * Responsable de serializar cambios a JSON y persistir en audit_log.
 *
 * Contrato: si la auditoría falla, se logea el error pero NO interrumpe el flujo principal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registra un cambio en la auditoría.
     *
     * @param tableName   nombre de la tabla (ej: "activities")
     * @param recordId    UUID del registro modificado
     * @param action      "INSERT", "UPDATE", o "DELETE"
     * @param oldValue    objeto con estado anterior (nullable)
     * @param newValue    objeto con estado nuevo
     * @param institutionId UUID de la institución (no nullable)
     */
    public void log(
            String tableName,
            UUID recordId,
            String action,
            Object oldValue,
            Object newValue,
            UUID institutionId) {

        try {
            // Serializar a JSON
            String oldValueJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newValueJson = objectMapper.writeValueAsString(newValue);

            // Crear y persistir entrada
            AuditLog entry = AuditLog.of(
                    tableName,
                    recordId,
                    action,
                    oldValueJson,
                    newValueJson,
                    null, // performedBy: no disponible en TenantContext actualmente
                    institutionId
            );

            auditLogRepository.save(entry);
            log.debug("Auditoría registrada: {}:{}#{}", tableName, recordId, action);

        } catch (Exception e) {
            // No interrumpir flujo principal si auditoría falla
            log.error("Error registrando auditoría de {}", tableName, e);
        }
    }
}

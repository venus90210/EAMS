package com.eams.shared.audit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de auditoría para cambios críticos.
 * Mapea tabla: audit_log (AD-07, RNF06 Ley 1581/2012)
 *
 * Rastrea: cambios en total_spots, estados de actividad, inscripciones
 */
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_table_record", columnList = "table_name, record_id"),
    @Index(name = "idx_audit_performed_at", columnList = "performed_at DESC"),
    @Index(name = "idx_audit_institution", columnList = "institution_id")
})
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @Column(name = "action", nullable = false, length = 20)
    private String action; // INSERT, UPDATE, DELETE

    @Column(name = "old_value", columnDefinition = "text", nullable = true)
    private String oldValue; // JSON serializado como texto

    @Column(name = "new_value", columnDefinition = "text", nullable = true)
    private String newValue; // JSON serializado como texto

    @Column(name = "performed_by")
    private UUID performedBy; // FK users.id — nullable

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId; // FK institutions.id

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    /**
     * Factory method para crear entrada de auditoría.
     *
     * @param tableName   nombre de tabla (ej: "activities")
     * @param recordId    UUID del registro modificado
     * @param action      INSERT, UPDATE, DELETE
     * @param oldValue    JSON del estado anterior (nullable)
     * @param newValue    JSON del estado nuevo
     * @param performedBy UUID del usuario que hizo el cambio (nullable)
     * @param institutionId UUID de la institución
     * @return AuditLog entity
     */
    public static AuditLog of(
            String tableName,
            UUID recordId,
            String action,
            String oldValue,
            String newValue,
            UUID performedBy,
            UUID institutionId) {
        return AuditLog.builder()
                .tableName(tableName)
                .recordId(recordId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(performedBy)
                .institutionId(institutionId)
                .build();
    }
}

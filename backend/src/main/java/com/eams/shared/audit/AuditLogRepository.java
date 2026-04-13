package com.eams.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository para auditoría. Operaciones de lectura/escritura en tabla audit_log.
 * Inicialmente solo soporta inserción vía save().
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // Queries personalizadas pueden agregarse aquí en futuro
    // ej: List<AuditLog> findByTableNameAndRecordId(String tableName, UUID recordId)
}

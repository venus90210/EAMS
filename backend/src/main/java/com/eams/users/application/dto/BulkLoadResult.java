package com.eams.users.application.dto;

import java.util.List;

/**
 * Resultado de la carga masiva desde CSV.
 * Contiene el número de registros procesados exitosamente y los errores por fila.
 */
public record BulkLoadResult(
        int totalRows,
        int successCount,
        List<RowError> errors
) {
    public record RowError(int rowNumber, String reason) {}

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

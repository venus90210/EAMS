package com.eams.attendance.domain;

/**
 * Puerto para política de ventana de edición de registros de asistencia (RF13).
 *
 * Define si un registro dentro de una sesión puede ser editado basado en el tiempo
 * transcurrido desde la apertura de la sesión.
 */
public interface EditWindowPolicy {

    /**
     * Verifica si una sesión está dentro de su ventana de edición (24h).
     *
     * @param session sesión a verificar
     * @return true si aún es editable, false si la ventana expiró
     */
    boolean isEditable(AttendanceSession session);

    /**
     * Retorna el código de error cuando la ventana expira.
     */
    default String getExpiredErrorCode() {
        return "EDIT_WINDOW_EXPIRED";
    }
}

package com.eams.attendance.infrastructure.policy;

import com.eams.attendance.domain.AttendanceSession;
import com.eams.attendance.domain.EditWindowPolicy;
import org.springframework.stereotype.Component;

/**
 * Implementación de política de ventana de edición de 24 horas (RF13).
 *
 * Una sesión es editable si menos de 24 horas han transcurrido desde su apertura.
 */
@Component
public class TwentyFourHourEditWindowPolicy implements EditWindowPolicy {

    @Override
    public boolean isEditable(AttendanceSession session) {
        return session.isEditable();
    }
}

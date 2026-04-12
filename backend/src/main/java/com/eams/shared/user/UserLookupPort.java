package com.eams.shared.user;

import java.util.UUID;

/**
 * Puerto compartido para verificar la existencia de un usuario.
 *
 * Permite que módulos distintos a 'auth' (e.g., 'users') validen
 * que un guardianId existe, sin depender de los tipos internos de auth.domain.
 *
 * La implementación concreta vive en auth.infrastructure.persistence (AD-03).
 */
public interface UserLookupPort {

    /**
     * Retorna true si existe un usuario con el id dado.
     */
    boolean existsById(UUID userId);
}

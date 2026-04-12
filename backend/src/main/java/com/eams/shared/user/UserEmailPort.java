package com.eams.shared.user;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto compartido para consultar el email de un usuario.
 *
 * Permite que módulos como 'notifications' obtengan el email de un usuario
 * (ej. para enviar emails a acudientes) sin depender de los tipos internos de 'auth'.
 *
 * La implementación concreta vive en auth.infrastructure.persistence (AD-03).
 */
public interface UserEmailPort {

    /**
     * Retorna el email de un usuario dado su ID.
     *
     * @param userId ID del usuario
     * @return Optional con el email, o empty si el usuario no existe
     */
    Optional<String> findEmailById(UUID userId);
}

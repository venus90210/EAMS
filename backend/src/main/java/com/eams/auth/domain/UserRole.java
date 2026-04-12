package com.eams.auth.domain;

/**
 * Roles de usuario de la plataforma.
 * El rol determina los permisos de acceso (RBAC — AD-04).
 * MFA obligatorio para TEACHER, ADMIN y SUPERADMIN (RNF04, AD-06).
 */
public enum UserRole {
    GUARDIAN,
    TEACHER,
    ADMIN,
    SUPERADMIN;

    /** Roles que requieren MFA obligatorio (privilegios de escritura). */
    public boolean requiresMfa() {
        return this == TEACHER || this == ADMIN || this == SUPERADMIN;
    }
}

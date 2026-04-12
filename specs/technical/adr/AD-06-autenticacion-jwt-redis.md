# AD-06 — Autenticación con JWT + Refresh Tokens almacenados en Redis

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RNF04, RNF06, RF08, RF09       |

---

## Contexto

La plataforma maneja datos personales de menores de edad (Ley 1581 de 2012). Se requiere un mecanismo de autenticación que:

1. Sea robusto ante compromiso de credenciales.
2. Permita **revocar sesiones activas** de inmediato sin mantener estado en el servidor de aplicaciones.
3. No introduzca latencia adicional en cada petición al consultar la base de datos.
4. Soporte **MFA obligatorio** para cuentas con privilegios de escritura.

Las sesiones en base de datos tradicionales (server-side sessions) cumplen la revocación pero añaden una consulta a BD en cada petición, afectando el RNF09 (<3s en el 95% de transacciones).

## Decisión

Se implementa el esquema **JWT de corta duración + Refresh Token revocable en Redis**:

### Flujo de tokens

```
Login exitoso
    │
    ├── Access Token JWT (15 min)
    │     - Firmado con clave privada
    │     - Payload: { sub, role, institutionId, iat, exp }
    │     - Validado en el API Gateway SIN consultar BD
    │
    └── Refresh Token (7 días)
          - UUID opaco (no JWT)
          - Almacenado en Redis con TTL de 7 días
          - Revocable al instante (DELETE en Redis)
```

### Política de MFA

| Rol         | MFA requerido | Método   |
|-------------|---------------|----------|
| GUARDIAN    | No            | —        |
| TEACHER     | Sí            | TOTP     |
| ADMIN       | Sí            | TOTP     |
| SUPERADMIN  | Sí            | TOTP     |

### Escenarios de revocación

| Evento                        | Acción                                      |
|-------------------------------|---------------------------------------------|
| Logout del usuario            | DELETE refresh token en Redis               |
| Compromiso de cuenta          | DELETE todos los refresh tokens del usuario |
| Cambio de contraseña          | DELETE todos los refresh tokens del usuario |
| Expiración natural            | Redis TTL elimina el token automáticamente  |

## Justificación

- El API Gateway valida el JWT sin consultar BD, cumpliendo el requisito de baja latencia (RNF09).
- El refresh token en Redis permite revocación inmediata sin estado en el servidor de aplicaciones, cumpliendo RNF06 (protección de datos de menores).
- MFA con TOTP para roles de escritura cumple RNF04 (autenticación robusta).
- El access token de 15 minutos limita la ventana de exposición ante un token comprometido.

## Consecuencias

- **Positivas**: revocación inmediata, validación sin BD, MFA centralizado, baja latencia.
- **Negativas**: si Redis no está disponible, la renovación de tokens falla.
- **Mitigación**: Redis configurado con persistencia AOF y réplica; si cae, los access tokens existentes siguen válidos por sus 15 minutos restantes.

## Alternativas descartadas

- **Sesiones en base de datos (server-side sessions)**: descartadas por la consulta adicional a BD en cada petición y la complejidad de escalar sesiones entre instancias.
- **JWT de larga duración sin refresh token**: descartado porque no permite revocación; un token comprometido seguiría siendo válido hasta su expiración natural.
- **OAuth2 / OIDC con proveedor externo**: evaluado pero descartado para el alcance actual del proyecto por complejidad de integración y dependencia de un servicio externo.

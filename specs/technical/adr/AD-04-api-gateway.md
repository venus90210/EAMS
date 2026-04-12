# AD-04 — API Gateway como único perímetro de seguridad

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RNF04, RNF05, RNF06            |

---

## Contexto

Sin un punto de entrada centralizado, cada módulo del backend debería gestionar su propia autenticación y autorización. Esto genera:

- Lógica de seguridad duplicada en múltiples módulos.
- Un cambio en la política de roles obliga a modificar varios puntos del sistema.
- Mayor superficie de ataque: cualquier endpoint expuesto puede recibir peticiones no autenticadas.

## Decisión

Se implementa un **API Gateway en NestJS** como único interceptor de todas las peticiones externas.

### Responsabilidades del Gateway

| Función           | Detalle                                                          |
|-------------------|------------------------------------------------------------------|
| Validación JWT    | Verifica firma y expiración del access token                     |
| RBAC              | Comprueba que el rol del token tenga permiso para el endpoint    |
| Rate limiting     | Límite por IP y por usuario para prevenir abuso (RNF09)          |
| CORS              | Whitelist de orígenes permitidos                                 |
| Enrutamiento      | Solo reenvía al backend peticiones ya autenticadas y autorizadas |

### Flujo de una petición

```
Cliente (PWA)
    │
    ▼
API Gateway (NestJS)
    ├── 1. Extrae Bearer Token del header Authorization
    ├── 2. Valida firma JWT (sin consultar BD)
    ├── 3. Extrae rol e institution_id del payload
    ├── 4. Verifica permiso RBAC para el endpoint solicitado
    ├── 5. Aplica rate limiting
    └── 6. Enruta al Backend si todo es válido
              │
              ▼
         Backend (Spring Boot)
         — Solo procesa peticiones ya autenticadas
```

### Política de roles por módulo

| Endpoint                        | GUARDIAN | TEACHER | ADMIN | SUPERADMIN |
|---------------------------------|----------|---------|-------|------------|
| POST /enrollments               | ✓        | -       | ✓     | ✓          |
| POST /attendance/sessions       | -        | ✓       | ✓     | ✓          |
| POST /activities                | -        | ✓       | ✓     | ✓          |
| PATCH /activities/:id/status    | -        | -       | ✓     | ✓          |
| POST /institutions              | -        | -       | -     | ✓          |

## Justificación

- La política de seguridad queda centralizada en un único lugar.
- El backend solo procesa peticiones ya autenticadas, reduciendo su complejidad.
- El Gateway puede escalar o reemplazarse independientemente del backend.
- Cumple RNF04 (MFA y cifrado), RNF05 (RBAC) y RNF06 (protección de datos Ley 1581).

## Consecuencias

- **Positivas**: seguridad centralizada, backend simplificado, superficie de ataque reducida.
- **Negativas**: el Gateway se convierte en un punto crítico; requiere alta disponibilidad.
- **Mitigación**: el Gateway es stateless (valida JWT sin consultar BD), por lo que es fácilmente escalable horizontalmente con múltiples instancias detrás de un load balancer.

## Alternativas descartadas

- **Seguridad distribuida en cada módulo**: descartada por duplicación de lógica y mayor riesgo de inconsistencias entre módulos.
- **Spring Security directamente en el backend sin Gateway**: descartada porque expone la superficie del backend directamente a internet y complica el mantenimiento del control de acceso.

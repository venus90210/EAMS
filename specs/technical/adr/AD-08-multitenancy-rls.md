# AD-08 — Multi-tenancy por institution_id con Row-Level Security

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RNF09, RNF06                   |

---

## Contexto

El sistema debe soportar múltiples instituciones en la misma base de datos (RNF09: mínimo 5 instituciones simultáneas). Las opciones clásicas de multi-tenancy son:

| Estrategia              | Aislamiento | Costo operativo | Complejidad |
|-------------------------|-------------|-----------------|-------------|
| BD separada por tenant  | Total       | Alto (N DBs)    | Alta        |
| Schema separado         | Alto        | Medio           | Media       |
| Tabla compartida + RLS  | Alto        | **Bajo (1 BD)** | **Baja**    |

La Ley 1581 de 2012 exige que los datos personales de menores no se expongan entre instituciones, incluso ante errores en la capa de aplicación.

## Decisión

Se implementa **multi-tenancy por campo `institution_id`** en todas las tablas, reforzado con **Row-Level Security (RLS) en PostgreSQL**.

### Estructura en base de datos

Todas las tablas de dominio incluyen `institution_id` como campo obligatorio:

```sql
-- Ejemplo: tabla activities
CREATE TABLE activities (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id UUID NOT NULL REFERENCES institutions(id),
    name          VARCHAR(100) NOT NULL,
    status        activity_status NOT NULL DEFAULT 'DRAFT',
    -- ...
);

-- Política RLS: una sesión solo lee filas de su institución
ALTER TABLE activities ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON activities
    USING (institution_id = current_setting('app.current_institution_id')::UUID);
```

### Flujo de configuración del contexto de tenant

```
API Gateway extrae institution_id del JWT
    │
    ▼
Backend establece: SET LOCAL app.current_institution_id = '<id>'
    │
    ▼
PostgreSQL RLS filtra automáticamente todas las queries de la sesión
    │
    ▼
Incluso si el desarrollador olvida el WHERE institution_id = ...,
la BD impide físicamente acceder a datos de otra institución
```

### Caso especial: Superadmin

- El campo `institution_id` es `NULL` para el Superadmin.
- El API Gateway aplica una política especial: el Superadmin puede establecer el contexto de cualquier institución para operaciones de administración.

## Justificación

- **Doble capa de aislamiento**: aplicación (filtro en queries) + base de datos (RLS).
- **Cumplimiento Ley 1581**: incluso ante un bug en la aplicación que omita el filtro, la BD impide el acceso cruzado.
- **Costo operativo bajo**: una sola instancia de base de datos para todas las instituciones.
- **Transparente para el desarrollador**: Spring Data JPA aplica el contexto de tenant automáticamente a través del `TenantContextHolder`.

## Consecuencias

- **Positivas**: aislamiento garantizado a nivel de BD, un solo costo operativo, cumplimiento legal reforzado.
- **Negativas**: las políticas RLS añaden complejidad a las migraciones de base de datos; cada nueva tabla debe incluir la política explícitamente.
- **Mitigación**: se incluye una prueba de integración que verifica el aislamiento RLS para cada tabla del dominio, ejecutada en el pipeline de CI.

## Alternativas descartadas

- **Base de datos separada por institución**: descartada por alto costo operativo (N instancias de PostgreSQL, N procesos de backup, N migraciones a sincronizar).
- **Schema separado por institución**: descartada por complejidad de gestión de migraciones y conexiones en un pool compartido.
- **Filtro solo en capa de aplicación**: descartada porque un bug en una query podría exponer datos de otra institución, violando la Ley 1581.

# AD-07 — Control de cupos con bloqueo pesimista (SELECT FOR UPDATE)

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RF04, RF05, RNF09              |

---

## Contexto

Múltiples padres pueden intentar inscribir a sus hijos en la misma actividad al mismo tiempo. Sin sincronización, se produce una **condición de carrera (race condition)**:

```
Transacción A                    Transacción B
──────────────                   ──────────────
Lee available_spots = 1          Lee available_spots = 1
Decrementa → 0                   Decrementa → 0
COMMIT                           COMMIT  ← sobrecupo: -1 en realidad
```

Ambas transacciones leen el mismo valor, decrementan y hacen commit, generando un cupo negativo (sobrecupo). El RF05 exige 0% de registros duplicados válidos y el RF04 exige disponibilidad en menos de 1 segundo.

## Decisión

Se usa **bloqueo pesimista nativo de PostgreSQL** (`SELECT ... FOR UPDATE`) dentro de una transacción `@Transactional` en el Módulo de Inscripciones.

### Flujo transaccional del proceso de inscripción

```sql
BEGIN;

-- 1. Bloquea la fila del cupo hasta el commit
SELECT available_spots
FROM activities
WHERE id = $activityId
FOR UPDATE;

-- 2. Si available_spots <= 0 → lanza error 409 (SPOT_EXHAUSTED)

-- 3. Valida inscripción duplicada
SELECT 1 FROM enrollments
WHERE student_id = $studentId AND status = 'ACTIVE';
-- Si existe → lanza error 409 (ALREADY_ENROLLED o ACTIVE_ENROLLMENT_EXISTS)

-- 4. Crea el enrollment
INSERT INTO enrollments (student_id, activity_id, status) VALUES (...);

-- 5. Decrementa el cupo
UPDATE activities
SET available_spots = available_spots - 1
WHERE id = $activityId;

COMMIT;
-- La fila queda desbloqueada; la siguiente petición en cola puede proceder
```

### Comportamiento bajo concurrencia

```
Petición A (llega primero)    Petición B (llega 10ms después)
──────────────────────────    ────────────────────────────────
SELECT ... FOR UPDATE         Espera en cola (BD level)
available_spots = 1 ✓
INSERT enrollment A
UPDATE spots → 0
COMMIT ✓                      Desbloqueo — continúa
                              SELECT ... FOR UPDATE
                              available_spots = 0
                              → Error 409 SPOT_EXHAUSTED ✓
```

## Justificación

- **Consistencia absoluta**: imposible el sobrecupo, cumple RF05 (0% duplicados).
- **Sin infraestructura adicional**: no requiere Redis Lua scripts ni locks distribuidos.
- **Latencia mínima**: para la carga proyectada (5.000 usuarios, no miles concurrentes sobre un mismo cupo), la espera en cola es de milisegundos.
- **Nativo de PostgreSQL**: soporte ACID garantizado, sin dependencias adicionales.

## Consecuencias

- **Positivas**: consistencia absoluta sin complejidad adicional de infraestructura.
- **Negativas**: las peticiones concurrentes sobre el mismo cupo se serializan; bajo carga extrema puede aumentar la latencia.
- **Mitigación**: para la escala proyectada (5.000 usuarios distribuidos entre 5 instituciones), la probabilidad de alta concurrencia sobre un único cupo es baja. Si la carga creciera significativamente, se puede migrar a un contador atómico en Redis sin cambiar la interfaz del módulo.

## Alternativas descartadas

- **Bloqueo optimista (OCC con version field)**: descartado porque genera reintentos en el cliente ante conflictos, complicando el manejo de errores y la experiencia de usuario.
- **Contador atómico en Redis (DECR)**: descartado para el alcance actual por introducir complejidad adicional (sincronización Redis ↔ PostgreSQL, scripts Lua para atomicidad). Queda como ruta de migración si la carga lo exige.
- **Sin control de concurrencia**: descartado — viola directamente RF05 (0% duplicados) y genera sobrecupos.

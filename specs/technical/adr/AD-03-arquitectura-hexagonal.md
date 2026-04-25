# AD-03 — Arquitectura Hexagonal (Ports & Adapters) dentro de cada módulo

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RF04, RF05, RF06               |

---

## Contexto

Si la lógica de negocio depende directamente del framework (Spring), la base de datos (JPA) o el mecanismo de notificación, cambiar cualquiera de esas tecnologías obliga a tocar el núcleo del dominio.

Las reglas críticas del sistema —control de cupos, validación de duplicados, detección de conflictos de horario— no deben saber cómo se persisten los datos ni cómo se envían los correos.

## Decisión

Dentro de cada módulo se aplica el patrón **Ports & Adapters** (Arquitectura Hexagonal):

```
┌─────────────────────────────────────────────────┐
│                   MÓDULO                        │
│                                                 │
│  ┌─────────────┐    ┌──────────────────────┐   │
│  │   Adaptador │───▶│  Puerto de entrada   │   │
│  │  (HTTP REST)│    │  (Caso de uso / API) │   │
│  └─────────────┘    └──────────┬───────────┘   │
│                                │               │
│                    ┌───────────▼───────────┐   │
│                    │   NÚCLEO DE DOMINIO   │   │
│                    │  (Entidades + Reglas) │   │
│                    └───────────┬───────────┘   │
│                                │               │
│  ┌─────────────┐    ┌──────────▼───────────┐   │
│  │  Adaptador  │◀───│  Puerto de salida    │   │
│  │ (JPA / SMTP)│    │  (Repositorio / Cola)│   │
│  └─────────────┘    └──────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### Regla fundamental

> El núcleo de dominio **solo depende de interfaces (puertos)**, nunca de implementaciones concretas.
> Spring gestiona la inyección de los adaptadores en tiempo de ejecución.

### Ejemplo — Módulo Inscripciones

| Puerto de entrada         | Puerto de salida                        |
|---------------------------|-----------------------------------------|
| `enroll(studentId, actId)`| `EnrollmentRepository` (SELECT FOR UPDATE) |
| `cancelEnrollment(id)`    | `ActivityService` (decrementa cupos)    |
| `getEnrollmentsByStudent` | `UserService` (valida relación padre)   |
|                           | `EventPublisher` (EnrollmentConfirmed)  |

## Justificación

- El núcleo de dominio puede probarse en memoria sin levantar base de datos ni framework.
- Cambiar PostgreSQL por otro motor, o SMTP por una API de email, solo requiere reemplazar el adaptador.
- Las reglas más críticas (RF04, RF05, RF06) quedan aisladas y testeables de forma determinista.

## Consecuencias

- **Positivas**: máxima testeabilidad del dominio, bajo acoplamiento tecnológico, facilidad para cambiar adaptadores.
- **Negativas**: más archivos por módulo (interface + implementación para cada puerto de salida).
- **Mitigación**: el número de interfaces se mantiene acotado: cada módulo tiene entre 2 y 5 puertos de salida bien definidos.

## Alternativas descartadas

- **Arquitectura en capas clásica (Controller → Service → Repository)**: descartada porque el Service accede directamente a la implementación del Repository, acoplando el dominio a la tecnología de persistencia.

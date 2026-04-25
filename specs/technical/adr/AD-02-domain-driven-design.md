# AD-02 — Domain-Driven Design (DDD) para estructurar los módulos

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RF01, RF03, RF08               |

---

## Contexto

Sin una estrategia de organización del código, los módulos tienden a acoplarse entre sí:

- El módulo de Inscripciones accede directamente a la tabla de Usuarios.
- El módulo de Asistencia mezcla lógica de Actividades.
- Se generan dependencias circulares que hacen imposibles las pruebas unitarias aisladas.

Este patrón produce el "código espagueti" que la plataforma debe evitar desde su diseño inicial.

## Decisión

Cada módulo del backend es un **Bounded Context de DDD**:

- Tiene su propio modelo de dominio, entidades y reglas de negocio.
- Solo expone lo que otros módulos necesitan a través de **interfaces explícitas** (puertos).
- Ningún módulo accede directamente a la base de datos de otro.
- **Spring Modulith** hace cumplir estas fronteras en tiempo de compilación y pruebas.

### Bounded Contexts definidos

| Módulo            | Responsabilidad principal                              |
|-------------------|--------------------------------------------------------|
| Auth & Security   | Validación JWT, MFA, RBAC                              |
| Usuarios          | Ciclo de vida de usuarios y asociaciones padre-hijo    |
| Instituciones     | Catálogo de instituciones y contexto de tenant         |
| Actividades       | Catálogo y ciclo de vida de actividades extracurriculares |
| Inscripciones     | Asignación y liberación transaccional de cupos         |
| Asistencia        | Registro de sesiones y asistencia individual           |
| Notificaciones    | Bus de eventos y despacho asíncrono de emails          |

## Justificación

- Evita acoplamiento cruzado entre módulos.
- Cada Bounded Context puede probarse de forma aislada.
- Facilita la extracción futura de un módulo como microservicio (AD-01, patrón Strangler Fig).
- Alinea el código con el lenguaje del negocio (ubiquitous language).

## Consecuencias

- **Positivas**: bajo acoplamiento, alta cohesión, pruebas unitarias aisladas por módulo.
- **Negativas**: requiere disciplina del equipo para respetar las fronteras de dominio.
- **Mitigación**: Spring Modulith verifica las fronteras automáticamente en el ciclo de pruebas, generando fallos de compilación ante violaciones.

## Alternativas descartadas

- **Organización por capa técnica** (controllers/, services/, repositories/): descartada porque agrupa código de dominios diferentes en la misma carpeta, favoreciendo el acoplamiento cruzado a medida que crece el sistema.

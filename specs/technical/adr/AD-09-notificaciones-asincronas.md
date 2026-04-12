# AD-09 — Notificaciones asíncronas desacopladas — canal único: email

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RF07, RNF09                    |

---

## Contexto

RF07 exige que el acudiente reciba confirmación de inscripción en **menos de 60 segundos**. RNF09 exige que el 95% de las transacciones respondan en menos de 3 segundos.

Si el envío de email ocurre **dentro de la misma transacción de inscripción**, surgen dos problemas:

1. **Acoplamiento de fallo**: un error del servidor de correo revierte una inscripción exitosa.
2. **Latencia visible**: la llamada SMTP puede tomar entre 500ms y varios segundos, degradando el tiempo de respuesta percibido por el usuario.

## Decisión

El Módulo de Notificaciones actúa como **bus de eventos interno**. Los módulos de dominio publican eventos (`ApplicationEvent`), y un Worker independiente los consume y despacha por email.

### Flujo de notificación

```
Módulo Inscripciones
    │
    ├── COMMIT transacción (inscripción + cupo) ✓
    │
    └── Publica evento: EnrollmentConfirmed { enrollmentId, guardianEmail, ... }
              │
              ▼
        Módulo Notificaciones
        (ApplicationEvent — desacoplado de la transacción principal)
              │
              ▼
         Cola en Redis (BullMQ)
              │
              ▼
         Worker de Notificaciones
         (procesa de forma asíncrona)
              │
              ▼
         Envío SMTP / API Email
         (< 60 segundos desde el evento — RF07)
```

### Eventos del sistema

| Evento                  | Emisor         | Destinatario      |
|-------------------------|----------------|-------------------|
| `EnrollmentConfirmed`   | Inscripciones  | Padre/Acudiente   |
| `SpotExhausted`         | Inscripciones  | Padre/Acudiente   |
| `ObservationPublished`  | Asistencia     | Padre/Acudiente   |
| `ActivityStatusChanged` | Actividades    | Padres afectados  |
| `NewStudentEnrolled`    | Inscripciones  | Docente           |

### Canal único: email

Se elige **únicamente email** como canal de notificación para:
- Simplificar la implementación y evitar dependencias de WhatsApp Business API, SMS o push notifications.
- Reducir costos operativos.
- Mantener el alcance del proyecto acotado.

### Idempotencia

Los eventos se procesan de forma idempotente: si el Worker falla y reintenta, el mismo email no se envía dos veces (usando el `enrollmentId` como clave de deduplicación en Redis).

## Justificación

- El fallo del servidor de correo **no revierte** la transacción de inscripción (desacoplamiento de fallo).
- La respuesta al usuario es inmediata (<3s), el email llega en segundo plano (<60s), cumpliendo RF07 y RNF09 simultáneamente.
- El Worker puede reintentarse sin afectar la consistencia del dominio.

## Consecuencias

- **Positivas**: desacoplamiento de fallo, latencia de respuesta reducida, reintentos seguros.
- **Negativas**: el email puede llegar con un leve retraso respecto al commit; no hay garantía de entrega si el servidor SMTP falla repetidamente.
- **Mitigación**: política de reintentos con backoff exponencial en BullMQ (3 intentos en 5 min). Alertas operativas si la cola supera N mensajes pendientes.

## Alternativas descartadas

- **Envío de email en la misma transacción**: descartado porque acopla el fallo del SMTP con la transacción de negocio y degrada el tiempo de respuesta percibido.
- **Push notifications / WhatsApp / SMS**: descartados por complejidad de integración y costos adicionales fuera del alcance del proyecto.
- **Kafka / RabbitMQ como broker**: descartados por sobredimensionar la infraestructura para la carga proyectada; Redis con BullMQ es suficiente y ya forma parte del stack.

# F5 — Administrador cambia el estado de una actividad
# Actor: Admin Institucional
# Modulos: Auth · Actividades · Redis · Notificaciones
# RF01, RF02

Feature: Gestion del ciclo de vida de una actividad extracurricular

  Background:
    Given el admin "admin@colegio.com" esta autenticado con rol "ADMIN"
    And pertenece a la institucion "inst-001"

  # ─── CREACION ───────────────────────────────────────────────────────────────

  Scenario: Creacion exitosa de actividad en borrador
    When el admin envia POST /activities con los datos validos de la actividad
    Then el sistema retorna HTTP 201
    And la actividad queda en estado "DRAFT"
    And total_spots queda registrado como inmutable en la base de datos
    And available_spots es igual a total_spots

  Scenario: Creacion de actividad con datos incompletos
    When el admin envia POST /activities sin el campo "name"
    Then el sistema retorna HTTP 400
    And el cuerpo de respuesta contiene el campo "error" con valor "VALIDATION_ERROR"
    And no se crea la actividad

  # ─── PUBLICACION ────────────────────────────────────────────────────────────

  Scenario: Publicacion exitosa de actividad en borrador
    Given existe la actividad "act-001" en estado "DRAFT"
    When el admin envia POST /activities/act-001/publish
    Then el sistema retorna HTTP 200
    And la actividad cambia a estado "PUBLISHED"
    And la actividad es visible para los acudientes

  Scenario: No se puede publicar una actividad ya publicada
    Given existe la actividad "act-001" en estado "PUBLISHED"
    When el admin intenta publicar nuevamente la actividad "act-001"
    Then el sistema retorna HTTP 409
    And el cuerpo de respuesta contiene el campo "error" con valor "INVALID_STATE_TRANSITION"

  # ─── HABILITACION / DESHABILITACION ─────────────────────────────────────────

  Scenario: Admin deshabilita una actividad publicada
    Given existe la actividad "act-001" en estado "PUBLISHED"
    And tiene 10 estudiantes inscritos
    When el admin envia PATCH /activities/act-001/status con estado "DISABLED"
    Then el sistema retorna HTTP 200
    And la actividad cambia a estado "DISABLED"
    And el cache de actividades en Redis es invalidado
    And los 10 acudientes afectados reciben notificacion por email

  Scenario: Admin habilita una actividad deshabilitada
    Given existe la actividad "act-001" en estado "DISABLED"
    When el admin envia PATCH /activities/act-001/status con estado "PUBLISHED"
    Then el sistema retorna HTTP 200
    And la actividad vuelve a estado "PUBLISHED"
    And el cache de actividades en Redis es invalidado

  Scenario: Admin de otra institucion no puede modificar la actividad
    Given el admin "admin@otro.com" pertenece a "inst-002"
    When intenta cambiar el estado de la actividad "act-001" de "inst-001"
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "INSTITUTION_MISMATCH"

  Scenario: Docente no puede cambiar el estado de una actividad
    Given el usuario "docente@colegio.com" tiene rol "TEACHER"
    When intenta enviar PATCH /activities/act-001/status
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "INSUFFICIENT_ROLE"

  # ─── MODIFICACION DE CUPOS ──────────────────────────────────────────────────

  Scenario: Admin modifica total_spots y queda registrado en audit log
    Given existe la actividad "act-001" con total_spots 20 y available_spots 15
    When el admin envia PATCH /activities/act-001 con total_spots 25
    Then el sistema retorna HTTP 200
    And total_spots es 25
    And available_spots es 20 (incremento proporcional)
    And se genera una entrada en AUDIT_LOG con old_value y new_value

  Scenario: Docente no puede modificar total_spots
    Given el usuario tiene rol "TEACHER"
    When intenta enviar PATCH /activities/act-001 con total_spots 30
    Then el sistema retorna HTTP 403

  # ─── LISTADO ────────────────────────────────────────────────────────────────

  Scenario: Acudiente solo ve actividades publicadas de su institucion
    Given el usuario "padre@ejemplo.com" tiene rol "GUARDIAN" de "inst-001"
    When envia GET /activities
    Then la respuesta contiene solo actividades con estado "PUBLISHED" de "inst-001"
    And no contiene actividades de otras instituciones
    And no contiene actividades en estado "DRAFT" ni "DISABLED"

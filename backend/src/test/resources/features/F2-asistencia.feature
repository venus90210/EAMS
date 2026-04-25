# F2 — Registro de asistencia y observacion por docente
# Actor: Docente
# Modulos: Auth · Asistencia · CalendarPort · Notificaciones
# RF12, RF13

Feature: Registro de asistencia y observacion por docente

  Background:
    Given el docente "docente@ejemplo.com" esta autenticado con rol "TEACHER"
    And esta asignado a la actividad "act-001" en la institucion "inst-001"
    And la actividad "act-001" tiene 3 estudiantes inscritos: "student-001", "student-002", "student-003"

  Scenario: Apertura exitosa de sesion de asistencia
    Given la fecha actual es la fecha de hoy
    When el docente envia POST /attendance/sessions con activityId "act-001" y la fecha actual
    Then el sistema retorna HTTP 201
    And se crea una sesion "session-001" con estado "OPEN"
    And la sesion tiene recorded_at igual a la fecha y hora actual

  Scenario: No se puede abrir sesion para una fecha diferente a hoy
    Given la fecha proporcionada es ayer
    When el docente envia POST /attendance/sessions con activityId "act-001" y la fecha de ayer
    Then el sistema retorna HTTP 422
    And el cuerpo de respuesta contiene el campo "error" con valor "INVALID_DATE"

  Scenario: Registro de asistencia en maximo 3 toques por estudiante
    Given existe la sesion abierta "session-001"
    When el docente marca asistencia para "student-001" como presente en la sesion "session-001"
    Then el sistema retorna HTTP 200
    And el registro de "student-001" queda con present igual a true
    And la operacion se completa en maximo 3 interacciones

  Scenario: Registro de inasistencia
    Given existe la sesion abierta "session-001"
    When el docente marca asistencia para "student-002" como ausente en la sesion "session-001"
    Then el sistema retorna HTTP 200
    And el registro de "student-002" queda con present igual a false

  Scenario: Agregar observacion dentro de la ventana de 24 horas
    Given existe la sesion "session-001" creada hace 10 horas
    When el docente envia PATCH /attendance/records/record-001 con observacion "Excelente participacion"
    Then el sistema retorna HTTP 200
    And el registro queda con la observacion guardada
    And el acudiente de "student-001" recibe una notificacion por email

  Scenario: Edicion bloqueada fuera de la ventana de 24 horas
    Given existe la sesion "session-001" creada hace 25 horas
    When el docente intenta editar la observacion del registro "record-001"
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "EDIT_WINDOW_EXPIRED"

  Scenario: Docente no asignado no puede registrar asistencia
    Given el docente "otro-docente@ejemplo.com" no esta asignado a "act-001"
    When intenta abrir sesion para "act-001"
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "NOT_ASSIGNED_TEACHER"

  Scenario: Consulta de lista de inscritos por actividad
    When el docente envia GET /enrollments/activity/act-001
    Then el sistema retorna HTTP 200
    And la respuesta contiene una lista con 3 estudiantes
    And cada entrada incluye nombre del estudiante y estado del enrollment

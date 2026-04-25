# F1 — Inscripcion de estudiante en actividad extracurricular
# Actor: Padre / Acudiente
# Modulos: Auth · Inscripciones · Actividades · Usuarios · Notificaciones
# RF03, RF04, RF05, RF06, RF07

Feature: Inscripcion de estudiante en actividad extracurricular

  Background:
    Given el acudiente "maria@ejemplo.com" esta autenticado con rol "GUARDIAN"
    And tiene un hijo registrado con id "student-001" llamado "Juan Lopez"
    And pertenece a la institucion "inst-001"

  Scenario: Inscripcion exitosa con cupo disponible
    Given la actividad "act-001" tiene 5 cupos disponibles
    And la actividad "act-001" esta en estado "PUBLISHED"
    And "student-001" no tiene ningun enrollment activo
    When el acudiente envia POST /enrollments con studentId "student-001" y activityId "act-001"
    Then el sistema retorna HTTP 201
    And el enrollment queda en estado "ACTIVE"
    And los cupos disponibles de "act-001" se reducen a 4
    And el acudiente recibe un email de confirmacion en menos de 60 segundos

  Scenario: Inscripcion fallida por cupo agotado
    Given la actividad "act-002" tiene 0 cupos disponibles
    When el acudiente envia POST /enrollments con studentId "student-001" y activityId "act-002"
    Then el sistema retorna HTTP 409
    And el cuerpo de respuesta contiene el campo "error" con valor "SPOT_EXHAUSTED"
    And no se crea ningun enrollment

  Scenario: Bloqueo por inscripcion duplicada
    Given "student-001" ya tiene un enrollment activo en la actividad "act-001"
    When el acudiente envia POST /enrollments con studentId "student-001" y activityId "act-001"
    Then el sistema retorna HTTP 409
    And el cuerpo de respuesta contiene el campo "error" con valor "ALREADY_ENROLLED"

  Scenario: Bloqueo por enrollment activo existente (un enrollment activo a la vez)
    Given "student-001" ya tiene un enrollment activo en la actividad "act-003"
    When el acudiente envia POST /enrollments con studentId "student-001" y activityId "act-001"
    Then el sistema retorna HTTP 409
    And el cuerpo de respuesta contiene el campo "error" con valor "ACTIVE_ENROLLMENT_EXISTS"

  Scenario: Padre no puede inscribir a un estudiante que no es su hijo
    Given existe el estudiante "student-999" que pertenece a otro acudiente
    When el acudiente envia POST /enrollments con studentId "student-999" y activityId "act-001"
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "FORBIDDEN"

  Scenario: Cancelacion de inscripcion libera el cupo
    Given "student-001" tiene un enrollment activo "enroll-001" en la actividad "act-001"
    And la actividad "act-001" tiene 3 cupos disponibles
    When el acudiente envia DELETE /enrollments/enroll-001
    Then el sistema retorna HTTP 200
    And el enrollment "enroll-001" queda en estado "CANCELLED"
    And los cupos disponibles de "act-001" se incrementan a 4

  Scenario: Inscripcion concurrente no genera sobrecupo
    Given la actividad "act-005" tiene exactamente 1 cupo disponible
    When dos acudientes intentan inscribir a sus hijos en "act-005" al mismo tiempo
    Then exactamente 1 inscripcion es exitosa con HTTP 201
    And la otra retorna HTTP 409 con error "SPOT_EXHAUSTED"
    And los cupos disponibles de "act-005" son 0

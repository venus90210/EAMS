# F3 — Consulta offline: padre o docente sin conexion a internet
# Actor: Padre / Docente
# Modulos: Service Worker · Cache local (PWA)
# RNF08 — soporte offline 48 horas

Feature: Consulta offline de actividades e historial de asistencia

  Background:
    Given el usuario ha iniciado sesion al menos una vez con conexion activa
    And el Service Worker de la PWA ha precargado el cache de actividades e historial

  Scenario: Padre consulta actividades inscritas sin conexion
    Given el padre "maria@ejemplo.com" no tiene conexion a internet
    And el cache fue actualizado hace 24 horas
    When el padre navega a la seccion "Mis actividades"
    Then la PWA sirve la vista desde el cache local
    And el padre puede ver las actividades inscritas de sus hijos
    And la interfaz muestra un indicador de "modo offline"

  Scenario: Docente consulta lista de asistencia sin conexion
    Given el docente "docente@ejemplo.com" no tiene conexion a internet
    And el cache fue actualizado hace 30 horas
    When el docente navega a la lista de asistencia de "act-001"
    Then la PWA sirve la vista desde el cache local
    And el docente puede ver el roster de estudiantes inscritos
    And la interfaz muestra un indicador de "modo offline"

  Scenario: Cache expirado despues de 48 horas muestra mensaje de advertencia
    Given el usuario no tiene conexion a internet
    And el cache fue actualizado hace 49 horas
    When el usuario intenta acceder a sus actividades
    Then la PWA muestra un mensaje "Informacion desactualizada. Conectate para ver los datos mas recientes."
    And el contenido del cache anterior sigue siendo visible con advertencia visible

  Scenario: Primer acceso sin haber cargado cache previo
    Given es la primera vez que el usuario accede a la plataforma
    And no tiene conexion a internet
    When intenta acceder a la seccion de actividades
    Then la PWA muestra un mensaje "Sin datos disponibles. Se requiere conexion para cargar por primera vez."
    And no se muestra contenido desactualizado

  Scenario: Reconexion sincroniza el estado local con el servidor
    Given el padre estaba en modo offline y el cache tiene datos de 20 horas atras
    When el dispositivo recupera la conexion a internet
    Then la PWA detecta la reconexion automaticamente
    And solicita datos actualizados al servidor
    And actualiza el cache local con la informacion mas reciente
    And desaparece el indicador de "modo offline"

  Scenario: Acciones de escritura bloqueadas en modo offline
    Given el padre no tiene conexion a internet
    When intenta inscribir a su hijo en una actividad
    Then la PWA muestra un mensaje "Esta accion requiere conexion a internet"
    And no se envia ningun request al servidor

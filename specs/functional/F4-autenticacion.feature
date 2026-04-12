# F4 — Autenticacion y renovacion de sesion
# Actor: Todos los roles (Padre, Docente, Admin, Superadmin)
# Modulos: Auth · Redis · PostgreSQL
# RNF04 — MFA, cifrado | RNF05 — RBAC | RNF06 — Ley 1581

Feature: Autenticacion y gestion de sesion

  Background:
    Given el sistema tiene configurado JWT con access token de 15 minutos
    And el refresh token tiene duracion de 7 dias y se almacena en Redis

  # ─── LOGIN ──────────────────────────────────────────────────────────────────

  Scenario: Login exitoso sin MFA (rol de solo lectura)
    Given existe el usuario "padre@ejemplo.com" con rol "GUARDIAN" y estado activo
    When envia POST /auth/login con credenciales correctas
    Then el sistema retorna HTTP 200
    And la respuesta contiene un access token JWT valido por 15 minutos
    And la respuesta contiene un refresh token almacenado en Redis
    And el access token incluye los campos "sub", "role" e "institutionId"

  Scenario: Login con MFA obligatorio para rol con privilegios de escritura
    Given existe el usuario "admin@ejemplo.com" con rol "ADMIN" y MFA configurado
    When envia POST /auth/login con credenciales correctas
    Then el sistema retorna HTTP 200 con campo "mfaRequired" igual a true
    And no se emite access token hasta completar el paso MFA

  Scenario: Verificacion MFA exitosa
    Given el usuario "admin@ejemplo.com" completo el primer paso del login
    And tiene un codigo TOTP valido "123456"
    When envia POST /auth/mfa/verify con el codigo "123456"
    Then el sistema retorna HTTP 200
    And la respuesta contiene access token y refresh token

  Scenario: Verificacion MFA fallida con codigo incorrecto
    Given el usuario "admin@ejemplo.com" completo el primer paso del login
    When envia POST /auth/mfa/verify con el codigo incorrecto "000000"
    Then el sistema retorna HTTP 401
    And el cuerpo de respuesta contiene el campo "error" con valor "MFA_INVALID"
    And no se emite ningun token

  Scenario: Login con credenciales incorrectas
    When un usuario envia POST /auth/login con contrasena incorrecta
    Then el sistema retorna HTTP 401
    And el cuerpo de respuesta contiene el campo "error" con valor "INVALID_CREDENTIALS"
    And no se emite ningun token

  # ─── REFRESH TOKEN ──────────────────────────────────────────────────────────

  Scenario: Renovacion exitosa de access token con refresh token valido
    Given el usuario tiene un refresh token valido en Redis
    And su access token ha expirado
    When envia POST /auth/refresh con el refresh token
    Then el sistema retorna HTTP 200
    And la respuesta contiene un nuevo access token valido por 15 minutos

  Scenario: Refresh token revocado no permite renovacion
    Given el refresh token del usuario ha sido revocado en Redis
    When envia POST /auth/refresh con ese refresh token
    Then el sistema retorna HTTP 401
    And el cuerpo de respuesta contiene el campo "error" con valor "TOKEN_REVOKED"

  Scenario: Refresh token expirado no permite renovacion
    Given el refresh token del usuario expiro hace 1 dia
    When envia POST /auth/refresh con ese refresh token
    Then el sistema retorna HTTP 401
    And el cuerpo de respuesta contiene el campo "error" con valor "TOKEN_EXPIRED"

  # ─── LOGOUT Y REVOCACION ────────────────────────────────────────────────────

  Scenario: Logout revoca el refresh token en Redis
    Given el usuario "maria@ejemplo.com" tiene una sesion activa
    When envia POST /auth/logout con su refresh token
    Then el sistema retorna HTTP 200
    And el refresh token queda marcado como revocado en Redis
    And cualquier intento de renovacion con ese token retorna HTTP 401

  # ─── CONTROL DE ACCESO ──────────────────────────────────────────────────────

  Scenario: Padre no puede acceder a endpoints de administracion
    Given el usuario tiene rol "GUARDIAN"
    When intenta acceder a POST /activities (requiere rol TEACHER o ADMIN)
    Then el API Gateway retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "INSUFFICIENT_ROLE"

  Scenario: Docente no puede modificar cupos de una actividad
    Given el usuario tiene rol "TEACHER"
    When intenta enviar PATCH /activities/act-001 con cambio en total_spots
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "INSUFFICIENT_ROLE"

  Scenario: Token de otra institucion no accede a datos de la institucion actual
    Given el usuario pertenece a "inst-002"
    When intenta acceder a un recurso de "inst-001"
    Then el sistema retorna HTTP 403
    And el cuerpo de respuesta contiene el campo "error" con valor "INSTITUTION_MISMATCH"

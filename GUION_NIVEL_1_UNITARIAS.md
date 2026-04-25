# GUION: PRUEBAS UNITARIAS — Backend
## Duración: 3 minutos

---

Ahora vamos a profundizar en las pruebas unitarias. Es el nivel que probablemente conocen mejor porque es el que más ven los desarrolladores día a día.

Las pruebas unitarias testan **una sola unidad de código**, de forma aislada. Un servicio. Un método. Sin base de datos. Sin red. Sin nada de lo que podría salir mal en el mundo exterior.

La idea es simple: si el código está roto, queremos saberlo instantáneamente. En 5 segundos. No en 5 minutos esperando que PostgreSQL responda.

## Stack Tecnológico

Para las pruebas unitarias del backend, usamos:

```
├─ JUnit 5 (Jupiter)     → Framework de pruebas
├─ Mockito              → Mocking de dependencias
├─ AssertJ              → Assertions fluidas y legibles
└─ JaCoCo               → Medición de cobertura de código
```

Estas son herramientas estándar en Java. JUnit es lo básico. Mockito nos permite crear mocks — copias falsas — de las dependencias. AssertJ nos da una sintaxis bonita para escribir assertions. Y JaCoCo mide qué porcentaje del código está siendo cubierto por tests.

## Módulos Críticos — Qué Testamos

Ahora, tenemos varios módulos en el backend. Y cada uno tiene escenarios específicos que necesitamos testear. Déjame mostrarles los 4 más críticos:

### 1️⃣ **Módulo Inscripciones** (el corazón del sistema)

Este módulo es donde pasa la magia — donde los padres inscriben a sus hijos. Y por eso necesitamos testear todos los caminos posibles:

```java
✓ Cupo disponible → inscripción exitosa (camino feliz)
✓ Cupo agotado → HTTP 409 SPOT_EXHAUSTED (error esperado)
✓ Inscripción duplicada → HTTP 409 ALREADY_ENROLLED (no duplicar)
✓ Padre no responsable → HTTP 403 FORBIDDEN (seguridad)
```

Por ejemplo, ¿qué pasa si un padre intenta inscribir a un estudiante que no es su hijo? Nuestro test verifica que recibe un 403. ¿Qué pasa si intenta inscribirse dos veces en la misma actividad? Test verifica 409. ¿Qué si todos los cupos están llenos? Test verifica que no podemos inscribir y recibe error 409.

### 2️⃣ **Módulo Auth & Security** (la puerta de entrada)

Autenticación es lo primero que hace un usuario. Por eso tiene que estar blindada. Testamos todos los escenarios:

```java
✓ Login con credenciales correctas → genera JWT válido
✓ Token expirado → se revoca, retorna 401
✓ MFA TOTP válido → permite acceso (dos factores!)
✓ MFA TOTP inválido → rechaza, retorna 401
✓ Logout → el token se marca como revocado en Redis
```

Lo crítico acá es el MFA. Si un admin se logea, le pedimos un código de 6 dígitos de su autenticador. Testamos que sin ese código, aunque adivine la contraseña, no accede.

### 3️⃣ **Módulo Actividades** (gestión de la oferta)

Los docentes crean actividades. Los padres las ven. Admin las gestiona. Testamos:

```java
✓ Docente crea actividad en estado DRAFT → exitoso
✓ Docente publica (DRAFT→PUBLISHED) → exitoso
✓ Cambio de estado inválido (ej PUBLISHED→DRAFT) → 409 error
✓ Padre ve solo actividades PUBLISHED
✓ Docente y admin ven todas (DRAFT + PUBLISHED)
```

### 4️⃣ **Módulo Asistencia** (el registro en clase)

Docentes registran quién asistió. Pero hay reglas — solo pueden hacerlo dentro de una ventana de tiempo:

```java
✓ Docente abre sesión hoy → exitosa
✓ Intenta abrir sesión en fecha pasada → 422 UNPROCESSABLE_ENTITY
✓ Registra asistencia dentro de ventana 3h → exitosa
✓ Intenta fuera de ventana (después de 24h) → 403 WINDOW_EXPIRED
```

Esa ventana de 24 horas para editar es un requisito. Imaginen que un profesor olvidó registrar asistencia ayer. Tiene 24 horas para corregerlo. Pero después de 24 horas, se congela. Testamos que ese límite funciona exactamente.

---

## El Umbral: 95% de Cobertura

Ahora viene la pregunta: **¿cómo sabemos que testamos lo suficiente?**

Usamos JaCoCo, una herramienta que mide qué porcentaje del código está siendo ejecutado por tests. Le decimos: "Todos los tests juntos tienen que ejecutar al menos el 95% de las líneas de código y el 95% de las ramas (if/else)."

```xml
<!-- En pom.xml del backend -->
<minimum>0.95</minimum>  <!-- 95% es nuestro umbral -->
<counter>LINE</counter>   <!-- Contamos líneas de código -->
<counter>BRANCH</counter> <!-- Y también ramas (decisiones) -->
```

**¿Por qué no 100%?** Porque el 5% restante típicamente es:
- Clases que Lombok genera automáticamente (constructores, getters, setters)
- Entidades JPA (que son puro datos)
- Configuraciones de Spring (que son setup, no lógica)
- Migraciones de base de datos (scripts SQL)

Eso no aporta lógica de negocio, así que no lo testamos. Pero el 95% que sí es lógica, está 100% cubierto.

---

## Métricas Actuales

En el backend de EAMS ya tenemos:

- ✅ **8 unit tests** implementados en la capa de servicios
- ✅ **Cobertura verificada** en el pipeline de CI/CD
- ⏱️ **Ejecución:** menos de 5 segundos

Significa que en menos de 5 segundos, después de que hago un cambio de código, sé si rompí algo o no. Eso es insanamente rápido.

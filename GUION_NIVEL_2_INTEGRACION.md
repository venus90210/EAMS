# GUION: PRUEBAS DE INTEGRACIÓN
## Duración: 4 minutos

---

Ahora llegamos a la parte que asusta a mucha gente — las pruebas de integración. Y con razón. Son complicadas. Pero también son las que dan la verdadera confianza.

La pregunta básica es: **¿cómo sabemos que lo que funciona en mi laptop también funciona en el servidor real, con base de datos de verdad, con concurrencia de verdad?**

Respuesta corta: no sabemos. Hasta que lo testamos.

## Por Qué Los Mocks No Alcanzan

Miren, los mocks son excelentes para tests unitarios. Pero hay 4 cosas que los mocks simplemente no pueden simular:

1. **Bloqueos pesimistas en PostgreSQL.** Un mock devuelve un valor. Punto. Pero una transacción real en PostgreSQL? Eso bloquea filas. Serializa acceso. Es diferente.

2. **Row-Level Security (RLS).** Es código SQL que PostgreSQL ejecuta automáticamente antes de retornar datos. Un mock nunca ve eso.

3. **Redis con TTL.** Un mock puede guardar un valor. Pero un Redis real marca ese valor para que expire en 15 minutos. Un mock no.

4. **Flujos asincronos complejos.** Cuando publico un evento, ese evento va a una cola, un worker lo consume, llama un endpoint remoto. Es un flujo que toca múltiples sistemas. Un mock de cada pieza por separado no garantiza que el flujo completo funciona.

Por eso tenemos pruebas de integración. Son las que levantan infraestructura real.

## Herramientas

```
├─ Testcontainers → Levanta contenedores Docker reales (PostgreSQL, Redis)
├─ @SpringBootTest → Carga el contexto completo de Spring Boot
├─ WireMock → Simula un servidor SMTP remoto
└─ ExecutorService → Crea threads concurrentes reales
```

Testcontainers es lo clave. Antes tenías que tener PostgreSQL instalado en tu máquina para correr tests de integración. Hoy con Testcontainers, el test levanta PostgreSQL en un Docker, lo usa, y después lo destruye. Todo automático.

---

## IT-01: Concurrencia en Cupos (La Carrera)

Volvemos a nuestro problema original: 1 cupo, 10 solicitudes simultáneas. ¿Cómo garantizamos que exactamente 1 la logra?

La respuesta está en una característica poco conocida de PostgreSQL llamada `SELECT ... FOR UPDATE.` Es un bloqueo pesimista. Así funciona:

```sql
BEGIN;
SELECT available_spots
FROM activities
WHERE id = $activityId
FOR UPDATE;  -- Esto bloquea la fila

-- Ahora nadie más puede leer o modificar esta fila
-- Hasta que yo hago COMMIT
```

Cuando la primera transacción hace `SELECT ... FOR UPDATE`, bloquea esa fila. La segunda transacción llega, intenta hacer lo mismo, y se pone en una cola esperando. Cuando la primera termina (COMMIT), la segunda continúa.

Pero acá está lo importante: **ningún mock puede simular eso.**

Un mock simplemente retorna: `available_spots = 1`. No hay bloqueo. No hay cola. Es instantáneo.

Entonces ¿cómo lo testamos de verdad? Con una prueba de integración:

```
Escenario:
  Given: 1 cupo disponible en una actividad
  When: 10 hilos intentan inscribir simultáneamente
  Then: ✅ 1 éxito | ✅ 9 fallan con 409 SPOT_EXHAUSTED
        ✅ available_spots = 0 (nunca negativo)

Infraestructura: Testcontainers PostgreSQL + ExecutorService (10 threads)
Duración: ~30 segundos
Resultado Esperado: Siempre el mismo — 0% sobrecupo
```

El test levanta un PostgreSQL real. Inserta una actividad con 1 cupo. Después crea 10 hilos (threads) que simultáneamente llaman a la API de inscripción. Cada uno intenta inscribirse. La BD serializa automáticamente con `SELECT ... FOR UPDATE.` 

Resultado: Exactamente 1 éxito, 9 fallan con 409. Siempre. Consistente.

Eso es confianza de verdad.

---

## IT-02: Aislamiento Multi-Tenant (La Seguridad)

Cambio de tema. EAMS soporta múltiples instituciones. El riesgo: un usuario de Institución A logra ver datos de Institución B.

¿Cómo lo prevenimos? Con **Row-Level Security (RLS).**

RLS es una característica de PostgreSQL que automáticamente filtra filas basado en el usuario actual:

```sql
CREATE POLICY "users_can_only_see_own_institution"
  ON activities
  USING (institution_id = current_user_id);
```

Eso significa: "Solo retorna filas donde institution_id es el de tu institución." Está a nivel de BD. No puedes saltarte con un WHERE diferente. No puedes hackear la query. Es imposible.

Pero nuevamente, **un mock nunca ve esto.**

Entonces:

```
Escenario:
  Given: Institución A con actividades propias
  And: Institución B con actividades propias
  When: Usuario de inst-A ejecuta GET /activities
  Then: ✅ Solo ve actividades de inst-A (filtradas por BD)
        ✅ No ve nada de inst-B
        ✅ Imposible de saltarse (está a nivel SQL)

Infraestructura: Testcontainers PostgreSQL + RLS policies reales
Regulación: Ley 1581 (protección datos menores)
Duración: ~20 segundos
```

El test levanta PostgreSQL con las políticas RLS activadas. Inserta actividades de dos instituciones diferentes. Luego intenta acceder como usuario de inst-A. La BD bloquea automáticamente. Resultado: solo ve sus datos. Perfecto.

---

## IT-03: Revocación de Tokens (El Logout)

Un usuario se logea. Recibe un JWT. Tiempo después, se logea de nuevo desde otro dispositivo. Queremos revocar el token anterior.

¿Cómo? Con Redis:

```
1. Usuario hace logout
2. Marcamos su token como revocado en Redis:
   SET revoked_tokens:$tokenId "true" EX 900
3. Cuando intenta usar ese token después:
   GET revoked_tokens:$tokenId
   Si existe → 401 TOKEN_REVOKED
   Si no existe → token válido
```

Pero aquí está el problema: **un mock no verifica que el token realmente fue borrado de la BD.**

```
Escenario:
  Given: Usuario con refresh token activo en Redis
  When: Usuario hace logout
        POST /auth/refresh con ese token
  Then: ✅ HTTP 401 TOKEN_REVOKED
        ✅ Token no reutilizable, aunque no haya expirado naturalmente

Infraestructura: Testcontainers Redis
Duración: ~10 segundos
```

El test levanta Redis real. Inserta un token. Hace logout (que llama a Redis DELETE). Luego intenta usar ese token. Redis dice "no existe" → 401.

---

## IT-04: Notificaciones Asincronas (El Email)

Cuando un padre inscribe a su hijo, le mandamos un email de confirmación. Pero no es síncrono — no esperamos a que el email se envíe antes de retornar la respuesta.

¿Por qué? Porque SMTP es lento. Enviar un email puede tomar 5 segundos. Si hacemos eso de forma síncrona, la inscripción tarda 5 segundos. Es lento.

Por eso lo hacemos asincrónico:

```
1. Padre inscribe hijo → 200 OK (instantáneo)
2. El módulo publica ApplicationEvent "EnrollmentConfirmed"
3. Listener recibe evento → encula a Redis (BullMQ)
4. Worker en background consume la cola
5. Worker llama endpoint SMTP → email enviado
```

El flujo completo tiene que terminar en <60 segundos. Pero ningún mock puede garantizar eso porque hay 3 sistemas involucrados.

```
Escenario:
  Given: Acudiente inscribe estudiante en actividad
  When: Sistema publica EnrollmentConfirmed
  Then: ✅ Cola de Redis encolada en <1s
        ✅ Worker consume en <2s
        ✅ Email enviado en <60s
        ✅ SMTP recibe POST con datos correctos

Infraestructura: Testcontainers Redis + WireMock (fake SMTP)
Duración: ~40 segundos
```

El test levanta Redis real. Publica el evento. Verifica que la cola recibió el mensaje. Después verifica que WireMock (servidor SMTP fake) recibió la petición con los datos correctos dentro de 60 segundos.

---

## Resumen: Los 4 Escenarios Críticos

| Nombre | Problema | Solución | Estado |
|--------|----------|----------|--------|
| **IT-01** | ¿Hay sobrecupo? | SELECT FOR UPDATE | ✅ Implementado |
| **IT-02** | ¿Se filtran datos? | RLS en PostgreSQL | ✅ Implementado |
| **IT-03** | ¿Se revoca token? | DELETE en Redis | ✅ Implementado |
| **IT-04** | ¿Email en <60s? | Event → Queue → Worker | ✅ Implementado |

Estos 4 tests usan infraestructura real. Toman más tiempo que los unitarios. Pero dan confianza absoluta.

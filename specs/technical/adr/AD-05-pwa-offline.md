# AD-05 — Frontend como PWA con soporte offline

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RNF08, RNF02, Restricción tecnológica de conectividad |

---

## Contexto

La plataforma debe operar en regiones colombianas con conectividad limitada (restricción tecnológica identificada en el alcance). Los casos de uso críticos sin red son:

- **Docentes**: consultar lista de asistencia sin internet.
- **Padres**: ver actividades inscritas de sus hijos sin cobertura móvil.

Una aplicación web tradicional sin soporte offline fallaría completamente en estos escenarios.

## Decisión

El frontend se implementa como una **Progressive Web App (PWA)** usando **Next.js + next-pwa**.

### Componentes clave

| Componente       | Responsabilidad                                              |
|------------------|--------------------------------------------------------------|
| Service Worker   | Intercepta peticiones y sirve vistas desde caché local       |
| Cache Strategy   | Cache-first para vistas de consulta, Network-first para escritura |
| manifest.json    | Permite instalación como app nativa en Android               |
| Tiempo de caché  | 48 horas (RNF08)                                             |

### Vistas precargadas en caché

| Vista                          | Rol        | Duración |
|--------------------------------|------------|----------|
| Lista de actividades inscritas | GUARDIAN   | 48 h     |
| Historial de asistencia        | GUARDIAN   | 48 h     |
| Roster de estudiantes          | TEACHER    | 48 h     |

### Restricciones de modo offline

- Las **acciones de escritura** (inscripción, registro de asistencia) requieren conexión activa.
- El Service Worker muestra un aviso visible cuando opera en modo offline.
- Si el caché supera las 48 horas, se muestra advertencia de datos desactualizados.

### Limitaciones conocidas

- **iOS Safari**: soporte PWA limitado; la instalación desde el navegador no está disponible en iOS antes de la versión 16.4. Se debe comunicar esta limitación al cliente.
- **Primer acceso**: requiere conexión para cargar el caché inicial.

## Justificación

- Resuelve directamente la restricción tecnológica de conectividad limitada.
- Cumple RNF08 (consulta offline de actividades e historial por 48 horas).
- SSR/SSG de Next.js mejora el tiempo de carga inicial, favoreciendo RNF09 (<3s).
- La instalación como app nativa en Android elimina la necesidad de publicar en tiendas de aplicaciones.
- La capa de presentación no contiene lógica de negocio: solo consume la API definida en los specs OpenAPI.

## Consecuencias

- **Positivas**: acceso offline, instalable en Android, mejor rendimiento de carga.
- **Negativas**: complejidad adicional en el manejo del Service Worker; datos desactualizados si el caché expira sin reconexión.
- **Mitigación**: el Service Worker muestra siempre el estado de conectividad al usuario, y bloquea explícitamente las acciones de escritura en modo offline.

## Alternativas descartadas

- **Aplicación móvil nativa (React Native / Flutter)**: descartada por mayor costo de desarrollo y mantenimiento, y por requerir publicación en tiendas de aplicaciones.
- **Web app tradicional sin Service Worker**: descartada porque no cumple RNF08 (soporte offline).

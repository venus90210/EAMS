# AD-01 — Monolito Modular como estilo arquitectónico

| Campo       | Valor                          |
|-------------|--------------------------------|
| **Estado**  | Aceptado                       |
| **Fecha**   | 2026-04-11                     |
| **Autores** | Equipo EAMS                    |
| **Fuente**  | RNF09, RNF02                   |

---

## Contexto

El sistema debe soportar hasta 5 instituciones simultáneas con 5.000 usuarios activos y tiempos de respuesta menores a 3 segundos (RNF09). Se evaluaron tres estilos arquitectónicos para el backend:

- **Monolito tradicional**: un único artefacto sin separación interna de dominio.
- **Monolito Modular**: un único artefacto donde cada módulo tiene fronteras lógicas estrictas y se comunica en memoria.
- **Microservicios**: servicios independientes desplegados por separado, que se comunican por red.

## Decisión

Se elige el **Monolito Modular** (también llamado "modulito") implementado con **Spring Boot + Spring Modulith**.

- Un único artefacto de despliegue (JAR / imagen Docker).
- Los módulos de dominio tienen fronteras explícitas verificadas en tiempo de compilación por Spring Modulith.
- La comunicación entre módulos ocurre en memoria compartida del proceso, eliminando latencia de red.
- La base de datos, el repositorio de código y el ciclo de despliegue son únicos.

## Justificación

| Criterio               | Monolito tradicional | **Monolito Modular** | Microservicios |
|------------------------|----------------------|----------------------|----------------|
| Complejidad operativa  | Baja                 | **Baja**             | Alta           |
| Separación de dominio  | Ninguna              | **Estricta**         | Estricta       |
| Latencia entre módulos | En memoria           | **En memoria**       | Red (ms–s)     |
| Costo de infraestructura | Bajo              | **Bajo**             | Alto           |
| Escalabilidad horizontal | Limitada          | **Suficiente (5k u)**| Alta           |
| Preparado para crecer  | No                   | **Sí (Strangler Fig)**| Sí            |

Para la carga proyectada (5.000 usuarios, 5 instituciones), los microservicios introducen complejidad operativa y costos de infraestructura que no se justifican.

## Consecuencias

- **Positivas**: despliegue simple, cero latencia entre módulos, baja curva operativa para el equipo.
- **Negativas**: escalar un módulo específico requiere escalar todo el artefacto.
- **Mitigación**: el diseño modular permite extraer un módulo como microservicio (patrón Strangler Fig) si la carga futura lo exige, sin reescribir la aplicación.

## Alternativas descartadas

- **Monolito tradicional**: descartado porque concentra toda la lógica sin separación, generando acoplamiento que lo vuelve inmantenible al crecer.
- **Microservicios**: descartados porque introducen latencia de red, orquestación compleja y costos de infraestructura no justificados para la escala actual del proyecto.

/**
 * Módulo Notificaciones — despacho asincrónico de emails basado en eventos de dominio.
 *
 * Responsabilidades:
 * - Escuchar eventos publicados por otros módulos (@TransactionalEventListener)
 * - Convertir eventos de dominio a jobs de notificación con plantillas HTML
 * - Encolar jobs en Redis con soporte para idempotencia y reintentos
 * - Procesar la cola con @Scheduled worker y despachar emails vía SMTP
 *
 * ADR de referencia: AD-09 (Notificaciones asincrónicas).
 * Patrones: Hexagonal (puertos domain/ vs adapters infrastructure/).
 */
@org.springframework.modulith.ApplicationModule
package com.eams.notification;

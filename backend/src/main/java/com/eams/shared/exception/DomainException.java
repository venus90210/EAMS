package com.eams.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores de dominio de negocio.
 * Cada módulo define sus propias subclases con el código de error apropiado.
 */
public class DomainException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public DomainException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }

    // ── Subclases de uso frecuente ──────────────────────────────────────────

    public static DomainException notFound(String message) {
        return new DomainException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public static DomainException conflict(String errorCode, String message) {
        return new DomainException(errorCode, message, HttpStatus.CONFLICT);
    }

    public static DomainException forbidden(String errorCode, String message) {
        return new DomainException(errorCode, message, HttpStatus.FORBIDDEN);
    }

    public static DomainException unprocessable(String errorCode, String message) {
        return new DomainException(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

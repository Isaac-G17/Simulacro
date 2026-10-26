package com.stockbridge.inventory.domain.exception;

/** Base de todas las excepciones de negocio. El GlobalExceptionHandler las traduce a ProblemDetail. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}

package com.stockbridge.inventory.domain.exception;

/** Cambio de estado no permitido en una solicitud (409). */
public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}

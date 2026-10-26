package com.stockbridge.inventory.domain.exception;

/** El usuario intenta acceder a un recurso que no le pertenece (403). */
public class ResourceAccessDeniedException extends DomainException {

    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}

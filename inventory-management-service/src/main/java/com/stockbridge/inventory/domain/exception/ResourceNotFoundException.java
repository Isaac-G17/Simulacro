package com.stockbridge.inventory.domain.exception;

/** Producto, usuario o solicitud inexistente (404). */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

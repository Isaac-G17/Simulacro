package com.stockbridge.inventory.domain.exception;

/** Código de producto o email ya registrado (409). */
public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

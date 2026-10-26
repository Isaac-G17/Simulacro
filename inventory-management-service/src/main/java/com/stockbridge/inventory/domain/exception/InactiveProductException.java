package com.stockbridge.inventory.domain.exception;

/** Se intentó solicitar un producto inactivo (422). */
public class InactiveProductException extends DomainException {

    public InactiveProductException(String message) {
        super(message);
    }
}

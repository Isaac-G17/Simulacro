package com.stockbridge.inventory.domain.exception;

/** No hay existencias suficientes para la operación (409). */
public class InsufficientStockException extends DomainException {

    public InsufficientStockException(String message) {
        super(message);
    }
}

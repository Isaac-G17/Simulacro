package com.stockbridge.inventory.domain.exception;

/** El servicio de proveedores no respondió, falló o superó el timeout (503). */
public class SupplierUnavailableException extends DomainException {

    public SupplierUnavailableException(String message) {
        super(message);
    }
}

package com.stockbridge.inventory.domain.exception;

/** Datos que violan una regla del dominio: precio <= 0, cantidad <= 0... (400/422). */
public class InvalidDomainDataException extends DomainException {

    public InvalidDomainDataException(String message) {
        super(message);
    }
}

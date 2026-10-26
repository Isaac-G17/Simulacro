package com.stockbridge.inventory.domain.exception;

/** Email o contraseña incorrectos en el login (401). */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

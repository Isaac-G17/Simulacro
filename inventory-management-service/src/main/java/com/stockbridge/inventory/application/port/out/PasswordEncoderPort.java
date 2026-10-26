package com.stockbridge.inventory.application.port.out;

/** Así el caso de uso de autenticación no depende de Spring Security. */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}

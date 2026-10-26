package com.stockbridge.inventory.application.port.in;

public interface AuthUseCase {

    AuthResult register(RegisterCommand command);

    AuthResult login(LoginCommand command);

    record RegisterCommand(String fullName, String email, String password) {
    }

    record LoginCommand(String email, String password) {
    }

    record AuthResult(String token, String email, String role) {
    }
}

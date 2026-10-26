package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.AuthUseCase;
import com.stockbridge.inventory.application.port.out.PasswordEncoderPort;
import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    @Override
    @Transactional
    public AuthResult register(RegisterCommand command) {
        // TODO:
        // 1. si existsByEmail -> DuplicateResourceException
        // 2. User.register(fullName, email, passwordEncoder.encode(password))
        // 3. guardar y devolver new AuthResult(tokenProvider.generateToken(user), email, role)
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        // TODO: buscar por email y comprobar passwordEncoder.matches(...);
        //       si algo falla -> InvalidCredentialsException (mismo mensaje en ambos casos)
        throw new UnsupportedOperationException("TODO");
    }
}

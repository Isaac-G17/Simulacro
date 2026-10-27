package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.AuthUseCase;
import com.stockbridge.inventory.application.port.out.PasswordEncoderPort;
import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import com.stockbridge.inventory.domain.exception.DuplicateResourceException;
import com.stockbridge.inventory.domain.exception.InvalidCredentialsException;
import com.stockbridge.inventory.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private static final String INVALID_CREDENTIALS = "Email o contraseña incorrectos";

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    @Override
    @Transactional
    public AuthResult register(RegisterCommand command) {
        String email = normalize(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + email);
        }
        // Se guarda el hash BCrypt, nunca la contraseña en texto plano. Todo registro nace como EMPLEADO.
        User user = userRepository.save(User.register(command.fullName(), email,
                passwordEncoder.encode(command.password())));
        log.info("Usuario registrado: id={}, email={}, rol={}", user.getId(), user.getEmail(), user.getRole());
        return toResult(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        // Mismo mensaje si el email no existe o la contraseña es incorrecta: no se revela qué emails existen
        User user = userRepository.findByEmail(normalize(command.email()))
                .filter(User::isActive)
                .filter(u -> passwordEncoder.matches(command.password(), u.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Login fallido para {}", command.email());
                    return new InvalidCredentialsException(INVALID_CREDENTIALS);
                });
        log.info("Login correcto: id={}, rol={}", user.getId(), user.getRole());
        return toResult(user);
    }

    private AuthResult toResult(User user) {
        return new AuthResult(tokenProvider.generateToken(user), user.getEmail(), user.getRole().name());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

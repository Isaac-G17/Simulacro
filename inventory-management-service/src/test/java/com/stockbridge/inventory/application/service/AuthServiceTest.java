package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.AuthUseCase.AuthResult;
import com.stockbridge.inventory.application.port.in.AuthUseCase.LoginCommand;
import com.stockbridge.inventory.application.port.in.AuthUseCase.RegisterCommand;
import com.stockbridge.inventory.application.port.out.PasswordEncoderPort;
import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import com.stockbridge.inventory.domain.exception.DuplicateResourceException;
import com.stockbridge.inventory.domain.exception.InvalidCredentialsException;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private TokenProviderPort tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldStoreHashAndAssignEmployeeRole() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Secreta123")).thenReturn("$2a$hash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.generateToken(any())).thenReturn("jwt");

        AuthResult result = authService.register(new RegisterCommand("Ana", " Ana@Test.com ", "Secreta123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("$2a$hash");
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@test.com");
        assertThat(result.role()).isEqualTo(Role.EMPLEADO.name());
        assertThat(result.token()).isEqualTo("jwt");
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterCommand("Ana", "ana@test.com", "Secreta123")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldFail_whenPasswordDoesNotMatch() {
        User user = new User(1L, "Admin", "admin@test.com", "$2a$hash", Role.ADMIN, true);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mala", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("admin@test.com", "mala")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldFailWithSameException_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("nadie@test.com", "x")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}

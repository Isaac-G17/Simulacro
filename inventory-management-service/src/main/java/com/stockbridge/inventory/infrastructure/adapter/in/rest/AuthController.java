package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.port.in.AuthUseCase;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.AuthResponse;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.RegisterRequest;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.mapper.RestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final RestMapper mapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return mapper.toResponse(authUseCase.register(mapper.toCommand(request)));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.toResponse(authUseCase.login(mapper.toCommand(request)));
    }
}

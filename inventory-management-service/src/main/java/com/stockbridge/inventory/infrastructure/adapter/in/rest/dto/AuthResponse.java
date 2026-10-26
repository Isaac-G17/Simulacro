package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

public record AuthResponse(String token, String email, String role) {
}

package com.stockbridge.inventory.domain.model;

/** Usuario autenticado que llega a los casos de uso (sin depender de Spring Security). */
public record CurrentUser(Long id, String email, Role role) {

    public boolean hasRole(Role expected) {
        return role == expected;
    }
}

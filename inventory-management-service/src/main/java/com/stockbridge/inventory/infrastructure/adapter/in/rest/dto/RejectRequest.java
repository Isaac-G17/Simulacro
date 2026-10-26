package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(@NotBlank String reason) {
}

package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

import com.stockbridge.inventory.domain.model.MovementType;

import java.time.LocalDateTime;

public record MovementResponse(Long id, Long productId, Long requestId, Long userId, MovementType type,
                               int quantity, String note, LocalDateTime createdAt) {
}

package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

import com.stockbridge.inventory.domain.model.RequestStatus;

import java.time.LocalDateTime;

public record InventoryRequestResponse(Long id, Long userId, Long productId, int quantity, RequestStatus status,
                                       String observation, String rejectionReason,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
}

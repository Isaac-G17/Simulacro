package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockEntryRequest(
        @Positive int quantity,
        @Size(max = 255) String note
) {
}

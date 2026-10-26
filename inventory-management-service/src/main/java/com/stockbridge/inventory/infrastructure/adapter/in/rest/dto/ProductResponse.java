package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String code, String name, String description, String category,
                              BigDecimal price, int stock, boolean active) {
}

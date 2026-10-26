package com.stockbridge.inventory.infrastructure.adapter.in.rest.dto;

public record AvailabilityResponse(String supplierCode, boolean available, int availableQuantity,
                                   int estimatedDeliveryDays) {
}

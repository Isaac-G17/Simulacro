package com.stockbridge.inventory.domain.model;

public record SupplierAvailability(String supplierCode, boolean available, int availableQuantity,
                                   int estimatedDeliveryDays) {
}

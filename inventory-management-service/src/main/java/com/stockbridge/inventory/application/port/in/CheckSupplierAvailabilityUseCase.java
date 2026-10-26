package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.SupplierAvailability;

public interface CheckSupplierAvailabilityUseCase {

    SupplierAvailability check(Long productId, int quantity);
}

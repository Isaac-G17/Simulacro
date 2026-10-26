package com.stockbridge.supplier.service;

import com.stockbridge.supplier.dto.AvailabilityRequest;
import com.stockbridge.supplier.dto.AvailabilityResponse;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    /**
     * Debe responder SIEMPRE lo mismo para la misma combinación de datos (determinista).
     * Idea: hash = Math.abs((productCode + category).hashCode())
     *   supplierCode          = "SUP-00" + (hash % 5 + 1)
     *   availableQuantity     = hash % 50
     *   available             = availableQuantity >= quantity
     *   estimatedDeliveryDays = hash % 7 + 1
     */
    public AvailabilityResponse check(AvailabilityRequest request) {
        // TODO: implementar la lógica determinista
        throw new UnsupportedOperationException("TODO");
    }
}

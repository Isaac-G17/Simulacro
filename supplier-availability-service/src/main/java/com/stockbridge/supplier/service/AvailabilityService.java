package com.stockbridge.supplier.service;

import com.stockbridge.supplier.dto.AvailabilityRequest;
import com.stockbridge.supplier.dto.AvailabilityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

    private static final int SUPPLIERS = 5;
    private static final int MAX_STOCK = 50;
    private static final int MAX_DELIVERY_DAYS = 7;

    /**
     * Responde SIEMPRE lo mismo para la misma combinación de datos (determinista): no hay azar ni estado,
     * todo sale de un hash de productCode + category. String.hashCode() está definido por la especificación
     * de Java, así que el resultado es igual entre reinicios y entre máquinas.
     */
    public AvailabilityResponse check(AvailabilityRequest request) {
        String key = request.productCode().trim().toUpperCase(Locale.ROOT) + "|"
                + request.category().trim().toUpperCase(Locale.ROOT);
        // floorMod en vez de Math.abs: Math.abs(Integer.MIN_VALUE) sigue siendo negativo
        int hash = Math.floorMod(key.hashCode(), Integer.MAX_VALUE);

        String supplierCode = String.format("SUP-%03d", hash % SUPPLIERS + 1);
        int availableQuantity = hash % MAX_STOCK;
        boolean available = availableQuantity >= request.quantity();
        int estimatedDeliveryDays = hash % MAX_DELIVERY_DAYS + 1;

        log.info("Consulta {} x {} ({}): proveedor={}, disponible={}, cantidad={}, días={}",
                request.quantity(), request.productCode(), request.category(), supplierCode, available,
                availableQuantity, estimatedDeliveryDays);
        return new AvailabilityResponse(supplierCode, available, availableQuantity, estimatedDeliveryDays);
    }
}

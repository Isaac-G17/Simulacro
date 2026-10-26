package com.stockbridge.inventory.infrastructure.adapter.out.supplier;

import com.stockbridge.inventory.application.port.out.SupplierClientPort;
import com.stockbridge.inventory.domain.exception.SupplierUnavailableException;
import com.stockbridge.inventory.domain.model.SupplierAvailability;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador HTTP hacia supplier-availability-service.
 * Timeouts configurados en RestClientConfig: si el proveedor no responde a tiempo,
 * se lanza SupplierUnavailableException (-> 503) en lugar de bloquear indefinidamente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierRestAdapter implements SupplierClientPort {

    private final RestClient supplierRestClient;
    private final MeterRegistry meterRegistry;

    @Override
    public SupplierAvailability checkAvailability(String productCode, int quantity, String category) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            SupplierResponse response = supplierRestClient.post()
                    .uri("/api/suppliers/check-availability")
                    .body(new SupplierRequest(productCode, quantity, category))
                    .retrieve()
                    .body(SupplierResponse.class);

            if (response == null) {
                outcome = "empty";
                throw new SupplierUnavailableException("El proveedor devolvió una respuesta vacía");
            }
            return new SupplierAvailability(response.supplierCode(), response.available(),
                    response.availableQuantity(), response.estimatedDeliveryDays());
        } catch (RestClientException ex) { // incluye timeouts, conexión rechazada, 4xx y 5xx
            outcome = "error";
            log.error("Error consultando proveedor para producto {}: {}", productCode, ex.getMessage());
            throw new SupplierUnavailableException("El servicio de proveedores no está disponible");
        } finally {
            sample.stop(meterRegistry.timer("inventory.supplier.requests", "outcome", outcome));
        }
    }

    record SupplierRequest(String productCode, int quantity, String category) {
    }

    record SupplierResponse(String supplierCode, boolean available, int availableQuantity,
                            int estimatedDeliveryDays) {
    }
}

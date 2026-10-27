package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.CheckSupplierAvailabilityUseCase;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.SupplierClientPort;
import com.stockbridge.inventory.domain.exception.ResourceNotFoundException;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.domain.model.SupplierAvailability;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierAvailabilityService implements CheckSupplierAvailabilityUseCase {

    private final ProductRepositoryPort productRepository;
    private final SupplierClientPort supplierClient;

    /** Sin @Transactional: no se mantiene una conexión de BD abierta mientras se espera la respuesta HTTP. */
    @Override
    public SupplierAvailability check(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + productId));
        // Si el proveedor falla, el adapter lanza SupplierUnavailableException (503)
        SupplierAvailability availability = supplierClient.checkAvailability(product.getCode(), quantity,
                product.getCategory());
        log.info("Proveedor {} para {} x {}: disponible={}, cantidad={}, entrega en {} días",
                availability.supplierCode(), quantity, product.getCode(), availability.available(),
                availability.availableQuantity(), availability.estimatedDeliveryDays());
        return availability;
    }
}

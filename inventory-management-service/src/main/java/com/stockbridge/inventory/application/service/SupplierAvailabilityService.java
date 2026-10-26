package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.CheckSupplierAvailabilityUseCase;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.SupplierClientPort;
import com.stockbridge.inventory.domain.model.SupplierAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierAvailabilityService implements CheckSupplierAvailabilityUseCase {

    private final ProductRepositoryPort productRepository;
    private final SupplierClientPort supplierClient;

    @Override
    public SupplierAvailability check(Long productId, int quantity) {
        // TODO: buscar producto y llamar supplierClient.checkAvailability(code, quantity, category)
        //       (sin @Transactional: no mantengas una conexión de BD abierta mientras esperas un HTTP)
        throw new UnsupportedOperationException("TODO");
    }
}

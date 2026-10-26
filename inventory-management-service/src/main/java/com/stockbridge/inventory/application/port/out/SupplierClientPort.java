package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.domain.model.SupplierAvailability;

/** Puerto hacia el servicio externo de proveedores. Si falla debe lanzar SupplierUnavailableException. */
public interface SupplierClientPort {

    SupplierAvailability checkAvailability(String productCode, int quantity, String category);
}

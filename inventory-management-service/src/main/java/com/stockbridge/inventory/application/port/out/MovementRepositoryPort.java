package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.domain.model.InventoryMovement;

import java.util.List;

public interface MovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);

    List<InventoryMovement> findByProductId(Long productId);
}

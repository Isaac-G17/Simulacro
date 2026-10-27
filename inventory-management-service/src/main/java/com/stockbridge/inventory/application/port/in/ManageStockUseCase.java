package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.Product;

import java.util.List;

/** Movimientos de inventario: reposición de stock y trazabilidad por producto. */
public interface ManageStockUseCase {

    /** Suma unidades al producto y registra un movimiento de ENTRADA, en una sola transacción. */
    Product registerEntry(Long productId, StockEntryCommand command, CurrentUser user);

    List<InventoryMovement> findMovements(Long productId);

    record StockEntryCommand(int quantity, String note) {
    }
}

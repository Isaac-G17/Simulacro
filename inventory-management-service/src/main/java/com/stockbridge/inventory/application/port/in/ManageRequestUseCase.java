package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.PageResult;

public interface ManageRequestUseCase {

    InventoryRequest create(CreateRequestCommand command, CurrentUser requester);

    /** EMPLEADO solo puede ver las suyas; BODEGA y ADMIN pueden ver todas. */
    InventoryRequest findById(Long id, CurrentUser currentUser);

    PageResult<InventoryRequest> findMine(CurrentUser currentUser, int page, int size);

    PageResult<InventoryRequest> findAll(int page, int size);

    InventoryRequest approve(Long id, CurrentUser approver);

    InventoryRequest reject(Long id, String reason, CurrentUser approver);

    InventoryRequest cancel(Long id, CurrentUser currentUser);

    /** Descuenta stock + registra movimiento + marca ENTREGADA, todo en UNA transacción. */
    InventoryRequest deliver(Long id, CurrentUser warehouseUser);

    record CreateRequestCommand(Long productId, int quantity, String observation) {
    }
}

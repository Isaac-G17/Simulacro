package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.PageResult;

import java.util.Optional;

public interface RequestRepositoryPort {

    InventoryRequest save(InventoryRequest request);

    Optional<InventoryRequest> findById(Long id);

    PageResult<InventoryRequest> findByUserId(Long userId, int page, int size);

    PageResult<InventoryRequest> findAll(int page, int size);
}

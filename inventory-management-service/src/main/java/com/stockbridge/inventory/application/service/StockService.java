package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageStockUseCase;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.exception.ResourceNotFoundException;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService implements ManageStockUseCase {

    private final ProductRepositoryPort productRepository;
    private final MovementRepositoryPort movementRepository;

    @Override
    @Transactional
    public Product registerEntry(Long productId, StockEntryCommand command, CurrentUser user) {
        Product product = findProduct(productId);
        product.increaseStock(command.quantity());   // el dominio valida cantidad > 0
        Product saved = productRepository.save(product);
        movementRepository.save(InventoryMovement.entry(productId, user.id(), command.quantity(), command.note()));
        log.info("Entrada de inventario: +{} {} por usuario {} (stock {})", command.quantity(),
                saved.getCode(), user.id(), saved.getStock());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovement> findMovements(Long productId) {
        findProduct(productId);   // 404 si el producto no existe, en lugar de una lista vacía
        return movementRepository.findByProductId(productId);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + id));
    }
}

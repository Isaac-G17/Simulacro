package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.PageResult;
import com.stockbridge.inventory.domain.model.Product;

import java.math.BigDecimal;
import java.util.List;

/** Puerto de entrada: lo que el mundo exterior (REST) puede pedirle al sistema sobre productos. */
public interface ManageProductUseCase {

    Product create(CreateProductCommand command);

    Product update(Long id, UpdateProductCommand command);

    Product findById(Long id);

    PageResult<Product> findAll(int page, int size);

    Product changeStatus(Long id, boolean active);

    List<Product> findLowStock(int minimum);

    record CreateProductCommand(String code, String name, String description, String category,
                                BigDecimal price, int stock) {
    }

    record UpdateProductCommand(String name, String description, String category, BigDecimal price) {
    }
}

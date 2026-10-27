package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.domain.model.PageResult;
import com.stockbridge.inventory.domain.model.Product;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: el dominio define QUÉ necesita; la infraestructura decide CÓMO (JPA, Mongo...). */
public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsByCode(String code);

    PageResult<Product> findAll(int page, int size);

    List<Product> findByStockLessThan(int minimum);

    long countByStockLessThan(int minimum);
}

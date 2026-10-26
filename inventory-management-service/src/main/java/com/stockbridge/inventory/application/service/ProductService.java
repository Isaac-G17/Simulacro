package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageProductUseCase;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.model.PageResult;
import com.stockbridge.inventory.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ManageProductUseCase {

    private final ProductRepositoryPort productRepository;

    @Override
    @Transactional
    public Product create(CreateProductCommand command) {
        // TODO:
        // 1. si productRepository.existsByCode(code) -> DuplicateResourceException
        // 2. Product.create(...) (el dominio valida precio y stock)
        // 3. guardar, log.info y retornar
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public Product update(Long id, UpdateProductCommand command) {
        // TODO: buscar (o ResourceNotFoundException), product.updateInfo(...), guardar
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        // TODO: productRepository.findById(id).orElseThrow(ResourceNotFoundException)
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> findAll(int page, int size) {
        return productRepository.findAll(page, size);
    }

    @Override
    @Transactional
    public Product changeStatus(Long id, boolean active) {
        // TODO: buscar, activate()/deactivate(), guardar
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStock(int minimum) {
        return productRepository.findByStockLessThan(minimum);
    }
}

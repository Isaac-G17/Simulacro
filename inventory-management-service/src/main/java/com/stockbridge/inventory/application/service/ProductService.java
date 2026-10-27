package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageProductUseCase;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.exception.DuplicateResourceException;
import com.stockbridge.inventory.domain.exception.ResourceNotFoundException;
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
        if (productRepository.existsByCode(command.code())) {
            throw new DuplicateResourceException("Ya existe un producto con el código " + command.code());
        }
        // El dominio valida precio > 0 y stock >= 0
        Product product = Product.create(command.code(), command.name(), command.description(),
                command.category(), command.price(), command.stock());
        Product saved = productRepository.save(product);
        log.info("Producto creado: id={}, código={}, stock={}", saved.getId(), saved.getCode(), saved.getStock());
        return saved;
    }

    @Override
    @Transactional
    public Product update(Long id, UpdateProductCommand command) {
        Product product = findById(id);
        product.updateInfo(command.name(), command.description(), command.category(), command.price());
        Product saved = productRepository.save(product);
        log.info("Producto actualizado: id={}, código={}", saved.getId(), saved.getCode());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> findAll(int page, int size) {
        return productRepository.findAll(page, size);
    }

    @Override
    @Transactional
    public Product changeStatus(Long id, boolean active) {
        Product product = findById(id);
        if (active) {
            product.activate();
        } else {
            product.deactivate();
        }
        Product saved = productRepository.save(product);
        log.info("Producto {} {}", saved.getCode(), active ? "activado" : "desactivado");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStock(int minimum) {
        return productRepository.findByStockLessThan(minimum);
    }
}

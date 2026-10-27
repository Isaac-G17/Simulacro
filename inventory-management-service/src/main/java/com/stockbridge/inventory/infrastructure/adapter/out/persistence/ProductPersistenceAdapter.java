package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.model.PageResult;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper.PageResults;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: implementa el puerto del dominio usando Spring Data JPA. */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public Product save(Product product) {
        return mapper.toDomain(repository.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public PageResult<Product> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return PageResults.from(repository.findAll(pageable), mapper::toDomain);
    }

    @Override
    public List<Product> findByStockLessThan(int minimum) {
        return repository.findLowStock(minimum).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByStockLessThan(int minimum) {
        return repository.countLowStock(minimum);
    }
}

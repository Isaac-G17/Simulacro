package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.port.out.RequestRepositoryPort;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.PageResult;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.InventoryRequestEntity;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper.PageResults;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.InventoryRequestJpaRepository;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RequestPersistenceAdapter implements RequestRepositoryPort {

    private final InventoryRequestJpaRepository repository;
    private final UserJpaRepository userRepository;
    private final ProductJpaRepository productRepository;
    private final PersistenceMapper mapper;

    @Override
    public InventoryRequest save(InventoryRequest request) {
        InventoryRequestEntity entity = mapper.toEntity(request);
        // getReferenceById no hace SELECT: crea un proxy con solo el id para la FK
        entity.setUser(userRepository.getReferenceById(request.getUserId()));
        entity.setProduct(productRepository.getReferenceById(request.getProductId()));
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<InventoryRequest> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<InventoryRequest> findByUserId(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResults.from(repository.findByUserId(userId, pageable), mapper::toDomain);
    }

    @Override
    public PageResult<InventoryRequest> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResults.from(repository.findAll(pageable), mapper::toDomain);
    }
}

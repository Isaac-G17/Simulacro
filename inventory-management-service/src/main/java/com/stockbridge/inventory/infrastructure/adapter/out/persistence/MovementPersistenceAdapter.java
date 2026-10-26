package com.stockbridge.inventory.infrastructure.adapter.out.persistence;

import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.InventoryMovementEntity;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.InventoryMovementJpaRepository;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.InventoryRequestJpaRepository;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MovementPersistenceAdapter implements MovementRepositoryPort {

    private final InventoryMovementJpaRepository repository;
    private final ProductJpaRepository productRepository;
    private final InventoryRequestJpaRepository requestRepository;
    private final UserJpaRepository userRepository;
    private final PersistenceMapper mapper;

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        InventoryMovementEntity entity = mapper.toEntity(movement);
        entity.setProduct(productRepository.getReferenceById(movement.getProductId()));
        entity.setUser(userRepository.getReferenceById(movement.getUserId()));
        if (movement.getRequestId() != null) {
            entity.setRequest(requestRepository.getReferenceById(movement.getRequestId()));
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<InventoryMovement> findByProductId(Long productId) {
        return repository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

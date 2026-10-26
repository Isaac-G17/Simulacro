package com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository;

import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementEntity, Long> {

    List<InventoryMovementEntity> findByProductIdOrderByCreatedAtDesc(Long productId);
}

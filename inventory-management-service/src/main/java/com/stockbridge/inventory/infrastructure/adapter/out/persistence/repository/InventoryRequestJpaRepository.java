package com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository;

import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.InventoryRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRequestJpaRepository extends JpaRepository<InventoryRequestEntity, Long> {

    /**
     * Reto 2 (N+1): @EntityGraph trae user y product en el mismo SELECT (JOIN)
     * en vez de 1 consulta por cada solicitud. Actívalo cuando la respuesta muestre datos del producto/usuario.
     */
    @EntityGraph(attributePaths = {"user", "product"})
    Page<InventoryRequestEntity> findByUserId(Long userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "product"})
    Page<InventoryRequestEntity> findAll(Pageable pageable);
}

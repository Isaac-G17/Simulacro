package com.stockbridge.inventory.infrastructure.adapter.out.persistence.repository;

import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsByCode(String code);

    /** Consulta JPQL: productos activos con existencias por debajo del mínimo. */
    @Query("SELECT p FROM ProductEntity p WHERE p.active = true AND p.stock < :minimum ORDER BY p.stock ASC")
    List<ProductEntity> findLowStock(@Param("minimum") int minimum);
}

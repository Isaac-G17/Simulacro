package com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper;

import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.domain.model.User;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.InventoryMovementEntity;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.InventoryRequestEntity;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.stockbridge.inventory.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Traduce entre el modelo de dominio y las entidades JPA (MapStruct genera la implementación). */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersistenceMapper {

    Product toDomain(ProductEntity entity);

    ProductEntity toEntity(Product product);

    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    InventoryRequest toDomain(InventoryRequestEntity entity);

    // Las relaciones (user, product) las asigna el adapter con getReferenceById
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    InventoryRequestEntity toEntity(InventoryRequest request);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "request.id", target = "requestId")
    @Mapping(source = "user.id", target = "userId")
    InventoryMovement toDomain(InventoryMovementEntity entity);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "user", ignore = true)
    InventoryMovementEntity toEntity(InventoryMovement movement);
}

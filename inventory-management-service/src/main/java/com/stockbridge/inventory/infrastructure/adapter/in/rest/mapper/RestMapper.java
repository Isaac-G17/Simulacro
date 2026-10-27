package com.stockbridge.inventory.infrastructure.adapter.in.rest.mapper;

import com.stockbridge.inventory.application.port.in.AuthUseCase.AuthResult;
import com.stockbridge.inventory.application.port.in.AuthUseCase.LoginCommand;
import com.stockbridge.inventory.application.port.in.AuthUseCase.RegisterCommand;
import com.stockbridge.inventory.application.port.in.ManageProductUseCase.CreateProductCommand;
import com.stockbridge.inventory.application.port.in.ManageProductUseCase.UpdateProductCommand;
import com.stockbridge.inventory.application.port.in.ManageRequestUseCase.CreateRequestCommand;
import com.stockbridge.inventory.application.port.in.ManageStockUseCase.StockEntryCommand;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.domain.model.SupplierAvailability;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.*;
import org.mapstruct.Mapper;

/** DTO HTTP <-> comandos/modelo de dominio. Los DTOs nunca entran al dominio. */
@Mapper(componentModel = "spring")
public interface RestMapper {

    CreateProductCommand toCommand(ProductRequest request);

    UpdateProductCommand toCommand(ProductUpdateRequest request);

    ProductResponse toResponse(Product product);

    CreateRequestCommand toCommand(CreateInventoryRequestRequest request);

    InventoryRequestResponse toResponse(InventoryRequest request);

    StockEntryCommand toCommand(StockEntryRequest request);

    MovementResponse toResponse(InventoryMovement movement);

    RegisterCommand toCommand(RegisterRequest request);

    LoginCommand toCommand(LoginRequest request);

    AuthResponse toResponse(AuthResult result);

    AvailabilityResponse toResponse(SupplierAvailability availability);
}

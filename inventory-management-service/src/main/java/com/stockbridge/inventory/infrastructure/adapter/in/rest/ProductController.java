package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.port.in.CheckSupplierAvailabilityUseCase;
import com.stockbridge.inventory.application.port.in.ManageProductUseCase;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.*;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.mapper.RestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST: solo traduce HTTP <-> caso de uso. Sin lógica de negocio aquí. */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ManageProductUseCase productUseCase;
    private final CheckSupplierAvailabilityUseCase supplierUseCase;
    private final RestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return mapper.toResponse(productUseCase.create(mapper.toCommand(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return mapper.toResponse(productUseCase.update(id, mapper.toCommand(request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse changeStatus(@PathVariable Long id, @RequestParam boolean active) {
        return mapper.toResponse(productUseCase.changeStatus(id, active));
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return mapper.toResponse(productUseCase.findById(id));
    }

    @GetMapping
    public PageResponse<ProductResponse> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return PageResponse.from(productUseCase.findAll(page, size), mapper::toResponse);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')")
    public List<ProductResponse> findLowStock(@RequestParam(defaultValue = "5") @Min(0) int minimum) {
        return productUseCase.findLowStock(minimum).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}/supplier-availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')")
    public AvailabilityResponse checkSupplier(@PathVariable Long id, @RequestParam @Positive int quantity) {
        return mapper.toResponse(supplierUseCase.check(id, quantity));
    }
}

package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.port.in.ManageRequestUseCase;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.CreateInventoryRequestRequest;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.InventoryRequestResponse;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.PageResponse;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.dto.RejectRequest;
import com.stockbridge.inventory.infrastructure.adapter.in.rest.mapper.RestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final ManageRequestUseCase requestUseCase;
    private final RestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLEADO')")
    public InventoryRequestResponse create(@Valid @RequestBody CreateInventoryRequestRequest request,
                                           @AuthenticationPrincipal CurrentUser user) {
        return mapper.toResponse(requestUseCase.create(mapper.toCommand(request), user));
    }

    @GetMapping("/mine")
    public PageResponse<InventoryRequestResponse> findMine(@AuthenticationPrincipal CurrentUser user,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return PageResponse.from(requestUseCase.findMine(user, page, size), mapper::toResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')")
    public PageResponse<InventoryRequestResponse> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return PageResponse.from(requestUseCase.findAll(page, size), mapper::toResponse);
    }

    /** La verificación de propiedad (EMPLEADO solo ve las suyas) se hace en el caso de uso. */
    @GetMapping("/{id}")
    public InventoryRequestResponse findById(@PathVariable Long id, @AuthenticationPrincipal CurrentUser user) {
        return mapper.toResponse(requestUseCase.findById(id, user));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')")
    public InventoryRequestResponse approve(@PathVariable Long id, @AuthenticationPrincipal CurrentUser user) {
        return mapper.toResponse(requestUseCase.approve(id, user));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')")
    public InventoryRequestResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest body,
                                           @AuthenticationPrincipal CurrentUser user) {
        return mapper.toResponse(requestUseCase.reject(id, body.reason(), user));
    }

    @PatchMapping("/{id}/cancel")
    public InventoryRequestResponse cancel(@PathVariable Long id, @AuthenticationPrincipal CurrentUser user) {
        return mapper.toResponse(requestUseCase.cancel(id, user));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasRole('BODEGA')")
    public InventoryRequestResponse deliver(@PathVariable Long id, @AuthenticationPrincipal CurrentUser user) {
        return mapper.toResponse(requestUseCase.deliver(id, user));
    }
}

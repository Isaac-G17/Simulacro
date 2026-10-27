package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageRequestUseCase;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.RequestRepositoryPort;
import com.stockbridge.inventory.domain.exception.InactiveProductException;
import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.ResourceAccessDeniedException;
import com.stockbridge.inventory.domain.exception.ResourceNotFoundException;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.PageResult;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.domain.model.Role;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService implements ManageRequestUseCase {

    private final RequestRepositoryPort requestRepository;
    private final ProductRepositoryPort productRepository;
    private final MovementRepositoryPort movementRepository;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public InventoryRequest create(CreateRequestCommand command, CurrentUser requester) {
        Product product = findProduct(command.productId());
        if (!product.isActive()) {
            throw new InactiveProductException("El producto " + product.getCode() + " está inactivo y no se puede solicitar");
        }
        // El dominio valida cantidad > 0 (antes de mirar el stock, para dar el error correcto)
        InventoryRequest request = InventoryRequest.create(requester.id(), product.getId(),
                command.quantity(), command.observation());
        if (!product.hasEnoughStock(command.quantity())) {
            throw new InsufficientStockException("Stock insuficiente para " + product.getCode()
                    + ": disponible " + product.getStock() + ", solicitado " + command.quantity()
                    + ". Consulta la disponibilidad del proveedor.");
        }
        InventoryRequest saved = requestRepository.save(request);
        meterRegistry.counter("inventory.requests.created").increment();
        log.info("Solicitud {} creada por usuario {}: {} x {}", saved.getId(), requester.id(),
                saved.getQuantity(), product.getCode());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryRequest findById(Long id, CurrentUser currentUser) {
        InventoryRequest request = findRequest(id);
        // Propiedad del recurso: un EMPLEADO solo puede ver sus propias solicitudes
        if (currentUser.hasRole(Role.EMPLEADO) && !request.isOwnedBy(currentUser.id())) {
            log.warn("Usuario {} intentó consultar la solicitud ajena {}", currentUser.id(), id);
            throw new ResourceAccessDeniedException("No puedes consultar solicitudes de otros usuarios");
        }
        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<InventoryRequest> findMine(CurrentUser currentUser, int page, int size) {
        return requestRepository.findByUserId(currentUser.id(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<InventoryRequest> findAll(int page, int size) {
        return requestRepository.findAll(page, size);
    }

    @Override
    @Transactional
    public InventoryRequest approve(Long id, CurrentUser approver) {
        InventoryRequest request = findRequest(id);
        Product product = findProduct(request.getProductId());
        // El stock pudo cambiar desde que se creó la solicitud
        if (!product.hasEnoughStock(request.getQuantity())) {
            throw new InsufficientStockException("No se puede aprobar: stock de " + product.getCode()
                    + " es " + product.getStock() + " y la solicitud pide " + request.getQuantity());
        }
        request.approve();
        InventoryRequest saved = requestRepository.save(request);
        meterRegistry.counter("inventory.requests.approved").increment();
        log.info("Solicitud {} aprobada por usuario {}", id, approver.id());
        return saved;
    }

    @Override
    @Transactional
    public InventoryRequest reject(Long id, String reason, CurrentUser approver) {
        InventoryRequest request = findRequest(id);
        request.reject(reason);
        InventoryRequest saved = requestRepository.save(request);
        meterRegistry.counter("inventory.requests.rejected").increment();
        log.info("Solicitud {} rechazada por usuario {}: {}", id, approver.id(), reason);
        return saved;
    }

    @Override
    @Transactional
    public InventoryRequest cancel(Long id, CurrentUser currentUser) {
        InventoryRequest request = findRequest(id);
        if (!request.isOwnedBy(currentUser.id()) && !currentUser.hasRole(Role.ADMIN)) {
            throw new ResourceAccessDeniedException("Solo el solicitante o un administrador pueden cancelar la solicitud");
        }
        request.cancel();
        InventoryRequest saved = requestRepository.save(request);
        meterRegistry.counter("inventory.requests.cancelled").increment();
        log.info("Solicitud {} cancelada por usuario {}", id, currentUser.id());
        return saved;
    }

    /**
     * Todo ocurre en la MISMA transacción: si falla el guardado del movimiento o de la solicitud,
     * el descuento de stock hace rollback. Si dos entregas compiten por las mismas unidades,
     * el @Version de ProductEntity hace fallar a la segunda (409).
     */
    @Override
    @Transactional
    public InventoryRequest deliver(Long id, CurrentUser warehouseUser) {
        InventoryRequest request = findRequest(id);
        Product product = findProduct(request.getProductId());

        request.markDelivered();                         // InvalidStateTransitionException si no estaba APROBADA
        product.decreaseStock(request.getQuantity());    // InsufficientStockException si no alcanza

        productRepository.save(product);
        movementRepository.save(InventoryMovement.exitForRequest(product.getId(), request.getId(),
                warehouseUser.id(), request.getQuantity()));
        InventoryRequest saved = requestRepository.save(request);

        meterRegistry.counter("inventory.requests.delivered").increment();
        log.info("Solicitud {} entregada por usuario {}: -{} {} (stock restante {})", id, warehouseUser.id(),
                request.getQuantity(), product.getCode(), product.getStock());
        return saved;
    }

    private InventoryRequest findRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la solicitud con id " + id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + id));
    }
}

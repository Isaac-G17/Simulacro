package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageRequestUseCase;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.RequestRepositoryPort;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.PageResult;
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
    private final MeterRegistry meterRegistry; // métricas: meterRegistry.counter("inventory.requests.created").increment()

    @Override
    @Transactional
    public InventoryRequest create(CreateRequestCommand command, CurrentUser requester) {
        // TODO:
        // 1. buscar producto (ResourceNotFoundException)
        // 2. si !product.isActive() -> InactiveProductException
        // 3. InventoryRequest.create(requester.id(), productId, quantity, observation)  (valida cantidad > 0)
        // 4. (opcional) si !product.hasEnoughStock(quantity) -> avisar / sugerir consultar proveedor
        // 5. guardar, incrementar métrica, log.info
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryRequest findById(Long id, CurrentUser currentUser) {
        // TODO: buscar; si currentUser es EMPLEADO y !request.isOwnedBy(currentUser.id())
        //       -> ResourceAccessDeniedException (o ResourceNotFoundException para no revelar que existe)
        throw new UnsupportedOperationException("TODO");
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
        // TODO: buscar, verificar stock suficiente, request.approve(), guardar, métrica approved
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public InventoryRequest reject(Long id, String reason, CurrentUser approver) {
        // TODO: buscar, request.reject(reason), guardar, métrica rejected
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public InventoryRequest cancel(Long id, CurrentUser currentUser) {
        // TODO: solo el dueño (o ADMIN) puede cancelar; request.cancel(); guardar
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * RETO TÉCNICO: todo ocurre en la MISMA transacción.
     * Si falla el guardado del movimiento, el descuento de stock hace rollback.
     */
    @Override
    @Transactional
    public InventoryRequest deliver(Long id, CurrentUser warehouseUser) {
        // TODO:
        // 1. buscar solicitud y producto
        // 2. product.decreaseStock(request.getQuantity())    -> InsufficientStockException si no alcanza
        // 3. request.markDelivered()                           -> InvalidStateTransitionException si no estaba APROBADA
        // 4. productRepository.save(product)                   (@Version detecta concurrencia)
        // 5. movementRepository.save(InventoryMovement.exitForRequest(...))
        // 6. requestRepository.save(request), log.info
        throw new UnsupportedOperationException("TODO");
    }
}

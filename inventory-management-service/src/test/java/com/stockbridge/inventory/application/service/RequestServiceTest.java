package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageRequestUseCase.CreateRequestCommand;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.RequestRepositoryPort;
import com.stockbridge.inventory.domain.exception.InactiveProductException;
import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.InvalidDomainDataException;
import com.stockbridge.inventory.domain.exception.InvalidStateTransitionException;
import com.stockbridge.inventory.domain.exception.ResourceAccessDeniedException;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.InventoryMovement;
import com.stockbridge.inventory.domain.model.InventoryRequest;
import com.stockbridge.inventory.domain.model.MovementType;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.domain.model.RequestStatus;
import com.stockbridge.inventory.domain.model.Role;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepositoryPort requestRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private MovementRepositoryPort movementRepository;
    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();   // registro real en memoria

    @InjectMocks
    private RequestService requestService;

    private final CurrentUser employee = new CurrentUser(10L, "emp@test.com", Role.EMPLEADO);
    private final CurrentUser otherEmployee = new CurrentUser(11L, "otro@test.com", Role.EMPLEADO);
    private final CurrentUser warehouse = new CurrentUser(2L, "bodega@test.com", Role.BODEGA);

    private Product product(int stock, boolean active) {
        return new Product(1L, "MON-001", "Monitor", null, "MONITORES", BigDecimal.TEN, stock, active, 0L);
    }

    private InventoryRequest request(RequestStatus status, int quantity) {
        return new InventoryRequest(100L, employee.id(), 1L, quantity, status, null, null,
                LocalDateTime.now(), null);
    }

    // ---------- crear ----------

    @Test
    void create_shouldSaveAndCountMetric() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(20, true)));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryRequest created = requestService.create(new CreateRequestCommand(1L, 5, "Sede norte"), employee);

        assertThat(created.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
        assertThat(created.getUserId()).isEqualTo(employee.id());
        assertThat(meterRegistry.counter("inventory.requests.created").count()).isEqualTo(1.0);
    }

    @Test
    void create_shouldRejectInvalidQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(20, true)));

        assertThatThrownBy(() -> requestService.create(new CreateRequestCommand(1L, 0, null), employee))
                .isInstanceOf(InvalidDomainDataException.class);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(20, false)));

        assertThatThrownBy(() -> requestService.create(new CreateRequestCommand(1L, 5, null), employee))
                .isInstanceOf(InactiveProductException.class);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectWhenStockIsInsufficient() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(3, true)));

        assertThatThrownBy(() -> requestService.create(new CreateRequestCommand(1L, 5, null), employee))
                .isInstanceOf(InsufficientStockException.class);
        verify(requestRepository, never()).save(any());
    }

    // ---------- propiedad del recurso ----------

    @Test
    void findById_shouldDenyEmployeeReadingAnotherEmployeesRequest() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.PENDIENTE, 5)));

        assertThatThrownBy(() -> requestService.findById(100L, otherEmployee))
                .isInstanceOf(ResourceAccessDeniedException.class);
    }

    @Test
    void findById_shouldAllowWarehouseToReadAnyRequest() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.PENDIENTE, 5)));

        assertThat(requestService.findById(100L, warehouse).getId()).isEqualTo(100L);
    }

    @Test
    void cancel_shouldDenyNonOwner() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.PENDIENTE, 5)));

        assertThatThrownBy(() -> requestService.cancel(100L, otherEmployee))
                .isInstanceOf(ResourceAccessDeniedException.class);
        verify(requestRepository, never()).save(any());
    }

    // ---------- aprobar / rechazar ----------

    @Test
    void approve_shouldFailWhenStockDroppedBelowRequestedQuantity() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.PENDIENTE, 5)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(2, true)));

        assertThatThrownBy(() -> requestService.approve(100L, warehouse))
                .isInstanceOf(InsufficientStockException.class);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void reject_shouldCountMetric() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.PENDIENTE, 5)));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryRequest rejected = requestService.reject(100L, "Sin presupuesto", warehouse);

        assertThat(rejected.getStatus()).isEqualTo(RequestStatus.RECHAZADA);
        assertThat(meterRegistry.counter("inventory.requests.rejected").count()).isEqualTo(1.0);
    }

    // ---------- entregar ----------

    @Test
    void deliver_shouldDecreaseStockAndRegisterExitMovement() {
        Product product = product(20, true);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.APROBADA, 5)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryRequest delivered = requestService.deliver(100L, warehouse);

        assertThat(delivered.getStatus()).isEqualTo(RequestStatus.ENTREGADA);
        assertThat(product.getStock()).isEqualTo(15);
        verify(productRepository).save(product);
        ArgumentCaptor<InventoryMovement> movement = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(movementRepository).save(movement.capture());
        assertThat(movement.getValue().getType()).isEqualTo(MovementType.SALIDA);
        assertThat(movement.getValue().getQuantity()).isEqualTo(5);
        assertThat(movement.getValue().getRequestId()).isEqualTo(100L);
    }

    @Test
    void deliver_shouldFailWithoutEnoughStock_andSaveNothing() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.APROBADA, 5)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(3, true)));

        assertThatThrownBy(() -> requestService.deliver(100L, warehouse))
                .isInstanceOf(InsufficientStockException.class);
        verify(productRepository, never()).save(any());
        verifyNoInteractions(movementRepository);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void deliver_shouldRejectInvalidTransition_whenRequestIsPending() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request(RequestStatus.PENDIENTE, 5)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(20, true)));

        assertThatThrownBy(() -> requestService.deliver(100L, warehouse))
                .isInstanceOf(InvalidStateTransitionException.class);
        verifyNoInteractions(movementRepository);
    }
}

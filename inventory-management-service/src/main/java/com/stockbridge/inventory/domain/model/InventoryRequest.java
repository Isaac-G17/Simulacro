package com.stockbridge.inventory.domain.model;

import java.time.LocalDateTime;
import com.stockbridge.inventory.domain.exception.InvalidDomainDataException;
import com.stockbridge.inventory.domain.exception.InvalidStateTransitionException;

/** Solicitud interna de productos. Contiene las reglas de cambio de estado. */
public class InventoryRequest {

    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;
    private RequestStatus status;
    private String observation;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryRequest(Long id, Long userId, Long productId, int quantity, RequestStatus status,
                            String observation, String rejectionReason,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.observation = observation;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Nueva solicitud: estado PENDIENTE y fecha de creación actual. */
    public static InventoryRequest create(Long userId, Long productId, int quantity, String observation) {
        if (quantity <= 0) {
            throw new InvalidDomainDataException("La cantidad solicitada debe ser mayor que cero");
        }
        return new InventoryRequest(null, userId, productId, quantity,
                RequestStatus.PENDIENTE, observation, null, LocalDateTime.now(), null);
    }

    public void approve() {
        changeStatus(RequestStatus.APROBADA);
    }

    public void reject(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidDomainDataException("El motivo del rechazo es obligatorio");
        }
        changeStatus(RequestStatus.RECHAZADA);
        this.rejectionReason = reason;
    }

    public void cancel() {
        changeStatus(RequestStatus.CANCELADA);
    }

    public void markDelivered() {
        changeStatus(RequestStatus.ENTREGADA);
    }

    public boolean isOwnedBy(Long otherUserId) {
        return userId != null && userId.equals(otherUserId);
    }

    private void changeStatus(RequestStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "No se puede pasar la solicitud de " + status + " a " + target);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public RequestStatus getStatus() { return status; }
    public String getObservation() { return observation; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

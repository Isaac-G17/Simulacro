package com.stockbridge.inventory.domain.model;

import java.time.LocalDateTime;

/** Solicitud interna de productos. Contiene las reglas de cambio de estado. */
public class InventoryRequest {

    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;
    private RequestStatus status;
    private String observation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryRequest(Long id, Long userId, Long productId, int quantity, RequestStatus status,
                            String observation, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.observation = observation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Nueva solicitud: estado PENDIENTE y fecha de creación actual. */
    public static InventoryRequest create(Long userId, Long productId, int quantity, String observation) {
        // TODO: quantity > 0, si no lanzar InvalidDomainDataException
        throw new UnsupportedOperationException("TODO");
    }

    public void approve() {
        changeStatus(RequestStatus.APROBADA);
    }

    public void reject(String reason) {
        // TODO: guardar el motivo en observation
        changeStatus(RequestStatus.RECHAZADA);
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
        // TODO: si !status.canTransitionTo(target) lanzar InvalidStateTransitionException
        //       si es válido: status = target; updatedAt = now
        throw new UnsupportedOperationException("TODO");
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public RequestStatus getStatus() { return status; }
    public String getObservation() { return observation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

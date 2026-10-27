package com.stockbridge.inventory.domain.model;

import java.time.LocalDateTime;

/** Registro de trazabilidad: cada cambio de existencias deja un movimiento. */
public class InventoryMovement {

    private Long id;
    private Long productId;
    private Long requestId;
    private Long userId;
    private MovementType type;
    private int quantity;
    private String note;
    private LocalDateTime createdAt;

    public InventoryMovement(Long id, Long productId, Long requestId, Long userId, MovementType type,
                             int quantity, String note, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.requestId = requestId;
        this.userId = userId;
        this.type = type;
        this.quantity = quantity;
        this.note = note;
        this.createdAt = createdAt;
    }

    /** Salida de inventario asociada a la entrega de una solicitud. */
    public static InventoryMovement exitForRequest(Long productId, Long requestId, Long userId, int quantity) {
        return new InventoryMovement(null, productId, requestId, userId, MovementType.SALIDA,
                quantity, "Entrega de solicitud " + requestId, LocalDateTime.now());
    }

    /** Entrada de inventario (reposición) registrada por bodega o administración. */
    public static InventoryMovement entry(Long productId, Long userId, int quantity, String note) {
        return new InventoryMovement(null, productId, null, userId, MovementType.ENTRADA,
                quantity, note, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getRequestId() { return requestId; }
    public Long getUserId() { return userId; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

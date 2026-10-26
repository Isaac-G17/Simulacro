package com.stockbridge.inventory.domain.model;

public enum RequestStatus {
    PENDIENTE,
    APROBADA,
    RECHAZADA,
    ENTREGADA,
    CANCELADA;

    /**
     * Transiciones válidas:
     *   PENDIENTE -> APROBADA | RECHAZADA | CANCELADA
     *   APROBADA  -> ENTREGADA | CANCELADA
     *   RECHAZADA, ENTREGADA, CANCELADA -> (estados finales, ninguna)
     */
    public boolean canTransitionTo(RequestStatus target) {
        // TODO: implementar la tabla de transiciones (un switch sobre this)
        throw new UnsupportedOperationException("TODO");
    }
}

package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InvalidDomainDataException;
import com.stockbridge.inventory.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryRequestTest {

    @Test
    void create_shouldStartPendingWithCreationDate() {
        InventoryRequest request = InventoryRequest.create(1L, 2L, 5, "Para la sede norte");

        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
        assertThat(request.getCreatedAt()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    void create_shouldRejectInvalidQuantity(int quantity) {
        assertThatThrownBy(() -> InventoryRequest.create(1L, 2L, quantity, null))
                .isInstanceOf(InvalidDomainDataException.class);
    }

    @Test
    void reject_shouldKeepObservationAndStoreReason() {
        InventoryRequest request = InventoryRequest.create(1L, 2L, 5, "Para la sede norte");

        request.reject("Sin presupuesto");

        assertThat(request.getStatus()).isEqualTo(RequestStatus.RECHAZADA);
        assertThat(request.getObservation()).isEqualTo("Para la sede norte");
        assertThat(request.getRejectionReason()).isEqualTo("Sin presupuesto");
    }

    @Test
    void reject_shouldRequireReason() {
        InventoryRequest request = InventoryRequest.create(1L, 2L, 5, null);

        assertThatThrownBy(() -> request.reject(" ")).isInstanceOf(InvalidDomainDataException.class);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
    }

    @Test
    void markDelivered_shouldFailWhenPending() {
        InventoryRequest request = InventoryRequest.create(1L, 2L, 5, null);

        assertThatThrownBy(request::markDelivered).isInstanceOf(InvalidStateTransitionException.class);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
    }

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "PENDIENTE, APROBADA, true",
            "PENDIENTE, RECHAZADA, true",
            "PENDIENTE, CANCELADA, true",
            "PENDIENTE, ENTREGADA, false",
            "APROBADA, ENTREGADA, true",
            "APROBADA, CANCELADA, true",
            "APROBADA, RECHAZADA, false",
            "RECHAZADA, APROBADA, false",
            "ENTREGADA, CANCELADA, false",
            "CANCELADA, PENDIENTE, false"
    })
    void canTransitionTo_followsTheStateTable(RequestStatus from, RequestStatus to, boolean expected) {
        assertThat(from.canTransitionTo(to)).isEqualTo(expected);
    }
}

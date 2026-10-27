package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.SupplierClientPort;
import com.stockbridge.inventory.domain.exception.ResourceNotFoundException;
import com.stockbridge.inventory.domain.exception.SupplierUnavailableException;
import com.stockbridge.inventory.domain.model.Product;
import com.stockbridge.inventory.domain.model.SupplierAvailability;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** El proveedor externo se simula con Mockito: no hace falta que el otro servicio esté levantado. */
@ExtendWith(MockitoExtension.class)
class SupplierAvailabilityServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private SupplierClientPort supplierClient;

    @InjectMocks
    private SupplierAvailabilityService service;

    private void givenExistingProduct() {
        Product product = new Product(1L, "MON-001", "Monitor", null, "MONITORES", BigDecimal.TEN, 2, true, 0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    }

    @Test
    void check_shouldSendProductCodeAndCategoryToSupplier() {
        givenExistingProduct();
        var expected = new SupplierAvailability("SUP-003", true, 25, 3);
        when(supplierClient.checkAvailability("MON-001", 10, "MONITORES")).thenReturn(expected);

        assertThat(service.check(1L, 10)).isEqualTo(expected);
    }

    @Test
    void check_shouldPropagateSupplierUnavailable_whenSupplierIsDown() {
        givenExistingProduct();
        when(supplierClient.checkAvailability(anyString(), anyInt(), anyString()))
                .thenThrow(new SupplierUnavailableException("El servicio de proveedores no está disponible"));

        assertThatThrownBy(() -> service.check(1L, 10)).isInstanceOf(SupplierUnavailableException.class);
    }

    @Test
    void check_shouldNotCallSupplier_whenProductDoesNotExist() {
        assertThatThrownBy(() -> service.check(99L, 10)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(supplierClient);
    }
}

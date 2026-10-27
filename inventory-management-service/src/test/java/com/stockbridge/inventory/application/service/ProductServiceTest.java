package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ManageProductUseCase.CreateProductCommand;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.domain.exception.DuplicateResourceException;
import com.stockbridge.inventory.domain.exception.InvalidDomainDataException;
import com.stockbridge.inventory.domain.exception.ResourceNotFoundException;
import com.stockbridge.inventory.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Prueba unitaria: el puerto de salida se simula con Mockito, sin Spring ni base de datos. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @InjectMocks
    private ProductService productService;

    private final CreateProductCommand command = new CreateProductCommand("MON-001", "Monitor 24\"",
            "Full HD", "MONITORES", new BigDecimal("650000"), 20);

    @Test
    void create_shouldSaveProduct_whenCodeIsNew() {
        when(productRepository.existsByCode("MON-001")).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product created = productService.create(command);

        assertThat(created.getCode()).isEqualTo("MON-001");
        assertThat(created.getStock()).isEqualTo(20);
        assertThat(created.isActive()).isTrue();
        verify(productRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicate_whenCodeExists() {
        when(productRepository.existsByCode("MON-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(command)).isInstanceOf(DuplicateResourceException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldNotSave_whenPriceIsInvalid() {
        when(productRepository.existsByCode("MON-001")).thenReturn(false);
        var invalid = new CreateProductCommand("MON-001", "Monitor", null, "MONITORES", BigDecimal.ZERO, 1);

        assertThatThrownBy(() -> productService.create(invalid)).isInstanceOf(InvalidDomainDataException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void findById_shouldThrowNotFound_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeStatus_shouldDeactivateProduct() {
        Product product = new Product(1L, "MON-001", "Monitor", null, "MONITORES", BigDecimal.TEN, 5, true, 0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(productService.changeStatus(1L, false).isActive()).isFalse();
    }
}

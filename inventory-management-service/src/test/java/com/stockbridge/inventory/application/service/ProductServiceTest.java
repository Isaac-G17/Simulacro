package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Prueba unitaria: el puerto de salida se simula con Mockito, sin Spring ni base de datos. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @Disabled("TODO: implementar")
    void create_shouldSaveProduct_whenCodeIsNew() {
        // given: when(productRepository.existsByCode("MON-001")).thenReturn(false);
        //        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // when:  productService.create(new CreateProductCommand(...));
        // then:  verify(productRepository).save(any());  assertThat(...)
    }

    @Test
    @Disabled("TODO: implementar")
    void create_shouldThrowDuplicate_whenCodeExists() {
        // assertThrows(DuplicateResourceException.class, () -> productService.create(...));
        // verify(productRepository, never()).save(any());
    }
}

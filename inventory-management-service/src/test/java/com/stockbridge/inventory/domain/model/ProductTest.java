package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.InvalidDomainDataException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Dominio puro: no hace falta Mockito ni Spring. */
class ProductTest {

    private Product product(int stock) {
        return Product.create("MON-001", "Monitor", null, "MONITORES", new BigDecimal("650000"), stock);
    }

    @Test
    void create_shouldStartActive() {
        assertThat(product(5).isActive()).isTrue();
    }

    @Test
    void create_shouldRejectPriceZeroOrNegative() {
        assertThatThrownBy(() -> Product.create("X", "X", null, "C", BigDecimal.ZERO, 1))
                .isInstanceOf(InvalidDomainDataException.class);
        assertThatThrownBy(() -> Product.create("X", "X", null, "C", new BigDecimal("-1"), 1))
                .isInstanceOf(InvalidDomainDataException.class);
    }

    @Test
    void create_shouldRejectNegativeStock() {
        assertThatThrownBy(() -> product(-1)).isInstanceOf(InvalidDomainDataException.class);
    }

    @Test
    void decreaseStock_shouldSubtract() {
        Product product = product(10);
        product.decreaseStock(4);
        assertThat(product.getStock()).isEqualTo(6);
    }

    @Test
    void decreaseStock_shouldNeverLeaveNegativeStock() {
        Product product = product(3);
        assertThatThrownBy(() -> product.decreaseStock(5)).isInstanceOf(InsufficientStockException.class);
        assertThat(product.getStock()).isEqualTo(3);
    }

    @Test
    void increaseStock_shouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> product(1).increaseStock(0)).isInstanceOf(InvalidDomainDataException.class);
    }
}

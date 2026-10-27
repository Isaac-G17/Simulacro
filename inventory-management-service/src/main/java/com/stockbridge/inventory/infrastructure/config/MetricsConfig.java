package com.stockbridge.inventory.infrastructure.config;

import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Gauge de productos activos con stock bajo. A diferencia de un contador, el gauge no se incrementa:
 * Micrometer ejecuta la consulta cada vez que alguien lee la métrica (/actuator/metrics o Prometheus).
 */
@Configuration
public class MetricsConfig {

    public MetricsConfig(MeterRegistry meterRegistry, ProductRepositoryPort productRepository,
                         @Value("${app.inventory.low-stock-threshold}") int threshold) {
        Gauge.builder("inventory.products.low-stock", () -> productRepository.countByStockLessThan(threshold))
                .description("Productos activos con stock menor a " + threshold)
                .tag("threshold", String.valueOf(threshold))
                .register(meterRegistry);
    }
}

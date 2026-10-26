package com.stockbridge.inventory.domain.model;

import java.util.List;

/** Página de resultados propia del dominio, para no exponer org.springframework.data.domain.Page. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}

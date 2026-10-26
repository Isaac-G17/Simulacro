package com.stockbridge.inventory.infrastructure.adapter.out.persistence.mapper;

import com.stockbridge.inventory.domain.model.PageResult;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public final class PageResults {

    private PageResults() {
    }

    public static <E, D> PageResult<D> from(Page<E> page, Function<E, D> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}

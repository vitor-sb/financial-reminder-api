package com.reminder.financial.api.domains.pagination;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, PageRequest request, long totalElements) {

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(content.stream().<R>map(mapper).toList(), request, totalElements);
    }

    public int totalPages() {
        return request.size() == 0 ? 1 : (int) Math.ceil((double) totalElements / request.size());
    }
}

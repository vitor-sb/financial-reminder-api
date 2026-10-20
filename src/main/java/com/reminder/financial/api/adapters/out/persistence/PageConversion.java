package com.reminder.financial.api.adapters.out.persistence;

import com.reminder.financial.api.domains.pagination.PageRequest;
import com.reminder.financial.api.domains.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class PageConversion {

    private PageConversion() {}

    static Pageable toPageable(PageRequest request) {
        Sort sort = Sort.by(request.orders().stream()
                .map(o -> new Sort.Order(o.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, o.property()))
                .toList());
        return org.springframework.data.domain.PageRequest.of(request.page(), request.size(), sort);
    }

    static <T> PageResult<T> toResult(Page<T> page, PageRequest request) {
        return new PageResult<>(page.getContent(), request, page.getTotalElements());
    }
}

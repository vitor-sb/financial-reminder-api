package com.reminder.financial.api.domains.pagination;

import java.util.List;

public record PageRequest(int page, int size, List<Order> orders) {

    public record Order(String property, boolean ascending) {}

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, List.of());
    }

    public long offset() {
        return (long) page * size;
    }
}

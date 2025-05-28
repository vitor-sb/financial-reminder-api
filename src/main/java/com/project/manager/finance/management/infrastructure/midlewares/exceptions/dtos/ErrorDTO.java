package com.project.manager.finance.management.infrastructure.midlewares.exceptions.dtos;

import java.time.LocalDateTime;

public record ErrorDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ErrorDTO(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}


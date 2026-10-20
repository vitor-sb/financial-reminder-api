package com.reminder.financial.api.adapters.dtos.user;

import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank
        @Size(max=100)
        String name,
        @NotBlank
        @Email
        @Size(max=254)
        String email,
        @NotBlank
        String password
) {
}

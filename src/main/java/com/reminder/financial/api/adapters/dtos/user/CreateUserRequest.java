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
        @Size(min=10, max=72, message="size must be between 10 and 72")
        String password
) {
    @AssertTrue(message = "password must differ from email")
    private boolean isPasswordDifferentFromEmail() {
        return password == null || !password.equalsIgnoreCase(email);
    }
}

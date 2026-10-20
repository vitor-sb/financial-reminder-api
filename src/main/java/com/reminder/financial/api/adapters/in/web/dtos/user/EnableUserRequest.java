package com.reminder.financial.api.adapters.in.web.dtos.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnableUserRequest(
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

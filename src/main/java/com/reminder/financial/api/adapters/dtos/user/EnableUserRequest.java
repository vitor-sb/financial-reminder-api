package com.reminder.financial.api.adapters.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnableUserRequest(
        @NotBlank
        @Email
        @Size(max=254)
        String email,

        @NotBlank
        @Size(max=72)
        String password
) {
}

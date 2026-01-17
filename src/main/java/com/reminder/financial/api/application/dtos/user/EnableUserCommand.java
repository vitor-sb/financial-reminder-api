package com.reminder.financial.api.application.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnableUserCommand(
        @NotBlank
        @Email
        @Size(max=254)
        String email
) {
}

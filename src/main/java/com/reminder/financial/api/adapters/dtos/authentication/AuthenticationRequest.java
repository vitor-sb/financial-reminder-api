package com.reminder.financial.api.adapters.dtos.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) { }

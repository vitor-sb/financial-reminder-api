package com.project.manager.finance.management.adapters.dtos.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) { }

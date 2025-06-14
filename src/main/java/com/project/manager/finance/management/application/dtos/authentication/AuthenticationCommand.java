package com.project.manager.finance.management.application.dtos.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationCommand(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) { }

package com.project.manager.finance_management.adapters.dtos.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO(
        @NotNull
        @Email
        String login,
        @NotNull
        String password
) { }

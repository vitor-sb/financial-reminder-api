package com.project.manager.finance_management.adapters.dtos.authentication;

import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO(
        @NotNull
        String login,
        @NotNull
        String password
) { }

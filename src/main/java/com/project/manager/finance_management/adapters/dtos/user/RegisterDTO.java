package com.project.manager.finance_management.adapters.dtos.user;

import com.project.manager.finance_management.domains.models.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @NotNull
        String name,
        @NotNull
        @Email
        String email,
        @NotNull
        String password,
        @NotNull
        Role role
) {
}

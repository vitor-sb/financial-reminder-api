package com.project.manager.finance_management.adapters.dtos.user;

import com.project.manager.finance_management.domains.models.user.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        @NotBlank
        RoleEnum role
) {
}

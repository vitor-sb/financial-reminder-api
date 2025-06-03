package com.project.manager.finance.management.adapters.dtos.user;

import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank
        @Size(max=100)
        String name,
        @NotBlank
        @Email
        @Size(max=254)
        String email,
        @NotBlank
        String password,
        @NotNull
        RoleEnum role
) {
}

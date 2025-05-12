package com.project.manager.finance_management.adapters.dtos.user;

import com.project.manager.finance_management.domains.models.user.enums.RoleEnum;

public record ListUsersResponseDTO(String name, String email, RoleEnum role) {
}

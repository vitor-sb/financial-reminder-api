package com.project.manager.finance_management.adapters.dtos.user;

import com.project.manager.finance_management.domains.models.user.Role;

public record ListUsersResponseDTO(String name, String email, Role role) {
}

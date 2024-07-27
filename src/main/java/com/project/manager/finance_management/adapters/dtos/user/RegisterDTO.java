package com.project.manager.finance_management.adapters.dtos.user;

import com.project.manager.finance_management.domains.models.user.Role;

public record RegisterDTO(String name, String login, String password, Role role) {
}

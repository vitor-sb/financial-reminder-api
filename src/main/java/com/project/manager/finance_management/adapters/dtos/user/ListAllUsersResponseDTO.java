package com.project.manager.finance_management.adapters.dtos.user;

import com.project.manager.finance_management.domains.models.user.Role;

import java.util.List;

public record ListAllUsersResponseDTO(String name, String email, List<Role> roles) {
}

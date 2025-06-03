package com.project.manager.finance.management.adapters.dtos.user;

import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;

public record ListUsersSingleResponse(String name, String email, RoleEnum role, Boolean enabled, Boolean deleted) {
}

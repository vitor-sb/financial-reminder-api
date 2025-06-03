package com.project.manager.finance.management.application.dtos;

import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;

public record ListUsersSingleResult(String name, String email, RoleEnum role, Boolean enabled, Boolean deleted) {
}

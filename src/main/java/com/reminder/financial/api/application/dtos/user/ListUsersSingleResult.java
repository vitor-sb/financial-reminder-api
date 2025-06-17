package com.reminder.financial.api.application.dtos.user;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;

public record ListUsersSingleResult(String name, String email, RoleEnum role, Boolean enabled, Boolean deleted) {
}

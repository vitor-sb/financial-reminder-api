package com.reminder.financial.api.adapters.dtos.user;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;

public record ListUsersFilterRequest(
        String name,
        String email,
        RoleEnum role,
        Boolean enabled,
        Boolean deleted
) {
}

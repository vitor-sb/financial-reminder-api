package com.reminder.financial.api.application.dtos.user;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;

import java.time.Instant;

public record ListUsersSingleResult(
        String name,
        String email,
        RoleEnum role,
        Instant create_at,
        Boolean enabled,
        Boolean deleted
) {
}

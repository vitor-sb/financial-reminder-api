package com.reminder.financial.api.adapters.dtos.user;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;

import java.time.Instant;


public record ListUsersSingleResponse(
        String name,
        String email,
        RoleEnum role,
        Instant created_at,
        Boolean enabled,
        Boolean deleted
) {
}

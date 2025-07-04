package com.reminder.financial.api.adapters.dtos.user;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;

import java.time.LocalDate;

public record ListUsersFilterRequest(
        String name,
        String email,
        RoleEnum role,
        LocalDate createdFrom,
        LocalDate createdUntil,
        Boolean enabled,
        Boolean deleted
) {
}

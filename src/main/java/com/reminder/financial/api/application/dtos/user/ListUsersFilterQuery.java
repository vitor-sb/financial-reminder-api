package com.reminder.financial.api.application.dtos.user;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;

import java.time.LocalDate;

public record ListUsersFilterQuery(
        String name,
        String email,
        RoleEnum role,
        LocalDate startDate,
        LocalDate endDate,
        Boolean enabled,
        Boolean deleted
) {
}

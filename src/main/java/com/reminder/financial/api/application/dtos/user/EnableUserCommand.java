package com.reminder.financial.api.application.dtos.user;

public record EnableUserCommand(
        String email,
        String password
) {
}

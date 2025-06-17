package com.reminder.financial.api.application.usecases.user;

import com.reminder.financial.api.application.dtos.user.CreateUserCommand;

public interface CreateUserUseCase {
    void execute(CreateUserCommand dto);
}

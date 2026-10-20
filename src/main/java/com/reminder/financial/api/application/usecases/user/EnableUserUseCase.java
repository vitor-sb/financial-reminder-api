package com.reminder.financial.api.application.usecases.user;

import com.reminder.financial.api.application.dtos.user.EnableUserCommand;

public interface EnableUserUseCase {
    void execute(EnableUserCommand command);
}

package com.project.manager.finance.management.application.usecases.user;

import com.project.manager.finance.management.application.dtos.user.CreateUserCommand;

public interface CreateUserUseCase {
    void execute(CreateUserCommand dto);
}

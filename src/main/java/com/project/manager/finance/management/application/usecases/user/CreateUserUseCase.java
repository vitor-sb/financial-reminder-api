package com.project.manager.finance.management.application.usecases.user;

import com.project.manager.finance.management.application.dtos.CreateUserCommand;

public interface CreateUserUseCase {
    void execute(CreateUserCommand dto);
}

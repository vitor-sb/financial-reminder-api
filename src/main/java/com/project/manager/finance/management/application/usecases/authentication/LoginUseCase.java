package com.project.manager.finance.management.application.usecases.authentication;

import com.project.manager.finance.management.application.dtos.AuthenticationCommand;
import com.project.manager.finance.management.application.dtos.AuthenticationResult;

public interface LoginUseCase {
    AuthenticationResult execute(AuthenticationCommand dto);
}

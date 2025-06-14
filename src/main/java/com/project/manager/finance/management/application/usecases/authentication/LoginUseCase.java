package com.project.manager.finance.management.application.usecases.authentication;

import com.project.manager.finance.management.application.dtos.authentication.AuthenticationCommand;
import com.project.manager.finance.management.application.dtos.authentication.AuthenticationResult;

public interface LoginUseCase {
    AuthenticationResult execute(AuthenticationCommand dto);
}

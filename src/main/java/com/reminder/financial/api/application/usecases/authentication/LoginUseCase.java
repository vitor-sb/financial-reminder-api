package com.reminder.financial.api.application.usecases.authentication;

import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationResult;

public interface LoginUseCase {
    AuthenticationResult execute(AuthenticationCommand dto);
}

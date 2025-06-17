package com.reminder.financial.api.adapters.mappers;

import com.reminder.financial.api.adapters.dtos.authentication.AuthenticationRequest;
import com.reminder.financial.api.adapters.dtos.authentication.AuthenticationResponse;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationResult;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMapper {

    public AuthenticationResponse toResponse(AuthenticationResult result) {
        return new AuthenticationResponse(
                result.token()
        );
    }

    public AuthenticationCommand toCommand(AuthenticationRequest request) {
        return new AuthenticationCommand(
                request.email(),
                request.password()
        );
    }
}
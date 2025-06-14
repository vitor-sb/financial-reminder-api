package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationRequest;
import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationResponse;
import com.project.manager.finance.management.application.dtos.authentication.AuthenticationCommand;
import com.project.manager.finance.management.application.dtos.authentication.AuthenticationResult;
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
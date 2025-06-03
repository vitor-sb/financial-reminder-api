package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationRequest;
import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationResponse;
import com.project.manager.finance.management.adapters.dtos.paymentremider.GetPaymentReminderResponse;
import com.project.manager.finance.management.application.dtos.AuthenticationCommand;
import com.project.manager.finance.management.application.dtos.AuthenticationResult;
import com.project.manager.finance.management.application.dtos.PaymentReminderResult;
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
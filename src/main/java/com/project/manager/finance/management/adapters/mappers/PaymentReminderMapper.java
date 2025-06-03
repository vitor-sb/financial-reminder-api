package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderRequest;
import com.project.manager.finance.management.adapters.dtos.paymentremider.GetPaymentReminderResponse;
import com.project.manager.finance.management.application.dtos.CreatePaymentReminderCommand;
import com.project.manager.finance.management.application.dtos.GetPaymentReminderQuery;
import com.project.manager.finance.management.application.dtos.PaymentReminderResult;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderMapper {

    public GetPaymentReminderResponse toResponse(PaymentReminderResult result) {
        return new GetPaymentReminderResponse(
                result.title(),
                result.description(),
                result.date(),
                result.recurrence(),
                result.method(),
                result.value(),
                result.installmentsNumber()
        );
    }

    public CreatePaymentReminderCommand toCommand(@Valid CreatePaymentReminderRequest dto, String loggedUser) {
        return new CreatePaymentReminderCommand(
                dto.title(),
                dto.description(),
                dto.date(),
                dto.recurrence(), dto.method(),
                dto.value(),
                dto.installmentsNumber(),
                loggedUser
        );
    }

    public GetPaymentReminderQuery toQuery(String id, String name) {
        return new GetPaymentReminderQuery(
                id, name
        );
    }
}
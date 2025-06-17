package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderRequest;
import com.project.manager.finance.management.adapters.dtos.paymentremider.PaymentReminderResponse;
import com.project.manager.finance.management.adapters.dtos.paymentremider.UpdatePaymentReminderRequest;
import com.project.manager.finance.management.application.dtos.paymentreminder.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderMapper {

    public PaymentReminderResponse toResponse(PaymentReminderResult result) {
        return new PaymentReminderResponse(
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
                dto.recurrence(),
                dto.method(),
                dto.value(),
                dto.installmentsNumber(),
                loggedUser
        );
    }

    public UpdatePaymentReminderCommand toCommand(@Valid UpdatePaymentReminderRequest dto, String reminderId, String loggedUser) {
        return new UpdatePaymentReminderCommand(
                reminderId,
                dto.updatePendingTransaction(),
                dto.title(),
                dto.description(),
                dto.method(),
                dto.value(),
                loggedUser
        );
    }

    public GetPaymentReminderQuery toQuery(String id, String name) {
        return new GetPaymentReminderQuery(
                id, name
        );
    }
}
package com.project.manager.finance.management.adapters.usecases.paymentreminder;

import com.project.manager.finance.management.adapters.dtos.paymentremider.ResponseGetPaymentReminderDTO;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.ConflictException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class GetPaymentReminderUseCase {
    @Autowired
    PaymentReminderService service;

    public ResponseGetPaymentReminderDTO execute(String id, String email) {
        PaymentReminder paymentReminder = this.service.getPaymentReminderById(id).orElseThrow(
                () -> new NotFoundException("Payment reminder not found")
        );

        if(!Objects.equals(paymentReminder.getUser().getEmail(), email))
            throw new ConflictException("This reminder does not belong to the logged in user");

        return new ResponseGetPaymentReminderDTO(
                paymentReminder.getTitle(),
                paymentReminder.getDescription(),
                paymentReminder.getBaseDate().toString(),
                paymentReminder.getRecurrence(),
                paymentReminder.getMethod(),
                paymentReminder.getBaseValue(),
                paymentReminder.getInstallmentsNumber()
        );
    }
}

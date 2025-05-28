package com.project.manager.finance.management.adapters.usecases.paymentreminder;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderDTO;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.UserService;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.ConflictException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CreatePaymentReminderUseCase {
    @Autowired
    PaymentReminderService service;
    @Autowired
    UserService userService;

    public PaymentReminder execute(CreatePaymentReminderDTO dto, String email) {
        if(this.service.paymentReminderExists(dto.title(), email))
            throw new ConflictException("Reminder title already in use");

        User user = userService.getUserByEmail(email).orElseThrow(
                () -> new NotFoundException("Authenticated user not found")
        );

        PaymentReminder reminder = new PaymentReminder(
                user,
                dto.title(),
                dto.description(),
                LocalDate.parse(dto.date()),
                dto.recurrence(),
                dto.method(),
                dto.value(),
                dto.installmentsNumber()
        );

        return service.createPaymentReminder(reminder);
    }
}

package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.CreatePaymentReminderCommand;
import com.reminder.financial.api.application.exceptions.ConflictException;
import com.reminder.financial.api.application.usecases.paymentreminder.CreatePaymentReminderUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import com.reminder.financial.api.domains.services.UserService;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class CreatePaymentReminderUseCaseImpl implements CreatePaymentReminderUseCase {

    @Autowired
    PaymentReminderService paymentReminderService;

    @Autowired
    UserService userService;

    @Autowired
    PaymentTransactionService transactionService;

    public PaymentReminder execute(CreatePaymentReminderCommand dto) {
        paymentReminderService.validateReminderTitleConflict(dto.title(), dto.email());

        User user = userService.getActiveUserByEmail(dto.email()).orElseThrow(
                () -> new NotFoundException("Authenticated user not found")
        );

        LocalDate reminderDate = parseAndValidateReminderDate(dto.date());

        PaymentReminder reminder = new PaymentReminder(
                user,
                dto.title(),
                dto.description(),
                reminderDate,
                dto.recurrence(),
                dto.method(),
                dto.value(),
                dto.installmentsNumber()
        );

        var persistedReminder = paymentReminderService.savePaymentReminder(reminder);

        transactionService.createPaymentTransactionByReminder(reminder);

        return persistedReminder;
    }

    private LocalDate parseAndValidateReminderDate(String dateString) {
        try {
            LocalDate parsedDate = LocalDate.parse(dateString);

            if (parsedDate.isBefore(LocalDate.now())) {
                throw new ConflictException("Reminder date cannot be in the past.");
            }

            return parsedDate;

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for reminder. Expected 'YYYY-MM-DD'");
        }
    }
}

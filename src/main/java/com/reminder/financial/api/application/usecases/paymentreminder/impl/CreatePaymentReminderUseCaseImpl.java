package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.CreatePaymentReminderCommand;
import com.reminder.financial.api.application.usecases.paymentreminder.CreatePaymentReminderUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import com.reminder.financial.api.domains.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RequiredArgsConstructor
@Component
public class CreatePaymentReminderUseCaseImpl implements CreatePaymentReminderUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreatePaymentReminderUseCaseImpl.class);

    private final PaymentReminderService paymentReminderService;

    private final UserService userService;

    private final PaymentTransactionService transactionService;

    public PaymentReminder execute(String userEmail, CreatePaymentReminderCommand dto) {
        paymentReminderService.validateReminderTitleConflict(dto.title(), userEmail);

        User user = userService.findUserByEmailOrThrow(userEmail);
        user.getAudit().validateIsActive("Authenticated user");

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

        transactionService.createInitialTransactionForReminder(reminder);

        logger.info("reminder created reminderId={} userId={}", persistedReminder.getId(), user.getId());

        return persistedReminder;
    }

    private LocalDate parseAndValidateReminderDate(String dateString) {
        try {
            LocalDate parsedDate = LocalDate.parse(dateString);

            if (parsedDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Reminder date cannot be in the past.");
            }

            return parsedDate;

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for reminder. Expected 'YYYY-MM-DD'");
        }
    }
}

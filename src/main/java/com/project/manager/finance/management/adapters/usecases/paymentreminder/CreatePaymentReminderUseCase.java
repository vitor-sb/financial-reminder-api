package com.project.manager.finance.management.adapters.usecases.paymentreminder;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderDTO;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import com.project.manager.finance.management.domains.services.UserService;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.ConflictException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class CreatePaymentReminderUseCase {
    @Autowired
    PaymentReminderService reminderService;
    @Autowired
    UserService userService;
    @Autowired
    PaymentTransactionService transactionService;

    public PaymentReminder execute(CreatePaymentReminderDTO dto, String email) {
        reminderService.validateReminderTitleConflict(dto.title(), email);

        User user = userService.getUserByEmail(email).orElseThrow(
                () -> new NotFoundException("Authenticated user not found")
        );

        LocalDate reminderDate = parseReminderDate(dto.date());
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

        var persistedReminder = reminderService.createPaymentReminder(reminder);

        transactionService.createPaymentTransactionByReminder(reminder);

        return persistedReminder;
    }

    private LocalDate parseReminderDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for reminder. Expected 'YYYY-MM-DD'");
        }
    }
}

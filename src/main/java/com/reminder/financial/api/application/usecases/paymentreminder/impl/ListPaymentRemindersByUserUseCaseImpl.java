package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersResult;
import com.reminder.financial.api.application.usecases.paymentreminder.ListPaymentRemindersByUserUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import com.reminder.financial.api.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListPaymentRemindersByUserUseCaseImpl implements ListPaymentRemindersByUserUseCase {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentTransactionService transactionService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Override
    public Page<ListPaymentRemindersResult> execute(String loggedUserEmail, Pageable pageable) {
        User user = userService.findUserByEmailOrThrow(loggedUserEmail);
        user.getAudit().validateIsActive("User");

        Page<PaymentReminder> reminderPage = paymentReminderService.listPaymentRemindersByUser(user.getId(), pageable);

        return reminderPage.map(reminder -> new ListPaymentRemindersResult(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getBaseDate(),
                reminder.getRecurrence(),
                reminder.getMethod(),
                reminder.getBaseValue(),
                reminder.getInstallmentsNumber()
        ));
    }
}
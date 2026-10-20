package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.domains.filters.ReminderFilter;
import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersResult;
import com.reminder.financial.api.application.usecases.paymentreminder.ListPaymentRemindersByUserUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import com.reminder.financial.api.domains.services.UserService;
import lombok.RequiredArgsConstructor;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListPaymentRemindersByUserUseCaseImpl implements ListPaymentRemindersByUserUseCase {

    private final UserService userService;

    private final PaymentTransactionService transactionService;

    private final PaymentReminderService paymentReminderService;

    @Override
    public PageResult<ListPaymentRemindersResult> execute(
            String loggedUserEmail,
            ReminderFilter query,
            PageRequest pageable) {
        User user = userService.findValidUserByEmail(loggedUserEmail);

        PageResult<PaymentReminder> reminderPage =
                paymentReminderService.listPaymentRemindersByUserAndFilters(
                        user,
                        query,
                        pageable
                );

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
package com.project.manager.finance.management.application.usecases.paymentreminder;

import com.project.manager.finance.management.application.dtos.paymentreminder.CreatePaymentReminderCommand;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;

public interface CreatePaymentReminderUseCase {
    PaymentReminder execute(CreatePaymentReminderCommand dto);
}

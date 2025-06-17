package com.project.manager.finance.management.application.usecases.paymentreminder;

import com.project.manager.finance.management.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;

public interface UpdatePaymentReminderUseCase {
    PaymentReminder execute(UpdatePaymentReminderCommand dto);
}

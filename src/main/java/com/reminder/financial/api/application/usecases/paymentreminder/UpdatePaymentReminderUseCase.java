package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;

public interface UpdatePaymentReminderUseCase {
    PaymentReminder execute(UpdatePaymentReminderCommand dto);
}

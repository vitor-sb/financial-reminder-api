package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.CreatePaymentReminderCommand;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;

public interface CreatePaymentReminderUseCase {
    PaymentReminder execute(String userEmail, CreatePaymentReminderCommand dto);
}

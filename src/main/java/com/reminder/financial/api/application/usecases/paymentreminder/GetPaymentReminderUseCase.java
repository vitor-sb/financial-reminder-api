package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.GetPaymentReminderQuery;
import com.reminder.financial.api.application.dtos.paymentreminder.PaymentReminderResult;

public interface GetPaymentReminderUseCase {
    PaymentReminderResult execute(GetPaymentReminderQuery dto);
}

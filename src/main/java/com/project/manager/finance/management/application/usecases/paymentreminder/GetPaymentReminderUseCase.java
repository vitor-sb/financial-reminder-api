package com.project.manager.finance.management.application.usecases.paymentreminder;

import com.project.manager.finance.management.application.dtos.paymentreminder.GetPaymentReminderQuery;
import com.project.manager.finance.management.application.dtos.paymentreminder.PaymentReminderResult;

public interface GetPaymentReminderUseCase {
    PaymentReminderResult execute(GetPaymentReminderQuery dto);
}

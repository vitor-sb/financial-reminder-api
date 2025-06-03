package com.project.manager.finance.management.application.usecases.paymentreminder;

import com.project.manager.finance.management.application.dtos.GetPaymentReminderQuery;
import com.project.manager.finance.management.application.dtos.PaymentReminderResult;

public interface GetPaymentReminderUseCase {
    PaymentReminderResult execute(GetPaymentReminderQuery dto);
}

package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListPaymentRemindersByUserUseCase {
    Page<ListPaymentRemindersResult> execute(String loggedUserEmail, Pageable pageable);
}

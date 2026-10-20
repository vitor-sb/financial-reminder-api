package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.domains.filters.ReminderFilter;
import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersResult;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;

public interface ListPaymentRemindersByUserUseCase {
    PageResult<ListPaymentRemindersResult> execute(
            String loggedUserEmail,
            ReminderFilter query,
            PageRequest pageable
    );
}

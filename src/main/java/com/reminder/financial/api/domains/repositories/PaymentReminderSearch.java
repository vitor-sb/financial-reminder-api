package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.domains.filters.ReminderFilter;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;

public interface PaymentReminderSearch {

    PageResult<PaymentReminder> searchByUserAndFilters(User user, ReminderFilter query, PageRequest pageable);
}

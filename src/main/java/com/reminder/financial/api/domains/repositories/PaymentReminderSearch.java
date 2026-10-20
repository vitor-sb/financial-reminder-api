package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.domains.filters.ReminderFilter;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentReminderSearch {

    Page<PaymentReminder> searchByUserAndFilters(User user, ReminderFilter query, Pageable pageable);
}

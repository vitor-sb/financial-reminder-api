package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersFilterQuery;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentReminderSearch {

    Page<PaymentReminder> searchByUserAndFilters(User user, ListPaymentRemindersFilterQuery query, Pageable pageable);
}

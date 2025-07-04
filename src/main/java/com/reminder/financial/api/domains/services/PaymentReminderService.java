package com.reminder.financial.api.domains.services;

import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersFilterQuery;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.jpa.PaymentReminderRepository;
import com.reminder.financial.api.application.exceptions.ConflictException;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentReminderService {

    @Autowired
    private PaymentReminderRepository paymentReminderRepository;


    public PaymentReminder savePaymentReminder(PaymentReminder reminder) {
        return paymentReminderRepository.save(reminder);
    }

    public PaymentReminder getPaymentReminderByIdOrThrow(String id) {
        return paymentReminderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment Reminder not found."));
    }

    public List<PaymentReminder> listPaymentRemindersByEmail(String email) {
        return paymentReminderRepository.findByUserEmailAndAuditDeletedFalse(email);
    }

    public void validateReminderTitleConflict(String title, String email) {
        Optional<PaymentReminder> paymentReminder =
                paymentReminderRepository.findByTitleAndUserEmailAndAuditDeletedFalse(title, email);

        if (paymentReminder.isPresent())
            throw new ConflictException("Reminder title already in use. Check if it is not disabled.");
    }

    public Page<PaymentReminder> listPaymentRemindersByUserAndFilters(
            User user,
            ListPaymentRemindersFilterQuery query,
            Pageable pageable
    ) {
        Specification<PaymentReminder> spec = Specification
                .where(SpecificationFactory.<PaymentReminder>equal("user", user))
                .and(SpecificationFactory.equal("id", query.reminderId()))
                .and(SpecificationFactory.isGreaterThanOrEqualTo("dueDate", query.dueDateFrom()))
                .and(SpecificationFactory.isLessThanOrEqualTo("dueDate", query.dueDateUntil()))
                .and(SpecificationFactory.equal("recurrence", query.recurrence()))
                .and(SpecificationFactory.equal("method", query.method()))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", false));

        return paymentReminderRepository.findAll(spec, pageable);
    }
}

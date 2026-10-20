package com.reminder.financial.api.domains.services;

import lombok.RequiredArgsConstructor;

import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersFilterQuery;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.PaymentReminderSearch;
import com.reminder.financial.api.domains.repositories.jpa.PaymentReminderRepository;
import com.reminder.financial.api.application.exceptions.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentReminderService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentReminderService.class);

    private final PaymentReminderRepository paymentReminderRepository;
    private final PaymentReminderSearch paymentReminderSearch;


    public PaymentReminder savePaymentReminder(PaymentReminder reminder) {
        PaymentReminder saved = paymentReminderRepository.save(reminder);
        logger.info("Saved payment reminder {} for user {}", saved.getId(), saved.getUser().getId());
        return saved;
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

        if (paymentReminder.isPresent()){
            logger.warn("Title conflict detected for reminder {}", paymentReminder.get().getId());
            throw new ConflictException("Reminder title already in use. Check if it is not disabled.");
        }
    }

    public Page<PaymentReminder> listPaymentRemindersByUserAndFilters(
            User user,
            ListPaymentRemindersFilterQuery query,
            Pageable pageable
    ) {
        return paymentReminderSearch.searchByUserAndFilters(user, query, pageable);
    }

    public List<PaymentReminder> getRelevantRemindersForSimulation(String email, YearMonth yearMonth) {
        LocalDate monthEndDate = yearMonth.atEndOfMonth();
        return paymentReminderRepository.findRelevantRemindersForSimulation(email, monthEndDate);
    }
}

package com.reminder.financial.api.domains.services;

import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.repositories.jpa.PaymentReminderRepository;
import com.reminder.financial.api.application.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Optional<PaymentReminder> getPaymentReminderByTitleAndEmail(String title, String email) {
        return paymentReminderRepository.findByTitleAndUserEmailAndAuditDeletedFalse(title, email);
    }

    public List<PaymentReminder> listPaymentRemindersByEmail(String email) {
        return paymentReminderRepository.findByUserEmailAndAuditDeletedFalse(email);
    }

    public void validateReminderTitleConflict(String title, String email) {
        if (getPaymentReminderByTitleAndEmail(title, email).isPresent())
            throw new ConflictException("Reminder title already in use. Check if it is not disabled.");
    }
}

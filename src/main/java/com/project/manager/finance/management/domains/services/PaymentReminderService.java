package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.repositories.jpa.PaymentReminderRepository;
import com.project.manager.finance.management.application.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentReminderService {

    @Autowired
    private PaymentReminderRepository paymentReminderRepository;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    public PaymentReminder  savePaymentReminder(PaymentReminder reminder) {
        return paymentReminderRepository.save(reminder);
    }

    public Optional<PaymentReminder> getPaymentReminderById(String id) {
        return paymentReminderRepository.findById(id);
    }

    public Optional<PaymentReminder> getPaymentReminderByTitleAndEmail(String title, String email) {
        return paymentReminderRepository.findByTitleAndUserEmailAndAuditDeletedFalse(title, email);
    }

    public List<PaymentReminder> listPaymentRemindersByEmail(String email) {
        return paymentReminderRepository.findByUserEmailAndAuditDeletedFalse(email);
    }

    @Transactional
    public void toggleReminderStatus(String reminderId, boolean enable, String userEmail) {
        PaymentReminder reminder = paymentReminderRepository.findById(reminderId)
                .orElseThrow(() -> new NotFoundException("Payment Reminder not found."));

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User does not own this reminder.");
        }

        reminder.getAudit().setEnabled(enable);
        if (Boolean.TRUE.equals(enable)) {
           reminder.getAudit().setEnabledAt(Instant.now());
           reminder.getAudit().setEnabledBy(userEmail);
        }

        paymentTransactionService.togglePendingTransactionStatus(reminderId, enable, userEmail);

        paymentReminderRepository.save(reminder);
    }

    @Transactional
    public void softDeleteReminder(String reminderId, String userEmail) {
        PaymentReminder reminder = paymentReminderRepository.findById(reminderId)
                .orElseThrow(() -> new NotFoundException("Payment Reminder not found."));

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User does not own this reminder.");
        }

        reminder.getAudit().setDeleted(true);
        reminder.getAudit().setDeletedBy(userEmail);
        reminder.getAudit().setDeletedAt(Instant.now());

        paymentTransactionService.cancelAndSoftDeletePendingTransaction(reminderId, userEmail);

        paymentReminderRepository.save(reminder);
    }

    public void validateReminderTitleConflict(String title, String email) {
        if (getPaymentReminderByTitleAndEmail(title, email).isPresent())
            throw new ConflictException("Reminder title already in use. Check if it is not disabled.");
    }
}

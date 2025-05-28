package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.repositories.jpa.PaymentReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentReminderService {
    @Autowired
    private PaymentReminderRepository repository;

    public boolean paymentReminderExists(String title, String email) {
        return repository.findByTitleAndUserEmailAndDeletedFalse(title, email).isPresent();
    }

    public PaymentReminder createPaymentReminder(PaymentReminder reminder) {
        return repository.save(reminder);
    }

    public Optional<PaymentReminder> getPaymentReminderById(String id) {
        return this.repository.findById(id);
    }

    public Optional<PaymentReminder> getPaymentReminderByTitleAndEmail(String title, String email) {
        return this.repository.findByTitleAndUserEmailAndDeletedFalse(title, email);
    }

    public List<PaymentReminder> listPaymentRemindersByEmail(String email) {
        return this.repository.findByUserEmailAndDeletedFalse(email);
    }
}

package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import com.project.manager.finance.management.domains.models.reminder.enums.RecurrenceEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;
import com.project.manager.finance.management.domains.repositories.jpa.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;


@Service
public class PaymentTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionService.class);

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private PaymentReminderService paymentReminderService;

    public PaymentTransaction save(PaymentTransaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void createPaymentTransactionByReminder(PaymentReminder reminder) {
        PaymentTransaction transaction = new PaymentTransaction(
                reminder,
                reminder.getBaseDate(),
                reminder.getMethod(),
                1,
                reminder.getBaseValue(),
                TransactionStatusEnum.PENDENTE
        );

        transactionRepository.save(transaction);
    }

    public Optional<PaymentTransaction> getTransactionById(String id) {
        return transactionRepository.findById(id);
    }

    public Optional<PaymentTransaction> findPendingTransactionByReminderId(String reminderId) {
        return transactionRepository.findFirstByReminderIdAndStatusAndAuditDeletedFalse(reminderId, TransactionStatusEnum.PENDENTE);
    }

    public Page<PaymentTransaction> listTransactionsByReminder(String reminderId, Pageable pageable) {
        return transactionRepository.findByReminderIdAndAuditDeletedFalse(reminderId, pageable);
    }

    @Transactional
    public PaymentTransaction processSuccessfulPayment(PaymentTransaction transaction) {
        transaction.setStatus(TransactionStatusEnum.PAGO);
        transaction.setPaymentDate(LocalDate.now());
        PaymentTransaction savedTransaction = transactionRepository.save(transaction);

        PaymentReminder reminder = savedTransaction.getReminder();
        if (reminder.getRecurrence() != RecurrenceEnum.UNICA) {
            generateNextTransaction(savedTransaction);
        }
        return savedTransaction;
    }

    private void generateNextTransaction(PaymentTransaction currentTransaction) {
        PaymentReminder reminder = currentTransaction.getReminder();

        if (reminder.getRecurrence() == RecurrenceEnum.UNICA)
            throw new IllegalStateException("generateNextTransaction should not be called for UNICA recurrence. Reminder ID: " + reminder.getId());

        if (reminder.getInstallmentsNumber() != 0) {
            int currentInstallment = currentTransaction.getInstallmentNumber() != 0 ? currentTransaction.getInstallmentNumber() : 1;
            if (currentInstallment >= reminder.getInstallmentsNumber()) {
                return;
            }
        }

        LocalDate nextDueDate = reminder.getRecurrence().getNextDate(reminder.getBaseDate());

        if (nextDueDate != null) {
            PaymentTransaction nextTransaction = new PaymentTransaction(
                    reminder,
                    nextDueDate,
                    reminder.getMethod(),
                    currentTransaction.getInstallmentNumber() != 0 ? currentTransaction.getInstallmentNumber() + 1 : null,
                    reminder.getBaseValue(),
                    TransactionStatusEnum.PENDENTE
            );

            transactionRepository.save(nextTransaction);
        }
    }
}

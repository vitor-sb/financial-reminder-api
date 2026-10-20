package com.reminder.financial.api.domains.services;

import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsFilterQuery;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.jpa.PaymentTransactionRepository;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;


@Service
public class PaymentTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionService.class);

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    public PaymentTransaction save(PaymentTransaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void createInitialTransactionForReminder(PaymentReminder reminder) {
        logger.info("Creating initial transaction for reminder: {}", reminder.getId());

        Integer initialInstallment = (reminder.getInstallmentsNumber() == null) ? null : 1;

        PaymentTransaction transaction = new PaymentTransaction(
                reminder,
                reminder.getBaseDate(),
                reminder.getMethod(),
                initialInstallment,
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

    public Page<PaymentTransaction> listTransactionsByUserAndFilters(User user, ListTransactionsFilterQuery query, Pageable pageable) {
        Specification<PaymentTransaction> spec = Specification
                .where(SpecificationFactory.<PaymentTransaction>equalInJoin("reminder", "user", user))
                .and(SpecificationFactory.equal("id", query.transactionId()))
                .and(SpecificationFactory.equalInJoin("reminder", "id", query.reminderId()))
                .and(SpecificationFactory.isGreaterThanOrEqualTo("dueDate", query.dueDateFrom()))
                .and(SpecificationFactory.isLessThanOrEqualTo("dueDate", query.dueDateUntil()))
                .and(SpecificationFactory.equal("method", query.method()))
                .and(SpecificationFactory.equal("status", query.status()))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", false));

        return transactionRepository.findAll(spec, pageable);
    }

    public PaymentTransaction processSuccessfulPayment(PaymentTransaction transaction) {
        logger.info("Processing successful payment for Transaction ID: {} | Reminder: {}",
                transaction.getId(), transaction.getReminder().getTitle());

        transaction.setStatus(TransactionStatusEnum.PAGO);
        transaction.setPaymentDate(LocalDate.now());
        PaymentTransaction savedTransaction = transactionRepository.save(transaction);

        PaymentReminder reminder = savedTransaction.getReminder();
        if (reminder.getRecurrence() != RecurrenceEnum.UNICA) {
            logger.info("Recurrence detected ({}). Generating next transaction.", reminder.getRecurrence());
            generateNextTransaction(savedTransaction);
        }
        return savedTransaction;
    }

    private void generateNextTransaction(PaymentTransaction currentTransaction) {
        PaymentReminder reminder = currentTransaction.getReminder();
        Integer currentInstallment = currentTransaction.getInstallmentNumber();

        if (reminder.getRecurrence() == RecurrenceEnum.UNICA)
            throw new IllegalStateException("generateNextTransaction should not be called for UNICA recurrence. Reminder ID: " + reminder.getId());

        if (reminder.getInstallmentsNumber() != null && currentInstallment != null &&
                currentInstallment >= reminder.getInstallmentsNumber()) {
            return;
        }

        LocalDate nextDueDate = reminder.getRecurrence().getNextDate(currentTransaction.getDueDate());

        if (nextDueDate != null) {
            Integer nextInstallmentNumber = null;

            if (currentInstallment != null && currentInstallment != 0) {
                nextInstallmentNumber = currentInstallment + 1;
            }

            PaymentTransaction nextTransaction = new PaymentTransaction(
                    reminder,
                    nextDueDate,
                    reminder.getMethod(),
                    nextInstallmentNumber,
                    reminder.getBaseValue(),
                    TransactionStatusEnum.PENDENTE
            );

            transactionRepository.save(nextTransaction);
        }
    }

    public void togglePendingTransactionStatus(String reminderId, boolean enable, String userEmail) {
        findPendingTransactionByReminderId(reminderId)
                .ifPresent(transaction -> {
                    transaction.getAudit().setEnabled(enable);
                    if (enable) {
                        transaction.getAudit().setEnabledAt(Instant.now());
                        transaction.getAudit().setToggledBy(userEmail);
                    }
                    save(transaction);
                });
    }

    public void cancelAndSoftDeletePendingTransaction(String reminderId, String userEmail) {
        logger.info("Cancelling and soft-deleting pending transaction for reminder: {}", reminderId);

        findPendingTransactionByReminderId(reminderId)
                .ifPresentOrElse(transaction -> {
                    transaction.setStatus(TransactionStatusEnum.CANCELADO);
                    transaction.getAudit().setDeleted(true);
                    transaction.getAudit().setDeletedBy(userEmail);
                    transaction.getAudit().setDeletedAt(Instant.now());
                    save(transaction);
                    logger.info("Transaction {} cancelled successfully.", transaction.getId());
                }, () -> logger.warn("No pending transaction found to cancel for reminder: {}", reminderId));
    }

    public List<PaymentTransaction> findTransactionsForMonth(String userEmail, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return transactionRepository.findAllByReminderUserEmailAndDueDateBetweenAndAuditEnabledAndAuditDeleted(
                userEmail,
                startDate,
                endDate,
                true,
                false
        );
    }
}

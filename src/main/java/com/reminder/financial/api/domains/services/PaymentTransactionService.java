package com.reminder.financial.api.domains.services;

import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.domains.models.reminder.NextTransactionFactory;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.PaymentTransactionSearch;
import com.reminder.financial.api.domains.repositories.jpa.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionService.class);

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentTransactionSearch transactionSearch;

    public PaymentTransaction save(PaymentTransaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void createInitialTransactionForReminder(PaymentReminder reminder) {
        logger.info("Creating initial transaction for reminder: {}", reminder.getId());
        transactionRepository.save(PaymentTransaction.initialFor(reminder));
    }

    public Optional<PaymentTransaction> getTransactionById(String id) {
        return transactionRepository.findById(id);
    }

    public Optional<PaymentTransaction> findPendingTransactionByReminderId(String reminderId) {
        return transactionRepository.findFirstByReminderIdAndStatusAndAuditDeletedFalse(reminderId, TransactionStatusEnum.PENDENTE);
    }

    public Page<PaymentTransaction> listTransactionsByUserAndFilters(User user, TransactionFilter query, Pageable pageable) {
        return transactionSearch.searchByUserAndFilters(user, query, pageable);
    }

    public PaymentTransaction processSuccessfulPayment(PaymentTransaction transaction) {
        logger.info("Processing successful payment transactionId={} reminderId={}",
                transaction.getId(), transaction.getReminder().getId());

        transaction.setStatus(TransactionStatusEnum.PAGO);
        transaction.setPaymentDate(LocalDate.now());
        PaymentTransaction savedTransaction = transactionRepository.save(transaction);

        NextTransactionFactory.createNext(savedTransaction).ifPresent(transactionRepository::save);
        return savedTransaction;
    }

    public void togglePendingTransactionStatus(String reminderId, boolean enable, String userEmail) {
        findPendingTransactionByReminderId(reminderId).ifPresent(transaction -> {
            transaction.getAudit().toggle(enable, userEmail);
            save(transaction);
        });
    }

    public void cancelAndSoftDeletePendingTransaction(String reminderId, String userEmail) {
        findPendingTransactionByReminderId(reminderId).ifPresentOrElse(transaction -> {
            transaction.setStatus(TransactionStatusEnum.CANCELADO);
            transaction.getAudit().softDelete(userEmail);
            save(transaction);
            logger.info("Transaction {} cancelled successfully.", transaction.getId());
        }, () -> logger.warn("No pending transaction found to cancel for reminder: {}", reminderId));
    }

    public List<PaymentTransaction> findTransactionsForMonth(String userEmail, YearMonth yearMonth) {
        return transactionRepository.findAllByReminderUserEmailAndDueDateBetweenAndAuditEnabledAndAuditDeleted(
                userEmail, yearMonth.atDay(1), yearMonth.atEndOfMonth(), true, false);
    }
}

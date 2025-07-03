package com.reminder.financial.api.domains.repositories.jpa;

import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String>, JpaSpecificationExecutor<PaymentTransaction> {
    Optional<PaymentTransaction> findFirstByReminderIdAndStatusAndAuditDeletedFalse(String reminderId, TransactionStatusEnum status);

    List<PaymentTransaction> findAllByReminderUserEmailAndDueDateBetween(String email, LocalDate startDate, LocalDate endDate);
}

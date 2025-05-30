package com.project.manager.finance.management.domains.repositories.jpa;

import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByReminderIdAndAuditDeletedFalse(String reminderId);
}

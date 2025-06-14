package com.project.manager.finance.management.domains.repositories.jpa;

import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    Page<PaymentTransaction> findByReminderIdAndAuditDeletedFalse(String reminderId, Pageable pageable);
}

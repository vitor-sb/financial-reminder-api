package com.reminder.financial.api.domains.repositories.jpa;

import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, String> {
    Optional<PaymentReminder> findByTitleAndUserEmailAndAuditDeletedFalse(String title, String email);

    List<PaymentReminder> findByUserEmailAndAuditDeletedFalse(String email);

    Page<PaymentReminder> findByUserIdAndAuditDeletedFalse(String userId, Pageable pageable);
}

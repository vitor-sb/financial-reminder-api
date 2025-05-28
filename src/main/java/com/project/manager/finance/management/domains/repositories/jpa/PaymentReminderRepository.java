package com.project.manager.finance.management.domains.repositories.jpa;

import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, String> {
    Optional<PaymentReminder> findByTitleAndUserEmailAndDeletedFalse(String title, String email);

    List<PaymentReminder> findByUserEmailAndDeletedFalse(String email);
}

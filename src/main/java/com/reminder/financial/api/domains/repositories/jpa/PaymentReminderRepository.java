package com.reminder.financial.api.domains.repositories.jpa;

import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, String>, JpaSpecificationExecutor<PaymentReminder> {
    Optional<PaymentReminder> findByTitleAndUserEmailAndAuditDeletedFalse(String title, String email);

    List<PaymentReminder> findByUserEmailAndAuditDeletedFalse(String email);

    Page<PaymentReminder> findByUserIdAndAuditDeletedFalse(String userId, Pageable pageable);

    @Query("SELECT pr FROM PaymentReminder pr " +
            "WHERE pr.user.email = :email " +
            "AND pr.audit.deleted = false " +
            "AND pr.audit.enabled = true " +
            "AND pr.recurrence <> 'UNICA' " +
            "AND pr.baseDate <= :monthEndDate")
    List<PaymentReminder> findRelevantRemindersForSimulation(
            @Param("email") String email,
            @Param("monthEndDate") LocalDate monthEndDate
    );

}

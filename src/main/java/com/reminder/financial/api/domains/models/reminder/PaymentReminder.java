package com.reminder.financial.api.domains.models.reminder;

import com.reminder.financial.api.domains.models.audit.PaymentReminderAudit;
import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "payment_reminders")
public class PaymentReminder extends AbstractReminder implements Serializable {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "audit_id", referencedColumnName = "id")
    private PaymentReminderAudit audit = new PaymentReminderAudit();

    public PaymentReminder(
            User user,
            String title,
            String description,
            LocalDate date,
            RecurrenceEnum recurrence,
            TransactionMethodEnum method,
            BigDecimal value,
            Integer installmentsNumber
    ) {
        this.setUser(user);
        this.setTitle(title);
        this.setDescription(description);
        this.setBaseDate(date);
        this.setRecurrence(recurrence);
        this.setMethod(method);
        this.setBaseValue(value);
        this.setInstallmentsNumber(installmentsNumber);
    }
}

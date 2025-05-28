package com.project.manager.finance.management.domains.models.reminder;

import com.project.manager.finance.management.domains.models.reminder.enums.RecurrenceEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;
import com.project.manager.finance.management.domains.models.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "payment_reminders")
public class PaymentReminder extends Reminder implements Serializable {
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

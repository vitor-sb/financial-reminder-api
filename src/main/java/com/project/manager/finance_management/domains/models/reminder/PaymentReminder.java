package com.project.manager.finance_management.domains.models.reminder;

import com.project.manager.finance_management.domains.models.reminder.enums.TransactionMethodEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "payment_reminders")
public class PaymentReminder extends Reminder implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private TransactionMethodEnum method;

    @Column
    private LocalDate dueDate;
}

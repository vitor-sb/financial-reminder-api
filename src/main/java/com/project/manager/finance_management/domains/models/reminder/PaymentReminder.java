package com.project.manager.finance_management.domains.models.reminder;

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

    @ManyToOne
    @JoinColumn(name = "payment_method", referencedColumnName = "id")
    private TransactionMethod paymentMethod;

    @Column
    private LocalDate dueDate;
}

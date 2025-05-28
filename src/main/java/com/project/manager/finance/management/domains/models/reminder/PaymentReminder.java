package com.project.manager.finance.management.domains.models.reminder;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "payment_reminders")
public class PaymentReminder extends Reminder implements Serializable {
}

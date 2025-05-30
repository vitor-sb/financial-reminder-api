package com.project.manager.finance.management.domains.models.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_reminders_audit")
public class PaymentReminderAudit extends AbstractAudit {
}
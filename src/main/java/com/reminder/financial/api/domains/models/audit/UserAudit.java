package com.reminder.financial.api.domains.models.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users_audit")
public class UserAudit extends AbstractAudit {
}
package com.project.manager.finance.management.domains.models.reminder;

import com.project.manager.finance.management.domains.models.audit.Audit;
import com.project.manager.finance.management.domains.models.audit.interfaces.IAuditable;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "payment_transactions")
@EntityListeners(AuditingEntityListener.class)
public class PaymentTransaction implements IAuditable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "reminder_id", referencedColumnName = "id", nullable = false)
    private PaymentReminder reminder;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private TransactionMethodEnum method;

    @Column
    private Integer installmentNumber;

    @Column
    private BigDecimal paymentValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatusEnum status;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Embedded
    private Audit audit = new Audit();
}

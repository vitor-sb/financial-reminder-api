package com.reminder.financial.api.domains.models.reminder;

import com.reminder.financial.api.domains.models.audit.PaymentTransactionAudit;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import jakarta.persistence.*;
import lombok.*;
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
public class PaymentTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "reminder_id", referencedColumnName = "id", nullable = false)
    private PaymentReminder reminder;

    @Column(name="due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name="payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionMethodEnum method;

    @Column
    private Integer installmentNumber;

    @Column(nullable = false)
    private BigDecimal paymentValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatusEnum status;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "audit_id", referencedColumnName = "id")
    private PaymentTransactionAudit audit = new PaymentTransactionAudit();

    public PaymentTransaction(
            PaymentReminder reminder,
            LocalDate dueDate,
            TransactionMethodEnum method,
            Integer installmentNumber,
            BigDecimal paymentValue,
            TransactionStatusEnum status
    ) {
        this.reminder = reminder;
        this.dueDate = dueDate;
        this.method = method;
        this.installmentNumber = installmentNumber;
        this.paymentValue = paymentValue;
        this.status = status;
        this.paymentDate = null;
    }

    public static PaymentTransaction initialFor(PaymentReminder reminder) {
        return new PaymentTransaction(
                reminder,
                reminder.getBaseDate(),
                reminder.getMethod(),
                reminder.getInstallmentsNumber() == null ? null : 1,
                reminder.getBaseValue(),
                TransactionStatusEnum.PENDENTE
        );
    }
}

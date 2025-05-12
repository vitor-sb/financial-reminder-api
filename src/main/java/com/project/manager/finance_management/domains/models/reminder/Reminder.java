package com.project.manager.finance_management.domains.models.reminder;

import com.project.manager.finance_management.domains.models.reminder.enums.RecurrenceEnum;
import com.project.manager.finance_management.domains.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@MappedSuperclass
public abstract class Reminder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private LocalDate baseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", nullable = false)
    private RecurrenceEnum recurrence;

    @Column(nullable = false)
    private BigDecimal baseValue;

    @Column
    private Integer installmentsNumber;

    @ManyToOne
    @JoinColumn(name = "transaction_method", referencedColumnName = "id")
    private TransactionMethod transactionMethod;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    private Boolean deleted = false;
}

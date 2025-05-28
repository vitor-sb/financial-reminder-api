package com.project.manager.finance.management.domains.models.reminder;

import com.project.manager.finance.management.domains.models.audit.Audit;
import com.project.manager.finance.management.domains.models.audit.interfaces.IAuditable;
import com.project.manager.finance.management.domains.models.reminder.enums.RecurrenceEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;
import com.project.manager.finance.management.domains.models.user.User;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Reminder implements IAuditable, Serializable {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private TransactionMethodEnum method;

    @Column(nullable = false)
    private BigDecimal baseValue;

    @Column
    private Integer installmentsNumber;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Embedded
    private Audit audit = new Audit();

}

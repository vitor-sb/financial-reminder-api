package com.reminder.financial.api.domains.models.audit;

import com.reminder.financial.api.application.exceptions.ResourceDeletedException;
import com.reminder.financial.api.application.exceptions.ResourceDisabledException;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedBy
    @Column(name = "created_by", length = 254, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 254)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "enabled_at")
    private Instant enabledAt;

    @Column(name = "disabled_at")
    private Instant disabledAt;

    @Column(name = "toggled_by", length = 36)
    private String toggledBy;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    public void validateIsDeleted(String resourceName) {
        if (Boolean.TRUE.equals(this.deleted))
            throw new ResourceDeletedException(resourceName, this.getDeletedBy());
    }

    public void validateIsEnabled(String resourceName) {
        if (Boolean.FALSE.equals(this.enabled))
            throw new ResourceDisabledException(resourceName, this.getToggledBy());
    }

    public void validateIsActive(String resourceName) {
        this.validateIsDeleted(resourceName);
        this.validateIsEnabled(resourceName);
    }
}
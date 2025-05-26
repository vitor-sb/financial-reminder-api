package com.project.manager.finance_management.domains.models.audit.interfaces;

import com.project.manager.finance_management.domains.models.audit.Audit;

public interface IAuditable {
    Audit getAudit();

    void setAudit(Audit audit);
}

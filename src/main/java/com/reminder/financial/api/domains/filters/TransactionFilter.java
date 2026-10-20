package com.reminder.financial.api.domains.filters;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.time.LocalDate;

public record TransactionFilter(
        String transactionId,
        String reminderId,
        LocalDate dueDateFrom,
        LocalDate dueDateUntil,
        TransactionMethodEnum method,
        TransactionStatusEnum status,
        Boolean enabled
) {
}

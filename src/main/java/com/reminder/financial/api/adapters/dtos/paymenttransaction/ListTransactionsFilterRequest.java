package com.reminder.financial.api.adapters.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.time.LocalDate;

public record ListTransactionsFilterRequest(
        String transactionId,
        String reminderId,
        LocalDate startDate,
        LocalDate endDate,
        TransactionMethodEnum method,
        TransactionStatusEnum status,
        Boolean enabled,
        Boolean deleted
) {
}

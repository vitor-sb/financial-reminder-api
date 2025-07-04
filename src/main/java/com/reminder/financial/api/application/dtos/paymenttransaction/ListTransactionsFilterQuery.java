package com.reminder.financial.api.application.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.time.LocalDate;

public record ListTransactionsFilterQuery(
        String transactionId,
        String reminderId,
        LocalDate dueDateFrom,
        LocalDate dueDateUntil,
        TransactionMethodEnum method,
        TransactionStatusEnum status,
        Boolean enabled
) {
}

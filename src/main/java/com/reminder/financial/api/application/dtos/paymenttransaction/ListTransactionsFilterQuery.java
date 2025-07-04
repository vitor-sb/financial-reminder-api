package com.reminder.financial.api.application.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.time.LocalDate;

public record ListTransactionsFilterQuery(
        String transactionId,
        String reminderId,
        LocalDate createdFrom,
        LocalDate createdUntil,
        TransactionMethodEnum method,
        TransactionStatusEnum status,
        Boolean enabled,
        Boolean deleted
) {
}

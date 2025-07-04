package com.reminder.financial.api.application.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;

public record UpdatePaymentTransactionCommand(
        BigDecimal paymentValue,
        TransactionMethodEnum method
) {
}

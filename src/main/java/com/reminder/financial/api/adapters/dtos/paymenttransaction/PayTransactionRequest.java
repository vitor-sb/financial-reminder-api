package com.reminder.financial.api.adapters.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;

public record PayTransactionRequest(
        BigDecimal paymentValue,
        TransactionMethodEnum method
) {
}

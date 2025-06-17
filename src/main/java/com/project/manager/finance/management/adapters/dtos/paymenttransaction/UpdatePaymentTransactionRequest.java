package com.project.manager.finance.management.adapters.dtos.paymenttransaction;

import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;

public record UpdatePaymentTransactionRequest(
        BigDecimal paymentValue,
        TransactionMethodEnum method
) {
}

package com.project.manager.finance.management.application.dtos.paymenttransaction;

import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;

public record PaymentExecutionDetails(
        BigDecimal paymentValue,
        TransactionMethodEnum method
) {
}

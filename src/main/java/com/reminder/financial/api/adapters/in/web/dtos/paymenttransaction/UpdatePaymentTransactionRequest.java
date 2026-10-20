package com.reminder.financial.api.adapters.in.web.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdatePaymentTransactionRequest(
        @Positive @Digits(integer = 12, fraction = 2)
        BigDecimal paymentValue,
        TransactionMethodEnum method
) {
}

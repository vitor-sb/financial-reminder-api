package com.reminder.financial.api.adapters.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ListTransactionsResponse(
        String id,
        LocalDate paymentDate,
        Integer installmentNumber,
        BigDecimal paymentValue,
        TransactionMethodEnum method,
        TransactionStatusEnum status
) {}

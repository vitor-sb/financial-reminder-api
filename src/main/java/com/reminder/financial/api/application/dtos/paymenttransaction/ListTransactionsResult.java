package com.reminder.financial.api.application.dtos.paymenttransaction;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ListTransactionsResult(
        String id,
        LocalDate paymentDate,
        Integer installmentNumber,
        BigDecimal paymentValue,
        TransactionMethodEnum method,
        TransactionStatusEnum status
) {}

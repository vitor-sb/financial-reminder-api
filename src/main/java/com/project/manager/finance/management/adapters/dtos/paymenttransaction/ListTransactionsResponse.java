package com.project.manager.finance.management.adapters.dtos.paymenttransaction;

import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;

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

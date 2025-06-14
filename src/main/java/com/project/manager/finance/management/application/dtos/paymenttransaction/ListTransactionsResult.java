package com.project.manager.finance.management.application.dtos.paymenttransaction;

import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;

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

package com.reminder.financial.api.adapters.dtos.paymentremider;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyBillResponse(
        String transactionId,
        String reminderId,
        String title,
        LocalDate dueDate,
        BigDecimal value,
        TransactionMethodEnum method,
        TransactionStatusEnum status,
        Integer installmentNumber
) {}
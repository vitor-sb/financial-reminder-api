package com.reminder.financial.api.adapters.dtos.paymentremider;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ListPaymentRemindersResponse(
        String id,
        String title,
        String description,
        LocalDate baseDate,
        RecurrenceEnum recurrence,
        TransactionMethodEnum method,
        BigDecimal baseValue,
        Integer installmentsNumber
) {}

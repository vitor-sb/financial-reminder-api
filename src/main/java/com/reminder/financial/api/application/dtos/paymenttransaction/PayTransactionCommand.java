package com.reminder.financial.api.application.dtos.paymenttransaction;


public record PayTransactionCommand(
        String loggedUserEmail,
        String transactionId,
        PaymentExecutionDetails transactionDetails
) {}
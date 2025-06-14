package com.project.manager.finance.management.application.dtos.paymenttransaction;


public record PayTransactionCommand(
        String loggedUserEmail,
        String transactionId,
        PaymentExecutionDetails transactionDetails
) {}
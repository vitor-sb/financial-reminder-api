package com.reminder.financial.api.application.usecases.paymenttransaction;

import com.reminder.financial.api.application.dtos.paymenttransaction.UpdatePaymentTransactionCommand;

public interface UpdatePaymentTransactionUseCase {
    void execute(String userEmail, String transactionId, UpdatePaymentTransactionCommand command);
}
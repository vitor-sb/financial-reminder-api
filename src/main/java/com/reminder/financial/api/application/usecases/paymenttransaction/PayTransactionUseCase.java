package com.reminder.financial.api.application.usecases.paymenttransaction;

import com.reminder.financial.api.application.dtos.paymenttransaction.PayTransactionDetailsCommand;

public interface PayTransactionUseCase {
    void execute(String userEmail, String transactionId, PayTransactionDetailsCommand command);
}
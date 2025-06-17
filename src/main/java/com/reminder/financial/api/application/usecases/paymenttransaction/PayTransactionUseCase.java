package com.reminder.financial.api.application.usecases.paymenttransaction;

import com.reminder.financial.api.application.dtos.paymenttransaction.PayTransactionCommand;

public interface PayTransactionUseCase {
    void execute(PayTransactionCommand command);
}
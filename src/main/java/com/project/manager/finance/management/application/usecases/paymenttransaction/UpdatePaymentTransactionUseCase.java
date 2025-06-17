package com.project.manager.finance.management.application.usecases.paymenttransaction;

import com.project.manager.finance.management.application.dtos.paymenttransaction.UpdatePaymentTransactionCommand;

public interface UpdatePaymentTransactionUseCase {
    void execute(UpdatePaymentTransactionCommand dto);
}
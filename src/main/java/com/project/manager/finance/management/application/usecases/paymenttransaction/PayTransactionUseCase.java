package com.project.manager.finance.management.application.usecases.paymenttransaction;

import com.project.manager.finance.management.application.dtos.paymenttransaction.PayTransactionCommand;

public interface PayTransactionUseCase {
    void execute(PayTransactionCommand command);
}
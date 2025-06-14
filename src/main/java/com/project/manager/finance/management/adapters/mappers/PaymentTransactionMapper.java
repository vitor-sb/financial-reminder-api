package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.paymenttransaction.ListTransactionsResponse;
import com.project.manager.finance.management.adapters.dtos.paymenttransaction.PayTransactionRequest;
import com.project.manager.finance.management.application.dtos.paymenttransaction.ListTransactionsResult;
import com.project.manager.finance.management.application.dtos.paymenttransaction.PayTransactionCommand;
import com.project.manager.finance.management.application.dtos.paymenttransaction.PaymentExecutionDetails;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PayTransactionCommand toCommand(
            String loggedUser,
            String transactionId,
            @Valid PayTransactionRequest dto
    ) {
        return new PayTransactionCommand(
                loggedUser,
                transactionId,
                new PaymentExecutionDetails(
                        dto.paymentValue(),
                        dto.method()
                )
        );
    }

    public ListTransactionsResponse toListTransactionsResponse(ListTransactionsResult result) {
        return new ListTransactionsResponse(
                result.id(),
                result.paymentDate(),
                result.installmentNumber(),
                result.paymentValue(),
                result.method(),
                result.status());
    }
}
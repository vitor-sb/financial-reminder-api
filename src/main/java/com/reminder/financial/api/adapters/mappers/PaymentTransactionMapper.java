package com.reminder.financial.api.adapters.mappers;

import com.reminder.financial.api.adapters.dtos.paymenttransaction.ListTransactionsResponse;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.PayTransactionRequest;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.UpdatePaymentTransactionRequest;
import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsResult;
import com.reminder.financial.api.application.dtos.paymenttransaction.PayTransactionCommand;
import com.reminder.financial.api.application.dtos.paymenttransaction.PaymentExecutionDetails;
import com.reminder.financial.api.application.dtos.paymenttransaction.UpdatePaymentTransactionCommand;
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

    public UpdatePaymentTransactionCommand toCommand(
            String loggedUser,
            String transactionId,
            @Valid UpdatePaymentTransactionRequest dto
    ) {
        return new UpdatePaymentTransactionCommand(
                loggedUser,
                transactionId,
                dto.paymentValue(),
                dto.method()
        );
    }
}
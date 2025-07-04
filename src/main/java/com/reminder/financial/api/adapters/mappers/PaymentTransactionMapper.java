package com.reminder.financial.api.adapters.mappers;

import com.reminder.financial.api.adapters.dtos.paymenttransaction.ListTransactionsFilterRequest;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.ListTransactionsResponse;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.PayTransactionRequest;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.UpdatePaymentTransactionRequest;
import com.reminder.financial.api.application.dtos.paymenttransaction.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PayTransactionDetailsCommand toCommand(
            @Valid PayTransactionRequest dto
    ) {
        return new PayTransactionDetailsCommand(
                dto.paymentValue(),
                dto.method()
        );
    }

    public ListTransactionsFilterQuery toListTransactionsFilterQuery(ListTransactionsFilterRequest filter) {
        if(filter == null)
            return new ListTransactionsFilterQuery(null, null, null, null, null, null, null, null);

        return new ListTransactionsFilterQuery(
                filter.transactionId(),
                filter.reminderId(),
                filter.createdFrom(),
                filter.createdUntil(),
                filter.method(),
                filter.status(),
                filter.enabled(),
                filter.deleted()
        );
    }

    public ListTransactionsResponse toListTransactionsResponse(ListTransactionsResult result) {
        return new ListTransactionsResponse(
                result.id(),
                result.reminderId(),
                result.dueDate(),
                result.paymentDate(),
                result.installmentNumber(),
                result.paymentValue(),
                result.method(),
                result.status(),
                result.enabled(),
                result.deleted()
        );
    }

    public UpdatePaymentTransactionCommand toCommand(
            @Valid UpdatePaymentTransactionRequest dto
    ) {
        return new UpdatePaymentTransactionCommand(
                dto.paymentValue(),
                dto.method()
        );
    }
}
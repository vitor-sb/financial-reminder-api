package com.reminder.financial.api.adapters.in.web.controllers;

import com.reminder.financial.api.adapters.in.web.dtos.paymenttransaction.ListTransactionsFilterRequest;
import com.reminder.financial.api.adapters.in.web.dtos.paymenttransaction.ListTransactionsResponse;
import com.reminder.financial.api.adapters.in.web.dtos.paymenttransaction.PayTransactionRequest;
import com.reminder.financial.api.adapters.in.web.dtos.paymenttransaction.UpdatePaymentTransactionRequest;
import com.reminder.financial.api.adapters.in.web.mappers.PaymentTransactionMapper;
import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.application.usecases.paymenttransaction.ListTransactionsUseCase;
import com.reminder.financial.api.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.reminder.financial.api.application.usecases.paymenttransaction.UpdatePaymentTransactionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.reminder.financial.api.adapters.in.web.mappers.PageAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payments/transactions")
@Tag(name = "Payment Transactions", description = "Handles individual payment transactions, including processing payments and listing transactions for a specific reminder.")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionMapper paymentTransactionMapper;

    private final PayTransactionUseCase payTransactionUseCase;

    private final ListTransactionsUseCase listTransactionsUseCase;

    private final UpdatePaymentTransactionUseCase updateTransactionUseCase;


    @PostMapping("/{transactionId}/pay")
    @Operation(summary = "Make a payment for a pending transaction")
    public ResponseEntity<Void> payTransaction(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @PathVariable String transactionId,
            @RequestBody @Valid PayTransactionRequest request
    ) {
        var command = paymentTransactionMapper.toPayTransactionDetailsCommand(
                request
        );

        payTransactionUseCase.execute(loggedUserEmail, transactionId, command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    @Operation(
            summary = "Lists and filters user transactions",
            description = "Returns a paginated list of transactions based on filters."
    )
    public ResponseEntity<Page<ListTransactionsResponse>> listByReminder(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            ListTransactionsFilterRequest filter,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = {"dueDate", "status"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        TransactionFilter query = paymentTransactionMapper.toListTransactionsFilterQuery(filter);
        var result = listTransactionsUseCase.execute(loggedUserEmail, query, PageAdapter.toRequest(pageable));

        Page<ListTransactionsResponse> response = PageAdapter.toPage(result.map(paymentTransactionMapper::toListTransactionsResponse), pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Updates the details of a transaction",
            description = "Allows you to change the amount or payment method of a transaction that has not yet been paid."
    )
    public ResponseEntity<Void> updateTransaction(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @PathVariable String id,
            @RequestBody @Valid UpdatePaymentTransactionRequest request
    ) {
        var command = paymentTransactionMapper.toPayTransactionDetailsCommand(
                request
        );

        updateTransactionUseCase.execute(loggedUserEmail, id, command);

        return ResponseEntity.noContent().build();
    }
}

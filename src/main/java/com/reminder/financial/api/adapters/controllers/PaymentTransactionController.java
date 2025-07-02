package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.adapters.dtos.paymenttransaction.ListTransactionsResponse;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.PayTransactionRequest;
import com.reminder.financial.api.adapters.dtos.paymenttransaction.UpdatePaymentTransactionRequest;
import com.reminder.financial.api.adapters.mappers.PaymentTransactionMapper;
import com.reminder.financial.api.application.usecases.paymenttransaction.ListTransactionsByReminderUseCase;
import com.reminder.financial.api.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.reminder.financial.api.application.usecases.paymenttransaction.UpdatePaymentTransactionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payments/transactions")
@Tag(name = "Payment Transactions", description = "Payment transactions management.")
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionMapper paymentTransactionMapper;

    @Autowired
    PayTransactionUseCase payTransactionUseCase;

    @Autowired
    ListTransactionsByReminderUseCase listTransactionsByReminderUseCase;

    @Autowired
    private UpdatePaymentTransactionUseCase updateTransactionUseCase;


    @PostMapping("/{transactionId}/pay")
    @Operation(summary = "Make a payment for a pending transaction")
    public ResponseEntity<Void> payTransaction(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @PathVariable String transactionId,
            @RequestBody PayTransactionRequest request
    ) {
        var command = paymentTransactionMapper.toCommand(
                loggedUserEmail,
                transactionId,
                request
        );

        payTransactionUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    @Operation(
            summary = "List transactions for a specific reminder",
            description = "Returns a paginated list of all transactions associated with a reminder ID."
    )
    public ResponseEntity<Page<ListTransactionsResponse>> listByReminder(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @RequestParam String reminderId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = {"status", "paymentDate"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        var result = listTransactionsByReminderUseCase.execute(loggedUserEmail, reminderId, pageable);

        Page<ListTransactionsResponse> response = result.map(paymentTransactionMapper::toListTransactionsResponse);
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
        var command = paymentTransactionMapper.toCommand(
                loggedUserEmail,
                id,
                request
        );

        updateTransactionUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}

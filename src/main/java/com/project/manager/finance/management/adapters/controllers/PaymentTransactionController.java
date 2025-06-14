package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.paymenttransaction.ListTransactionsResponse;
import com.project.manager.finance.management.adapters.dtos.paymenttransaction.PayTransactionRequest;
import com.project.manager.finance.management.adapters.mappers.PaymentTransactionMapper;
import com.project.manager.finance.management.application.usecases.paymenttransaction.ListTransactionsByReminderUseCase;
import com.project.manager.finance.management.application.usecases.paymenttransaction.PayTransactionUseCase;
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
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionMapper paymentTransactionMapper;

    @Autowired
    PayTransactionUseCase payTransactionUseCase;

    @Autowired
    ListTransactionsByReminderUseCase listTransactionsByReminderUseCase;


    @PostMapping("/{transactionId}/pay")
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
    public ResponseEntity<Page<ListTransactionsResponse>> listByReminder(
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = {"status", "paymentDate"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @RequestParam String reminderId
    ) {
        var result = listTransactionsByReminderUseCase.execute(loggedUserEmail, reminderId, pageable);

        Page<ListTransactionsResponse> response = result.map(paymentTransactionMapper::toListTransactionsResponse);
        return ResponseEntity.ok(response);
    }
}

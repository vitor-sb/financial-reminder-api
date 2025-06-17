package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.paymenttransaction.ListTransactionsResponse;
import com.project.manager.finance.management.adapters.dtos.paymenttransaction.PayTransactionRequest;
import com.project.manager.finance.management.adapters.dtos.paymenttransaction.UpdatePaymentTransactionRequest;
import com.project.manager.finance.management.adapters.mappers.PaymentTransactionMapper;
import com.project.manager.finance.management.application.usecases.paymenttransaction.ListTransactionsByReminderUseCase;
import com.project.manager.finance.management.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.project.manager.finance.management.application.usecases.paymenttransaction.UpdatePaymentTransactionUseCase;
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
@Tag(name = "Transações de Pagamento", description = "Endpoints para listar, pagar e atualizar transações individuais.")
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
    @Operation(summary = "Efetua o pagamento de uma transação pendente")
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
    @Operation(summary = "Lista as transações de um lembrete específico", description = "Retorna uma lista paginada de todas as transações associadas a um ID de lembrete.")
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

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza os detalhes de uma transação", description = "Permite alterar o valor ou o método de pagamento de uma transação que ainda não foi paga.")
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

package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderRequest;
import com.project.manager.finance.management.adapters.dtos.paymentremider.PaymentReminderResponse;
import com.project.manager.finance.management.adapters.dtos.paymentremider.UpdatePaymentReminderRequest;
import com.project.manager.finance.management.adapters.mappers.PaymentReminderMapper;
import com.project.manager.finance.management.application.dtos.paymentreminder.CreatePaymentReminderCommand;
import com.project.manager.finance.management.application.dtos.paymentreminder.GetPaymentReminderQuery;
import com.project.manager.finance.management.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.project.manager.finance.management.application.usecases.paymentreminder.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/payments/reminders")
@Tag(name = "Lembretes de Pagamento", description = "Gerenciamento de lembretes de pagamento recorrentes ou únicos.")
public class PaymentReminderController {

    @Autowired
    private PaymentReminderMapper paymentReminderMapper;

    @Autowired
    private CreatePaymentReminderUseCase createUseCase;

    @Autowired
    private GetPaymentReminderUseCase getByIdUseCase;

    @Autowired
    private UpdatePaymentReminderUseCase updateUseCase;

    @Autowired
    private ToggleReminderStatusUseCase toggleReminderStatusUseCase;

    @Autowired
    private DeletePaymentReminderUseCase deletePaymentReminderUseCase;


    @PostMapping
    @Operation(summary = "Cria um novo lembrete de pagamento")
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @RequestBody @Valid CreatePaymentReminderRequest dto,
            UriComponentsBuilder uriBuilder
    ) {
        CreatePaymentReminderCommand command = paymentReminderMapper.toCommand(dto, loggedUserEmail);
        var result = createUseCase.execute(command);

        var uri = uriBuilder.path("/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um lembrete de pagamento pelo ID")
    public ResponseEntity<PaymentReminderResponse> getById(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @PathVariable String id
    ) {
        GetPaymentReminderQuery query = paymentReminderMapper.toQuery(id, loggedUserEmail);
        var result = getByIdUseCase.execute(query);

        PaymentReminderResponse response = paymentReminderMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza um lembrete de pagamento")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @PathVariable String id,
            @RequestBody @Valid UpdatePaymentReminderRequest dto
    ) {
        UpdatePaymentReminderCommand command = paymentReminderMapper.toCommand(dto, id, loggedUserEmail);
        updateUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Desativa um lembrete de pagamento")
    public ResponseEntity<Void> disable(@PathVariable String id, @AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleReminderStatusUseCase.execute(id, false, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Reativa um lembrete de pagamento")
    public ResponseEntity<Void> enable(@PathVariable String id, @AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleReminderStatusUseCase.execute(id, true, userEmail);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um lembrete de pagamento")
    public ResponseEntity<Void> delete(@PathVariable String id, @AuthenticationPrincipal(expression = "username") String userEmail) {
        deletePaymentReminderUseCase.execute(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}

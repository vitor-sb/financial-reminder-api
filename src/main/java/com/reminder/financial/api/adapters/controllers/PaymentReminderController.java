package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.adapters.dtos.paymentremider.CreatePaymentReminderRequest;
import com.reminder.financial.api.adapters.dtos.paymentremider.MonthlyBillResponse;
import com.reminder.financial.api.adapters.dtos.paymentremider.PaymentReminderResponse;
import com.reminder.financial.api.adapters.dtos.paymentremider.UpdatePaymentReminderRequest;
import com.reminder.financial.api.adapters.mappers.PaymentReminderMapper;
import com.reminder.financial.api.application.dtos.paymentreminder.CreatePaymentReminderCommand;
import com.reminder.financial.api.application.dtos.paymentreminder.GetPaymentReminderQuery;
import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.reminder.financial.api.application.usecases.paymentreminder.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/payments/reminders")
@Tag(name = "Lembretes de Pagamento", description = "Gerenciamento de lembretes de pagamento recorrentes ou únicos.")
public class PaymentReminderController {

    private static final int MAX_YEARS_IN_FUTURE_ALLOWED = 5;

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

    @Autowired
    private ListMonthlyBillsUseCase listMonthlyBillsUseCase;


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

    @GetMapping("/monthly-bills")
    @Operation(summary = "Lista contas a pagar reais e esperadas para um mês específico",
            description = "Retorna uma lista combinada de transações existentes e contas futuras simuladas com base nos lembretes. Se o mês e o ano não forem fornecidos, retorna as contas para o mês atual.")
    public ResponseEntity<List<MonthlyBillResponse>> getMonthlyBills(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        YearMonth now = YearMonth.now();
        YearMonth queryYearMonth = YearMonth.of(
                year != null ? year : now.getYear(),
                month != null ? month : now.getMonthValue()
        );

        YearMonth limitDate = now.plusYears(MAX_YEARS_IN_FUTURE_ALLOWED);
        if (queryYearMonth.isAfter(limitDate)) {
            throw new IllegalArgumentException(
                    "Date exceeds the allowed limit. You can only query up to " + MAX_YEARS_IN_FUTURE_ALLOWED + " years in the future."
            );
        }

        List<ListMonthlyBillsResult> results = listMonthlyBillsUseCase.execute(loggedUserEmail, queryYearMonth);

        List<MonthlyBillResponse> response = results.stream()
                .map(paymentReminderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}

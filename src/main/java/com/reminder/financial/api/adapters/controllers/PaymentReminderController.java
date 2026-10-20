package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.domains.filters.ReminderFilter;
import com.reminder.financial.api.adapters.dtos.paymentreminder.*;
import com.reminder.financial.api.adapters.mappers.PaymentReminderMapper;
import com.reminder.financial.api.application.dtos.paymentreminder.*;
import com.reminder.financial.api.application.usecases.paymentreminder.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/payments/reminders")
@Tag(name = "Payment Reminders", description = "Manages all aspects of payment reminders, from creation and updates to fetching monthly bill forecasts.")
@RequiredArgsConstructor
public class PaymentReminderController {


    private final PaymentReminderMapper paymentReminderMapper;

    private final CreatePaymentReminderUseCase createUseCase;

    private final GetPaymentReminderUseCase getByIdUseCase;

    private final UpdatePaymentReminderUseCase updateUseCase;

    private final ToggleReminderStatusUseCase toggleReminderStatusUseCase;

    private final DeletePaymentReminderUseCase deletePaymentReminderUseCase;

    private final ListMonthlyBillsUseCase listMonthlyBillsUseCase;

    private final ListPaymentRemindersByUserUseCase listPaymentRemindersByUserUseCase;


    @PostMapping
    @Operation(summary = "Create a new payment reminder")
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @RequestBody @Valid CreatePaymentReminderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        CreatePaymentReminderCommand command = paymentReminderMapper.toCommand(request);
        var result = createUseCase.execute(loggedUserEmail, command);

        var uri = uriBuilder.path("/payments/reminders/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Search for a payment reminder by ID")
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
    @Operation(summary = "Update a payment reminder")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @PathVariable String id,
            @RequestBody @Valid UpdatePaymentReminderRequest request
    ) {
        UpdatePaymentReminderCommand command = paymentReminderMapper.toCommand(request);
        updateUseCase.execute(loggedUserEmail, id, command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disable a payment reminder")
    public ResponseEntity<Void> disable(@PathVariable String id, @AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleReminderStatusUseCase.execute(id, false, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Reactivate a payment reminder")
    public ResponseEntity<Void> enable(@PathVariable String id, @AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleReminderStatusUseCase.execute(id, true, userEmail);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payment reminder")
    public ResponseEntity<Void> delete(@PathVariable String id, @AuthenticationPrincipal(expression = "username") String userEmail) {
        deletePaymentReminderUseCase.execute(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/monthly-bills")
    @Operation(
            summary = "Lists a paginated view of the month's bills (actual and expected) with totals",
            description = "Returns existing transactions and simulated future accounts, including a summary of totals. Defaults to the current month."
    )
    public ResponseEntity<MonthlyBillsSummaryResponse> getMonthlyBills(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(name = "withSimulatedBills", defaultValue = "false") boolean withSimulatedBills,
            @PageableDefault(size = 15, page = 0, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        MonthlyBillsSummaryResult result = listMonthlyBillsUseCase.execute(
                loggedUserEmail,
                year,
                month,
                withSimulatedBills,
                pageable
        );

        MonthlyBillsSummaryResponse response = paymentReminderMapper.toSummaryResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(
            summary = "List Payment Reminders for a logged user",
            description = "Returns a paginated list of all payment reminders associated with the logged in user."
    )
    public ResponseEntity<Page<ListPaymentRemindersResponse>> listByUser(
            @AuthenticationPrincipal(expression = "username") String loggedUserEmail,
            ListPaymentRemindersFilterRequest filter,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = {"baseDate", "recurrence"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        ReminderFilter query = paymentReminderMapper.toListTransactionsFilterQuery(filter);
        var result = listPaymentRemindersByUserUseCase.execute(loggedUserEmail, query, pageable);

        Page<ListPaymentRemindersResponse> response = result.map(paymentReminderMapper::toListPaymentRemindersResponse);
        return ResponseEntity.ok(response);
    }
}

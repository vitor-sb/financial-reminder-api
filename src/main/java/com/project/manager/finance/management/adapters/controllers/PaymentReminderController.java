package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderRequest;
import com.project.manager.finance.management.adapters.dtos.paymentremider.GetPaymentReminderResponse;
import com.project.manager.finance.management.adapters.mappers.PaymentReminderMapper;
import com.project.manager.finance.management.application.dtos.CreatePaymentReminderCommand;
import com.project.manager.finance.management.application.dtos.GetPaymentReminderQuery;
import com.project.manager.finance.management.application.usecases.paymentreminder.CreatePaymentReminderUseCase;
import com.project.manager.finance.management.application.usecases.paymentreminder.GetPaymentReminderUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

@RestController
@RequestMapping("/reminders/payments")
public class PaymentReminderController {
    @Autowired
    private PaymentReminderMapper paymentReminderMapper;
    @Autowired
    private CreatePaymentReminderUseCase createUseCase;
    @Autowired
    private GetPaymentReminderUseCase getByIdUseCase;

    @PostMapping
    public ResponseEntity<Void> create(
            Principal loggedUser,
            @RequestBody @Valid CreatePaymentReminderRequest dto,
            UriComponentsBuilder uriBuilder
    ) {
        CreatePaymentReminderCommand command = paymentReminderMapper.toCommand(dto, loggedUser.getName());
        var result = createUseCase.execute(command);
        var uri = uriBuilder.path("/reminders/payments/" + result.getId()).build().toUri();

        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetPaymentReminderResponse> getById(Principal loggedUser, @PathVariable String id) {
        GetPaymentReminderQuery query = paymentReminderMapper.toQuery(id, loggedUser.getName());
        var result = getByIdUseCase.execute(query);

        GetPaymentReminderResponse response = paymentReminderMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}

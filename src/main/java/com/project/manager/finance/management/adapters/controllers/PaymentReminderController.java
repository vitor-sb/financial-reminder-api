package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.paymentremider.CreatePaymentReminderDTO;
import com.project.manager.finance.management.adapters.dtos.paymentremider.ResponseGetPaymentReminderDTO;
import com.project.manager.finance.management.adapters.usecases.paymentreminder.CreatePaymentReminderUseCase;
import com.project.manager.finance.management.adapters.usecases.paymentreminder.GetPaymentReminderUseCase;
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
    private CreatePaymentReminderUseCase createUseCase;
    @Autowired
    private GetPaymentReminderUseCase getByIdUseCase;

    @PostMapping
    public ResponseEntity<Void> create(
            Principal loggedUser,
            @RequestBody @Valid CreatePaymentReminderDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        var createdReminder = createUseCase.execute(dto, loggedUser.getName());
        var uri = uriBuilder.path("/reminders/payments/" + createdReminder.getId()).build().toUri();

        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGetPaymentReminderDTO> getById(Principal loggedUser, @PathVariable String id) {
        var response = getByIdUseCase.execute(id, loggedUser.getName());

        return ResponseEntity.ok(response);
    }
}

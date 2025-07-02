package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.adapters.dtos.user.CreateUserRequest;
import com.reminder.financial.api.adapters.dtos.user.ListUsersFilterRequest;
import com.reminder.financial.api.adapters.dtos.user.ListUsersSingleResponse;
import com.reminder.financial.api.adapters.mappers.UserMapper;
import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.usecases.user.DeleteSelfUserAccountUseCase;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import com.reminder.financial.api.application.usecases.user.CreateUserUseCase;
import com.reminder.financial.api.application.usecases.user.ToggleSelfUserAccountUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("users")
@Tag(name = "Users", description = "Handles user self-service actions, such as registration and personal account management (disable, enable, delete).")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Autowired
    private ListUsersUseCase listUsersUseCase;

    @Autowired
    private ToggleSelfUserAccountUseCase toggleSelfUserAccountUseCase;

    @Autowired
    private DeleteSelfUserAccountUseCase deleteSelfUserAccountUseCase;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Public endpoint to register a new user in the system.")
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserRequest dto) {
        CreateUserCommand command = userMapper.toCommand(dto);

        createUserUseCase.execute(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/disable")
    @Operation(summary = "Disables the logged in user's account")
    public ResponseEntity<Void> disableSelf(@AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleSelfUserAccountUseCase.execute(userEmail, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/enable")
    @Operation(summary = "Reactivates the logged in user's account")
    public ResponseEntity<Void> enableSelf(@AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleSelfUserAccountUseCase.execute(userEmail, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Deletes the logged in user account")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal(expression = "username") String userEmail) {
        deleteSelfUserAccountUseCase.execute(userEmail);
        return ResponseEntity.noContent().build();
    }
}

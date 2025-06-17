package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersFilterRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersSingleResponse;
import com.project.manager.finance.management.adapters.mappers.UserMapper;
import com.project.manager.finance.management.application.dtos.user.CreateUserCommand;
import com.project.manager.finance.management.application.dtos.user.ListUsersFilterQuery;
import com.project.manager.finance.management.application.usecases.user.DeleteSelfUserAccountUseCase;
import com.project.manager.finance.management.application.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.application.usecases.user.CreateUserUseCase;
import com.project.manager.finance.management.application.usecases.user.ToggleSelfUserAccountUseCase;
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
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserRequest dto) {
        CreateUserCommand command = userMapper.toCommand(dto);

        createUserUseCase.execute(command);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ADMIN_PERMISSION')")
    @GetMapping("/list")
    public ResponseEntity<Page<ListUsersSingleResponse>> listAll(
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = {"audit.createdAt", "name"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            ListUsersFilterRequest filter
    ) {
        ListUsersFilterQuery query = userMapper.toListUsersFilterQuery(filter);
        var result = listUsersUseCase.execute(query, pageable);

        Page<ListUsersSingleResponse> response = result.map(userMapper::toListUsersSingleResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account/disable")
    public ResponseEntity<Void> disableSelf(@AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleSelfUserAccountUseCase.execute(userEmail, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/account/enable")
    public ResponseEntity<Void> enableSelf(@AuthenticationPrincipal(expression = "username") String userEmail) {
        toggleSelfUserAccountUseCase.execute(userEmail, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal(expression = "username") String userEmail) {
        deleteSelfUserAccountUseCase.execute(userEmail);
        return ResponseEntity.noContent().build();
    }
}

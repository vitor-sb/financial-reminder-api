package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.adapters.dtos.user.ListUsersFilterRequest;
import com.reminder.financial.api.adapters.dtos.user.ListUsersSingleResponse;
import com.reminder.financial.api.adapters.mappers.UserMapper;
import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ADMIN_PERMISSION')")
public class UserAdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ListUsersUseCase listUsersUseCase;

    @GetMapping("/list")
    public ResponseEntity<Page<ListUsersSingleResponse>> listAll(
            ListUsersFilterRequest filter,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = {"audit.createdAt", "name"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        ListUsersFilterQuery query = userMapper.toListUsersFilterQuery(filter);
        var result = listUsersUseCase.execute(query, pageable);

        Page<ListUsersSingleResponse> response = result.map(userMapper::toListUsersSingleResponse);
        return ResponseEntity.ok(response);
    }
}
package com.reminder.financial.api.adapters.in.web.controllers;

import com.reminder.financial.api.adapters.in.web.dtos.user.ListUsersFilterRequest;
import com.reminder.financial.api.adapters.in.web.dtos.user.ListUsersSingleResponse;
import com.reminder.financial.api.adapters.in.web.mappers.UserMapper;
import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.reminder.financial.api.adapters.in.web.mappers.PageAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ADMIN_PERMISSION')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserMapper userMapper;

    private final ListUsersUseCase listUsersUseCase;

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
        UserFilter query = userMapper.toListUsersFilterQuery(filter);
        var result = listUsersUseCase.execute(query, PageAdapter.toRequest(pageable));

        Page<ListUsersSingleResponse> response = PageAdapter.toPage(result.map(userMapper::toListUsersSingleResponse), pageable);
        return ResponseEntity.ok(response);
    }
}
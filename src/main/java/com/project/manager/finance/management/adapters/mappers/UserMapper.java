package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersFilterRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersSingleResponse;
import com.project.manager.finance.management.application.dtos.CreateUserCommand;
import com.project.manager.finance.management.application.dtos.ListUsersFilterQuery;
import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public CreateUserCommand toCommand(@Valid CreateUserRequest dto) {
        return new CreateUserCommand(
                dto.name(),
                dto.email(),
                dto.password(),
                dto.role()
        );
    }

    public ListUsersSingleResponse toListUsersSingleResponse(ListUsersSingleResult result) {
        return new ListUsersSingleResponse(result.name(), result.email(), result.role(), result.enabled(), result.deleted());
    }

    public ListUsersFilterQuery toListUsersFilterQuery(ListUsersFilterRequest filter) {
        return new ListUsersFilterQuery(
                filter.name(),
                filter.email(),
                filter.role(),
                filter.enabled(),
                filter.deleted()
        );
    }
}
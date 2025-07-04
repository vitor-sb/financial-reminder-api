package com.reminder.financial.api.adapters.mappers;

import com.reminder.financial.api.adapters.dtos.user.CreateUserRequest;
import com.reminder.financial.api.adapters.dtos.user.ListUsersFilterRequest;
import com.reminder.financial.api.adapters.dtos.user.ListUsersSingleResponse;
import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
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
        return new ListUsersSingleResponse(
                result.name(),
                result.email(),
                result.role(),
                result.create_at(),
                result.enabled(),
                result.deleted());
    }

    public ListUsersFilterQuery toListUsersFilterQuery(ListUsersFilterRequest filter) {
        if (filter == null)
            return new ListUsersFilterQuery(null, null, null, null, null, null, null);

        return new ListUsersFilterQuery(
                filter.name(),
                filter.email(),
                filter.role(),
                filter.createdFrom(),
                filter.createdUntil(),
                filter.enabled(),
                filter.deleted()
        );
    }
}
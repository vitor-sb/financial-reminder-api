package com.reminder.financial.api.adapters.in.web.mappers;

import com.reminder.financial.api.adapters.in.web.dtos.user.CreateUserRequest;
import com.reminder.financial.api.adapters.in.web.dtos.user.EnableUserRequest;
import com.reminder.financial.api.adapters.in.web.dtos.user.ListUsersFilterRequest;
import com.reminder.financial.api.adapters.in.web.dtos.user.ListUsersSingleResponse;
import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.application.dtos.user.EnableUserCommand;
import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public CreateUserCommand toCommand(@Valid CreateUserRequest dto) {
        return new CreateUserCommand(
                dto.name(),
                dto.email(),
                dto.password()
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

    public UserFilter toListUsersFilterQuery(ListUsersFilterRequest filter) {
        if (filter == null)
            return new UserFilter(null, null, null, null, null, null, null);

        return new UserFilter(
                filter.name(),
                filter.email(),
                filter.role(),
                filter.createdFrom(),
                filter.createdUntil(),
                filter.enabled(),
                filter.deleted()
        );
    }

    public EnableUserCommand toCommand(@Valid EnableUserRequest dto) {
        return new EnableUserCommand(
                dto.email(),
                dto.password()
        );
    }
}
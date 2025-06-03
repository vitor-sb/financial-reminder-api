package com.project.manager.finance.management.adapters.mappers;

import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersSingleResponse;
import com.project.manager.finance.management.application.dtos.CreateUserCommand;
import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.List;

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

    private ListUsersSingleResponse toSingleResponse(ListUsersSingleResult result) {
        return new ListUsersSingleResponse(result.name(), result.email(), result.role(), result.enabled(), result.deleted());
    }

    public List<ListUsersSingleResponse> toResponse(List<ListUsersSingleResult> results) {
        return results.stream()
                .map(this::toSingleResponse)
                .toList();
    }
}
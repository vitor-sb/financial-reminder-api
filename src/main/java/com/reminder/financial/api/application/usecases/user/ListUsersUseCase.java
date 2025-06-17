package com.reminder.financial.api.application.usecases.user;


import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListUsersUseCase {
    Page<ListUsersSingleResult> execute(ListUsersFilterQuery role, Pageable pageable);
}

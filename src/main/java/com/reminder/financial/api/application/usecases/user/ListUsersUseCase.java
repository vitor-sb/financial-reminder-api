package com.reminder.financial.api.application.usecases.user;


import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;

public interface ListUsersUseCase {
    PageResult<ListUsersSingleResult> execute(UserFilter role, PageRequest pageable);
}

package com.project.manager.finance.management.application.usecases.user;


import com.project.manager.finance.management.application.dtos.user.ListUsersFilterQuery;
import com.project.manager.finance.management.application.dtos.user.ListUsersSingleResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListUsersUseCase {
    Page<ListUsersSingleResult> execute(ListUsersFilterQuery role, Pageable pageable);
}

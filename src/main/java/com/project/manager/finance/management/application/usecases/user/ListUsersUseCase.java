package com.project.manager.finance.management.application.usecases.user;


import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;

import java.util.List;

public interface ListUsersUseCase {
    List<ListUsersSingleResult> execute();
}

package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.domains.models.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserSearch {

    Page<User> searchByFilters(ListUsersFilterQuery query, Pageable pageable);
}

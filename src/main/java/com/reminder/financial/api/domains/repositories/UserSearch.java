package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;

public interface UserSearch {

    PageResult<User> searchByFilters(UserFilter query, PageRequest pageable);
}

package com.project.manager.finance_management.domains.models.user;


import lombok.Getter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
public enum Role {
    OWNER(EnumSet.of(Permission.OWNER_PERMISSION, Permission.ADMIN_PERMISSION)),
    ADMIN(EnumSet.of(Permission.ADMIN_PERMISSION)),
    MEMBER(EnumSet.of(Permission.MEMBER_PERMISSION, Permission.USER_PERMISSION)),
    USER(EnumSet.of(Permission.USER_PERMISSION));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = Collections.unmodifiableSet(EnumSet.copyOf(permissions));
    }
}
package com.project.manager.finance_management.domains.models.user.enums;


import lombok.Getter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
public enum RoleEnum {
    OWNER(EnumSet.of(PermissionEnum.OWNER_PERMISSION, PermissionEnum.ADMIN_PERMISSION)),
    ADMIN(EnumSet.of(PermissionEnum.ADMIN_PERMISSION)),
    MEMBER(EnumSet.of(PermissionEnum.MEMBER_PERMISSION, PermissionEnum.USER_PERMISSION)),
    USER(EnumSet.of(PermissionEnum.USER_PERMISSION));

    private final Set<PermissionEnum> permissions;

    RoleEnum(Set<PermissionEnum> permissions) {
        this.permissions = Collections.unmodifiableSet(EnumSet.copyOf(permissions));
    }
}
package com.project.manager.finance_management.domains.models.user;

public enum Role {
    ADMIN("Admin"),
    USER("User");

    private String role;

    Role(String role) {
        this.role = role;
    }
}

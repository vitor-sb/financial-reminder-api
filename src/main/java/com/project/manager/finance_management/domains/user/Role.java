package com.project.manager.finance_management.domains.user;

public enum Role {
    ADMIN("Admin"),
    USER("User");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}

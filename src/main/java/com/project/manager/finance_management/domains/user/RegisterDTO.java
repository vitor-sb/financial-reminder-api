package com.project.manager.finance_management.domains.user;

public record RegisterDTO(String name, String login, String password, Role role) {
}

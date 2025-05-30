package com.project.manager.finance.management.infrastructure.midlewares.exceptions;

import lombok.Getter;

@Getter
public class UserDisabledException extends RuntimeException {
  private final String disabledBy;

  public UserDisabledException(String message, String disabledBy) {
    super(message);
    this.disabledBy = disabledBy;
  }

  public UserDisabledException(String disabledBy) {
    super("User account is disabled by: " + disabledBy);
    this.disabledBy = disabledBy;
  }
}
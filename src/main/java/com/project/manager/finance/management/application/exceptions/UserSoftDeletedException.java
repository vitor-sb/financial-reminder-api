package com.project.manager.finance.management.application.exceptions;

import lombok.Getter;

@Getter
public class UserSoftDeletedException extends RuntimeException {
  private final String deletedBy;

  public UserSoftDeletedException(String message, String deletedBy) {
    super(message);
    this.deletedBy = deletedBy;
  }

  public UserSoftDeletedException(String deletedBy) {
    super("User account has been deleted by: " + deletedBy);
    this.deletedBy = deletedBy;
  }

}
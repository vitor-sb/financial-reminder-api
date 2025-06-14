package com.project.manager.finance.management.application.exceptions;

import lombok.Getter;

@Getter
public class ResourceDisabledException extends RuntimeException {
  private final String disabledBy;

  public ResourceDisabledException(String resourceName, String disabledBy) {
    super(resourceName + " is disabled by: " + (disabledBy != null ? disabledBy : "system"));
    this.disabledBy = disabledBy;
  }
}
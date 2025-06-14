package com.project.manager.finance.management.application.exceptions;

import lombok.Getter;

@Getter
public class ResourceDeletedException extends RuntimeException {
  private final String deletedBy;

  public ResourceDeletedException(String resourceName, String deletedBy) {
    super(resourceName + " has been deleted by: " + (deletedBy != null ? deletedBy : "system"));
    this.deletedBy = deletedBy;
  }

}
package com.appsBuild.club_management_system.exception.impl;

import com.appsBuild.club_management_system.exception.ApplicationException;

public class ConflictException extends ApplicationException {
  public ConflictException(String message) {
    super(message);
  }
}

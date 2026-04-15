package com.appsBuild.club_management_system.exception.impl;

import com.appsBuild.club_management_system.exception.ApplicationException;

public class NotFoundException extends ApplicationException {
  public NotFoundException(String message) {
    super(message);
  }
}

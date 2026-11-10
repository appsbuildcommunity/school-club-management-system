package com.appsBuild.club_management_system.exception.impl;

import com.appsBuild.club_management_system.exception.ApplicationException;

public class UnauthorizedException extends ApplicationException {
  public UnauthorizedException(String message) {
    super(message);
  }
}

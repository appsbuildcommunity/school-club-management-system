package com.appsBuild.club_management_system.annotation;

import com.appsBuild.club_management_system.model.enums.Category;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrantableEndpoint {
  String name();

  String description();

  Category category();

  boolean privileged();
}

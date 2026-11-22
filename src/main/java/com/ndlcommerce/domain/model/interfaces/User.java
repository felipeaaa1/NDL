package com.ndlcommerce.domain.model.interfaces;

import com.ndlcommerce.domain.UserType;

public interface User {
  boolean passwordIsValid();

  boolean loginIsValid();

  String getLogin();

  String getEmail();

  String getPassword();

  UserType getType();
}

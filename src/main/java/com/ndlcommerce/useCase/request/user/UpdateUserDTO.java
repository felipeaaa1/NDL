package com.ndlcommerce.useCase.request.user;

import com.ndlcommerce.domain.UserType;

public class UpdateUserDTO {
  private String login;
  private UserType type;
  private String email;

  public String getLogin() {
    return login;
  }

  public UserType getType() {
    return type;
  }

  public String getEmail() {
    return email;
  }
}

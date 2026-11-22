package com.ndlcommerce.domain.model.implementation;

import com.ndlcommerce.domain.UserType;
import com.ndlcommerce.domain.model.interfaces.User;
import java.util.regex.Pattern;

public class CommonUser implements User {
  private String login;
  private String email;
  private String password;
  private UserType type;

  @Override
  public boolean passwordIsValid() {
    return password != null
        && password.length() > 5
        && Pattern.compile("[a-z]").matcher(password).find()
        && Pattern.compile("[A-Z]").matcher(password).find();
  }

  @Override
  public boolean loginIsValid() {
    return login != null && login.length() >= 4;
  }

  @Override
  public String getLogin() {
    return this.login;
  }

  @Override
  public String getEmail() {
    return this.email;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public UserType getType() {
    return this.type;
  }

  public CommonUser(String login, String email, UserType type, String password) {
    this.login = login;
    this.email = email;
    this.type = type;
    this.password = password;
  }

  public CommonUser(String login, String email, UserType type) {
    this.login = login;
    this.email = email;
    this.type = type;
  }
}

package com.ndlcommerce.domain.factory.implementation;

import com.ndlcommerce.domain.UserType;
import com.ndlcommerce.domain.factory.interfaces.UserFactory;
import com.ndlcommerce.domain.model.implementation.CommonUser;
import com.ndlcommerce.domain.model.interfaces.User;

public class CommonUserFactoryImp implements UserFactory {
  @Override
  public User create(String login, String email, UserType type, String password) {
    CommonUser user = new CommonUser(login, email, type, password);
    return user;
  }
}

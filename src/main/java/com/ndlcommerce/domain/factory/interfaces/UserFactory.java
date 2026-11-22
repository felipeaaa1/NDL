package com.ndlcommerce.domain.factory.interfaces;

import com.ndlcommerce.domain.UserType;
import com.ndlcommerce.domain.model.interfaces.User;

public interface UserFactory {
  User create(String login, String email, UserType type, String password);
}

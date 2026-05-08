package com.ndlcommerce.useCase.request.user;

import com.ndlcommerce.entity.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserFilterDTO {

  String login;
  String email;
  UserType type;

  public UserFilterDTO() {}

  public UserFilterDTO(String login, String email, String type) {
    this.login = login;
    this.email = email;
    this.type = UserType.valueOf(type);
  }
}

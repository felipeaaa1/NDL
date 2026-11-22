package com.ndlcommerce.useCase.request.user;

import com.ndlcommerce.domain.UserType;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

public class UserDbRequestDTO {

  @Getter private final String login;
  @Getter private final String email;
  @Getter private final UserType type;
  @Getter private String password;
  private LocalDateTime creationTime;
  @Setter private boolean active;

  public UserDbRequestDTO(String login, String email, UserType type, String password) {
    this.login = login;
    this.email = email;
    this.type = type;
    this.password = password;
  }

  public UserDbRequestDTO(String login, String email, UserType type) {
    this.login = login;
    this.email = email;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserDbRequestDTO)) return false;
    UserDbRequestDTO that = (UserDbRequestDTO) o;
    return Objects.equals(login, that.login)
        && Objects.equals(password, that.password)
        && Objects.equals(type, that.type)
        && Objects.equals(email, that.email)
        && Objects.equals(creationTime, that.creationTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(login, password, creationTime);
  }
}

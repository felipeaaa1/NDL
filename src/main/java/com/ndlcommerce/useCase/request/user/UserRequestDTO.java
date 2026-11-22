package com.ndlcommerce.useCase.request.user;

import com.ndlcommerce.domain.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Setter;

@Schema(name = "Usuário")
public class UserRequestDTO {

  @Schema(example = "common user")
  @NotBlank
  @Size(min = 4, max = 50)
  String login;

  @Schema(example = "Password")
  @NotBlank
  @Size(
      min = 5,
      message =
          "Senha inválida, senha deve ter no mínimo 5 caracteres, uma letra maiúscula e uma minuscula")
  String password;

  @Schema(example = "teste@teste.com")
  @NotBlank
  @Email(message = "E-mail inválido")
  String email;

  @Setter @NotNull UserType type;

  public UserRequestDTO(String login, UserType type, String email, String password) {
    this.login = login;
    this.type = type;
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public UserType getType() {
    return type;
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }
}

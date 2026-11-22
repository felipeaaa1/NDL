package com.ndlcommerce.adapters.web;

import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.adapters.web.dto.AuthenticationDTO;
import com.ndlcommerce.adapters.web.dto.LoginResponseDTO;
import com.ndlcommerce.config.TokenService;
import com.ndlcommerce.domain.UserType;
import com.ndlcommerce.useCase.interfaces.user.UserInputBoundary;
import com.ndlcommerce.useCase.request.user.UserRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@Tag(name = "Autorização")
public class AuthenticationController {

  private final UserInputBoundary userInput;

  private final AuthenticationManager authenticationManager;

  private final TokenService tokenService;

  public AuthenticationController(
      UserInputBoundary userInput,
      AuthenticationManager authenticationManager,
      TokenService tokenService) {
    this.userInput = userInput;
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
  }

  @PostMapping("/login")
  @Operation(
      summary = "Logar",
      description = "End-point que retorna JWT token usado para autenticar na aplicação")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sucesso ao gerar o token"),
    @ApiResponse(responseCode = "401", description = "Usuário inexistente ou senha inválida"),
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
    @ApiResponse(responseCode = "406", description = "Erro de leitura do JSON"),
  })
  public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {

    var userPass = new UsernamePasswordAuthenticationToken(data.login(), data.password());
    var auth = this.authenticationManager.authenticate(userPass);

    String token = tokenService.generateToken((UserDataMapper) auth.getPrincipal());

    return ResponseEntity.ok(new LoginResponseDTO(token));
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Registrar",
      description = "Registrar usuários sem necessidade de usuário de criação")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
    @ApiResponse(responseCode = "406", description = "Erro de leitura do JSON"),
    @ApiResponse(
        responseCode = "406",
        description =
            "Valor inválido para o campo 'type': 'teste'. Valores permitidos: [COMMON, ADMIN]"),
    @ApiResponse(responseCode = "409", description = "Email já cadastrado"),
    @ApiResponse(responseCode = "422", description = "não deve estar em branco")
  })
  public ResponseEntity<?> create(@Valid @RequestBody UserRequestDTO requestModel) {
    requestModel.setType(UserType.COMMON);
    var userCreated = userInput.create(requestModel);
    return ResponseEntity.created(null).body(userCreated);
  }

  @Operation(
      summary = "Enviar Email de validação",
      description = "Enviar email com link de validação contendo token UUID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "E-mail enviado com sucesso"),
    @ApiResponse(
        responseCode = "400",
        description =
            "ID com formato inesperado. Era esperado um UUID (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)")
  })
  @PostMapping("/emailValidation/{userID}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity<?> sendEmailValidate(@PathVariable("userID") String userID)
      throws IOException {
    userInput.sendValidationToken(UUID.fromString(userID));
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Validar Email",
      description = "Endpoint para apontar email valido e colocar token como usado")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "E-mail enviado com sucesso"),
    @ApiResponse(
        responseCode = "400",
        description =
            "ID com formato inesperado. Era esperado um UUID (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)")
  })
  @GetMapping("/verifyEmail")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity<?> verifyEmail(@RequestParam(name = "token") String token) {
    userInput.verifyEmail(UUID.fromString(token));
    return ResponseEntity.ok().build();
  }
}

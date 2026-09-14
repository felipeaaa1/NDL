package com.ndlcommerce.config.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ndlcommerce.adapters.web.dto.ErrorFieldDTO;
import com.ndlcommerce.adapters.web.dto.ErrorResponseDTO;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.View;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final View error;
  private final DefaultErrorAttributes errorAttributes;

  public GlobalExceptionHandler(View error, DefaultErrorAttributes errorAttributes) {
    this.error = error;
    this.errorAttributes = errorAttributes;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponseDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    System.err.println(e.getMessage());
    List<FieldError> fieldError = e.getFieldErrors();
    List<ErrorFieldDTO> erros =
        fieldError.stream()
            .map(fe -> new ErrorFieldDTO(fe.getField(), fe.getDefaultMessage()))
            .collect(Collectors.toList());

    return new ErrorResponseDTO(
        HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de validação", erros);
  }

  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  public ErrorResponseDTO handleBusinessException(BusinessException e) {
    return ErrorResponseDTO.notAcceptable(e.getMessage());
  }

  @ExceptionHandler(EntityAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponseDTO handleUserAlreadyExistsException(EntityAlreadyExistsException e) {
    return ErrorResponseDTO.conflict(e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  public ErrorResponseDTO handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    String message = "Erro de leitura do JSON";

    // Caso seja um erro de enum inválido
    if (cause instanceof InvalidFormatException invalidEx) {
      JsonMappingException.Reference ref =
          invalidEx.getPath().isEmpty() ? null : invalidEx.getPath().get(0);
      String fieldName = (ref != null) ? ref.getFieldName() : "campo desconhecido";
      String invalidValue = invalidEx.getValue().toString();
      Class<?> targetType = invalidEx.getTargetType();

      // Se targetType for enum, podemos customizar
      if (targetType.isEnum()) {
        Object[] allowedValues = targetType.getEnumConstants();
        String allowed =
            java.util.Arrays.stream(allowedValues)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        message =
            String.format(
                "Valor inválido para o campo '%s': '%s'. Valores permitidos: [%s]",
                fieldName, invalidValue, allowed);
      } else {
        message = "Formato inválido no campo '" + fieldName + "'";
      }
    }

    return new ErrorResponseDTO(HttpStatus.NOT_ACCEPTABLE.value(), message, java.util.List.of());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDTO handleInvalidUUID(IllegalArgumentException e) {
    if (e.getMessage().contains("Invalid UUID string")) {
      return ErrorResponseDTO.badRequest(
          "ID com formato inesperado. Era esperado um UUID (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)");
    }
    return ErrorResponseDTO.withErrors(e.getMessage(), List.of());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoSuchElementException.class)
  public ErrorResponseDTO handleNoSuchElementException(NoSuchElementException e) {
    System.err.println(e.getMessage());
    return ErrorResponseDTO.notFound(
        e.getMessage() == null ? "Elemento com id fornecido não encontrado" : e.getMessage());
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(AuthorizationDeniedException.class)
  public ErrorResponseDTO handleAccessDeniedException(AuthorizationDeniedException e) {
    System.err.println(e.getMessage());
    return ErrorResponseDTO.forbidden("Usuário sem permissão para essa operação");
  }

  @ExceptionHandler(EmailSendException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ErrorResponseDTO handleEmailSendException(EmailSendException e) {
    return ErrorResponseDTO.withErrors(
        "Erro ao enviar email. Por gentileza, tente novamente mais tarde.", List.of());
  }

  @ExceptionHandler({
    BadCredentialsException.class,
    LockedException.class,
    CredentialsExpiredException.class,
    DisabledException.class
  })
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponseDTO handleBadCredentialsException(Exception e) {
    return ErrorResponseDTO.badCredentials(e.getMessage());
  }

  @ExceptionHandler(InternalAuthenticationServiceException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponseDTO handleInternalAuthenticationServiceException(
      InternalAuthenticationServiceException e) {
    if (e.getMessage()
        .contains("UserDetailsService returned null, which is an interface contract violation")) {

      return ErrorResponseDTO.notFound("Usuário não encontrado");
    }
    return ErrorResponseDTO.notFound(e.getMessage());
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDTO handleHandlerMethodValidationException(
      HandlerMethodValidationException e) {

    List<ErrorFieldDTO> errors =
        e.getAllValidationResults().stream()
            .map(
                error ->
                    new ErrorFieldDTO(
                        error.getMethodParameter().getParameterName(),
                        error.getResolvableErrors().getFirst().getDefaultMessage()))
            .toList();

    return ErrorResponseDTO.withErrors("Validation failure", errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDTO handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {

    String parameterName = e.getName();

    Object value = e.getValue();

    String expectedType =
        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";

    ErrorFieldDTO error =
        new ErrorFieldDTO(
            parameterName,
            String.format("Invalid value '%s'. Expected type: %s", value, expectedType));

    return ErrorResponseDTO.withErrors("Validation failure", List.of(error));
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponseDTO handleDataAccessException(DataAccessException e) {
    log.error(String.valueOf(e));
    ErrorFieldDTO error =
        new ErrorFieldDTO(
            "Erro nosso",
            "Na real isso não era nem pra um usuário estar vendo, eu sinto mto vc passar por isso :/");
    return ErrorResponseDTO.internalServer(
        "Erro ao tratar requisição, favor contate o suporte", List.of(error));
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponseDTO handleUnexpected(RuntimeException e) {
    log.error("Erro inesperado durante a requisição", e);
    return new ErrorResponseDTO(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "🎉 Parabeeens🎉 você achou um erro não tratado! Por gentileza entre em contato com o suporte e informe a mensagem e causa do erro",
        List.of());
  }
}

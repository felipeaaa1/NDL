package com.ndlcommerce.adapters.persistence.brand;

import com.ndlcommerce.config.exception.BusinessException;
import com.ndlcommerce.config.exception.EntityAlreadyExistsException;
import com.ndlcommerce.useCase.interfaces.brand.BrandPresenter;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class BrandResponseFormatter implements BrandPresenter {

  private static final Map<String, RuntimeException> ERRORS =
      Map.of(
          "NameNotValid",
          new BusinessException(
              "Nome da marca não é válido, deve ter no mínimo 3 e no máximo 50 caracteres"),
          "ExistByName",
          new EntityAlreadyExistsException("Marca já cadastrada"),
          "NotFound",
          new NoSuchElementException());

  @Override
  public BrandResponseDTO prepareFailView(String error) {
    RuntimeException runtimeException = ERRORS.get(error);

    if (runtimeException != null) {
      throw runtimeException;
    }

    throw new RuntimeException("Erro desconhecido: " + error);
  }

  @Override
  public BrandResponseDTO prepareSuccessView(BrandResponseDTO response) {
    if (response != null) {
      LocalDateTime date = LocalDateTime.parse(response.getCreatedAt());
      response.setCreatedAt(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    return response;
  }

  @Override
  public PaginatedResult<BrandResponseDTO> prepareListSuccessView(
      PaginatedResult<BrandResponseDTO> list) {
    if (list != null) {
      list.getContent()
          .forEach(
              brandResponseDTO -> {
                LocalDateTime date = LocalDateTime.parse(brandResponseDTO.getCreatedAt());
                brandResponseDTO.setCreatedAt(
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
              });
      return list;
    }

    return new PaginatedResult<>(List.of(), 0, 0, 0, 0, true, true);
  }
}

package com.ndlcommerce.adapters.persistence.category;

import com.ndlcommerce.config.exception.BusinessException;
import com.ndlcommerce.config.exception.EntityAlreadyExistsException;
import com.ndlcommerce.useCase.interfaces.category.CategoryPresenter;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.category.CategoryResponseDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CategoryResponseFormatter implements CategoryPresenter {

  private static final Map<String, RuntimeException> ERRORS =
      Map.of(
          "NameNotValid",
          new BusinessException(
              "Nome da categoria não é válido, deve ter no mínimo 3 e no máximo 50 caracteres"),
          "DescriptionNotValid",
          new BusinessException(
              "Descrição da categoria não é válida, Descrição deve ter no mínimo 3 e no max 200 caracteres"),
          "ExistByName",
          new EntityAlreadyExistsException("Categoria já cadastrada"),
          "NotFound",
          new NoSuchElementException(),
          "ParentNotFound",
          new NoSuchElementException("Categoria pai não encontrada"));

  @Override
  public CategoryResponseDTO prepareSuccessView(CategoryResponseDTO response) {

    if (response != null) {
      LocalDateTime date = LocalDateTime.parse(response.getCreatedAt());

      response.setCreatedAt(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    return response;
  }

  @Override
  public PaginatedResult<CategoryResponseDTO> prepareListSuccessView(
      PaginatedResult<CategoryResponseDTO> list) {
    if (list != null) {
      list.getContent()
          .forEach(
              categoryResponseDTO -> {
                LocalDateTime date = LocalDateTime.parse(categoryResponseDTO.getCreatedAt());

                categoryResponseDTO.setCreatedAt(
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
              });
      return list;
    }

    return new PaginatedResult<>(List.of(), 0, 0, 0, 0, true, true);
  }

  @Override
  public CategoryResponseDTO prepareFailView(String error) {

    RuntimeException runtimeException = ERRORS.get(error);

    if (runtimeException != null) {
      throw runtimeException;
    }

    throw new RuntimeException("Erro desconhecido: " + error);
  }
}

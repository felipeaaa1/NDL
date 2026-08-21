package com.ndlcommerce.adapters.persistence.product;

import com.ndlcommerce.config.exception.BusinessException;
import com.ndlcommerce.config.exception.EntityAlreadyExistsException;
import com.ndlcommerce.useCase.interfaces.product.ProductPresenter;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NoSuchElementException;

public class ProductResponseFormatter implements ProductPresenter {

  private static final Map<String, RuntimeException> ERRORS =
      Map.of(
          "NameNotValid",
          new BusinessException(
              "Nome do produto não é válido, Nome deve ter no mínimo 3 e no max 200 letras"),
          "DescriptionNotValid",
          new BusinessException(
              "o Descrição não é válida, Descrição deve ter no mínimo 5 e no max 500 letras"),
          "SkuCodeDuplicated",
          new BusinessException(
              "A lista de SKU contem SKUs duplicados, por favor, verifique a lista de SKU"),
          "ExistByName",
          new EntityAlreadyExistsException("Produto Já cadastrado"),
          "SkuAlreadyExists",
          new EntityAlreadyExistsException("SKU fornecida Já cadastrado"),
          "BrandNotFound",
          new NoSuchElementException("UUID da Marca fornecida não foi encontrado"),
          "CategoryNotFound",
          new NoSuchElementException("UUID da Categoria fornecida não foi encontrado"),
          "SkuIsNotValid",
          new BusinessException("Um ou mais SKUs fornecidos são inválidos"),
          "NotFound",
          new NoSuchElementException());

  @Override
  public ProductResponseDTO prepareSuccessView(ProductResponseDTO product) {
    if (product != null) {
      LocalDateTime date = LocalDateTime.parse(product.getCreatedAt());
      product.setCreatedAt(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
      return product;
    }
    return null;
  }

  @Override
  public ProductResponseDTO prepareFailView(String error) {
    RuntimeException runtimeException = ERRORS.get(error);
    if (runtimeException != null) {
      throw runtimeException;
    }

    throw new RuntimeException("Erro desconhecido: " + error);
  }

  @Override
  public SliceResult<ProductResponseDTO> prepareListSuccessView(
      SliceResult<ProductResponseDTO> list) {
    list.items()
        .forEach(
            productResponseDTO -> {
              LocalDateTime date = LocalDateTime.parse(productResponseDTO.getCreatedAt());

              productResponseDTO.setCreatedAt(
                  date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            });
    return list;
  }
}

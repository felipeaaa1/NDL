package com.ndlcommerce.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ndlcommerce.adapters.persistence.product.ProductResponseFormatter;
import com.ndlcommerce.config.exception.BusinessException;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductResponseFormatterTest {

  private ProductResponseFormatter formatter;

  @BeforeEach
  void setup() {
    formatter = new ProductResponseFormatter();
  }

  @Test
  void givenSliceResult_whenPrepareListSuccessView_thenFormatDatesAndPreserveOrder() {
    UUID firstId = UUID.randomUUID();
    UUID secondId = UUID.randomUUID();
    ProductResponseDTO first =
        new ProductResponseDTO(firstId, "Notebook", "Notebook profissional", "2026-05-04T20:30");
    ProductResponseDTO second =
        new ProductResponseDTO(secondId, "Mouse", "Mouse sem fio", "2026-05-03T19:20");
    SliceResult<ProductResponseDTO> source = SliceResult.of(List.of(first, second), 1, 2, true);

    SliceResult<ProductResponseDTO> result = formatter.prepareListSuccessView(source);

    assertThat(result).isSameAs(source);
    assertThat(result.items())
        .extracting(ProductResponseDTO::getUuid)
        .containsExactly(firstId, secondId);
    assertThat(result.items())
        .extracting(ProductResponseDTO::getCreatedAt)
        .containsExactly("04/05/2026 20:30", "03/05/2026 19:20");
  }

  @Test
  void givenInvalidSkuError_whenPrepareFailView_thenThrowBusinessException() {
    assertThatThrownBy(() -> formatter.prepareFailView("SkuIsNotValid"))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Um ou mais SKUs fornecidos são inválidos");
  }
}

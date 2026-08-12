package com.ndlcommerce.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ndlcommerce.adapters.persistence.brand.BrandResponseFormatter;
import com.ndlcommerce.config.exception.EntityAlreadyExistsException;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BrandResponseFormatterTest {

  private BrandResponseFormatter brandResponseFormatter;

  @BeforeEach
  void setup() {
    this.brandResponseFormatter = new BrandResponseFormatter();
  }

  @Test
  void givenIsoDate_whenPrepareSuccessView_thenFormatDate() {
    BrandResponseDTO response =
        new BrandResponseDTO(UUID.randomUUID(), "Nike", "2020-12-20T03:00:00.000");

    BrandResponseDTO formatted = brandResponseFormatter.prepareSuccessView(response);

    assertThat(formatted.getCreatedAt()).isEqualTo("20/12/2020 03:00");
  }

  @Test
  void givenPaginatedResult_whenPrepareListSuccessView_thenFormatAllDates() {
    BrandResponseDTO response =
        new BrandResponseDTO(UUID.randomUUID(), "Adidas", "2020-12-20T03:00:00.000");

    PaginatedResult<BrandResponseDTO> page =
        new PaginatedResult<>(List.of(response), 0, 10, 1, 1, true, true);

    PaginatedResult<BrandResponseDTO> formatted =
        brandResponseFormatter.prepareListSuccessView(page);

    assertThat(formatted.getContent()).hasSize(1);
    assertThat(formatted.getContent().get(0).getCreatedAt()).isEqualTo("20/12/2020 03:00");
  }

  @Test
  void givenExistByNameError_whenPrepareFailView_thenThrowEntityAlreadyExistsException() {
    assertThatThrownBy(() -> brandResponseFormatter.prepareFailView("ExistByName"))
        .isInstanceOf(EntityAlreadyExistsException.class);
  }
}

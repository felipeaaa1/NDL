package com.ndlcommerce.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ndlcommerce.useCase.interfaces.brand.BrandInputBoundary;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandFilterDTO;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class BrandControllerTest {

  private BrandInputBoundary brandInputBoundary;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    brandInputBoundary = mock(BrandInputBoundary.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new BrandController(brandInputBoundary)).build();
  }

  @Test
  void givenNameAndPagination_whenListBrands_thenPassFilterAndReturnPage() throws Exception {
    BrandResponseDTO brand = new BrandResponseDTO(UUID.randomUUID(), "Nike", "12/08/2026 10:00");
    PaginatedResult<BrandResponseDTO> response =
        new PaginatedResult<>(List.of(brand), 1, 20, 21, 2, false, true);
    when(brandInputBoundary.list(any(BrandFilterDTO.class), eq(1), eq(20))).thenReturn(response);
    ArgumentCaptor<BrandFilterDTO> filterCaptor = ArgumentCaptor.forClass(BrandFilterDTO.class);

    mockMvc
        .perform(get("/brand").param("name", "Nik").param("page", "1").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Nike"))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(21));

    verify(brandInputBoundary).list(filterCaptor.capture(), eq(1), eq(20));
    assertThat(filterCaptor.getValue().name()).isEqualTo("Nik");
  }

  @Test
  void givenNoParameters_whenListBrands_thenUseDefaultsAndEmptyFilter() throws Exception {
    mockMvc.perform(get("/brand")).andExpect(status().isOk());

    ArgumentCaptor<BrandFilterDTO> filterCaptor = ArgumentCaptor.forClass(BrandFilterDTO.class);
    verify(brandInputBoundary).list(filterCaptor.capture(), eq(0), eq(15));
    assertThat(filterCaptor.getValue().name()).isNull();
  }

  @Test
  void givenSizeAboveMaximum_whenListBrands_thenReturnBadRequest() throws Exception {
    mockMvc
        .perform(get("/brand").param("size", "51"))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException())
                    .isInstanceOf(HandlerMethodValidationException.class));
  }
}

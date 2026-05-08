package com.ndlcommerce.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ndlcommerce.config.PaginatedResult;
import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductControllerTest {

  @Test
  void givenNoBody_whenSearchProducts_thenUseEmptyFilterAndReturnOk() throws Exception {
    ProductInputBoundary productInputBoundary = mock(ProductInputBoundary.class);
    ProductController controller = new ProductController(productInputBoundary);
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    PaginatedResult<ProductResponseDTO> response =
        new PaginatedResult<>(List.of(), 0, 20, 0, 0, true, true);
    ArgumentCaptor<ProductFilterDTO> filterCaptor =
        ArgumentCaptor.forClass(ProductFilterDTO.class);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(post("/products/search").param("page", "0").param("size", "20"))
        .andExpect(status().isOk());

    verify(productInputBoundary).list(filterCaptor.capture(), eq(0), eq(20));
    ProductFilterDTO filter = filterCaptor.getValue();
    assertThat(filter).isNotNull();
    assertThat(filter.getName()).isNull();
    assertThat(filter.getDescription()).isNull();
    assertThat(filter.getBrand()).isNull();
    assertThat(filter.getCategory()).isNull();
  }

  @Test
  void givenBody_whenSearchProducts_thenPassFilterToUseCase() throws Exception {
    ProductInputBoundary productInputBoundary = mock(ProductInputBoundary.class);
    ProductController controller = new ProductController(productInputBoundary);
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    PaginatedResult<ProductResponseDTO> response =
        new PaginatedResult<>(List.of(), 0, 20, 0, 0, true, true);
    UUID brandId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    ArgumentCaptor<ProductFilterDTO> filterCaptor =
        ArgumentCaptor.forClass(ProductFilterDTO.class);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/products/search")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "shoe",
                      "brand": "%s",
                      "category": "%s"
                    }
                    """
                        .formatted(brandId, categoryId)))
        .andExpect(status().isOk());

    verify(productInputBoundary).list(filterCaptor.capture(), eq(0), eq(20));
    ProductFilterDTO filter = filterCaptor.getValue();
    assertThat(filter.getName()).isEqualTo("shoe");
    assertThat(filter.getBrand()).isEqualTo(brandId);
    assertThat(filter.getCategory()).isEqualTo(categoryId);
  }
}

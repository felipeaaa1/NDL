package com.ndlcommerce.adapters.web;

import com.ndlcommerce.config.PaginatedResult;
import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

  @Test
  void givenNoFilterParameters_whenSearchProducts_thenUseEmptyFilterAndReturnOk() throws Exception {
    ProductInputBoundary productInputBoundary = mock(ProductInputBoundary.class);
    ProductController controller = new ProductController(productInputBoundary);
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    PaginatedResult<ProductResponseDTO> response =
        new PaginatedResult<>(List.of(), 0, 20, 0, 0, true, true);
    ArgumentCaptor<ProductFilterDTO> filterCaptor = ArgumentCaptor.forClass(ProductFilterDTO.class);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(get("/products/").param("page", "0").param("size", "20"))
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
    ArgumentCaptor<ProductFilterDTO> filterCaptor = ArgumentCaptor.forClass(ProductFilterDTO.class);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/products")
                .param("page", "0")
                .param("size", "20")
                .param("name", "shoe")
                .param("brand", brandId.toString())
                .param("category", categoryId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                )
        .andExpect(status().isOk());

    verify(productInputBoundary).list(filterCaptor.capture(), eq(0), eq(20));
    ProductFilterDTO filter = filterCaptor.getValue();
    assertThat(filter.getName()).isEqualTo("shoe");
    assertThat(filter.getBrand()).isEqualTo(brandId);
    assertThat(filter.getCategory()).isEqualTo(categoryId);
  }
}

package com.ndlcommerce.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

import com.ndlcommerce.config.PaginatedResult;
import com.ndlcommerce.config.exception.GlobalExceptionHandler;
import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class ProductControllerTest {

  ProductInputBoundary productInputBoundary;
  ProductController controller;
  MockMvc mockMvc;
  GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setup() {
    this.productInputBoundary = mock(ProductInputBoundary.class);
    this.controller = new ProductController(productInputBoundary);
    this.globalExceptionHandler = mock(GlobalExceptionHandler.class);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .apply(sharedHttpSession())
            //            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void givenNoFilterParameters_whenSearchProducts_thenUseEmptyFilterAndReturnOk() throws Exception {

    PaginatedResult<ProductResponseDTO> response =
        new PaginatedResult<>(List.of(), 0, 20, 0, 0, true, true);
    ArgumentCaptor<ProductFilterDTO> filterCaptor = ArgumentCaptor.forClass(ProductFilterDTO.class);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(get("/product/").param("page", "0").param("size", "20"))
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

    PaginatedResult<ProductResponseDTO> response =
        new PaginatedResult<>(List.of(), 0, 20, 0, 0, true, true);
    UUID brandId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    ArgumentCaptor<ProductFilterDTO> filterCaptor = ArgumentCaptor.forClass(ProductFilterDTO.class);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/product")
                .param("page", "0")
                .param("size", "20")
                .param("name", "shoe")
                .param("brand", brandId.toString())
                .param("category", categoryId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(productInputBoundary).list(filterCaptor.capture(), eq(0), eq(20));
    ProductFilterDTO filter = filterCaptor.getValue();
    assertThat(filter.getName()).isEqualTo("shoe");
    assertThat(filter.getBrand()).isEqualTo(brandId);
    assertThat(filter.getCategory()).isEqualTo(categoryId);
  }

  @Test
  void whenSizeParamIsMissing_ShouldUseDefaultValue15() throws Exception {

    mockMvc.perform(get("/product"));

    verify(productInputBoundary).list(any(ProductFilterDTO.class), eq(0), eq(15));
  }

  @Test
  void whenParamsIsGiven_ShouldReturnIt() throws Exception {

    mockMvc.perform(get("/product").param("page", "1").param("size", "20"));

    verify(productInputBoundary).list(any(ProductFilterDTO.class), eq(1), eq(20));
  }

  @Test
  void whenInvalidParamsIsGiven_ShouldThrowException() throws Exception {

    mockMvc
        .perform(get("/product").param("size", "207777777777777777777777777777"))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException())
                    .isInstanceOf(MethodArgumentTypeMismatchException.class));

    mockMvc
        .perform(get("/product").param("size", "51"))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertThat(result.getResolvedException())
                    .isInstanceOf(HandlerMethodValidationException.class));
  }
}

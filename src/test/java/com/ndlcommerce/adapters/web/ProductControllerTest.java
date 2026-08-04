package com.ndlcommerce.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

import com.ndlcommerce.config.exception.GlobalExceptionHandler;
import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.model.SliceResult;
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

    SliceResult<ProductResponseDTO> response = new SliceResult<>(List.of(), 0, 15, false, null);
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

    SliceResult<ProductResponseDTO> response = new SliceResult<>(List.of(), 0, 15, false, null);
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

  @Test
  void givenProductsWithNextPage_whenList_thenSerializeInfiniteScrollContract() throws Exception {
    ProductResponseDTO product =
        new ProductResponseDTO(
            UUID.randomUUID(), "Notebook", "Notebook profissional", "04/05/2026 20:30");
    SliceResult<ProductResponseDTO> response = SliceResult.of(List.of(product), 0, 15, true);
    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(15)))
        .thenReturn(response);

    mockMvc
        .perform(get("/product").param("page", "0").param("size", "15"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].name").value("Notebook"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(15))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.nextPage").value(1));
  }

  @Test
  void givenLastPage_whenList_thenSerializeNullNextPage() throws Exception {
    SliceResult<ProductResponseDTO> response = SliceResult.of(List.of(), 9, 15, false);
    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(9), eq(15)))
        .thenReturn(response);

    mockMvc
        .perform(get("/product").param("page", "9").param("size", "15"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.page").value(9))
        .andExpect(jsonPath("$.size").value(15))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextPage").value(nullValue()));
  }
}

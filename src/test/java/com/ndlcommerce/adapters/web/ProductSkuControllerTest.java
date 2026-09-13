package com.ndlcommerce.adapters.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ndlcommerce.adapters.web.cursor.CursorUtil;
import com.ndlcommerce.config.exception.GlobalExceptionHandler;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuInputBoundary;
import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

class ProductSkuControllerTest {

  private ProductSkuInputBoundary inputBoundary;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    inputBoundary = mock(ProductSkuInputBoundary.class);
    var controller = new ProductSkuController(inputBoundary);
    var exceptionHandler =
        new GlobalExceptionHandler(mock(View.class), new DefaultErrorAttributes());
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler).build();
  }

  @Test
  void givenNoCursor_whenList_thenPassNullCursorAndDefaultSize() throws Exception {
    when(inputBoundary.list(any(ProductFilterDTO.class), eq(null), eq(15)))
        .thenReturn(Map.of("data", java.util.List.of()));

    mockMvc.perform(get("/product/sku")).andExpect(status().isOk());
  }

  @Test
  void givenInvalidCursor_whenList_thenReturnBadRequest() throws Exception {
    mockMvc
        .perform(get("/product/sku").param("cursor", "invalid"))
        .andExpect(status().isBadRequest())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
                .value("Invalid cursor"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.trace")
                .doesNotExist());
  }

  @Test
  void givenNextCursorFromFirstPage_whenList_thenDecodeSameCursorForSecondPage() throws Exception {
    var orderCursor = new OrderCursor(LocalDateTime.of(2026, 9, 10, 10, 0), UUID.randomUUID());
    var nextCursor = CursorUtil.encode(orderCursor);
    when(inputBoundary.list(any(ProductFilterDTO.class), eq(orderCursor), eq(15)))
        .thenReturn(Map.of("data", java.util.List.of()));

    mockMvc
        .perform(get("/product/sku").param("cursor", nextCursor).param("size", "15"))
        .andExpect(status().isOk());

    org.mockito.Mockito.verify(inputBoundary)
        .list(any(ProductFilterDTO.class), eq(orderCursor), eq(15));
  }

  @Test
  void givenSizeOutsideApiLimit_whenList_thenReturnBadRequest() throws Exception {
    mockMvc.perform(get("/product/sku").param("size", "0")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/product/sku").param("size", "51")).andExpect(status().isBadRequest());
  }
}

package com.ndlcommerce.adapters.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ndlcommerce.adapters.persistence.user.JpaUserRepository;
import com.ndlcommerce.config.PaginatedResult;
import com.ndlcommerce.config.SecurityConfiguration;
import com.ndlcommerce.config.TokenService;
import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(SecurityConfiguration.class)
class ProductControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ProductInputBoundary productInputBoundary;

  @MockBean private TokenService tokenService;
  @MockBean private JpaUserRepository userRepository;

  @Test
  @DisplayName("Should allow GET /product without authentication")
  void shouldAllowGetProductsWithoutAuthentication() throws Exception {

    PaginatedResult<ProductResponseDTO> response =
        new PaginatedResult<>(List.of(), 0, 20, 0, 0, true, true);

    when(productInputBoundary.list(any(ProductFilterDTO.class), eq(0), eq(20)))
        .thenReturn(response);

    mockMvc
        .perform(get("/product").param("page", "0").param("size", "20"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should block POST /product without authentication")
  void shouldBlockPostWithoutAuthentication() throws Exception {

    mockMvc
        .perform(
            post("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Notebook"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should block PUT /product/{id} without authentication")
  void shouldBlockPutWithoutAuthentication() throws Exception {

    mockMvc
        .perform(
            put("/product/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Updated product"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should block DELETE /product/{id} without authentication")
  void shouldBlockDeleteWithoutAuthentication() throws Exception {

    mockMvc.perform(delete("/product/1")).andExpect(status().isForbidden());
  }
}

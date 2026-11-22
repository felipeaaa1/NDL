package com.ndlcommerce.adapters.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ndlcommerce.adapters.persistence.brand.BrandResponseFormatter;
import com.ndlcommerce.config.exception.GlobalExceptionHandler;
import com.ndlcommerce.domain.factory.implementation.CommonBrandFactoryImp;
import com.ndlcommerce.useCase.BrandRegisterInteractor;
import com.ndlcommerce.useCase.interfaces.brand.BrandRegisterDsGateway;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

/**
 * Characterization of the Brand HTTP contract: real controller, interactor, factory, presenter and
 * exception handler; only the persistence gateway is mocked.
 */
class BrandControllerHttpContractTest {

  private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 8, 12, 10, 0);

  private BrandRegisterDsGateway gateway;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    gateway = mock(BrandRegisterDsGateway.class);
    var interactor =
        new BrandRegisterInteractor(
            gateway, new BrandResponseFormatter(), new CommonBrandFactoryImp());
    mockMvc =
        MockMvcBuilders.standaloneSetup(new BrandController(interactor))
            .setControllerAdvice(
                new GlobalExceptionHandler(mock(View.class), new DefaultErrorAttributes()))
            .build();
  }

  @Test
  void givenValidBody_whenCreate_thenReturn200WithFormattedDate() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.save(any(BrandDbRequestDTO.class)))
        .thenReturn(new BrandGatewayResponseDTO(id, "Nike", CREATED_AT));

    // @ResponseStatus(CREATED) is overridden by ResponseEntity.ok(): the real status is 200.
    mockMvc
        .perform(
            post("/brand").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Nike\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Nike"))
        .andExpect(jsonPath("$.createdAt").value("12/08/2026 10:00"));
  }

  @Test
  void givenDuplicatedName_whenCreate_thenReturn409() throws Exception {
    when(gateway.existsByName("Nike")).thenReturn(true);

    mockMvc
        .perform(
            post("/brand").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Nike\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value("Marca já cadastrada"));
    verify(gateway, never()).save(any());
  }

  @Test
  void givenBlankName_whenCreate_thenReturn422WithFieldErrors() throws Exception {
    mockMvc
        .perform(post("/brand").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors[0].field").value("name"));
  }

  @Test
  void givenExistingId_whenGetById_thenReturn200() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Nike", CREATED_AT)));

    mockMvc
        .perform(get("/brand/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Nike"))
        .andExpect(jsonPath("$.createdAt").value("12/08/2026 10:00"));
  }

  @Test
  void givenUnknownId_whenGetById_thenReturn404() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/brand/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Elemento com id fornecido não encontrado"));
  }

  @Test
  void givenMalformedId_whenGetById_thenReturn400() throws Exception {
    mockMvc
        .perform(get("/brand/not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void givenNameUsedByAnotherBrand_whenUpdate_thenReturn409() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Old", CREATED_AT)));
    when(gateway.existsByNameAndNotId("Adidas", id)).thenReturn(true);

    mockMvc
        .perform(
            put("/brand/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Adidas\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void givenTooShortNameOnUpdate_whenUpdate_thenReturn406() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Old", CREATED_AT)));

    // PUT has no @Valid: the domain rule rejects it with a BusinessException (406).
    mockMvc
        .perform(
            put("/brand/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"A\"}"))
        .andExpect(status().isNotAcceptable())
        .andExpect(jsonPath("$.status").value(406));
  }

  @Test
  void givenUnknownId_whenUpdate_thenReturn404() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            put("/brand/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Adidas\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void givenExistingId_whenDelete_thenReturn200() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Nike", CREATED_AT)));

    mockMvc.perform(delete("/brand/" + id)).andExpect(status().isOk());
    verify(gateway).delete(id);
  }

  @Test
  void givenUnknownId_whenDelete_thenReturn404() throws Exception {
    UUID id = UUID.randomUUID();
    when(gateway.getById(id)).thenReturn(Optional.empty());

    mockMvc.perform(delete("/brand/" + id)).andExpect(status().isNotFound());
    verify(gateway, never()).delete(any());
  }

  @Test
  void givenLegacySearchWithoutBody_whenSearch_thenReturn200() throws Exception {
    mockMvc.perform(post("/brand/search")).andExpect(status().isOk());
  }
}

package com.ndlcommerce.useCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ndlcommerce.entity.factory.interfaces.BrandFactory;
import com.ndlcommerce.entity.model.implementation.CommonBrand;
import com.ndlcommerce.entity.model.interfaces.Brand;
import com.ndlcommerce.useCase.interfaces.brand.BrandPresenter;
import com.ndlcommerce.useCase.interfaces.brand.BrandRegisterDsGateway;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandFilterDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import com.ndlcommerce.useCase.request.brand.BrandRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BrandRegisterInteractorTests {

  private BrandRegisterDsGateway brandDsGateway;
  private BrandPresenter brandPresenter;
  private BrandFactory brandFactory;
  private BrandRegisterInteractor interactor;

  @BeforeEach
  void setUp() {
    brandDsGateway = mock(BrandRegisterDsGateway.class);
    brandPresenter = mock(BrandPresenter.class);
    brandFactory = mock(BrandFactory.class);

    interactor = new BrandRegisterInteractor(brandDsGateway, brandPresenter, brandFactory);
  }

  @Test
  void givenValidName_whenCreate_thenSaveAndPrepareSuccessView() {
    Brand brand = new CommonBrand("Nike");
    BrandRequestDTO requestDTO = new BrandRequestDTO("Nike");

    BrandGatewayResponseDTO saved =
        new BrandGatewayResponseDTO(UUID.randomUUID(), "Nike", LocalDateTime.now());

    when(brandFactory.create(anyString())).thenReturn(brand);
    when(brandDsGateway.existsByName("Nike")).thenReturn(false);
    when(brandDsGateway.save(any(BrandDbRequestDTO.class))).thenReturn(saved);

    interactor.create(requestDTO);

    verify(brandDsGateway, times(1)).save(any(BrandDbRequestDTO.class));
    verify(brandPresenter, times(1)).prepareSuccessView(any(BrandResponseDTO.class));
    verify(brandPresenter, never()).prepareFailView(anyString());
  }

  @Test
  void givenInvalidName_whenCreate_thenPrepareFailViewAndDoNotSave() {
    Brand brand = new CommonBrand("A");
    BrandRequestDTO requestDTO = new BrandRequestDTO("A");

    when(brandFactory.create(anyString())).thenReturn(brand);

    interactor.create(requestDTO);

    verify(brandPresenter, times(1)).prepareFailView("NameNotValid");
    verify(brandDsGateway, never()).save(any(BrandDbRequestDTO.class));
    verify(brandPresenter, never()).prepareSuccessView(any(BrandResponseDTO.class));
  }

  @Test
  void givenNotFoundBrandId_whenUpdate_thenPrepareFailView() {
    UUID id = UUID.randomUUID();
    BrandRequestDTO requestDTO = new BrandRequestDTO("Adidas");

    when(brandDsGateway.getById(id)).thenReturn(Optional.empty());

    interactor.updateBrand(id, requestDTO);

    verify(brandPresenter, times(1)).prepareFailView("NotFound");
    verify(brandDsGateway, never()).update(any(UUID.class), any(BrandDbRequestDTO.class));
  }

  @Test
  void givenFiltersAndPagination_whenList_thenPrepareListSuccessView() {
    BrandGatewayResponseDTO brand =
        new BrandGatewayResponseDTO(UUID.randomUUID(), "Puma", LocalDateTime.now());

    PaginatedResult<BrandGatewayResponseDTO> page =
        new PaginatedResult<>(List.of(brand), 0, 10, 1, 1, true, true);

    when(brandDsGateway.list(any(BrandDbRequestDTO.class), anyInt(), anyInt())).thenReturn(page);

    interactor.list(new BrandFilterDTO("Pu"), 0, 10);

    verify(brandDsGateway, times(1)).list(any(BrandDbRequestDTO.class), eq(0), eq(10));
    verify(brandPresenter, times(1)).prepareListSuccessView(any());
  }

  @Test
  void givenExistingBrandId_whenDelete_thenDeleteAndPrepareSuccessView() {
    UUID id = UUID.randomUUID();

    BrandGatewayResponseDTO brand =
        new BrandGatewayResponseDTO(id, "Olympikus", LocalDateTime.now());

    when(brandDsGateway.getById(id)).thenReturn(Optional.of(brand));

    interactor.deleteBrand(id);

    verify(brandDsGateway, times(1)).delete(id);
    verify(brandPresenter, times(1)).prepareSuccessView(null);
    verify(brandPresenter, never()).prepareFailView(anyString());
  }
}

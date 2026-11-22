package com.ndlcommerce.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ndlcommerce.domain.factory.interfaces.BrandFactory;
import com.ndlcommerce.domain.model.implementation.CommonBrand;
import com.ndlcommerce.domain.model.interfaces.Brand;
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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

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
  void givenDuplicatedName_whenCreate_thenPrepareFailViewAndDoNotSave() {
    when(brandFactory.create("Nike")).thenReturn(new CommonBrand("Nike"));
    when(brandDsGateway.existsByName("Nike")).thenReturn(true);

    interactor.create(new BrandRequestDTO("Nike"));

    verify(brandPresenter).prepareFailView("ExistByName");
    verify(brandDsGateway, never()).save(any(BrandDbRequestDTO.class));
  }

  @Test
  void givenSavedBrand_whenCreate_thenPresenterReceivesIsoCreatedAt() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    UUID id = UUID.randomUUID();
    when(brandFactory.create("Nike")).thenReturn(new CommonBrand("Nike"));
    when(brandDsGateway.save(any(BrandDbRequestDTO.class)))
        .thenReturn(new BrandGatewayResponseDTO(id, "Nike", createdAt));

    interactor.create(new BrandRequestDTO("Nike"));

    verify(brandPresenter).prepareSuccessView(argThat(matches(id, "Nike", createdAt.toString())));
  }

  private static ArgumentMatcher<BrandResponseDTO> matches(UUID id, String name, String date) {
    return dto ->
        dto != null
            && id.equals(dto.getId())
            && name.equals(dto.getName())
            && date.equals(dto.getCreatedAt());
  }

  @Test
  void givenExistingId_whenGetById_thenPrepareSuccessView() {
    UUID id = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    when(brandDsGateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Nike", createdAt)));

    interactor.getById(id);

    verify(brandPresenter).prepareSuccessView(argThat(matches(id, "Nike", createdAt.toString())));
  }

  @Test
  void givenUnknownId_whenGetById_thenPrepareFailViewNotFound() {
    UUID id = UUID.randomUUID();
    when(brandDsGateway.getById(id)).thenReturn(Optional.empty());

    interactor.getById(id);

    verify(brandPresenter).prepareFailView("NotFound");
    verify(brandPresenter, never()).prepareSuccessView(any());
  }

  @Test
  void givenNullFilter_whenList_thenQueryGatewayWithNullName() {
    when(brandDsGateway.list(any(BrandDbRequestDTO.class), anyInt(), anyInt())).thenReturn(null);

    interactor.list(null, 2, 5);

    verify(brandDsGateway).list(new BrandDbRequestDTO(null), 2, 5);
    verify(brandPresenter).prepareListSuccessView(null);
  }

  @Test
  void givenGatewayPage_whenList_thenMapItemsToResponseDtos() {
    UUID id = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    when(brandDsGateway.list(any(BrandDbRequestDTO.class), anyInt(), anyInt()))
        .thenReturn(
            new PaginatedResult<>(
                List.of(new BrandGatewayResponseDTO(id, "Puma", createdAt)),
                0,
                10,
                1,
                1,
                true,
                true));

    interactor.list(new BrandFilterDTO("Pu"), 0, 10);

    ArgumentCaptor<PaginatedResult<BrandResponseDTO>> captor =
        ArgumentCaptor.forClass(PaginatedResult.class);
    verify(brandPresenter).prepareListSuccessView(captor.capture());
    BrandResponseDTO item = captor.getValue().getContent().get(0);
    assertThat(item.getId()).isEqualTo(id);
    assertThat(item.getName()).isEqualTo("Puma");
    assertThat(item.getCreatedAt()).isEqualTo(createdAt.toString());
  }

  @Test
  void givenNameUsedByAnotherBrand_whenUpdate_thenPrepareFailViewAndDoNotUpdate() {
    UUID id = UUID.randomUUID();
    when(brandDsGateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Old", LocalDateTime.now())));
    when(brandDsGateway.existsByNameAndNotId("Adidas", id)).thenReturn(true);

    interactor.updateBrand(id, new BrandRequestDTO("Adidas"));

    verify(brandPresenter).prepareFailView("ExistByName");
    verify(brandDsGateway, never()).update(any(UUID.class), any(BrandDbRequestDTO.class));
  }

  @Test
  void givenSameNameOfItself_whenUpdate_thenUpdateIsAllowed() {
    UUID id = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    BrandGatewayResponseDTO current = new BrandGatewayResponseDTO(id, "Nike", createdAt);
    when(brandDsGateway.getById(id)).thenReturn(Optional.of(current));
    when(brandDsGateway.existsByNameAndNotId("Nike", id)).thenReturn(false);
    when(brandFactory.create("Nike")).thenReturn(new CommonBrand("Nike"));
    when(brandDsGateway.update(eq(id), any(BrandDbRequestDTO.class))).thenReturn(current);

    interactor.updateBrand(id, new BrandRequestDTO("Nike"));

    verify(brandDsGateway).update(id, new BrandDbRequestDTO("Nike"));
    verify(brandPresenter).prepareSuccessView(argThat(matches(id, "Nike", createdAt.toString())));
  }

  @Test
  void givenInvalidName_whenUpdate_thenPrepareFailViewNameNotValid() {
    UUID id = UUID.randomUUID();
    when(brandDsGateway.getById(id))
        .thenReturn(Optional.of(new BrandGatewayResponseDTO(id, "Old", LocalDateTime.now())));
    when(brandFactory.create("A")).thenReturn(new CommonBrand("A"));

    interactor.updateBrand(id, new BrandRequestDTO("A"));

    verify(brandPresenter).prepareFailView("NameNotValid");
    verify(brandDsGateway, never()).update(any(UUID.class), any(BrandDbRequestDTO.class));
  }

  @Test
  void givenUnknownId_whenDelete_thenPrepareFailViewAndDoNotDelete() {
    UUID id = UUID.randomUUID();
    when(brandDsGateway.getById(id)).thenReturn(Optional.empty());

    interactor.deleteBrand(id);

    verify(brandPresenter).prepareFailView("NotFound");
    verify(brandDsGateway, never()).delete(any(UUID.class));
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

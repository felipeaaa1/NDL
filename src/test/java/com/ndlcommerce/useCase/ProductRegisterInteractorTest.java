package com.ndlcommerce.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ndlcommerce.adapters.persistence.product.ProductDataMapper;
import com.ndlcommerce.entity.factory.implementation.CommonProductFactoryImp;
import com.ndlcommerce.entity.factory.interfaces.ProductFactory;
import com.ndlcommerce.entity.factory.interfaces.ProductSkuFactory;
import com.ndlcommerce.useCase.interfaces.brand.BrandRegisterDsGateway;
import com.ndlcommerce.useCase.interfaces.category.CategoryRegisterDsGateway;
import com.ndlcommerce.useCase.interfaces.product.ProductPresenter;
import com.ndlcommerce.useCase.interfaces.product.ProductRegisterDsGateway;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductDbRequestDTO;
import com.ndlcommerce.useCase.request.product.ProductRequestDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import com.ndlcommerce.useCase.request.product.ProductUpdateRequestDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProductRegisterInteractorTest {

  private ProductRegisterDsGateway productDsGateway;
  private ProductPresenter productPresenter;
  private ProductFactory productFactory;
  private BrandRegisterDsGateway brandRegisterDsGateway;
  private CategoryRegisterDsGateway categoryRegisterDsGateway;
  private ProductSkuFactory productSkuFactory;
  private ProductRegisterInteractor interactor;

  @BeforeEach
  void setup() {
    this.productDsGateway = mock(ProductRegisterDsGateway.class);
    this.productPresenter = mock(ProductPresenter.class);
    this.productFactory = new CommonProductFactoryImp();
    this.brandRegisterDsGateway = mock(BrandRegisterDsGateway.class);
    this.productSkuFactory = mock(ProductSkuFactory.class);
    this.categoryRegisterDsGateway = mock(CategoryRegisterDsGateway.class);

    this.interactor =
        new ProductRegisterInteractor(
            productDsGateway,
            productPresenter,
            productFactory,
            productSkuFactory,
            brandRegisterDsGateway,
            categoryRegisterDsGateway);
  }

  @Test
  void givenUnknownProductId_whenGetById_thenPrepareNotFoundFailView() {
    UUID productId = UUID.randomUUID();
    ProductResponseDTO failResponse = new ProductResponseDTO();

    when(productDsGateway.findById(productId)).thenReturn(Optional.empty());
    when(productPresenter.prepareFailView("NotFound")).thenReturn(failResponse);

    ProductResponseDTO response = interactor.getById(productId);

    assertThat(response).isSameAs(failResponse);
    verify(productPresenter).prepareFailView("NotFound");
  }

  @Test
  void givenExistingProductId_whenGetById_thenReturnMappedProductResponse() {
    UUID productId = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 5, 4, 20, 30, 0);
    ProductDataMapper product =
        productDataMapper(productId, "Notebook Pro", "16GB RAM SSD", createdAt);

    when(productDsGateway.findById(productId)).thenReturn(Optional.of(product));
    when(productPresenter.prepareSuccessView(any(ProductResponseDTO.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ProductResponseDTO response = interactor.getById(productId);

    assertThat(response.getUuid()).isEqualTo(productId);
    assertThat(response.getName()).isEqualTo("Notebook Pro");
    assertThat(response.getDescription()).isEqualTo("16GB RAM SSD");
    assertThat(response.getCreatedAt()).isEqualTo(createdAt.toString());
  }

  @Test
  void givenUnknownBrandOnUpdate_whenUpdateProduct_thenPrepareBrandNotFoundFailView() {
    UUID productId = UUID.randomUUID();
    UUID brandId = UUID.randomUUID();

    ProductDataMapper currentProduct =
        productDataMapper(productId, "Mouse", "Descrição atual", LocalDateTime.now());

    ProductUpdateRequestDTO requestDTO = mock(ProductUpdateRequestDTO.class);
    ProductResponseDTO failResponse = new ProductResponseDTO();

    when(productDsGateway.findById(productId)).thenReturn(Optional.of(currentProduct));
    when(requestDTO.getBrand()).thenReturn(brandId);
    when(requestDTO.getCategory()).thenReturn(null);
    when(requestDTO.getName()).thenReturn("Mouse Pro");
    when(requestDTO.getDescription()).thenReturn("Descrição atualizada");
    when(brandRegisterDsGateway.getById(brandId)).thenReturn(Optional.empty());
    when(productPresenter.prepareFailView("BrandNotFound")).thenReturn(failResponse);

    ProductResponseDTO response = interactor.updateProduct(productId, requestDTO);

    assertThat(response).isSameAs(failResponse);
    verify(productPresenter).prepareFailView("BrandNotFound");
    verify(productDsGateway, never())
        .update(any(ProductDataMapper.class), any(ProductUpdateRequestDTO.class));
  }

  @Test
  void givenValidUpdateRequest_whenUpdateProduct_thenPersistAndReturnUpdatedProduct() {
    UUID productId = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 5, 4, 21, 0, 0);
    ProductDataMapper currentProduct =
        productDataMapper(productId, "Notebook", "Descrição inicial", createdAt);
    ProductDataMapper updatedProduct =
        productDataMapper(productId, "Notebook Gamer", "Descrição atualizada", createdAt);
    ProductUpdateRequestDTO requestDTO = mock(ProductUpdateRequestDTO.class);

    when(productDsGateway.findById(productId)).thenReturn(Optional.of(currentProduct));
    when(requestDTO.getBrand()).thenReturn(null);
    when(requestDTO.getCategory()).thenReturn(null);
    when(requestDTO.getName()).thenReturn("Notebook Gamer");
    when(requestDTO.getDescription()).thenReturn("Descrição atualizada");
    when(productDsGateway.existsByNameAndIdNot("Notebook Gamer", productId)).thenReturn(false);
    when(productDsGateway.update(currentProduct, requestDTO)).thenReturn(updatedProduct);
    when(productPresenter.prepareSuccessView(any(ProductResponseDTO.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ProductResponseDTO response = interactor.updateProduct(productId, requestDTO);

    assertThat(response.getUuid()).isEqualTo(productId);
    assertThat(response.getName()).isEqualTo("Notebook Gamer");
    assertThat(response.getDescription()).isEqualTo("Descrição atualizada");
    assertThat(response.getCreatedAt()).isEqualTo(createdAt.toString());
    verify(productDsGateway).update(currentProduct, requestDTO);
  }

  @Test
  void givenUnknownProductId_whenDeleteProduct_thenPrepareNotFoundFailView() {
    UUID productId = UUID.randomUUID();
    ProductResponseDTO failResponse = new ProductResponseDTO();

    when(productDsGateway.findById(productId)).thenReturn(Optional.empty());
    when(productPresenter.prepareFailView("NotFound")).thenReturn(failResponse);

    ProductResponseDTO response = interactor.deleteProduct(productId);

    assertThat(response).isSameAs(failResponse);
    verify(productDsGateway, never()).delete(any(UUID.class));
  }

  @Test
  void givenExistingProductId_whenDeleteProduct_thenDeleteAndReturnSuccess() {
    UUID productId = UUID.randomUUID();
    ProductDataMapper currentProduct =
        productDataMapper(productId, "Fone", "Descrição", LocalDateTime.now());
    ProductResponseDTO successResponse = new ProductResponseDTO();

    when(productDsGateway.findById(productId)).thenReturn(Optional.of(currentProduct));
    when(productPresenter.prepareSuccessView(null)).thenReturn(successResponse);

    ProductResponseDTO response = interactor.deleteProduct(productId);

    assertThat(response).isSameAs(successResponse);
    verify(productDsGateway).delete(productId);
    verify(productPresenter).prepareSuccessView(null);
  }

  @Test
  void givenNullFilter_whenListProducts_thenUseEmptyFilterAndReturnListSuccess() {
    SliceResult<ProductResponseDTO> emptyProducts =
        new SliceResult<>(List.of(), 0, 15, false, null);

    SliceResult<ProductResponseDTO> emptyResponse =
        new SliceResult<>(List.of(), 0, 15, false, null);

    ArgumentCaptor<ProductDbRequestDTO> filterCaptor =
        ArgumentCaptor.forClass(ProductDbRequestDTO.class);

    when(productDsGateway.list(any(ProductDbRequestDTO.class), eq(0), eq(10)))
        .thenReturn(emptyProducts);
    when(productPresenter.prepareListSuccessView(any())).thenReturn(emptyResponse);

    SliceResult<ProductResponseDTO> response = interactor.list(null, 0, 10);

    assertThat(response).isSameAs(emptyResponse);
    verify(productDsGateway).list(filterCaptor.capture(), eq(0), eq(10));
    ProductDbRequestDTO capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.getName()).isNull();
    assertThat(capturedFilter.getDescription()).isNull();
    assertThat(capturedFilter.getBrand()).isNull();
    assertThat(capturedFilter.getCategory()).isNull();
    assertThat(capturedFilter.isActive()).isTrue();
    verify(productPresenter).prepareListSuccessView(any());
  }

  @Test
  void givenProducts_whenList_thenMapItemsAndCallPresenter() {
    UUID firstId = UUID.randomUUID();
    UUID secondId = UUID.randomUUID();

    ProductResponseDTO first =
        new ProductResponseDTO(
            firstId,
            "Notebook",
            "Notebook profissional",
            LocalDateTime.of(2026, 5, 4, 20, 30).toString());
    ProductResponseDTO second =
        new ProductResponseDTO(
            secondId, "Mouse", "Mouse sem fio", LocalDateTime.of(2026, 5, 3, 19, 20).toString());

    SliceResult<ProductResponseDTO> gatewayResult =
        SliceResult.of(List.of(first, second), 1, 2, true);

    when(productDsGateway.list(any(ProductDbRequestDTO.class), eq(1), eq(2)))
        .thenReturn(gatewayResult);
    when(productPresenter.prepareListSuccessView(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SliceResult<ProductResponseDTO> result = interactor.list(null, 1, 2);

    assertThat(result.items())
        .extracting(ProductResponseDTO::getUuid)
        .containsExactly(firstId, secondId);
    assertThat(result.items())
        .extracting(ProductResponseDTO::getName)
        .containsExactly("Notebook", "Mouse");
    verify(productPresenter).prepareListSuccessView(result);
  }

  @Test
  void givenUnknownBrandOnCreate_thenPrepareBrandNotFoundFailView() {
    UUID brandId = UUID.randomUUID();

    ProductRequestDTO requestDTO = new ProductRequestDTO("teste", "teste", brandId, null);
    ProductResponseDTO failResponseDTO = new ProductResponseDTO();

    when(brandRegisterDsGateway.getById(brandId)).thenReturn(Optional.empty());
    when(productPresenter.prepareFailView("BrandNotFound")).thenReturn((failResponseDTO));

    ProductResponseDTO response = interactor.create(requestDTO);

    assertThat(response).isSameAs(failResponseDTO);
    verify(productPresenter).prepareFailView("BrandNotFound");
    verify(productDsGateway, never()).save(any());
    verifyNoMoreInteractions(productPresenter);
  }

  private ProductDataMapper productDataMapper(
      UUID id, String name, String description, LocalDateTime createdAt) {
    ProductDataMapper product = new ProductDataMapper();
    product.setId(id);
    product.setName(name);
    product.setDescription(description);
    product.setCreatedAt(createdAt);
    return product;
  }
}

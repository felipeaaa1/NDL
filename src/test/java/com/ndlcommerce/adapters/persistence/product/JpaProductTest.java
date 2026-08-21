package com.ndlcommerce.adapters.persistence.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.adapters.persistence.productSku.JpaProductSkuRepository;
import com.ndlcommerce.adapters.persistence.productSku.ProductSkuDataMapper;
import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.config.SecurityFilter;
import com.ndlcommerce.entity.model.implementation.CommonProductSku;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductDbRequestDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

class JpaProductTest {

  private JpaProductRepository repository;
  private JpaProductSkuRepository productSkuRepository;
  private SecurityFilter securityFilter;
  private JpaProduct adapter;

  @BeforeEach
  void setup() {
    repository = mock(JpaProductRepository.class);
    productSkuRepository = mock(JpaProductSkuRepository.class);
    securityFilter = mock(SecurityFilter.class);
    adapter = new JpaProduct(repository, productSkuRepository, securityFilter);
  }

  @Test
  void givenNullTextFilters_whenList_thenUseEmptyFiltersAndDeterministicSort() {
    ProductDbRequestDTO request = new ProductDbRequestDTO(null, null, null, null, null, true);
    Pageable returnedPageable = PageRequest.of(1, 2);
    when(repository.findProducts(eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new SliceImpl<>(List.of(), returnedPageable, false));
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    SliceResult<ProductResponseDTO> result = adapter.list(request, 1, 2);

    verify(repository).findProducts(eq(""), eq(""), eq(null), eq(null), pageableCaptor.capture());
    Pageable pageable = pageableCaptor.getValue();
    assertThat(pageable.getPageNumber()).isEqualTo(1);
    assertThat(pageable.getPageSize()).isEqualTo(2);
    assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
    assertThat(pageable.getSort().getOrderFor("createdAt").getDirection())
        .isEqualTo(Sort.Direction.DESC);
    assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
    assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    assertThat(result.page()).isEqualTo(1);
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextPage()).isNull();
  }

  @Test
  void givenRepositorySliceWithNextPage_whenList_thenExposeNextPage() {
    ProductDbRequestDTO request = new ProductDbRequestDTO("shoe", "indoor", null, null, null, true);
    Pageable returnedPageable = PageRequest.of(0, 1);
    ProductDataMapper product = new ProductDataMapper();
    product.setCreatedAt(LocalDateTime.now());

    when(repository.findProducts(eq("shoe"), eq("indoor"), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new SliceImpl<>(List.of(product), returnedPageable, true));

    SliceResult<ProductResponseDTO> result = adapter.list(request, 0, 1);

    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextPage()).isEqualTo(1);
  }

  @Test
  void givenProductWithSku_whenSave_thenPersistProductAndSkuLinkedByProductId() {
    UUID userId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 9, 30);
    UserDataMapper loggedUser = mock(UserDataMapper.class);
    ProductDataMapper savedProduct = new ProductDataMapper();
    savedProduct.setId(productId);
    savedProduct.setName("Tênis Corre 4");
    savedProduct.setDescription("Tênis para corrida");
    savedProduct.setCreatedAt(createdAt);
    var sku =
        new CommonProductSku(
            "TEN-COR-4-COR-BEG-TAM-41",
            Map.of("cor", "bege", "tamanho", "41"),
            new BigDecimal("499.90"),
            8,
            true);
    ProductDbRequestDTO request =
        new ProductDbRequestDTO(
            "Tênis Corre 4", "Tênis para corrida", null, null, List.of(sku), true);
    when(securityFilter.obterUsuarioLogado()).thenReturn(loggedUser);
    when(loggedUser.getId()).thenReturn(userId);
    when(repository.save(any(ProductDataMapper.class))).thenReturn(savedProduct);

    ProductResponseDTO result = adapter.save(request);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ProductSkuDataMapper>> skuListCaptor = ArgumentCaptor.forClass(List.class);
    verify(productSkuRepository).saveAll(skuListCaptor.capture());
    assertThat(skuListCaptor.getValue()).hasSize(1);
    ProductSkuDataMapper persistedSku = skuListCaptor.getValue().getFirst();
    assertThat(persistedSku.getProductId()).isEqualTo(productId);
    assertThat(persistedSku.getAttributes())
        .containsEntry("cor", "bege")
        .containsEntry("tamanho", "41");
    assertThat(persistedSku.getPrice()).isEqualByComparingTo("499.90");
    assertThat(persistedSku.getStock()).isEqualTo(8);
    assertThat(persistedSku.getSkuCode()).isEqualTo("TEN-COR-4-COR-BEG-TAM-41");
    assertThat(persistedSku.getActive()).isTrue();
    assertThat(persistedSku.getCreatedBy()).isEqualTo(userId);
    assertThat(result.getUuid()).isEqualTo(productId);
  }

  @Test
  void givenSkuCodes_whenCheckingExistence_thenDelegateCollectionToRepositoryOnce() {
    Set<String> skuCodes = Set.of("TEN-COR-PRE", "TEN-COR-AZU");
    when(productSkuRepository.existsBySkuCodeIn(skuCodes)).thenReturn(true);

    boolean exists = adapter.skuCodesExist(skuCodes);

    assertThat(exists).isTrue();
    verify(productSkuRepository).existsBySkuCodeIn(skuCodes);
  }
}

package com.ndlcommerce.adapters.persistence.productSku;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

class JpaProductSkuTest {

  @Test
  void givenPublicSku_whenList_thenReturnLightweightResponseWithDeterministicSort() {
    var repository = mock(JpaProductSkuRepository.class);
    var row = mock(JpaProductSkuRepository.PublicProductSkuView.class);
    var skuId = UUID.randomUUID();
    var productId = UUID.randomUUID();
    when(row.getSkuId()).thenReturn(skuId);
    when(row.getProductId()).thenReturn(productId);
    when(row.getName()).thenReturn("Tênis Corre 4");
    when(row.getPrice()).thenReturn(new BigDecimal("499.90"));
    when(row.getStock()).thenReturn(8);
    when(repository.findPublicSkus(eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenAnswer(
            invocation ->
                new SliceImpl<>(List.of(row), invocation.getArgument(4, Pageable.class), false));

    var result =
        new JpaProductSku(repository)
            .list(new ProductSkuDbRequestDTO(null, null, null, null), 0, 15);

    assertThat(result.items()).hasSize(1);
    var response = result.items().getFirst();
    assertThat(response.skuId()).isEqualTo(skuId);
    assertThat(response.productId()).isEqualTo(productId);
    assertThat(response.name()).isEqualTo("Tênis Corre 4");
    assertThat(response.slug()).isEqualTo("tenis-corre-4");
    assertThat(response.price()).isEqualByComparingTo("499.90");
    assertThat(response.thumbnailUrl()).isNull();
    assertThat(response.available()).isTrue();

    var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findPublicSkus(eq(""), eq(""), eq(null), eq(null), pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getSort().toString())
        .isEqualTo("createdAt: DESC,id: DESC");
  }
}

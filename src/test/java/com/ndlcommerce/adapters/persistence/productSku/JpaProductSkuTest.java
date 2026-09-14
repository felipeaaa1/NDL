package com.ndlcommerce.adapters.persistence.productSku;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

class JpaProductSkuTest {

  private JpaProductSkuRepository repository;
  private JpaProductSku jpaProductSku;

  @BeforeEach
  void setup() {
    this.repository = mock(JpaProductSkuRepository.class);
    this.jpaProductSku = new JpaProductSku(repository);
  }

  /*
   * Verifica se a projection retornada pelo repository é convertida corretamente para o DTO
   * público do SKU. Mudanças futuras não podem perder dados, alterar a geração do slug ou remover
   * a ordenação determinística por data de criação e ID enviada ao repository.
   */
  @Test
  void givenPublicSku_whenList_thenReturnLightweightResponseWithDeterministicSort() {
    var row = mock(JpaProductSkuRepository.PublicProductSkuView.class);
    var skuId = UUID.randomUUID();
    var productId = UUID.randomUUID();
    var createdAt = LocalDateTime.of(2026, 9, 4, 9, 30);

    when(row.getSkuId()).thenReturn(skuId);
    when(row.getProductId()).thenReturn(productId);
    when(row.getName()).thenReturn("Tênis Corre 4");
    when(row.getPrice()).thenReturn(new BigDecimal("499.90"));
    when(row.getCreatedAt()).thenReturn(createdAt);
    when(row.getStock()).thenReturn(8);

    when(repository.findPublicSkus(
            eq(""),
            eq(""),
            anyBoolean(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            any(Pageable.class)))
        .thenAnswer(
            invocation ->
                new SliceImpl<>(List.of(row), invocation.getArgument(7, Pageable.class), false));

    var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    var result =
        new JpaProductSku(repository)
            .list(new ProductSkuDbRequestDTO(null, null, null, null), null, 15);

    verify(repository)
        .findPublicSkus(
            eq(""),
            eq(""),
            eq(true),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            pageableCaptor.capture());
    var response = result.items().getFirst();

    assertThat(result.items()).hasSize(1);
    assertThat(response.skuId()).isEqualTo(skuId);
    assertThat(response.productId()).isEqualTo(productId);
    assertThat(response.name()).isEqualTo("Tênis Corre 4");
    assertThat(response.slug()).isEqualTo("tenis-corre-4");
    assertThat(response.price()).isEqualByComparingTo("499.90");
    assertThat(response.thumbnailUrl()).isNull();
    assertThat(response.createdAt()).isEqualTo(createdAt);
    assertThat(response.available()).isTrue();

    assertThat(pageableCaptor.getValue().getSort().toString()).isEqualTo("createdAt: ASC,id: DESC");
  }

  /*
   * Verifica o comportamento da primeira consulta quando nenhum cursor é informado. Um resultado
   * vazio deve continuar sendo devolvido como a primeira página, sem itens, com hasNext falso e sem
   * indicação de próxima página.
   */
  @Test
  void givenNoCursor_whenList_thenReturnFirstPage() {
    when(repository.findPublicSkus(
            eq(""),
            eq(""),
            anyBoolean(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            any(Pageable.class)))
        .thenAnswer(
            invocation ->
                new SliceImpl<>(List.of(), invocation.getArgument(7, Pageable.class), false));

    ProductSkuDbRequestDTO productSkuDbRequestDTO =
        new ProductSkuDbRequestDTO(null, null, null, null);

    var result = jpaProductSku.list(productSkuDbRequestDTO, null, 15);

    var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(repository)
        .findPublicSkus(
            eq(""),
            eq(""),
            eq(true),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            pageableCaptor.capture());

    assertThat(result.items()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    assertThat(result.page()).isZero();
    assertThat(result.items()).hasSize(0);
    assertThat(result.nextPage()).isNull();
    assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(15);
  }
}

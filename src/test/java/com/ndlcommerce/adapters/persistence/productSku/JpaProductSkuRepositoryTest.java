package com.ndlcommerce.adapters.persistence.productSku;

import static org.assertj.core.api.Assertions.assertThat;

import com.ndlcommerce.adapters.persistence.product.JpaProductRepository;
import com.ndlcommerce.adapters.persistence.product.ProductDataMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaProductSkuRepositoryTest {

  @Autowired private JpaProductRepository productRepository;
  @Autowired private JpaProductSkuRepository skuRepository;
  @Autowired private EntityManager entityManager;

  private ProductDataMapper product;

  @BeforeEach
  void setup() {
    product =
        productRepository.saveAndFlush(
            new ProductDataMapper(
                "Cursor Shoe " + UUID.randomUUID(),
                "Cursor test",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()));
  }

  @Test
  void givenSameCreatedAt_whenReadKeysetPages_thenOrderByIdAndAvoidOverlap() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 9, 10, 10, 0);
    saveSkus(createdAt, 3);
    Sort sort = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.desc("id"));

    Slice<JpaProductSkuRepository.PublicProductSkuView> first =
        skuRepository.findPublicSkus(
            "", "", true, null, null, null, null, PageRequest.of(0, 2, sort));
    var cursor = first.getContent().getLast();
    Slice<JpaProductSkuRepository.PublicProductSkuView> second =
        skuRepository.findPublicSkus(
            "",
            "",
            false,
            cursor.getCreatedAt(),
            cursor.getSkuId(),
            null,
            null,
            PageRequest.ofSize(2).withSort(sort));

    assertThat(first.getContent()).hasSize(2);
    assertThat(first.hasNext()).isTrue();
    assertThat(second.getContent()).hasSize(1);
    assertThat(second.hasNext()).isFalse();
    assertThat(first.getContent())
        .extracting(JpaProductSkuRepository.PublicProductSkuView::getSkuId)
        .doesNotContain(second.getContent().getFirst().getSkuId());
    assertThat(first.getContent())
        .extracting(JpaProductSkuRepository.PublicProductSkuView::getSkuId)
        .containsExactly(skuId(3), skuId(2));
  }

  @Test
  void givenNewSkuAfterFirstPage_whenReadNextPage_thenDoNotRepeatSeenItems() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 9, 11, 10, 0);
    saveSkus(createdAt, 3);
    Sort sort = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.desc("id"));
    Slice<JpaProductSkuRepository.PublicProductSkuView> first =
        skuRepository.findPublicSkus(
            "", "", true, null, null, null, null, PageRequest.of(0, 2, sort));
    var cursor = first.getContent().getLast();
    ProductSkuDataMapper inserted = saveSku(createdAt, 4);

    Slice<JpaProductSkuRepository.PublicProductSkuView> second =
        skuRepository.findPublicSkus(
            "",
            "",
            false,
            cursor.getCreatedAt(),
            cursor.getSkuId(),
            null,
            null,
            PageRequest.ofSize(2).withSort(sort));

    var seen =
        new HashSet<>(
            first.getContent().stream()
                .map(JpaProductSkuRepository.PublicProductSkuView::getSkuId)
                .toList());
    assertThat(second.getContent())
        .extracting(JpaProductSkuRepository.PublicProductSkuView::getSkuId)
        .noneMatch(seen::contains);
    assertThat(second.getContent())
        .extracting(JpaProductSkuRepository.PublicProductSkuView::getSkuId)
        .doesNotContain(inserted.getId());
  }

  @Test
  void givenExactlyPageSize_whenReadPage_thenKeepAllItemsAndNoNextCursor() {
    saveSkus(LocalDateTime.of(2026, 9, 12, 10, 0), 2);
    Sort sort = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.desc("id"));

    Slice<JpaProductSkuRepository.PublicProductSkuView> page =
        skuRepository.findPublicSkus(
            "", "", true, null, null, null, null, PageRequest.of(0, 2, sort));

    assertThat(page.getContent()).hasSize(2);
    assertThat(page.hasNext()).isFalse();
  }

  private List<ProductSkuDataMapper> saveSkus(LocalDateTime createdAt, int count) {
    return java.util.stream.IntStream.rangeClosed(1, count)
        .mapToObj(index -> saveSku(createdAt, index))
        .toList();
  }

  private ProductSkuDataMapper saveSku(LocalDateTime createdAt, int index) {
    var sku =
        new ProductSkuDataMapper(
            product.getId(),
            java.util.Map.of(),
            BigDecimal.valueOf(10 + index),
            5,
            "cursor-" + UUID.randomUUID(),
            true,
            UUID.randomUUID());
    var saved = skuRepository.saveAndFlush(sku);
    entityManager
        .createNativeQuery(
            "UPDATE ecommerce.product_sku SET id = :targetId, created_at = :createdAt WHERE id = :currentId")
        .setParameter("targetId", skuId(index))
        .setParameter("createdAt", createdAt)
        .setParameter("currentId", saved.getId())
        .executeUpdate();
    entityManager.clear();
    saved.setId(skuId(index));
    saved.setCreatedAt(createdAt);
    return saved;
  }

  private UUID skuId(int index) {
    return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", index));
  }
}

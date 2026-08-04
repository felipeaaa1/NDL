package com.ndlcommerce.adapters.persistence.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaProductRepositoryTest {

  @Autowired private JpaProductRepository repository;

  private UUID brandId;
  private UUID categoryId;

  @BeforeEach
  void setup() {
    brandId = UUID.randomUUID();
    categoryId = UUID.randomUUID();
    repository.saveAllAndFlush(
        List.of(
            product("Alpha Shoe", "Indoor shoe", true),
            product("Beta Shoe", "Indoor shoe", true),
            product("Gamma Shoe", "Outdoor shoe", true),
            product("Delta Shoe", "Indoor shoe", true),
            product("Inactive Shoe", "Indoor shoe", false)));
  }

  @Test
  void givenStableData_whenReadConsecutiveSlices_thenKeepOrderWithoutDuplicates() {
    Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    Pageable firstPage = PageRequest.of(0, 2, sort);
    Pageable secondPage = PageRequest.of(1, 2, sort);

    Slice<ProductDataMapper> first = repository.findProducts("", "", null, null, firstPage);
    Slice<ProductDataMapper> second = repository.findProducts("", "", null, null, secondPage);
    Slice<ProductDataMapper> repeatedFirst = repository.findProducts("", "", null, null, firstPage);

    List<ProductDataMapper> products =
        List.of(first.getContent(), second.getContent()).stream().flatMap(List::stream).toList();
    assertThat(first.hasNext()).isTrue();
    assertThat(second.hasNext()).isFalse();
    assertThat(products).hasSize(4);
    assertThat(products)
        .extracting(ProductDataMapper::getCreatedAt)
        .isSortedAccordingTo(java.util.Comparator.reverseOrder());
    assertThat(products).extracting(ProductDataMapper::getId).doesNotHaveDuplicates();
    assertThat(repeatedFirst.getContent())
        .extracting(ProductDataMapper::getId)
        .containsExactlyElementsOf(
            first.getContent().stream().map(ProductDataMapper::getId).toList());
  }

  @Test
  void givenFilters_whenFindProducts_thenReturnOnlyMatchingActiveProducts() {
    Pageable pageable =
        PageRequest.of(0, 15, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

    Slice<ProductDataMapper> result =
        repository.findProducts("shoe", "indoor", brandId, categoryId, pageable);

    assertThat(result.getContent())
        .extracting(ProductDataMapper::getName)
        .containsExactlyInAnyOrder("Alpha Shoe", "Beta Shoe", "Delta Shoe");
    assertThat(result.getContent()).allMatch(ProductDataMapper::getActive);
  }

  private ProductDataMapper product(String name, String description, boolean active) {
    ProductDataMapper product =
        new ProductDataMapper(name, description, brandId, categoryId, UUID.randomUUID());
    product.setActive(active);
    return product;
  }
}

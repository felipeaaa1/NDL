package com.ndlcommerce.adapters.persistence.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.config.SecurityFilter;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductDbRequestDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

class JpaProductTest {

  private JpaProductRepository repository;
  private JpaProduct adapter;

  @BeforeEach
  void setup() {
    repository = mock(JpaProductRepository.class);
    adapter = new JpaProduct(repository, mock(SecurityFilter.class));
  }

  @Test
  void givenNullTextFilters_whenList_thenUseEmptyFiltersAndDeterministicSort() {
    ProductDbRequestDTO request = new ProductDbRequestDTO(null, null, null, null, true);
    Pageable returnedPageable = PageRequest.of(1, 2);
    when(repository.findProducts(eq(""), eq(""), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new SliceImpl<>(List.of(), returnedPageable, false));
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    SliceResult<ProductDataMapper> result = adapter.list(request, 1, 2);

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
    ProductDbRequestDTO request = new ProductDbRequestDTO("shoe", "indoor", null, null, true);
    Pageable returnedPageable = PageRequest.of(0, 1);
    ProductDataMapper product = new ProductDataMapper();
    when(repository.findProducts(eq("shoe"), eq("indoor"), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(new SliceImpl<>(List.of(product), returnedPageable, true));

    SliceResult<ProductDataMapper> result = adapter.list(request, 0, 1);

    assertThat(result.items()).containsExactly(product);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextPage()).isEqualTo(1);
  }
}

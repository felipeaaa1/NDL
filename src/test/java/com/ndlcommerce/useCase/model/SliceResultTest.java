package com.ndlcommerce.useCase.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SliceResultTest {

  @Test
  void givenSliceWithNextPage_whenCreateResult_thenCalculateNextPage() {
    SliceResult<String> result = SliceResult.of(List.of("Produto"), 1, 15, true);

    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextPage()).isEqualTo(2);
  }

  @Test
  void givenLastSlice_whenCreateResult_thenNextPageShouldBeNull() {
    SliceResult<String> result = SliceResult.of(List.of("Produto"), 9, 15, false);

    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextPage()).isNull();
  }

  @Test
  void whenMapItems_thenConvertItemsAndPreservePaginationMetadata() {
    SliceResult<Integer> source = SliceResult.of(List.of(10, 20), 1, 15, true);

    SliceResult<String> result = source.map(String::valueOf);

    assertThat(result.items()).containsExactly("10", "20");
    assertThat(result.items().getFirst().getClass()).isEqualTo(String.class);
    assertThat(source.items().getFirst().getClass()).isNotEqualTo(String.class);
    assertThat(result.page()).isEqualTo(1);
    assertThat(result.size()).isEqualTo(15);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextPage()).isEqualTo(2);
  }
}

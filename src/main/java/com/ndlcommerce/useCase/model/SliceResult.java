package com.ndlcommerce.useCase.model;

import java.util.List;
import java.util.function.Function;

public record SliceResult<T>(List<T> items, int page, int size, boolean hasNext, Integer nextPage) {
  public static <T> SliceResult<T> of(List<T> items, int page, int size, boolean hasNext) {

    return new SliceResult<>(items, page, size, hasNext, hasNext ? page + 1 : null);
  }

  public <R> SliceResult<R> map(Function<T, R> mapper) {
    return new SliceResult<>(items.stream().map(mapper).toList(), page, size, hasNext, nextPage);
  }

  public SliceResult {
    items = List.copyOf(items);
  }
}

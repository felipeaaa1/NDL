package com.ndlcommerce.useCase.model;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;

@Getter
public class PaginatedResult<T> {

  private final List<T> content;
  private final int page;
  private final int size;
  private final long totalElements;
  private final int totalPages;
  private final boolean isFirst;
  private final boolean isLast;

  public PaginatedResult(
      List<T> content,
      int page,
      int size,
      long totalElements,
      int totalPages,
      boolean isFirst,
      boolean isLast) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.isFirst = isFirst;
    this.isLast = isLast;
  }

  public <R> PaginatedResult<R> map(Function<T, R> mapper) {
    return new PaginatedResult<>(
        this.content.stream().map(mapper).toList(),
        this.page,
        this.size,
        this.totalElements,
        this.totalPages,
        this.isFirst,
        this.isLast);
  }
}

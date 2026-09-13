package com.ndlcommerce.adapters.presenter.productSku;

import static org.assertj.core.api.Assertions.assertThat;

import com.ndlcommerce.adapters.web.cursor.CursorUtil;
import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductSkuResponseFormatterTest {

  private final ProductSkuResponseFormatter formatter = new ProductSkuResponseFormatter();

  @Test
  void givenFullPage_whenPrepareView_thenEncodeCursorFromLastItem() {
    var first = response(UUID.randomUUID(), LocalDateTime.of(2026, 9, 10, 10, 0));
    var last = response(UUID.randomUUID(), LocalDateTime.of(2026, 9, 10, 10, 1));

    var result = formatter.prepareListSuccessView(SliceResult.of(List.of(first, last), 0, 2, true));

    assertThat(result.get("data")).isEqualTo(List.of(first, last));
    var nextCursor = (String) result.get("nextCursor");
    assertThat(nextCursor).isNotBlank().doesNotContain("=");
    assertThat(CursorUtil.decode(nextCursor))
        .isEqualTo(new OrderCursor(last.createdAt(), last.skuId()));
  }

  @Test
  void givenLastPage_whenPrepareView_thenKeepItemsAndReturnNullCursor() {
    var item = response(UUID.randomUUID(), LocalDateTime.of(2026, 9, 10, 10, 0));

    var result = formatter.prepareListSuccessView(SliceResult.of(List.of(item), 0, 1, false));

    assertThat(result.get("data")).isEqualTo(List.of(item));
    assertThat(result.get("nextCursor")).isNull();
  }

  private ProductSkuResponseDTO response(UUID id, LocalDateTime createdAt) {
    return new ProductSkuResponseDTO(
        id, UUID.randomUUID(), "Shoe", "shoe", BigDecimal.TEN, null, createdAt, true);
  }
}

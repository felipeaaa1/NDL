package com.ndlcommerce.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.ndlcommerce.adapters.web.cursor.CursorUtil;
import com.ndlcommerce.useCase.model.OrderCursor;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CursorUtilTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "null",
        "{}",
        "{\"createdAt\":null,\"id\":null}",
        "{\"createdAt\":\"2026-09-10T10:30:00\"}",
        "{\"id\":\"00000000-0000-0000-0000-000000000001\"}",
        "{\"createdAt\":null,\"id\":\"00000000-0000-0000-0000-000000000001\"}",
        "{\"createdAt\":\"2026-09-10T10:30:00\",\"id\":null}",
        "{\"createdAt\":\"invalid\",\"id\":\"00000000-0000-0000-0000-000000000001\"}",
        "{\"createdAt\":\"2026-09-10T10:30:00\",\"id\":\"invalid\"}",
        "not-json"
      })
  void givenInvalidPayload_whenDecode_thenRejectCursor(String payload) {
    String cursor = Base64.getUrlEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> CursorUtil.decode(cursor))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid cursor");
  }

  @Test
  void givenInvalidBase64_whenDecode_thenRejectCursor() {
    assertThatThrownBy(() -> CursorUtil.decode("%%%"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid cursor");
  }

  @Test
  void givenOrderCursor_whenEncodeAndDecode_thenPreserveValues() {
    var original = new OrderCursor(LocalDateTime.of(2026, 9, 10, 10, 30), UUID.randomUUID());

    var encoded = CursorUtil.encode(original);
    assertThat(encoded).doesNotContain("=");
    var decoded = CursorUtil.decode(encoded);

    assertThat(decoded).isEqualTo(original);
  }
}

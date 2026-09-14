package com.ndlcommerce.adapters.web.cursor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndlcommerce.useCase.model.OrderCursor;
import java.util.Base64;

public final class CursorUtil {

  private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

  private CursorUtil() {}

  public static String encode(OrderCursor cursor) {
    try {
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(MAPPER.writeValueAsBytes(cursor));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encode cursor", e);
    }
  }

  public static OrderCursor decode(String cursor) {
    try {
      OrderCursor orderCursor =
          MAPPER.readValue(Base64.getUrlDecoder().decode(cursor), OrderCursor.class);
      if (orderCursor == null || orderCursor.createdAt() == null || orderCursor.id() == null) {
        throw new IllegalArgumentException("Invalid cursor");
      }
      return orderCursor;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid cursor");
    }
  }
}

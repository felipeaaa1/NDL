package com.ndlcommerce.useCase.request.productSku;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSkuResponseDTO(
    UUID skuId,
    UUID productId,
    String name,
    String slug,
    BigDecimal price,
    String thumbnailUrl,
    Boolean available) {}

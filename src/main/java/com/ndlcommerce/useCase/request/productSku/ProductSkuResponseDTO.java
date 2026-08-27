package com.ndlcommerce.useCase.request.productSku;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;

public record ProductSkuResponseDTO(
    @Getter UUID skuId,
    @Getter UUID productId,
    @Getter String name,
    @Getter String slug,
    @Getter BigDecimal price,
    @Getter String thumbnailUrl,
    @Getter Boolean available) {}

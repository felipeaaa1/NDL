package com.ndlcommerce.useCase.request.productSku;

import java.util.UUID;

public record ProductSkuDbRequestDTO(String name, String description, UUID brand, UUID category) {}

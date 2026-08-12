package com.ndlcommerce.useCase.request.brand;

import java.time.LocalDateTime;
import java.util.UUID;

public record BrandGatewayResponseDTO(UUID id, String name, LocalDateTime createdAt) {}

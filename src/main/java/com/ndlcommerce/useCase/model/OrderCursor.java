package com.ndlcommerce.useCase.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCursor(LocalDateTime createdAt, UUID id) {}

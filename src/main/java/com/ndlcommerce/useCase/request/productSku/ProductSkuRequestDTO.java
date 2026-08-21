package com.ndlcommerce.useCase.request.productSku;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;

public record ProductSkuRequestDTO(
    @Schema(
            description = "Características que diferenciam o SKU",
            example = "{\"memoria\":\"16GB\",\"armazenamento\":\"SSD 512GB\",\"cor\":\"preto\"}")
        @NotEmpty(message = "Atributos são obrigatórios")
        Map<@NotBlank String, @NotBlank String> attributes,
    @Schema(example = "4599.90")
        @NotNull(message = "Preço é obrigatório e possuir até 8 inteiros e 2 decimais")
        @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Preço é obrigatório e possuir até 8 inteiros e 2 decimais")
        @Digits(
            integer = 8,
            fraction = 2,
            message = "Preço é obrigatório e possuir até 8 inteiros e 2 decimais")
        BigDecimal price,
    @Schema(example = "10")
        @NotNull(message = "Estoque é obrigatório e não pode ser negativo")
        @PositiveOrZero(message = "Estoque é obrigatório e não pode ser negativo")
        Integer stock) {}

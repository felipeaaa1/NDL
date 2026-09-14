package com.ndlcommerce.useCase.request.product;

import com.ndlcommerce.useCase.request.productSku.ProductSkuRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public class ProductRequestDTO {

  @Schema(example = "Notebook Dell Inspiron")
  @NotBlank(message = "Nome deve ter entre 3 e 200 caracteres")
  @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres")
  private String name;

  @NotNull(message = "productSkuRequestDTO é um campo obrigatório")
  @Valid
  private List<ProductSkuRequestDTO> productSkuRequestDTO;

  @Schema(example = "Notebook com 16GB RAM e SSD 512GB")
  @NotBlank(message = "Descrição deve ter no mínimo 5 e no máximo 500 caracteres")
  @Size(min = 5, max = 500, message = "Descrição deve ter no mínimo 5 e no máximo 500 caracteres")
  private String description;

  @NotNull(message = "Marca do produto é obrigatória")
  @Schema(example = "af889372-ad3f-47a5-8315-09710ed4ec31")
  private UUID brand;

  @NotNull(message = "Categoria do produto é obrigatória")
  @Schema(example = "a9a713f9-995c-4855-b1cd-2c835b03d9b1")
  private UUID category;

  public ProductRequestDTO(String name, String description, UUID brand, UUID category) {
    this.name = name;
    this.description = description;
    this.brand = brand;
    this.category = category;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public UUID getBrand() {
    return brand;
  }

  public UUID getCategory() {
    return category;
  }

  public List<ProductSkuRequestDTO> getProductSkuRequestDTO() {
    return productSkuRequestDTO;
  }
}

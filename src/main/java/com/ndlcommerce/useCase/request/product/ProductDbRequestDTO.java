package com.ndlcommerce.useCase.request.product;

import com.ndlcommerce.entity.model.interfaces.ProductSku;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

public class ProductDbRequestDTO {

  @Getter private String name;
  @Getter private String description;
  @Getter private UUID brand;
  @Getter private UUID category;
  @Getter private List<ProductSku> productSku;
  @Getter private boolean active;

  public ProductDbRequestDTO(
      String name,
      String description,
      UUID brand,
      UUID category,
      List<ProductSku> productSku,
      boolean active) {
    this.name = name;
    this.description = description;
    this.brand = brand;
    this.category = category;
    this.productSku = productSku;
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ProductDbRequestDTO that = (ProductDbRequestDTO) o;
    return Objects.equals(name, that.name) && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description);
  }
}

package com.ndlcommerce.useCase.request.category;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CategoryRequestDTO {

  @Size(
      min = 3,
      max = 50,
      message = "Nome da categoria não é válido, deve ter no mínimo 3 e no máximo 50 caracteres")
  private String name;

  private UUID parentId;

  public CategoryRequestDTO() {}

  public CategoryRequestDTO(String name, UUID parentId) {
    this.name = name;
    this.parentId = parentId;
  }

  public String getName() {
    return name;
  }

  public UUID getParentId() {
    return parentId;
  }
}

package com.ndlcommerce.useCase.interfaces.category;

import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.category.CategoryDbRequestDTO;
import com.ndlcommerce.useCase.request.category.CategoryResponseDTO;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRegisterDsGateway {
  boolean existsByName(String name);

  CategoryResponseDTO save(CategoryDbRequestDTO dbRequest);

  boolean existsById(UUID parentId);

  PaginatedResult<CategoryResponseDTO> list(CategoryDbRequestDTO request, int page, int size);

  Optional<CategoryResponseDTO> getById(UUID uuid);

  boolean existsByNameAndNotId(String name, UUID uuid);

  CategoryResponseDTO update(UUID categoryUUID, CategoryDbRequestDTO categoryDbRequestDTO);

  void delete(UUID categoryId);
}

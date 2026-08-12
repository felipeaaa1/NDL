package com.ndlcommerce.useCase.interfaces.category;

import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.category.CategoryRequestDTO;
import com.ndlcommerce.useCase.request.category.CategoryResponseDTO;
import java.util.UUID;

public interface CategoryInputBoundary {
  CategoryResponseDTO create(CategoryRequestDTO requestDTO);

  PaginatedResult<?> list(CategoryRequestDTO filter, int page, int size);

  CategoryResponseDTO getById(UUID uuid);

  CategoryResponseDTO updateCategory(UUID uuid, CategoryRequestDTO requestModel);

  Object deleteCategory(UUID categoryId);
}

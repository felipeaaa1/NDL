package com.ndlcommerce.useCase.interfaces.category;

import com.ndlcommerce.adapters.persistence.category.CategoryDataMapper;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.category.CategoryDbRequestDTO;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRegisterDsGateway {
  boolean existsByName(String name);

  CategoryDataMapper save(CategoryDbRequestDTO dbRequest);

  boolean existsById(UUID parentId);

  PaginatedResult<CategoryDataMapper> list(CategoryDbRequestDTO request, int page, int size);

  Optional<CategoryDataMapper> getById(UUID uuid);

  boolean existsByNameAndNotId(String name, UUID uuid);

  CategoryDataMapper update(UUID categoryUUID, CategoryDbRequestDTO categoryDbRequestDTO);

  void delete(UUID categoryId);
}

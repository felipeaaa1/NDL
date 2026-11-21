package com.ndlcommerce.useCase;

import com.ndlcommerce.entity.factory.interfaces.CategoryFactory;
import com.ndlcommerce.entity.model.interfaces.Category;
import com.ndlcommerce.useCase.interfaces.category.CategoryInputBoundary;
import com.ndlcommerce.useCase.interfaces.category.CategoryPresenter;
import com.ndlcommerce.useCase.interfaces.category.CategoryRegisterDsGateway;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.category.CategoryDbRequestDTO;
import com.ndlcommerce.useCase.request.category.CategoryRequestDTO;
import com.ndlcommerce.useCase.request.category.CategoryResponseDTO;
import java.util.Optional;
import java.util.UUID;

public class CategoryRegisterInteractor implements CategoryInputBoundary {

  private final CategoryRegisterDsGateway categoryDsGateway;
  private final CategoryPresenter categoryPresenter;
  private final CategoryFactory categoryFactory;

  public CategoryRegisterInteractor(
      CategoryRegisterDsGateway categoryDsGateway,
      CategoryPresenter categoryPresenter,
      CategoryFactory categoryFactory) {
    this.categoryDsGateway = categoryDsGateway;
    this.categoryPresenter = categoryPresenter;
    this.categoryFactory = categoryFactory;
  }

  @Override
  public CategoryResponseDTO create(CategoryRequestDTO requestDTO) {

    Category category = categoryFactory.create(requestDTO.getName());

    if (!category.nameIsValid()) {
      return categoryPresenter.prepareFailView("NameNotValid");
    }

    if (categoryDsGateway.existsByName(category.getName())) {
      return categoryPresenter.prepareFailView("ExistByName");
    }

    if (requestDTO.getParentId() != null
        && !categoryDsGateway.existsById(requestDTO.getParentId())) {
      return categoryPresenter.prepareFailView("ParentNotFound");
    }

    CategoryDbRequestDTO dbRequest =
        new CategoryDbRequestDTO(category.getName(), requestDTO.getParentId());
    CategoryResponseDTO response = categoryDsGateway.save(dbRequest);

    return categoryPresenter.prepareSuccessView(response);
  }

  @Override
  public PaginatedResult<CategoryResponseDTO> list(CategoryRequestDTO filter, int page, int size) {
    CategoryDbRequestDTO request =
        new CategoryDbRequestDTO(
            filter != null ? filter.getName() : null, filter != null ? filter.getParentId() : null);

    PaginatedResult<CategoryResponseDTO> paginatedResultCategoryResponseDTO =
        categoryDsGateway.list(request, page, size);

    return categoryPresenter.prepareListSuccessView(paginatedResultCategoryResponseDTO);
  }

  @Override
  public CategoryResponseDTO getById(UUID uuid) {

    Optional<CategoryResponseDTO> optionalCategoryData = categoryDsGateway.getById(uuid);
    if (optionalCategoryData.isEmpty()) {
      return categoryPresenter.prepareFailView("NotFound");
    }
    return categoryPresenter.prepareSuccessView(optionalCategoryData.get());
  }

  @Override
  public CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO requestDTO) {
    Optional<CategoryResponseDTO> opt = categoryDsGateway.getById(categoryId);

    if (opt.isEmpty()) {
      return categoryPresenter.prepareFailView("NotFound");
    }

    if (categoryDsGateway.existsByNameAndNotId(requestDTO.getName(), categoryId)) {
      return categoryPresenter.prepareFailView("ExistByName");
    }

    Category category = categoryFactory.create(requestDTO.getName());

    if (!category.nameIsValid()) {
      return categoryPresenter.prepareFailView("NameNotValid");
    }

    CategoryDbRequestDTO categoryDbRequestDTO =
        new CategoryDbRequestDTO(category.getName(), requestDTO.getParentId());
    CategoryResponseDTO updated = categoryDsGateway.update(categoryId, categoryDbRequestDTO);

    return categoryPresenter.prepareSuccessView(updated);
  }

  @Override
  public Object deleteCategory(UUID categoryId) {
    Optional<CategoryResponseDTO> opt = categoryDsGateway.getById(categoryId);

    if (opt.isEmpty()) {
      return categoryPresenter.prepareFailView("NotFound");
    }

    categoryDsGateway.delete(categoryId);

    return categoryPresenter.prepareSuccessView(null);
  }
}

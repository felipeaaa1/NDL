package com.ndlcommerce.useCase;

import com.ndlcommerce.adapters.persistence.category.CategoryDataMapper;
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

    CategoryDataMapper saved = categoryDsGateway.save(dbRequest);

    CategoryResponseDTO response =
        new CategoryResponseDTO(
            saved.getId(), saved.getName(), saved.getParentId(), saved.getCreatedAt().toString());

    return categoryPresenter.prepareSuccessView(response);
  }

  @Override
  public PaginatedResult<CategoryResponseDTO> list(CategoryRequestDTO filter, int page, int size) {
    CategoryDbRequestDTO request =
        new CategoryDbRequestDTO(
            filter != null ? filter.getName() : null, filter != null ? filter.getParentId() : null);

    PaginatedResult<CategoryDataMapper> categoryDataMapperPage =
        categoryDsGateway.list(request, page, size);

    PaginatedResult<CategoryResponseDTO> paginatedResultCategoryResponseDTO =
        categoryDataMapperPage == null ? null : categoryDataMapperPage.map(this::mapperToDTO);

    return categoryPresenter.prepareListSuccessView(paginatedResultCategoryResponseDTO);
  }

  @Override
  public CategoryResponseDTO getById(UUID uuid) {

    Optional<CategoryDataMapper> optionalCategoryDataMapper = categoryDsGateway.getById(uuid);
    if (optionalCategoryDataMapper.isEmpty()) {
      return categoryPresenter.prepareFailView("NotFound");
    }
    CategoryDataMapper categoryDataMapper = optionalCategoryDataMapper.get();
    CategoryResponseDTO responseDTO =
        new CategoryResponseDTO(
            categoryDataMapper.getId(),
            categoryDataMapper.getName(),
            categoryDataMapper.getParentId(),
            categoryDataMapper.getCreatedAt().toString());
    return categoryPresenter.prepareSuccessView(responseDTO);
  }

  @Override
  public CategoryResponseDTO updateCategory(UUID categoryId, CategoryRequestDTO requestDTO) {
    Optional<CategoryDataMapper> opt = categoryDsGateway.getById(categoryId);

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
    CategoryDataMapper updated = categoryDsGateway.update(categoryId, categoryDbRequestDTO);

    CategoryResponseDTO responseDTO = mapperToDTO(updated);

    return categoryPresenter.prepareSuccessView(responseDTO);
  }

  @Override
  public Object deleteCategory(UUID categoryId) {
    Optional<CategoryDataMapper> opt = categoryDsGateway.getById(categoryId);

    if (opt.isEmpty()) {
      return categoryPresenter.prepareFailView("NotFound");
    }

    categoryDsGateway.delete(categoryId);

    return categoryPresenter.prepareSuccessView(null);
  }

  private CategoryResponseDTO mapperToDTO(CategoryDataMapper mapper) {
    return new CategoryResponseDTO(
        mapper.getId(), mapper.getName(), mapper.getParentId(), mapper.getCreatedAt().toString());
  }
}

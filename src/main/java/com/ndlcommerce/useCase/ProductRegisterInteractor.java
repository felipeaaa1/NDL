package com.ndlcommerce.useCase;

import com.ndlcommerce.adapters.persistence.product.ProductDataMapper;
import com.ndlcommerce.config.PaginatedResult;
import com.ndlcommerce.entity.factory.interfaces.ProductFactory;
import com.ndlcommerce.entity.model.interfaces.Product;
import com.ndlcommerce.useCase.interfaces.brand.BrandRegisterDsGateway;
import com.ndlcommerce.useCase.interfaces.category.CategoryRegisterDsGateway;
import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.interfaces.product.ProductPresenter;
import com.ndlcommerce.useCase.interfaces.product.ProductRegisterDsGateway;
import com.ndlcommerce.useCase.request.product.*;
import java.util.Optional;
import java.util.UUID;

public class ProductRegisterInteractor implements ProductInputBoundary {

  private final ProductRegisterDsGateway productDsGateway;
  private final ProductPresenter productPresenter;
  private final ProductFactory productFactory;
  private final BrandRegisterDsGateway brandRegisterDsGateway;
  private final CategoryRegisterDsGateway categoryRegisterDsGateway;

  public ProductRegisterInteractor(
      ProductRegisterDsGateway productDsGateway,
      ProductPresenter productPresenter,
      ProductFactory productFactory,
      BrandRegisterDsGateway brandRegisterDsGateway,
      CategoryRegisterDsGateway categoryRegisterDsGateway) {
    this.productDsGateway = productDsGateway;
    this.productPresenter = productPresenter;
    this.productFactory = productFactory;
    this.brandRegisterDsGateway = brandRegisterDsGateway;
    this.categoryRegisterDsGateway = categoryRegisterDsGateway;
  }

  @Override
  public ProductResponseDTO create(ProductRequestDTO requestDTO) {
    if (brandRegisterDsGateway.getById(requestDTO.getBrand()).isEmpty()) {
      return productPresenter.prepareFailView("BrandNotFound");
    }
    if (categoryRegisterDsGateway.getById(requestDTO.getCategory()).isEmpty()) {
      return productPresenter.prepareFailView("CategoryNotFound");
    }

    Product product = productFactory.create(requestDTO.getName(), requestDTO.getDescription());

    if (!product.nameIsValid()) {
      return productPresenter.prepareFailView("NameNotValid");
    } else if (!product.descriptionIsValid()) {
      return productPresenter.prepareFailView("DescriptionNotValid");
    } else if (productDsGateway.existsByName(product.getName())) {
      return productPresenter.prepareFailView("ExistByName");
    }

    ProductDbRequestDTO dbRequest =
        new ProductDbRequestDTO(
            product.getName(),
            product.getDescription(),
            requestDTO.getBrand(),
            requestDTO.getCategory(),
            true);

    ProductDataMapper save = productDsGateway.save(dbRequest);

    ProductResponseDTO response =
        new ProductResponseDTO(
            save.getId(), save.getName(), save.getDescription(), save.getCreatedAt().toString());

    return productPresenter.prepareSuccessView(response);
  }

  @Override
  public PaginatedResult<ProductResponseDTO> list(ProductFilterDTO filter, int page, int size) {
    ProductDbRequestDTO productDbRequestDTO =
        new ProductDbRequestDTO(
            filter != null ? filter.getName() : null,
            filter != null ? filter.getDescription() : null,
            filter != null ? filter.getBrand() : null,
            filter != null ? filter.getCategory() : null,
            true);

    PaginatedResult<ProductDataMapper> productDataMapperList =
        productDsGateway.list(productDbRequestDTO, page, size);

    PaginatedResult<ProductResponseDTO> paginatedResultProductResponseDTO =
        productDataMapperList == null ? null : productDataMapperList.map(this::mapperToDTO);

    return productPresenter.prepareListSuccessView(paginatedResultProductResponseDTO);
  }

  private ProductResponseDTO mapperToDTO(ProductDataMapper productDataMapper) {
    return new ProductResponseDTO(
        productDataMapper.getId(),
        productDataMapper.getName(),
        productDataMapper.getDescription(),
        productDataMapper.getCreatedAt().toString());
  }

  @Override
  public ProductResponseDTO getById(UUID productId) {
    Optional<ProductDataMapper> optional = productDsGateway.findById(productId);
    if (optional.isEmpty()) {
      return productPresenter.prepareFailView("NotFound");
    }
    ProductDataMapper productDataMapper = optional.get();
    ProductResponseDTO response =
        new ProductResponseDTO(
            productDataMapper.getId(),
            productDataMapper.getName(),
            productDataMapper.getDescription(),
            productDataMapper.getCreatedAt().toString());

    return productPresenter.prepareSuccessView(response);
  }

  @Override
  public ProductResponseDTO updateProduct(UUID productId, ProductUpdateRequestDTO requestDTO) {
    Optional<ProductDataMapper> optional = productDsGateway.findById(productId);

    if (optional.isEmpty()) {
      return productPresenter.prepareFailView("NotFound");
    }

    if (requestDTO.getBrand() != null
        && brandRegisterDsGateway.getById(requestDTO.getBrand()).isEmpty()) {
      return productPresenter.prepareFailView("BrandNotFound");
    }

    if (requestDTO.getCategory() != null
        && categoryRegisterDsGateway.getById(requestDTO.getCategory()).isEmpty()) {
      return productPresenter.prepareFailView("CategoryNotFound");
    }

    Product product = productFactory.create(requestDTO.getName(), requestDTO.getDescription());

    if (requestDTO.getName() != null && !product.nameIsValid()) {
      return productPresenter.prepareFailView("NameNotValid");
    } else if (requestDTO.getDescription() != null && !product.descriptionIsValid()) {
      return productPresenter.prepareFailView("DescriptionNotValid");
    } else if (productDsGateway.existsByNameAndIdNot(product.getName(), productId)) {
      return productPresenter.prepareFailView("ExistByName");
    }

    ProductDataMapper productDataMapper = productDsGateway.update(optional.get(), requestDTO);
    ProductResponseDTO response =
        new ProductResponseDTO(
            productDataMapper.getId(),
            productDataMapper.getName(),
            productDataMapper.getDescription(),
            productDataMapper.getCreatedAt().toString());

    return productPresenter.prepareSuccessView(response);
  }

  @Override
  public ProductResponseDTO deleteProduct(UUID productId) {
    Optional<ProductDataMapper> optional = productDsGateway.findById(productId);

    if (optional.isEmpty()) {
      return productPresenter.prepareFailView("NotFound");
    }

    productDsGateway.delete(productId);

    return productPresenter.prepareSuccessView(null);
  }
}

package com.ndlcommerce.useCase;

import com.ndlcommerce.entity.factory.interfaces.BrandFactory;
import com.ndlcommerce.entity.model.interfaces.Brand;
import com.ndlcommerce.useCase.interfaces.brand.BrandInputBoundary;
import com.ndlcommerce.useCase.interfaces.brand.BrandPresenter;
import com.ndlcommerce.useCase.interfaces.brand.BrandRegisterDsGateway;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandFilterDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import com.ndlcommerce.useCase.request.brand.BrandRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;
import java.util.Optional;
import java.util.UUID;

public class BrandRegisterInteractor implements BrandInputBoundary {

  private final BrandRegisterDsGateway brandDsGateway;
  private final BrandPresenter brandPresenter;
  private final BrandFactory brandFactory;

  public BrandRegisterInteractor(
      BrandRegisterDsGateway brandDsGateway,
      BrandPresenter brandPresenter,
      BrandFactory brandFactory) {
    this.brandDsGateway = brandDsGateway;
    this.brandPresenter = brandPresenter;
    this.brandFactory = brandFactory;
  }

  @Override
  public BrandResponseDTO create(BrandRequestDTO requestDTO) {
    Brand brand = brandFactory.create(requestDTO.name());

    if (!brand.nameIsValid()) {
      return brandPresenter.prepareFailView("NameNotValid");
    }

    if (brandDsGateway.existsByName(brand.getName())) {
      return brandPresenter.prepareFailView("ExistByName");
    }

    BrandDbRequestDTO dbRequest = new BrandDbRequestDTO(brand.getName());
    BrandGatewayResponseDTO saved = brandDsGateway.save(dbRequest);

    BrandResponseDTO response =
        new BrandResponseDTO(saved.id(), saved.name(), saved.createdAt().toString());

    return brandPresenter.prepareSuccessView(response);
  }

  @Override
  public PaginatedResult<BrandResponseDTO> list(BrandFilterDTO request, int page, int size) {
    BrandDbRequestDTO brandDbRequestDTO =
        new BrandDbRequestDTO(request != null ? request.name() : null);

    PaginatedResult<BrandGatewayResponseDTO> paginatedResult =
        brandDsGateway.list(brandDbRequestDTO, page, size);

    PaginatedResult<BrandResponseDTO> response =
        paginatedResult == null ? null : paginatedResult.map(this::mapperToDTO);

    return brandPresenter.prepareListSuccessView(response);
  }

  @Override
  public BrandResponseDTO getById(UUID uuid) {
    Optional<BrandGatewayResponseDTO> optional = brandDsGateway.getById(uuid);

    if (optional.isEmpty()) {
      return brandPresenter.prepareFailView("NotFound");
    }

    BrandResponseDTO responseDTO = mapperToDTO(optional.get());

    return brandPresenter.prepareSuccessView(responseDTO);
  }

  @Override
  public BrandResponseDTO updateBrand(UUID uuid, BrandRequestDTO requestDTO) {
    Optional<BrandGatewayResponseDTO> optional = brandDsGateway.getById(uuid);

    if (optional.isEmpty()) {
      return brandPresenter.prepareFailView("NotFound");
    }

    if (brandDsGateway.existsByNameAndNotId(requestDTO.name(), uuid)) {
      return brandPresenter.prepareFailView("ExistByName");
    }

    Brand brand = brandFactory.create(requestDTO.name());

    if (!brand.nameIsValid()) {
      return brandPresenter.prepareFailView("NameNotValid");
    }

    BrandDbRequestDTO request = new BrandDbRequestDTO(brand.getName());
    BrandGatewayResponseDTO updated = brandDsGateway.update(uuid, request);

    return brandPresenter.prepareSuccessView(mapperToDTO(updated));
  }

  @Override
  public BrandResponseDTO deleteBrand(UUID brandId) {
    Optional<BrandGatewayResponseDTO> optional = brandDsGateway.getById(brandId);

    if (optional.isEmpty()) {
      return brandPresenter.prepareFailView("NotFound");
    }

    brandDsGateway.delete(brandId);

    return brandPresenter.prepareSuccessView(null);
  }

  private BrandResponseDTO mapperToDTO(BrandGatewayResponseDTO brand) {
    return new BrandResponseDTO(brand.id(), brand.name(), brand.createdAt().toString());
  }
}

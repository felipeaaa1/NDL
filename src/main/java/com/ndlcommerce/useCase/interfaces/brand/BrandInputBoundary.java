package com.ndlcommerce.useCase.interfaces.brand;

import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandFilterDTO;
import com.ndlcommerce.useCase.request.brand.BrandRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;
import java.util.UUID;

public interface BrandInputBoundary {
  BrandResponseDTO create(BrandRequestDTO requestDTO);

  PaginatedResult<BrandResponseDTO> list(BrandFilterDTO filter, int page, int size);

  BrandResponseDTO getById(UUID uuid);

  BrandResponseDTO updateBrand(UUID uuid, BrandRequestDTO requestDTO);

  BrandResponseDTO deleteBrand(UUID brandId);
}

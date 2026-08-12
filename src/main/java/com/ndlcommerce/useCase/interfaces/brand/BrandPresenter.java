package com.ndlcommerce.useCase.interfaces.brand;

import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandResponseDTO;

public interface BrandPresenter {

  BrandResponseDTO prepareFailView(String error);

  BrandResponseDTO prepareSuccessView(BrandResponseDTO response);

  PaginatedResult<BrandResponseDTO> prepareListSuccessView(PaginatedResult<BrandResponseDTO> list);
}

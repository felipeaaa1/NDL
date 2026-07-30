package com.ndlcommerce.useCase.interfaces.product;

import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;

public interface ProductPresenter {

  ProductResponseDTO prepareSuccessView(ProductResponseDTO product);

  ProductResponseDTO prepareFailView(String error);

  SliceResult<ProductResponseDTO> prepareListSuccessView(SliceResult<ProductResponseDTO> list);
}

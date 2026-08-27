package com.ndlcommerce.useCase.interfaces.productSku;

import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;

public interface ProductSkuInputBoundary {
  SliceResult<ProductSkuResponseDTO> list(ProductFilterDTO filter, int page, int size);
}

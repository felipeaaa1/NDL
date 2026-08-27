package com.ndlcommerce.useCase.interfaces.productSku;

import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;

public interface ProductSkuRegisterDsGateway {
  SliceResult<ProductSkuResponseDTO> list(
      ProductSkuDbRequestDTO requestDTO, Integer page, Integer size);
}

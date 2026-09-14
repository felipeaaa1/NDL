package com.ndlcommerce.useCase.interfaces.productSku;

import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;

public interface ProductSkuRegisterDsGateway {
  SliceResult<ProductSkuResponseDTO> list(
      ProductSkuDbRequestDTO requestDTO, OrderCursor cursor, Integer size);
}
